package models;
import java.util.*;
import java.util.stream.Collectors;

public class EvenimentService {
    private List<Eveniment> evenimente;
    private List<User> users;
    private List<Bilet> bilete;
    private List<Comanda> comenzi;
    private List<Recenzie> recenzii;
    private List<Tranzactie> tranzactii;
    private List<CodPromo> coduriPromo;

    private Set<String> emailUnice;
    private Map<Integer, Eveniment> evenimenteMap;

    private int nextEvenimentId = 1;
    private int nextLocatieId = 1;
    private int nextUserId = 1;
    private int nextBiletId = 1;
    private int nextComandaId = 1;
    private int nextRecenzieId = 1;
    private int nextTranzactieId = 1;
    private int nextCodPromoId = 1;

    public EvenimentService() {
        this.evenimente = new ArrayList<>();
        this.users = new ArrayList<>();
        this.bilete = new ArrayList<>();
        this.comenzi = new ArrayList<>();
        this.recenzii = new ArrayList<>();
        this.tranzactii = new ArrayList<>();
        this.coduriPromo = new ArrayList<>();
        this.emailUnice = new HashSet<>();
        this.evenimenteMap = new HashMap<>();
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

        CodPromo promo = this.coduriPromo.stream()
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
        if (eveniment == null || eveniment.getBilete() == null) {
            return 0;
        }
        return eveniment.getBilete().stream().filter(Bilet::isValid).count();
    }

    public void adaugaEveniment(Eveniment eveniment) {
        if (eveniment == null) {
            return;
        }

        eveniment.setId(nextEvenimentId++);
        if (eveniment.getLocatie() != null) {
            if (eveniment.getLocatie().getId() <= 0) {
                eveniment.getLocatie().setId(nextLocatieId++);
            }
            eveniment.getLocatie().addEveniment(eveniment);
        }
        this.evenimente.add(eveniment);
        this.evenimenteMap.put(eveniment.getId(), eveniment);
    }
    public void stergeEveniment(int evenimentId) {
        this.evenimente.removeIf(e -> e.getId() == evenimentId);
        this.evenimenteMap.remove(evenimentId);
    }
    public void modificaEveniment(int evenimentId, Eveniment evenimentNou) {
        for (int i = 0; i < this.evenimente.size(); i++) {
            if (this.evenimente.get(i).getId() == evenimentId) {
                this.evenimente.set(i, evenimentNou);
                this.evenimenteMap.put(evenimentId, evenimentNou);
                break;
            }
        }
    }
    public Eveniment cautaEvenimentDupaId(int evenimentId) {
        // Using Map for faster O(1) lookup instead of O(n) stream
        return this.evenimenteMap.get(evenimentId);
    }

    public void cumparaBilet(User user, Eveniment eveniment, TipBilet tip, Comanda comanda) {
        if (user == null || eveniment == null) {
            System.out.println("User sau eveniment invalid!");
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
            if (capacitate <= 0 || vandute >= capacitate) {
                System.out.println("Capacitate atinsa, nu mai sunt bilete disponibile.");
                return;
            }
        }

        double pretBilet = calcPretBilet(eveniment, tip);
        if (comanda != null && comanda.getCodPromo() != null) {
            pretBilet = aplicaCodPromoLaSuma(pretBilet, comanda.getCodPromo().getCod());
        }

        if (user.getBalanta() < pretBilet) {
            System.out.println("Fonduri insuficiente! Top-up necesar.");
            return;
        }

        if (comanda == null) {
            comanda = creeazaComanda(user);
        }

        Bilet bilet = new Bilet(nextBiletId++, eveniment, user, tip);
        this.bilete.add(bilet);
        comanda.addBilet(bilet);
        bilet.setComanda(comanda);

        user.setBalanta(user.getBalanta() - pretBilet);

        comanda.setTranzactie(new Tranzactie(nextTranzactieId++, comanda, pretBilet, new Date()));
        comanda.setStatus("FINALIZATA");
        this.comenzi.add(comanda);
        this.tranzactii.add(comanda.getTranzactie());

    }

    public Comanda creeazaComanda(User user) {
        return new Comanda(nextComandaId++, user, new Date());
    }

    public Bilet cumparaBilet(int userId, int evenimentId, TipBilet tip) {
        User user = getUserById(userId);
        Eveniment eveniment = cautaEvenimentDupaId(evenimentId);
        if (user == null || eveniment == null) {
            return null;
        }

        Comanda comanda = creeazaComanda(user);
        cumparaBilet(user, eveniment, tip, comanda);
        return comanda.getBilete().isEmpty() ? null : comanda.getBilete().get(0);
    }

    public void refundBilet(int biletId) {
        Bilet bilet = this.bilete.stream()
                .filter(b -> b.getId() == biletId)
                .findFirst()
                .orElse(null);

        if (bilet != null && bilet.isValid()) {
            Comanda comanda = bilet.getComanda();
            double suma = comanda != null && comanda.getTranzactie() != null ? comanda.getTranzactie().getSuma() : 0;
            User user = bilet.getUser();
            user.setBalanta(user.getBalanta() + suma);
            comanda.setStatus("REFUNDATA");
            this.bilete.remove(bilet);
            if (comanda != null) {
                comanda.getBilete().remove(bilet);
            }
            if (bilet.getEveniment() != null && bilet.getEveniment().getBilete() != null) {
                bilet.getEveniment().getBilete().remove(bilet);
            }
        }
    }

    public boolean refundBiletPentruUser(int biletId, int userId) {
        Bilet bilet = this.bilete.stream()
                .filter(b -> b.getId() == biletId)
                .findFirst()
                .orElse(null);

        if (bilet == null || !bilet.isValid() || bilet.getUser() == null || bilet.getUser().getId() != userId) {
            return false;
        }

        refundBilet(biletId);
        return true;
    }

    public List<Comanda> getIstoricComenziUser(int userId) {
        User user = this.users.stream()
                .filter(u -> u.getId() == userId)
                .findFirst()
                .orElse(null);

        if (user != null && !user.getComenzi().isEmpty()) {
            return user.getComenzi();
        }
        return null;
    }

    public List<Eveniment> cautaEvenimentDupaNume(String nume) {
        if (nume == null || nume.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String pattern = ".*" + nume.toLowerCase().trim() + ".*";

        return this.evenimente.stream()
                .filter(e -> e.getNume().toLowerCase().matches(pattern))
                .collect(Collectors.toList());
    }
    public List<Eveniment> filtreazaEvenimenteDupaTip(String tipEveniment) {
        return this.evenimente.stream()
                .filter(e -> e.getTipEveniment().equals(tipEveniment))
                .collect(Collectors.toList());
    }
    public List<Eveniment> filtreazaEvenimenteDupaLocatie(int locatieId) {
        return this.evenimente.stream()
                .filter(e -> e.getLocatie().getId() == locatieId)
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
        return this.evenimente.stream()
                .filter(e -> e.getLocatie() != null && e.getLocatie().getCountry() != null && e.getLocatie().getCountry().trim().toLowerCase().equals(t))
                .collect(Collectors.toList());
    }
    public List<Eveniment> filtreazaEvenimenteDupaData(Date dataStart, Date dataEnd) {
        return this.evenimente.stream()
                .filter(e -> !e.getData().before(dataStart) && !e.getData().after(dataEnd))
                .collect(Collectors.toList());
    }

    public double raportVanzariPerLocatie(int locatieId) {
        return this.comenzi.stream()
                .filter(c -> "FINALIZATA".equals(c.getStatus()))
                .filter(c -> !c.getBilete().isEmpty() && c.getBilete().get(0).getEveniment().getLocatie().getId() == locatieId)
                .mapToDouble(c -> c.getTranzactie().getSuma())
                .sum();
    }
    public double raportVanzariPerEveniment(int evenimentId) {
        return this.comenzi.stream()
                .filter(c -> "FINALIZATA".equals(c.getStatus()))
                .filter(c -> !c.getBilete().isEmpty() && c.getBilete().get(0).getEveniment().getId() == evenimentId)
                .mapToDouble(c -> c.getTranzactie().getSuma())
                .sum();
    }
    public int countBileteVandute(int evenimentId) {
        return (int) this.bilete.stream()
                .filter(b -> b.getEveniment().getId() == evenimentId && b.isValid())
                .count();
    }

    public double aplicaCodPromo(Comanda comanda, String codPromo) {
        CodPromo promo = this.coduriPromo.stream()
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
        Bilet bilet = this.bilete.stream()
                .filter(b -> b.getId() == biletId)
                .findFirst()
                .orElse(null);

        if (bilet != null) {
            bilet.setUser(userNou);
            System.out.println("Bilet transferat catre user: " + userNou.getNume());
        } else {
            System.out.println("Bilet nu gasit!");
        }
    }

    public void adaugaRecenzie(User user, Eveniment eveniment, int nota, String comentariu) {
        Recenzie recenzie = new Recenzie(nextRecenzieId++, user, eveniment, nota, comentariu, new Date());
        this.recenzii.add(recenzie);
    }

    public List<Recenzie> getRecenziiEveniment(int evenimentId) {
        return this.recenzii.stream()
                .filter(r -> r.getEveniment().getId() == evenimentId)
                .collect(Collectors.toList());
    }

    public void adaugaUser(User user) {
        if (user == null) {
            return;
        }

        user.setId(nextUserId++);
        this.users.add(user);
        if (user.getEmail() != null) {
            this.emailUnice.add(user.getEmail().toLowerCase());
        }
    }

    public void adaugaCodPromo(CodPromo cod) {
        if (cod == null) {
            return;
        }

        cod.setId(nextCodPromoId++);
        this.coduriPromo.add(cod);
    }

    public User getUserById(int userId) {
        return this.users.stream()
                .filter(u -> u.getId() == userId)
                .findFirst()
                .orElse(null);
    }

    public User findUserByEmail(String email) {
        if (email == null) {
            return null;
        }

        String e = email.trim().toLowerCase();
        return this.users.stream()
                .filter(u -> u.getEmail() != null && u.getEmail().trim().toLowerCase().equals(e))
                .findFirst()
                .orElse(null);
    }

    public boolean isEmailDejaFolosit(String email) {
        if (email == null) {
            return false;
        }
        return this.emailUnice.contains(email.toLowerCase());
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
        return true;
    }

    public List<Bilet> getBileteUser(int userId) {
        return this.bilete.stream()
                .filter(b -> b.getUser() != null && b.getUser().getId() == userId && b.isValid())
                .collect(Collectors.toList());
    }

    public boolean transferBiletPentruUser(int biletId, int userIdFrom, int userIdTo) {
        if (userIdFrom == userIdTo) {
            return false;
        }
        User to = getUserById(userIdTo);
        if (to == null) {
            return false;
        }
        Bilet bilet = this.bilete.stream()
                .filter(b -> b.getId() == biletId)
                .findFirst()
                .orElse(null);
        if (bilet == null || bilet.getUser() == null || bilet.getUser().getId() != userIdFrom) {
            return false;
        }
        transferBilet(biletId, to);
        return true;
    }

    public List<Eveniment> getToateEvenimentele() {
        return this.evenimente;
    }
    public double getTotalVanzariComanda(int comandaId) {
        Comanda comanda = this.comenzi.stream()
                .filter(c -> c.getId() == comandaId)
                .findFirst()
                .orElse(null);
        return comanda != null ? comanda.getTranzactie().getSuma() : 0;
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
        User user = this.users.stream()
                .filter(u -> u.getId() == userId)
                .findFirst()
                .orElse(null);

        if (user != null) {
            System.out.println("\n========== USER ==========");
            System.out.println("ID: " + user.getId());
            System.out.println("Nume: " + user.getNume());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Parola: " + user.getParola());
            System.out.println("Balanta: " + String.format("%.2f", user.getBalanta()) + " lei");

            // Afisare comenzi
            System.out.println("\n--- COMENZI ---");
            if (user.getComenzi().isEmpty()) {
                System.out.println("Nu ai comenzi.");
            } else {
                for (Comanda c : user.getComenzi()) {
                    System.out.println("Comanda " + c.getId() + " din " + c.getDataComanda());
                    System.out.println("   Status: " + c.getStatus());
                    System.out.println("   Bilete: " + c.getBilete().size());
                    if (c.getTranzactie() != null) {
                        System.out.println("   Total: " + String.format("%.2f", c.getTranzactie().getSuma()) + " lei");
                    }
                    System.out.println();
                }
            }

            // Afisare tranzactii
            System.out.println("\n--- TRANZACTII ---");
            List<Tranzactie> userTransactions = this.tranzactii.stream()
                    .filter(t -> t.getComanda() != null && t.getComanda().getUser().getId() == userId)
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
            System.out.println("Bilete vandute: " + eveniment.getBilete().size());
            System.out.println("================================\n");
        } else {
            System.out.println("Eveniment inexistent!");
        }
    }

    public void showBilet(int biletId) {
        Bilet bilet = this.bilete.stream()
                .filter(b -> b.getId() == biletId)
                .findFirst()
                .orElse(null);

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
        Comanda comanda = this.comenzi.stream()
                .filter(c -> c.getId() == comandaId)
                .findFirst()
                .orElse(null);

        if (comanda != null) {
            System.out.println("\n========== COMANDA ==========");
            System.out.println("ID: " + comanda.getId());
            System.out.println("User: " + comanda.getUser().getNume());
            System.out.println("Data: " + comanda.getDataComanda());
            System.out.println("Bilete: " + comanda.getBilete().size());
            System.out.println("Status: " + comanda.getStatus());
            if (comanda.getTranzactie() != null) {
                System.out.println("Total: " + comanda.getTranzactie().getSuma());
            }
            System.out.println("=============================\n");
        } else {
            System.out.println("Comanda inexistenta!");
        }
    }

    public void showRecenzie(int recenzieId) {
        Recenzie recenzie = this.recenzii.stream()
                .filter(r -> r.getId() == recenzieId)
                .findFirst()
                .orElse(null);

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
        CodPromo codPromo = this.coduriPromo.stream()
                .filter(cp -> cp.getId() == codPromoId)
                .findFirst()
                .orElse(null);

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
        Eveniment evenimentTemp = this.evenimente.stream()
                .filter(e -> e.getLocatie().getId() == locatieId)
                .findFirst()
                .orElse(null);

        if (evenimentTemp != null) {
            Locatie locatie = evenimentTemp.getLocatie();
            System.out.println("\n========== LOCATIE ==========");
            System.out.println("ID: " + locatie.getId());
            System.out.println("Nume: " + locatie.getNume());
            System.out.println("Adresa: " + locatie.getAdresa());
            System.out.println("Tara: " + locatie.getCountry());
            System.out.println("Capacitate: " + locatie.getCapacitate());
            System.out.println("Evenimente: " + locatie.getEvenimente().size());
            System.out.println("=============================\n");
        } else {
            System.out.println("Locatie inexistenta!");
        }
    }

    public void showAllEvenimente() {
        if (this.evenimente.isEmpty()) {
            System.out.println("Niciun eveniment in sistem!");
            return;
        }
        System.out.println("\n========== TOATE EVENIMENTELE ==========");
        for (Eveniment e : this.evenimente) {
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
    }

    public void showAllUsers() {
        if (this.users.isEmpty()) {
            System.out.println("Niciun user in sistem!");
            return;
        }
        System.out.println("\n========== TOTI USERII ==========");
        for (User u : this.users) {
            System.out.println(u.getId() + ". " + u.getNume() + " (" + u.getEmail() + ")");
        }
        System.out.println("==================================\n");
    }

    public void showAllComenzi() {
        if (this.comenzi.isEmpty()) {
            System.out.println("Nicio comanda in sistem!");
            return;
        }
        System.out.println("\n========== TOATE COMENZILE ==========");
        for (Comanda c : this.comenzi) {
            System.out.println(c.getId() + ". User: " + c.getUser().getNume() + " - Status: " + c.getStatus());
        }
        System.out.println("=====================================\n");
    }

    public void showAllBilete() {
        if (this.bilete.isEmpty()) {
            System.out.println("Niciun bilet in sistem!");
            return;
        }
        System.out.println("\n========== TOATE BILETELE ==========");
        for (Bilet b : this.bilete) {
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
            String fileName = "bilet_" + bilet.getId() + ".txt";
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
            System.out.println("Bilet exportat in fisierul: " + fileName);
        } catch (java.io.IOException e) {
            System.out.println("Eroare la exportul biletului: " + e.getMessage());
        }
    }
}
