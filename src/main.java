import java.io.Console;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import models.*;

public class main {
    private static EvenimentService service;
    private static Console console;

    private static User currentUser;
    private static boolean isAdmin;

    public static void main(String[] args) {
        service = new EvenimentService();
        console = System.console();
        boolean running = true;
        while (running) {
            if (!isAdmin && currentUser == null) {
                running = meniuGuest();
            } else if (isAdmin) {
                running = meniuAdmin();
            } else {
                running = meniuUser();
            }
        }
    }

    // authentication
    private static boolean meniuGuest() {
        console.printf("\n========== LOGIN / REGISTER ==========\n");
        console.printf("1. Login\n");
        console.printf("2. Register\n");
        console.printf("0. Iesire\n");
        console.printf("Alegeti optiune: ");

        int optiune = citireInt();
        switch (optiune) {
            case 1:
                loginConsola();
                return true;
            case 2:
                registerConsola();
                return true;
            case 0:
                console.printf("La revedere!\n");
                return false;
            default:
                console.printf("Optiune invalida!\n");
                return true;
        }
    }

    private static void loginConsola() {
        console.printf("\n--- Login ---\n");
        console.printf("Email: ");
        String email = citireLinie();

        console.printf("Parola: ");
        String parola = citireParola();

        if ("admin".equals(email) && "admin".equals(parola)) {
            isAdmin = true;
            currentUser = null;
            console.printf("Login admin reusit!\n");
            return;
        }

        User u = service.authenticateUser(email, parola);
        if (u == null) {
            console.printf("Credentiale invalide!\n");
            return;
        }

        currentUser = u;
        isAdmin = false;
        console.printf("Login reusit. Bine ai venit, %s!\n", currentUser.getNume());
    }

    private static void registerConsola() {
        console.printf("\n--- Register ---\n");
        console.printf("Nume: ");
        String nume = citireLinie();

        console.printf("Email: ");
        String email = citireLinie();

        if (service.isEmailDejaFolosit(email)) {
            console.printf("Exista deja un cont cu acest email.\n");
            return;
        }

        console.printf("Parola: ");
        String parola = citireParola();

        User user = new User(0, nume, email, parola);
        service.adaugaUser(user);
        console.printf("Cont creat\n");
    }

    private static void logout() {
        currentUser = null;
        isAdmin = false;
        console.printf("Logout efectuat.\n");
    }

    private static boolean meniuAdmin() {
        console.printf("\n========== MENIU ADMIN ==========\n");
        console.printf("1. Gestionare evenimente\n");
        console.printf("2. Gestionare Utilizatori\n");
        console.printf("3. Gestionare Bilete\n");
        console.printf("4. Gestionare Comenzi\n");
        console.printf("5. Gestionare Recenzii\n");
        console.printf("6. Rapoarte si Statistici\n");
        console.printf("9. Logout\n");
        console.printf("Alegeti optiune: ");

        int optiune = citireInt();
        switch (optiune) {
            case 1:
                meniuEvenimente();
                return true;
            case 2:
                meniuUtilizatori();
                return true;
            case 3:
                meniuBileteAdmin();
                return true;
            case 4:
                meniuComenzi();
                return true;
            case 5:
                meniuRecenziiAdmin();
                return true;
            case 6:
                meniuRapoarte();
                return true;
            case 9:
                logout();
                return true;
            default:
                console.printf("Optiune invalida!\n");
                return true;
        }
    }

    // meniul userului 
    private static boolean meniuUser() {
        console.printf("\n========== MENIU USER ==========\n");
        console.printf("User: %s - Balanta: %.2f lei\n", currentUser.getNume(), currentUser.getBalanta());
        console.printf("1. Afiseaza toate evenimentele\n");
        console.printf("2. Cauta eveniment dupa nume\n");
        console.printf("3. Filtreaza evenimente dupa tara\n");
        console.printf("4. Cumpara bilet\n");
        console.printf("5. Transfer bilet\n");
        console.printf("6. Refund bilet\n");
        console.printf("7. Export bilet in TXT\n");
        console.printf("8. Top up balance\n");
        console.printf("9. Adauga recenzie\n");
        console.printf("10. Raport recenzii eveniment\n");
        console.printf("11. Detaliile contului meu\n");
        console.printf("0. Logout\n");
        console.printf("Alegeti optiune: ");

        int optiune = citireInt();
        switch (optiune) {
            case 1:
                service.showAllEvenimente();
                return true;
            case 2:
                cautaEvenimentConsola();
                return true;
            case 3:
                filtreazaDupaTara();
                return true;
            case 4:
                cumparaBiletConsola();
                return true;
            case 5:
                transferBiletConsola();
                return true;
            case 6:
                refundBiletConsola();
                return true;
            case 7:
                exportBiletConsola();
                return true;
            case 8:
                topUpConsola();
                return true;
            case 9:
                adaugaRecenzieUserConsola();
                return true;
            case 10:
                raportRecenziiEvenimentConsola();
                return true;
            case 11:
                service.showUser(currentUser.getId());
                console.printf("Balanta: %.2f lei\n", currentUser.getBalanta());
                return true;
            case 0:
                logout();
                return true;
            default:
                console.printf("Optiune invalida!\n");
                return true;
        }
    }

    // only for admins
    private static void meniuEvenimente() {
        boolean back = false;
        while (!back) {
            console.printf("\n========== MENIU EVENIMENTE ==========\n");
            console.printf("1. Adauga eveniment nou\n");
            console.printf("2. Sterge eveniment\n");
            console.printf("3. Modifica eveniment\n");
            console.printf("4. Cauta eveniment dupa nume\n");
            console.printf("5. Filtreaza dupa tara\n");
            console.printf("6. Afiseaza toate evenimentele\n");
            console.printf("0. Inapoi\n");
            console.printf("Alegeti optiune: ");
            
            int optiune = citireInt();
            
            switch (optiune) {
                case 1:
                    adaugaEvenimentConsola();
                    break;
                case 2:
                    stergeEvenimentConsola();
                    break;
                case 3:
                    modificaEvenimentConsola();
                    break;
                case 4:
                    cautaEvenimentConsola();
                    break;
                case 5:
                    filtreazaDupaTara();
                    break;
                case 6:
                    service.showAllEvenimente();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    console.printf("Optiune invalida!\n");
            }
        }
    }

    private static void adaugaEvenimentConsola() {
        console.printf("\n--- Adauga Eveniment Nou ---\n");
        console.printf("Tip eveniment (1=Concert, 2=SportsMatch, 3=Theatre, 4=Museum): ");
        int tipEveniment = citireInt();

        console.printf("Nume: ");
        String nume = citireLinie();
        
        console.printf("Pret: ");
        double pret = citireDouble();

        Date data = new Date();

        console.printf("\n--- Detalii locatie ---\n");
        console.printf("Nume locatie: ");
        String numeLoc = citireLinie();
        console.printf("Adresa: ");
        String adresa = citireLinie();
        console.printf("Tara: ");
        String tara = citireLinie();
        console.printf("Capacitate: ");
        int capacitate = citireInt();

        Locatie locatie = new Locatie(0, numeLoc, adresa, tara, capacitate);
        
        Eveniment eveniment = null;
        
        switch (tipEveniment) {
            case 1:
                console.printf("Artist: ");
                String artist = citireLinie();
                console.printf("Gen muzical: ");
                String gen = citireLinie();
                eveniment = new Concert(0, nume, data, locatie, pret, artist, gen);
                break;
            case 2:
                console.printf("Echipa gazda: ");
                String echipa1 = citireLinie();
                console.printf("Echipa oaspete: ");
                String echipa2 = citireLinie();
                console.printf("Competitie: ");
                String competitie = citireLinie();
                eveniment = new SportsMatch(0, nume, data, locatie, pret, echipa1, echipa2, competitie);
                break;
            case 3:
                console.printf("Piesa: ");
                String piesa = citireLinie();
                console.printf("Regizor: ");
                String regizor = citireLinie();
                eveniment = new Theatre(0, nume, data, locatie, pret, piesa, regizor);
                break;
            case 4:
                console.printf("Nume muzeu: ");
                String numeMuzeu = citireLinie();
                console.printf("Tip expozitie: ");
                String tipExpo = citireLinie();
                eveniment = new Museum(0, nume, data, locatie, pret, numeMuzeu, tipExpo);
                break;
            default:
                console.printf("Tip eveniment invalid!\n");
                return;
        }
        
        service.adaugaEveniment(eveniment);
        console.printf("Eveniment adaugat cu succes\n");
    }

    private static void stergeEvenimentConsola() {
        console.printf("Introduceti ID-ul evenimentului de sters: ");
        int id = citireInt();
        if (!confirm("Confirmi stergerea evenimentului " + id + "?")) {
            console.printf("Operatie anulata.\n");
            return;
        }
        service.stergeEveniment(id);
        console.printf("Eveniment sters!\n");
    }

    private static void modificaEvenimentConsola() {
        console.printf("Introduceti ID-ul evenimentului de modificat: ");
        int id = citireInt();
        
        Eveniment e = service.cautaEvenimentDupaId(id);
        if (e != null) {
            console.printf("Nume nou: ");
            String numeNou = citireLinie();
            e.setNume(numeNou);
            
            console.printf("Pret nou: ");
            double pretNou = citireDouble();
            e.setPret(pretNou);

            if (confirm("Vrei sa modifici si data?")) {
                Date dataNoua = citireData("Data noua (yyyy-MM-dd): ");
                if (dataNoua != null) {
                    e.setData(dataNoua);
                }
            }
            
            console.printf("Eveniment modificat!\n");
        } else {
            console.printf("Eveniment nu gasit!\n");
        }
    }

    private static void cautaEvenimentConsola() {
        console.printf("Introduceti numele evenimentului: ");
        String nume = citireLinie();
        
        var rezultate = service.cautaEvenimentDupaNume(nume);
        if (rezultate.isEmpty()) {
            console.printf("Niciun eveniment gasit!\n");
        } else {
            console.printf("\n--- Rezultate cautare ---\n");
            for (Eveniment e : rezultate) {
                console.printf("%d. %s - %.2f lei\n", e.getId(), e.getNume(), e.getPret());
            }
        }
    }


    private static void filtreazaDupaTara() {
        console.printf("Introduceti tara: ");
        String tara = citireLinie();

        var rezultate = service.filtreazaEvenimenteDupaTara(tara);
        if (rezultate.isEmpty()) {
            console.printf("Niciun eveniment in aceasta tara!\n");
        } else {
            console.printf("\n--- Rezultate ---\n");
            for (Eveniment e : rezultate) {
                console.printf("%d. %s - %s\n", e.getId(), e.getNume(), e.getLocatie().getCountry());
            }
        }
    }

    // gestionare a utilizatorilor de catre admni
    private static void meniuUtilizatori() {
        boolean back = false;
        while (!back) {
            console.printf("\n========== MENIU UTILIZATORI ==========\n");
            console.printf("1. Adauga utilizator nou\n");
            console.printf("2. Afiseaza toti utilizatorii\n");
            console.printf("3. Afiseaza detalii utilizator\n");
            console.printf("0. Inapoi\n");
            console.printf("Alegeti optiune: ");
            
            int optiune = citireInt();
            
            switch (optiune) {
                case 1:
                    adaugaUserConsola();
                    break;
                case 2:
                    service.showAllUsers();
                    break;
                case 3:
                    console.printf("ID utilizator: ");
                    service.showUser(citireInt());
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    console.printf("Optiune invalida!\n");
            }
        }
    }

    private static void adaugaUserConsola() {
        console.printf("\n--- Adauga Utilizator Nou ---\n");
        console.printf("Nume: ");
        String nume = citireLinie();
        
        console.printf("Email: ");
        String email = citireLinie();

        if (service.isEmailDejaFolosit(email)) {
            console.printf("Exista deja un user cu acest email!\n");
            return;
        }
        
        console.printf("Parola: ");
        String parolaStr = citireParola();

        User user = new User(0, nume, email, parolaStr);
        service.adaugaUser(user);
        console.printf("Utilizator adaugat cu succes!\n");
    }

    private static void meniuBileteAdmin() {
        boolean back = false;
        while (!back) {
            console.printf("\n========== MENIU BILETE (ADMIN) ==========\n");
            console.printf("1. Afiseaza bilet dupa ID\n");
            console.printf("2. Afiseaza toate biletele\n");
            console.printf("0. Inapoi\n");
            console.printf("Alegeti optiune: ");

            int optiune = citireInt();
            switch (optiune) {
                case 1:
                    console.printf("ID bilet: ");
                    service.showBilet(citireInt());
                    break;
                case 2:
                    service.showAllBilete();
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    console.printf("Optiune invalida!\n");
            }
        }
    }

    private static void cumparaBiletConsola() {
        console.printf("\n--- Cumparare Bilet ---\n");
        int userId = currentUser != null ? currentUser.getId() : -1;
        if (userId <= 0) {
            console.printf("Trebuie sa fii logat ca user.\n");
            return;
        }

        if (service.getToateEvenimentele().isEmpty()) {
            console.printf("Nu exista evenimente disponibile.\n");
            return;
        }

        service.showAllEvenimente();
        console.printf("ID eveniment: ");
        int evenimentId = citireInt();

        TipBilet tip = citireTipBilet();
        if (tip == null) {
            return;
        }

        if (!confirm("Confirmi cumpararea biletului?")) {
            console.printf("Operatie anulata.\n");
            return;
        }

        Bilet bilet = service.cumparaBilet(userId, evenimentId, tip);
        if (bilet == null) {
            console.printf("Cumparare esuata.\n");
            return;
        }

        double suma = bilet.getComanda() != null && bilet.getComanda().getTranzactie() != null ? bilet.getComanda().getTranzactie().getSuma() : 0;
        console.printf("Bilet cumparat cu succes! ID bilet=%d - Total=%.2f lei\n", bilet.getId(), suma);
    }

    private static void transferBiletConsola() {
        int userId =currentUser.getId();

        List<Bilet> bilete = service.getBileteUser(userId);
        if (bilete.isEmpty()) {
            console.printf("Nu ai bilete de transferat.\n");
            return;
        }

        int idx = alegeBiletDinLista(bilete, "Alege bilet pentru transfer: ");
        if (idx < 0) {
            return;
        }
        Bilet b = bilete.get(idx);

        console.printf("Email destinatar: ");
        String email = citireLinie();
        User dest = service.findUserByEmail(email);
        if (dest == null) {
            console.printf("Destinatar inexistent!\n");
            return;
        }

        if (!confirm("Confirmi transferul biletului " + b.getId() + " catre " + dest.getEmail() + "?")) {
            console.printf("Operatie anulata.\n");
            return;
        }

        boolean ok = service.transferBiletPentruUser(b.getId(), userId, dest.getId());
        console.printf(ok ? "Transfer efectuat!\n" : "Transfer esuat!\n");
    }

    private static void refundBiletConsola() {
        int userId = currentUser.getId();

        List<Bilet> bilete = service.getBileteUser(userId);
        if (bilete.isEmpty()) {
            console.printf("Nu ai bilete de refundat.\n");
            return;
        }

        int idx = alegeBiletDinLista(bilete, "Alege bilet pentru refund: ");
        if (idx < 0) {
            return;
        }
        Bilet b = bilete.get(idx);

        if (!confirm("Confirmi refund pentru biletul " + b.getId() + "?")) {
            console.printf("Operatie anulata.\n");
            return;
        }

        boolean ok = service.refundBiletPentruUser(b.getId(), userId);
        console.printf(ok ? "Refund efectuat!\n" : "Refund esuat!\n");
    }

    private static void topUpConsola() {
        int userId = currentUser.getId();
        if (userId <= 0) {
            console.printf("Trebuie sa fii logat ca user.\n");
            return;
        }

        console.printf("Suma de adaugat: ");
        double suma = citireDouble();
        if (!confirm("Confirmi top-up de " + String.format("%.2f", suma) + " lei?")) {
            console.printf("Operatie anulata.\n");
            return;
        }
        boolean ok = service.topUpBalance(userId, suma);
        console.printf(ok ? "Top-up efectuat!\n" : "Top-up esuat!\n");
    }

    private static void meniuComenzi() {
        boolean back = false;
        while (!back) {
            console.printf("\n========== MENIU COMENZI ==========\n");
            console.printf("1. Afiseaza toate comenzile\n");
            console.printf("2. Afiseaza detalii comanda\n");
            console.printf("3. Istoric comenzi utilizator\n");
            console.printf("0. Inapoi\n");
            console.printf("Alegeti optiune: ");
            
            int optiune = citireInt();
            
            switch (optiune) {
                case 1:
                    service.showAllComenzi();
                    break;
                case 2:
                    console.printf("ID comanda: ");
                    service.showComanda(citireInt());
                    break;
                case 3:
                    console.printf("ID utilizator: ");
                    var comenzi = service.getIstoricComenziUser(citireInt());
                    if (comenzi == null || comenzi.isEmpty()) {
                        console.printf("Niciuna comanda!\n");
                    } else {
                        for (Comanda c : comenzi) {
                            service.showComanda(c.getId());
                        }
                    }
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    console.printf("Optiune invalida!\n");
            }
        }
    }

    private static void meniuRecenziiAdmin() {
        boolean back = false;
        while (!back) {
            console.printf("\n========== MENIU RECENZII ==========\n");
            console.printf("1. Adauga recenzie\n");
            console.printf("2. Afiseaza recenzii eveniment\n");
            console.printf("3. Rating mediu eveniment\n");
            console.printf("0. Inapoi\n");
            console.printf("Alegeti optiune: ");
            
            int optiune = citireInt();
            
            switch (optiune) {
                case 1:
                    adaugaRecenzieConsola();
                    break;
                case 2:
                    console.printf("ID eveniment: ");
                    var recenzii = service.getRecenziiEveniment(citireInt());
                    for (Recenzie r : recenzii) {
                        service.showRecenzie(r.getId());
                    }
                    break;
                case 3:
                    console.printf("ID eveniment: ");
                    double rating = service.getAverageRatingEveniment(citireInt());
                    console.printf("Rating mediu: %.2f/5\n", rating);
                    break;
                case 0:
                    back = true;
                    break;
                default:
                    console.printf("Optiune invalida!\n");
            }
        }
    }

    private static void adaugaRecenzieConsola() {
        console.printf("\n--- Adauga Recenzie ---\n");
        int userId;
        if (currentUser != null) {
            userId = currentUser.getId();
        } else {
            console.printf("ID utilizator: ");
            userId = citireInt();
        }

        User user = service.getUserById(userId);
        if (user == null) {
            console.printf("User invalid!\n");
            return;
        }

        console.printf("ID eveniment: ");
        int evenimentId = citireInt();

        Eveniment ev = service.cautaEvenimentDupaId(evenimentId);
        if (ev == null) {
            console.printf("Eveniment invalid!\n");
            return;
        }
        
        console.printf("Nota (1-5): ");
        int nota = citireInt();
        
        console.printf("Comentariu: ");
        String comentariu = citireLinie();

        service.adaugaRecenzie(user, ev, nota, comentariu);
        console.printf("Recenzie adaugata!\n");
    }

    private static void raportRecenziiEvenimentConsola() {
        console.printf("ID eveniment: ");
        int evenimentId = citireInt();
        double rating = service.getAverageRatingEveniment(evenimentId);
        console.printf("Rating mediu: %.2f/5\n", rating);
        var recenzii = service.getRecenziiEveniment(evenimentId);
        if (recenzii.isEmpty()) {
            console.printf("Nu exista recenzii pentru acest eveniment.\n");
            return;
        }
        for (Recenzie r : recenzii) {
            service.showRecenzie(r.getId());
        }
    }

    private static void meniuRapoarte() {
        boolean back = false;
        while (!back) {
            console.printf("\n========== MENIU RAPOARTE ==========\n");
            console.printf("0. Inapoi\n");
            console.printf("Alegeti optiune: ");

            int optiune = citireInt();

            switch (optiune) {
                case 0:
                    back = true;
                    break;
                default:
                    console.printf("Optiune invalida!\n");
            }
        }
    }
    private static void exportBiletConsola() {
        int userId = currentUser.getId();

        List<Bilet> bilete = service.getBileteUser(userId);
        if (bilete.isEmpty()) {
            console.printf("Nu ai bilete de exportat.\n");
            return;
        }

        int idx = alegeBiletDinLista(bilete, "Alege bilet pentru export: ");
        if (idx < 0) {
            return;
        }
        Bilet b = bilete.get(idx);

        service.exportBiletToTXT(b);
        console.printf("Bilet exportat cu succes!\n");
    }

    private static void adaugaRecenzieUserConsola() {
        console.printf("\n--- Adauga Recenzie ---\n");
        int userId = currentUser.getId();

        service.showAllEvenimente();
        console.printf("ID eveniment: ");
        int evenimentId = citireInt();

        Eveniment ev = service.cautaEvenimentDupaId(evenimentId);
        if (ev == null) {
            console.printf("Eveniment invalid!\n");
            return;
        }

        console.printf("Nota (1-5): ");
        int nota = citireInt();

        if (nota < 1 || nota > 5) {
            console.printf("Nota trebuie sa fie intre 1 si 5!\n");
            return;
        }

        console.printf("Comentariu: ");
        String comentariu = citireLinie();

        service.adaugaRecenzie(currentUser, ev, nota, comentariu);
        console.printf("Recenzie adaugata cu succes!\n");
    }

    private static String citireLinie() {
        String s = console.readLine();
        return s == null ? "" : s.trim();
    }

    private static String citireParola() {
        char[] parola = console.readPassword();
        return parola == null ? "" : new String(parola);
    }

    private static int citireInt() {
        while (true) {
            String line = citireLinie();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                console.printf("Intrare invalida! Introduceti un numar: ");
            }
        }
    }

    private static double citireDouble() {
        while (true) {
            String line = citireLinie();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                console.printf("Intrare invalida! Introduceti un numar: ");
            }
        }
    }

    private static Date citireData(String prompt) {
        console.printf(prompt);
        String dataStr = citireLinie();
        if (dataStr.isEmpty()) {
            return null;
        }
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            fmt.setLenient(false);
            return fmt.parse(dataStr);
        } catch (Exception e) {
            console.printf("Data invalida (format corect: yyyy-MM-dd).\n");
            return null;
        }
    }

    private static TipBilet citireTipBilet() {
        console.printf("Tipuri bilet:\n");
        console.printf("1. STANDARD\n");
        console.printf("2. VIP\n");
        console.printf("3. STUDENT\n");
        console.printf("4. EARLY_BIRD\n");
        console.printf("Alege tip: ");
        int opt = citireInt();
        switch (opt) {
            case 1:
                return TipBilet.STANDARD;
            case 2:
                return TipBilet.VIP;
            case 3:
                return TipBilet.STUDENT;
            case 4:
                return TipBilet.EARLY_BIRD;
            default:
                console.printf("Tip invalid!\n");
                return null;
        }
    }

    private static boolean confirm(String mesaj) {
        while (true) {
            console.printf("%s (y/n): ", mesaj);
            String v = citireLinie().toLowerCase();
            if (v.equals("y")) {
                return true;
            }
            if (v.equals("n")) {
                return false;
            }
        }
    }

    private static int alegeBiletDinLista(List<Bilet> bilete, String prompt) {
        console.printf("\n--- Biletele tale ---\n");
        for (int i = 0; i < bilete.size(); i++) {
            Bilet b = bilete.get(i);
            String ev = b.getEveniment() != null ? b.getEveniment().getNume() : "?";
            console.printf("%d. BiletID=%d - %s - %s\n", i + 1, b.getId(), ev, b.getTipBilet());
        }
        console.printf(prompt);
        int opt = citireInt();
        if (opt < 1 || opt > bilete.size()) {
            console.printf("Optiune invalida!\n");
            return -1;
        }
        return opt - 1;
    }
}