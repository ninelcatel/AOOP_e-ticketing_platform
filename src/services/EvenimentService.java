package services;
import models.*;
import db.*;
import java.util.*;
import java.util.stream.Collectors;

public class EvenimentService {
    private EvenimentDAO evenimentDAO;
    private LocatieDAO locatieDAO;
    private UserDAO userDAO;
    private BiletDAO biletDAO;
    private ComandaDAO comandaDAO;
    private TranzactieDAO tranzactieDAO;
    private RecenzieDAO recenzieDAO;
    private CodPromoDAO codPromoDAO;
    private AuditService audit;

    public EvenimentService() {
        // Initializam baza de date (creeaza tabelele daca nu exista)
        DatabaseInitializer.initializeaza();

        this.evenimentDAO = EvenimentDAO.getInstance();
        this.locatieDAO = LocatieDAO.getInstance();
        this.userDAO = UserDAO.getInstance();
        this.biletDAO = BiletDAO.getInstance();
        this.comandaDAO = ComandaDAO.getInstance();
        this.tranzactieDAO = TranzactieDAO.getInstance();
        this.recenzieDAO = RecenzieDAO.getInstance();
        this.codPromoDAO = CodPromoDAO.getInstance();
        this.audit = AuditService.getInstance();

        // Populam baza de date cu date initiale daca este goala
        DatabaseSeeder.seed();
    }

    private double calcPretBilet(Eveniment eveniment, TipBilet tip) {
        if (eveniment == null || tip == null) {
            return 0;
        }
        double pretBilet = eveniment.getPret();
        switch (tip) {
            case VIP:
                pretBilet *= 1.5;
                break;
            case STANDARD:
                break;
            case STUDENT:
                pretBilet *= 0.8;
                break;
            case EARLY_BIRD:
                pretBilet *= 0.7;
                break;
        }
        return pretBilet;
    }

    private double aplicaCodPromoLaSuma(double suma, String codPromo) {
        if (codPromo == null) {
            return suma;
        }
        String cod = codPromo.trim();
        if (cod.isEmpty()) {
            return suma;
        }

        CodPromo promo = this.codPromoDAO.readAll().stream()
                .filter(cp -> cp.getCod() != null && cp.getCod().equals(cod) && cp.isActiv())
                .findFirst()
                .orElse(null);

        if (promo == null) {
            return suma;
        }

        if (promo.isProcentual()) {
            return suma * (1 - promo.getValoare() / 100);
        }
        return Math.max(0, suma - promo.getValoare());
    }

    private long countBileteValidePentruEveniment(Eveniment eveniment) {
        if (eveniment == null) {
            return 0;
        }
        return this.biletDAO.readByEveniment(eveniment.getId()).stream().filter(Bilet::isValid).count();
    }

    public void adaugaEveniment(Eveniment eveniment) {
        if (eveniment == null) {
            return;
        }

        // Persistam intai locatia, ca sa avem un id valid pentru FK
        if (eveniment.getLocatie() != null) {
            if (eveniment.getLocatie().getId() <= 0) {
                this.locatieDAO.create(eveniment.getLocatie());
            }
        }
        this.evenimentDAO.create(eveniment);
        this.audit.logActiune("adaugaEveniment");
    }
    public void stergeEveniment(int evenimentId) {
        this.evenimentDAO.delete(evenimentId);
        this.audit.logActiune("stergeEveniment");
    }
    public void modificaEveniment(int evenimentId, Eveniment evenimentNou) {
        evenimentNou.setId(evenimentId);
        this.evenimentDAO.update(evenimentNou);
        this.audit.logActiune("modificaEveniment");
    }
    public Eveniment cautaEvenimentDupaId(int evenimentId) {
        return this.evenimentDAO.read(evenimentId);
    }

    public void cumparaBilet(User user, Eveniment eveniment, TipBilet tip, Comanda comanda) {
        // O comanda cu un singur bilet - delegam la varianta cu cantitate
        cumparaBilete(user, eveniment, tip, comanda, 1);
    }

    public void cumparaBilete(User user, Eveniment eveniment, TipBilet tip, Comanda comanda, int cantitate) {
        if (user == null || eveniment == null) {
            System.out.println("User sau eveniment invalid!");
            return;
        }

        if (cantitate <= 0) {
            System.out.println("Cantitate invalida!");
            return;
        }

        // Verificare pentru early bird - doar cu o luna inainte
        if (tip == TipBilet.EARLY_BIRD) {
            Date currentDate = new Date();
            Date eventDate = eveniment.getData();

            // Calculam diferenta in milisecunde si convertim la zile
            long diffInMillies = eventDate.getTime() - currentDate.getTime();
            long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);

            if (diffInDays < 30) {
                System.out.println("Biletele Early Bird pot fi cumparate doar cu cel putin o luna (30 zile) inainte de eveniment!");
                System.out.println("Zile ramase pana la eveniment: " + diffInDays);
                return;
            }
        }

        Locatie locatie = eveniment.getLocatie();
        if (locatie != null) {
            int capacitate = locatie.getCapacitate();
            long vandute = countBileteValidePentruEveniment(eveniment);
            if (capacitate <= 0 || vandute + cantitate > capacitate) {
                System.out.println("Capacitate insuficienta! Locuri ramase: " + Math.max(0, capacitate - vandute));
                return;
            }
        }

        double pretBilet = calcPretBilet(eveniment, tip);
        if (comanda != null && comanda.getCodPromo() != null) {
            pretBilet = aplicaCodPromoLaSuma(pretBilet, comanda.getCodPromo().getCod());
        }

        double pretTotal = pretBilet * cantitate;
        if (user.getBalanta() < pretTotal) {
            System.out.println("Fonduri insuficiente! Top-up necesar.");
            return;
        }

        if (comanda == null) {
            comanda = creeazaComanda(user);
        }
        // Persistam comanda inainte de a-i atasa bilete/tranzactii
        comanda.setStatus("IN_PROGRESS");
        this.comandaDAO.create(comanda);

        // Cream cate un bilet pentru fiecare bucata, in aceeasi comanda
        for (int i = 0; i < cantitate; i++) {
            Bilet bilet = new Bilet(0, eveniment, user, tip);
            bilet.setComanda(comanda);
            comanda.addBilet(bilet);
            this.biletDAO.create(bilet);
        }

        user.setBalanta(user.getBalanta() - pretTotal);
        this.userDAO.update(user);

        Tranzactie tranzactie = new Tranzactie(0, comanda, pretTotal, new Date());
        comanda.setTranzactie(tranzactie);
        comanda.setStatus("FINALIZATA");
        this.tranzactieDAO.create(tranzactie);
        this.comandaDAO.update(comanda);

        this.audit.logActiune("cumparaBilet");
    }

    public Comanda creeazaComanda(User user) {
        return new Comanda(0, user, new Date());
    }

    public Bilet cumparaBilet(int userId, int evenimentId, TipBilet tip) {
        Comanda comanda = cumparaBilete(userId, evenimentId, tip, 1);
        return comanda == null || comanda.getBilete().isEmpty() ? null : comanda.getBilete().get(0);
    }

    public Comanda cumparaBilete(int userId, int evenimentId, TipBilet tip, int cantitate) {
        User user = getUserById(userId);
        Eveniment eveniment = cautaEvenimentDupaId(evenimentId);
        if (user == null || eveniment == null) {
            return null;
        }

        Comanda comanda = creeazaComanda(user);
        cumparaBilete(user, eveniment, tip, comanda, cantitate);
        return comanda.getBilete().isEmpty() ? null : comanda;
    }

    // Cumparare cu tipuri mixte intr-o singura comanda (un "cos").
    // cos = map de la tip de bilet la cantitate (ex: VIP->1, STUDENT->2)
    public Comanda cumparaCos(int userId, int evenimentId, Map<TipBilet, Integer> cos) {
        User user = getUserById(userId);
        Eveniment eveniment = cautaEvenimentDupaId(evenimentId);
        if (user == null || eveniment == null) {
            System.out.println("User sau eveniment invalid!");
            return null;
        }
        if (cos == null || cos.isEmpty()) {
            System.out.println("Cosul este gol!");
            return null;
        }

        // Cantitatea totala de bilete din cos
        int cantitateTotala = 0;
        for (int q : cos.values()) {
            cantitateTotala += q;
        }
        if (cantitateTotala <= 0) {
            System.out.println("Cantitate invalida!");
            return null;
        }

        // Verificare early bird pentru fiecare tip cerut
        if (cos.containsKey(TipBilet.EARLY_BIRD)) {
            long diffInMillies = eveniment.getData().getTime() - new Date().getTime();
            long diffInDays = diffInMillies / (24 * 60 * 60 * 1000);
            if (diffInDays < 30) {
                System.out.println("Biletele Early Bird pot fi cumparate doar cu cel putin o luna (30 zile) inainte de eveniment!");
                System.out.println("Zile ramase pana la eveniment: " + diffInDays);
                return null;
            }
        }

        // Verificare capacitate pentru tot cosul
        Locatie locatie = eveniment.getLocatie();
        if (locatie != null) {
            int capacitate = locatie.getCapacitate();
            long vandute = countBileteValidePentruEveniment(eveniment);
            if (capacitate <= 0 || vandute + cantitateTotala > capacitate) {
                System.out.println("Capacitate insuficienta! Locuri ramase: " + Math.max(0, capacitate - vandute));
                return null;
            }
        }

        // Calculam pretul total al cosului
        double pretTotal = 0;
        for (Map.Entry<TipBilet, Integer> linie : cos.entrySet()) {
            pretTotal += calcPretBilet(eveniment, linie.getKey()) * linie.getValue();
        }

        if (user.getBalanta() < pretTotal) {
            System.out.println("Fonduri insuficiente! Top-up necesar.");
            return null;
        }

        // Cream comanda si toate biletele (de toate tipurile) in aceeasi comanda
        Comanda comanda = creeazaComanda(user);
        comanda.setStatus("IN_PROGRESS");
        this.comandaDAO.create(comanda);

        for (Map.Entry<TipBilet, Integer> linie : cos.entrySet()) {
            TipBilet tip = linie.getKey();
            int cantitate = linie.getValue();
            for (int i = 0; i < cantitate; i++) {
                Bilet bilet = new Bilet(0, eveniment, user, tip);
                bilet.setComanda(comanda);
                comanda.addBilet(bilet);
                this.biletDAO.create(bilet);
            }
        }

        user.setBalanta(user.getBalanta() - pretTotal);
        this.userDAO.update(user);

        Tranzactie tranzactie = new Tranzactie(0, comanda, pretTotal, new Date());
        comanda.setTranzactie(tranzactie);
        comanda.setStatus("FINALIZATA");
        this.tranzactieDAO.create(tranzactie);
        this.comandaDAO.update(comanda);

        this.audit.logActiune("cumparaBilet");
        return comanda;
    }

    public void refundBilet(int biletId) {
        Bilet bilet = this.biletDAO.read(biletId);

        if (bilet != null && bilet.isValid()) {
            Comanda comanda = bilet.getComanda();

            // Restituim doar pretul biletului individual, nu totalul comenzii
            double suma = calcPretBilet(bilet.getEveniment(), bilet.getTipBilet());

            User user = bilet.getUser();
            user.setBalanta(user.getBalanta() + suma);
            this.userDAO.update(user);

            // Marcam biletul invalid si il stergem din DB
            bilet.setValid(false);
            this.biletDAO.delete(biletId);

            // Comanda devine REFUNDATA doar daca nu mai are niciun bilet valid;
            // altfel ramane FINALIZATA (refund partial dintr-o comanda cu mai multe bilete)
            if (comanda != null) {
                long bileteRamase = this.biletDAO.readAll().stream()
                        .filter(b -> b.getComanda() != null && b.getComanda().getId() == comanda.getId() && b.isValid())
                        .count();
                if (bileteRamase == 0) {
                    comanda.setStatus("REFUNDATA");
                    this.comandaDAO.update(comanda);
                }
            }

            this.audit.logActiune("refundBilet");
        }
    }

    public boolean refundBiletPentruUser(int biletId, int userId) {
        Bilet bilet = this.biletDAO.read(biletId);

        if (bilet == null || !bilet.isValid() || bilet.getUser() == null || bilet.getUser().getId() != userId) {
            return false;
        }

        refundBilet(biletId);
        return true;
    }

    public List<Comanda> getIstoricComenziUser(int userId) {
        List<Comanda> comenzi = this.comandaDAO.readByUser(userId);
        if (comenzi != null && !comenzi.isEmpty()) {
            return comenzi;
        }
        return null;
    }

    public List<Eveniment> cautaEvenimentDupaNume(String nume) {
        if (nume == null || nume.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String pattern = ".*" + nume.toLowerCase().trim() + ".*";

        List<Eveniment> rezultate = this.evenimentDAO.readAll().stream()
                .filter(e -> e.getNume().toLowerCase().matches(pattern))
                .collect(Collectors.toList());
        this.audit.logActiune("cautaEvenimentDupaNume");
        return rezultate;
    }
    public List<Eveniment> filtreazaEvenimenteDupaTip(String tipEveniment) {
        return this.evenimentDAO.readAll().stream()
                .filter(e -> e.getTipEveniment().equals(tipEveniment))
                .collect(Collectors.toList());
    }
    public List<Eveniment> filtreazaEvenimenteDupaLocatie(int locatieId) {
        return this.evenimentDAO.readAll().stream()
                .filter(e -> e.getLocatie() != null && e.getLocatie().getId() == locatieId)
                .collect(Collectors.toList());
    }

    public List<Eveniment> filtreazaEvenimenteDupaTara(String tara) {
        if (tara == null) {
            return new ArrayList<>();
        }
        String t = tara.trim().toLowerCase();
        if (t.isEmpty()) {
            return new ArrayList<>();
        }
        List<Eveniment> rezultate = this.evenimentDAO.readAll().stream()
                .filter(e -> e.getLocatie() != null && e.getLocatie().getCountry() != null && e.getLocatie().getCountry().trim().toLowerCase().equals(t))
                .collect(Collectors.toList());
        this.audit.logActiune("filtreazaEvenimenteDupaTara");
        return rezultate;
    }
    public List<Eveniment> filtreazaEvenimenteDupaData(Date dataStart, Date dataEnd) {
        return this.evenimentDAO.readAll().stream()
                .filter(e -> !e.getData().before(dataStart) && !e.getData().after(dataEnd))
                .collect(Collectors.toList());
    }

    public double raportVanzariPerLocatie(int locatieId) {
        return this.comandaDAO.readAll().stream()
                .filter(c -> "FINALIZATA".equals(c.getStatus()))
                .filter(c -> {
                    List<Bilet> bileteComanda = this.biletDAO.readAll().stream()
                            .filter(b -> b.getComanda() != null && b.getComanda().getId() == c.getId())
                            .collect(Collectors.toList());
                    return !bileteComanda.isEmpty() && bileteComanda.get(0).getEveniment().getLocatie() != null
                            && bileteComanda.get(0).getEveniment().getLocatie().getId() == locatieId;
                })
                .mapToDouble(c -> getTotalVanzariComanda(c.getId()))
                .sum();
    }
    public double raportVanzariPerEveniment(int evenimentId) {
        return this.comandaDAO.readAll().stream()
                .filter(c -> "FINALIZATA".equals(c.getStatus()))
                .filter(c -> {
                    List<Bilet> bileteComanda = this.biletDAO.readAll().stream()
                            .filter(b -> b.getComanda() != null && b.getComanda().getId() == c.getId())
                            .collect(Collectors.toList());
                    return !bileteComanda.isEmpty() && bileteComanda.get(0).getEveniment().getId() == evenimentId;
                })
                .mapToDouble(c -> getTotalVanzariComanda(c.getId()))
                .sum();
    }
    public int countBileteVandute(int evenimentId) {
        return (int) this.biletDAO.readByEveniment(evenimentId).stream()
                .filter(Bilet::isValid)
                .count();
    }

    public double aplicaCodPromo(Comanda comanda, String codPromo) {
        CodPromo promo = this.codPromoDAO.readAll().stream()
                .filter(cp -> cp.getCod().equals(codPromo) && cp.isActiv())
                .findFirst()
                .orElse(null);

        double totalBilete = comanda.getBilete().stream()
            .mapToDouble(b -> calcPretBilet(b.getEveniment(), b.getTipBilet()))
            .sum();

        if (promo != null) {

            if (promo.isProcentual()) {
                return totalBilete * (1 - promo.getValoare() / 100);
            } else {
                return Math.max(0, totalBilete - promo.getValoare());
            }
        }
        return totalBilete;
    }

    public void transferBilet(int biletId, User userNou) {
        Bilet bilet = this.biletDAO.read(biletId);

        if (bilet != null) {
            bilet.setUser(userNou);
            this.biletDAO.update(bilet);
            this.audit.logActiune("transferBilet");
            System.out.println("Bilet transferat catre user: " + userNou.getNume());
        } else {
            System.out.println("Bilet nu gasit!");
        }
    }

    public void adaugaRecenzie(User user, Eveniment eveniment, int nota, String comentariu) {
        Recenzie recenzie = new Recenzie(0, user, eveniment, nota, comentariu, new Date());
        this.recenzieDAO.create(recenzie);
        this.audit.logActiune("adaugaRecenzie");
    }

    public List<Recenzie> getRecenziiEveniment(int evenimentId) {
        return this.recenzieDAO.readByEveniment(evenimentId);
    }

    public void adaugaUser(User user) {
        if (user == null) {
            return;
        }
        this.userDAO.create(user);
        this.audit.logActiune("adaugaUser");
    }

    public void adaugaCodPromo(CodPromo cod) {
        if (cod == null) {
            return;
        }
        this.codPromoDAO.create(cod);
        this.audit.logActiune("adaugaCodPromo");
    }

    public User getUserById(int userId) {
        return this.userDAO.read(userId);
    }

    public User findUserByEmail(String email) {
        if (email == null) {
            return null;
        }
        return this.userDAO.readByEmail(email);
    }

    public boolean isEmailDejaFolosit(String email) {
        if (email == null) {
            return false;
        }
        return this.userDAO.readByEmail(email) != null;
    }

    public User authenticateUser(String email, String parola) {
        User u = findUserByEmail(email);
        if (u == null) {
            return null;
        }
        return u.getParola() != null && u.getParola().equals(parola) ? u : null;
    }

    public boolean topUpBalance(int userId, double suma) {
        if (suma <= 0) {
            return false;
        }
        User u = getUserById(userId);
        if (u == null) {
            return false;
        }
        u.setBalanta(u.getBalanta() + suma);
        this.userDAO.update(u);
        this.audit.logActiune("topUpBalance");
        return true;
    }

    public List<Bilet> getBileteUser(int userId) {
        return this.biletDAO.readByUser(userId);
    }

    public boolean transferBiletPentruUser(int biletId, int userIdFrom, int userIdTo) {
        if (userIdFrom == userIdTo) {
            return false;
        }
        User to = getUserById(userIdTo);
        if (to == null) {
            return false;
        }
        Bilet bilet = this.biletDAO.read(biletId);
        if (bilet == null || bilet.getUser() == null || bilet.getUser().getId() != userIdFrom) {
            return false;
        }
        transferBilet(biletId, to);
        return true;
    }

    public List<Eveniment> getToateEvenimentele() {
        return this.evenimentDAO.readAll();
    }
    public double getTotalVanzariComanda(int comandaId) {
        Tranzactie tranzactie = this.tranzactieDAO.readAll().stream()
                .filter(t -> t.getComanda() != null && t.getComanda().getId() == comandaId)
                .findFirst()
                .orElse(null);
        return tranzactie != null ? tranzactie.getSuma() : 0;
    }
    public double getAverageRatingEveniment(int evenimentId) {
        List<Recenzie> recenziiEveniment = getRecenziiEveniment(evenimentId);
        if (recenziiEveniment.isEmpty()) return 0;
        return recenziiEveniment.stream()
                .mapToDouble(Recenzie::getNota)
                .average()
                .orElse(0);
    }

    public void showUser(int userId) {
        User user = this.userDAO.read(userId);

        if (user != null) {
            System.out.println("\n========== USER ==========");
            System.out.println("ID: " + user.getId());
            System.out.println("Nume: " + user.getNume());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Parola: " + user.getParola());
            System.out.println("Balanta: " + String.format("%.2f", user.getBalanta()) + " lei");

            // Afisare comenzi
            System.out.println("\n--- COMENZI ---");
            List<Comanda> comenzi = this.comandaDAO.readByUser(userId);
            if (comenzi.isEmpty()) {
                System.out.println("Nu ai comenzi.");
            } else {
                for (Comanda c : comenzi) {
                    System.out.println("Comanda " + c.getId() + " din " + c.getDataComanda());
                    System.out.println("   Status: " + c.getStatus());
                    double total = getTotalVanzariComanda(c.getId());
                    if (total > 0) {
                        System.out.println("   Total: " + String.format("%.2f", total) + " lei");
                    }
                    System.out.println();
                }
            }

            // Afisare tranzactii
            System.out.println("\n--- TRANZACTII ---");
            List<Tranzactie> userTransactions = this.tranzactieDAO.readAll().stream()
                    .filter(t -> t.getComanda() != null && t.getComanda().getUser() != null && t.getComanda().getUser().getId() == userId)
                    .collect(Collectors.toList());

            if (userTransactions.isEmpty()) {
                System.out.println("Nu ai tranzactii.");
            } else {
                for (Tranzactie t : userTransactions) {
                    System.out.println("Tranzactie " + t.getId() + " din " + t.getDataTranzactie());
                    System.out.println("   Suma: " + String.format("%.2f", t.getSuma()) + " lei");
                    System.out.println("   Pentru comanda: " + t.getComanda().getId());
                    System.out.println();
                }
            }
            System.out.println("=========================\n");
        } else {
            System.out.println("User inexistent!");
        }
    }

    public void showEveniment(int evenimentId) {
        Eveniment eveniment = cautaEvenimentDupaId(evenimentId);

        if (eveniment != null) {
            System.out.println("\n========== EVENIMENT ==========");
            System.out.println("ID: " + eveniment.getId());
            System.out.println("Nume: " + eveniment.getNume());
            System.out.println("Tip: " + eveniment.getTipEveniment());
            System.out.println("Data: " + eveniment.getData());
            System.out.println("Pret: " + eveniment.getPret());
            System.out.println("Locatie: " + eveniment.getLocatie().getNume());
            System.out.println("Bilete vandute: " + countBileteVandute(evenimentId));
            System.out.println("================================\n");
        } else {
            System.out.println("Eveniment inexistent!");
        }
    }

    public void showBilet(int biletId) {
        Bilet bilet = this.biletDAO.read(biletId);

        if (bilet != null) {
            System.out.println("\n========== BILET ==========");
            System.out.println("ID: " + bilet.getId());
            System.out.println("Eveniment: " + bilet.getEveniment().getNume());
            System.out.println("User: " + bilet.getUser().getNume());
            System.out.println("Tip: " + bilet.getTipBilet());
            System.out.println("Valid: " + bilet.isValid());
            System.out.println("===========================\n");
        } else {
            System.out.println("Bilet inexistent!");
        }
    }

    public void showComanda(int comandaId) {
        Comanda comanda = this.comandaDAO.read(comandaId);

        if (comanda != null) {
            System.out.println("\n========== COMANDA ==========");
            System.out.println("ID: " + comanda.getId());
            System.out.println("User: " + comanda.getUser().getNume());
            System.out.println("Data: " + comanda.getDataComanda());
            System.out.println("Status: " + comanda.getStatus());
            double total = getTotalVanzariComanda(comandaId);
            if (total > 0) {
                System.out.println("Total: " + total);
            }
            System.out.println("=============================\n");
        } else {
            System.out.println("Comanda inexistenta!");
        }
    }

    public void showRecenzie(int recenzieId) {
        Recenzie recenzie = this.recenzieDAO.read(recenzieId);

        if (recenzie != null) {
            System.out.println("\n========== RECENZIE ==========");
            System.out.println("ID: " + recenzie.getId());
            System.out.println("User: " + recenzie.getUser().getNume());
            System.out.println("Eveniment: " + recenzie.getEveniment().getNume());
            System.out.println("Nota: " + recenzie.getNota() + "/5");
            System.out.println("Comentariu: " + recenzie.getComentariu());
            System.out.println("Data: " + recenzie.getDataRecenzie());
            System.out.println("==============================\n");
        } else {
            System.out.println("Recenzie inexistenta!");
        }
    }

    public void showCodPromo(int codPromoId) {
        CodPromo codPromo = this.codPromoDAO.read(codPromoId);

        if (codPromo != null) {
            System.out.println("\n========== COD PROMO ==========");
            System.out.println("ID: " + codPromo.getId());
            System.out.println("Cod: " + codPromo.getCod());
            System.out.println("Valoare: " + codPromo.getValoare());
            System.out.println("Tip: " + (codPromo.isProcentual() ? "Procent" : "Suma fixa"));
            System.out.println("Activ: " + codPromo.isActiv());
            System.out.println("Data Start: " + codPromo.getDataStart());
            System.out.println("Data End: " + codPromo.getDataEnd());
            System.out.println("===============================\n");
        } else {
            System.out.println("Cod promo inexistent!");
        }
    }

    public void showLocatie(int locatieId) {
        Locatie locatie = this.locatieDAO.read(locatieId);

        if (locatie != null) {
            int nrEvenimente = filtreazaEvenimenteDupaLocatie(locatieId).size();
            System.out.println("\n========== LOCATIE ==========");
            System.out.println("ID: " + locatie.getId());
            System.out.println("Nume: " + locatie.getNume());
            System.out.println("Adresa: " + locatie.getAdresa());
            System.out.println("Tara: " + locatie.getCountry());
            System.out.println("Capacitate: " + locatie.getCapacitate());
            System.out.println("Evenimente: " + nrEvenimente);
            System.out.println("=============================\n");
        } else {
            System.out.println("Locatie inexistenta!");
        }
    }

    public void showAllEvenimente() {
        List<Eveniment> evenimente = this.evenimentDAO.readAll();
        if (evenimente.isEmpty()) {
            System.out.println("Niciun eveniment in sistem!");
            return;
        }
        System.out.println("\n========== TOATE EVENIMENTELE ==========");
        for (Eveniment e : evenimente) {
            String locatieInfo = e.getLocatie() != null ?
                e.getLocatie().getNume() + ", " + e.getLocatie().getCountry() : "Necunoscuta";
            System.out.println(e.getId() + ". " + e.getNume() + " (" + e.getTipEveniment() + ")");
            System.out.println("   Data: " + e.getData());
            System.out.println("   Pret: " + String.format("%.2f", e.getPret()) + " lei");
            System.out.println("   Locatie: " + locatieInfo);
            System.out.println("   Detalii: " + e.getDetaliiSpecifice());
            System.out.println();
        }
        System.out.println("=======================================\n");
        this.audit.logActiune("showAllEvenimente");
    }

    public void showAllUsers() {
        List<User> users = this.userDAO.readAll();
        if (users.isEmpty()) {
            System.out.println("Niciun user in sistem!");
            return;
        }
        System.out.println("\n========== TOTI USERII ==========");
        for (User u : users) {
            System.out.println(u.getId() + ". " + u.getNume() + " (" + u.getEmail() + ")");
        }
        System.out.println("==================================\n");
    }

    public void showAllComenzi() {
        List<Comanda> comenzi = this.comandaDAO.readAll();
        if (comenzi.isEmpty()) {
            System.out.println("Nicio comanda in sistem!");
            return;
        }
        System.out.println("\n========== TOATE COMENZILE ==========");
        for (Comanda c : comenzi) {
            System.out.println(c.getId() + ". User: " + c.getUser().getNume() + " - Status: " + c.getStatus());
        }
        System.out.println("=====================================\n");
    }

    public void showAllBilete() {
        List<Bilet> bilete = this.biletDAO.readAll();
        if (bilete.isEmpty()) {
            System.out.println("Niciun bilet in sistem!");
            return;
        }
        System.out.println("\n========== TOATE BILETELE ==========");
        for (Bilet b : bilete) {
            String eveniment = b.getEveniment() != null ? b.getEveniment().getNume() : "Necunoscut";
            String user = b.getUser() != null ? b.getUser().getNume() : "Necunoscut";
            System.out.println("Bilet " + b.getId() + " - " + eveniment);
            System.out.println("   Utilizator: " + user);
            System.out.println("   Tip: " + b.getTipBilet());
            System.out.println("   Status: " + (b.isValid() ? "Valid" : "Invalid"));
            System.out.println();
        }
        System.out.println("=====================================\n");
    }

    public void exportBiletToTXT(Bilet bilet) {
        if (bilet == null) {
            return;
        }

        try {
            // Salvam in folderul "exports" (montat ca volum in docker-compose),
            // ca biletele exportate sa ramana pe disc dupa oprirea containerului.
            java.io.File folder = new java.io.File("exports");
            if (!folder.exists()) {
                folder.mkdirs();
            }
            String fileName = "exports/bilet_" + bilet.getId() + ".txt";
            java.io.FileWriter writer = new java.io.FileWriter(fileName);

            writer.write("========== BILET EVENIMENT ==========\n");
            writer.write("ID Bilet: " + bilet.getId() + "\n");
            writer.write("Eveniment: " + bilet.getEveniment().getNume() + "\n");
            writer.write("Tip Eveniment: " + bilet.getEveniment().getTipEveniment() + "\n");
            writer.write("Data Eveniment: " + bilet.getEveniment().getData() + "\n");
            writer.write("Locatie: " + bilet.getEveniment().getLocatie().getNume() + "\n");
            writer.write("Adresa: " + bilet.getEveniment().getLocatie().getAdresa() + "\n");
            writer.write("Tara: " + bilet.getEveniment().getLocatie().getCountry() + "\n");
            writer.write("Capacitate: " + bilet.getEveniment().getLocatie().getCapacitate() + "\n");
            writer.write("Posesor: " + bilet.getUser().getNume() + "\n");
            writer.write("Email: " + bilet.getUser().getEmail() + "\n");
            writer.write("Tip Bilet: " + bilet.getTipBilet() + "\n");
            writer.write("Pret: " + String.format("%.2f", calcPretBilet(bilet.getEveniment(), bilet.getTipBilet())) + " lei\n");
            writer.write("Status: " + (bilet.isValid() ? "Valid" : "Invalid") + "\n");
            writer.write("Detalii Eveniment: " + bilet.getEveniment().getDetaliiSpecifice() + "\n");
            writer.write("=====================================\n");

            writer.close();
            this.audit.logActiune("exportBiletToTXT");
            System.out.println("Bilet exportat in fisierul: " + fileName);
        } catch (java.io.IOException e) {
            System.out.println("Eroare la exportul biletului: " + e.getMessage());
        }
    }
}