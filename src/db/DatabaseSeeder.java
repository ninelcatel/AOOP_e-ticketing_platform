package db;
import models.*;
import java.util.Date;

// Populeaza baza de date cu date initiale, dar DOAR daca este goala
// (nu exista niciun utilizator). Astfel nu se dubleaza datele la fiecare pornire.
public class DatabaseSeeder {

    public static void seed() {
        UserDAO userDAO = UserDAO.getInstance();
        LocatieDAO locatieDAO = LocatieDAO.getInstance();
        EvenimentDAO evenimentDAO = EvenimentDAO.getInstance();

        // Daca exista deja utilizatori, presupunem ca DB-ul a fost populat.
        if (!userDAO.readAll().isEmpty()) {
            return;
        }

        System.out.println("Baza de date este goala. Adaugam date initiale (seed)...");

        // --- Utilizator de test ---
        User user = new User(0, "Test User", "test@test.ro", "test");
        user.setBalanta(1000.0);
        userDAO.create(user);

        // Date pentru evenimente (peste 60 de zile, ca sa permita si Early Bird)
        Date dataViitoare = new Date(System.currentTimeMillis() + 60L * 24 * 60 * 60 * 1000);

        // --- Concert ---
        Locatie locConcert = new Locatie(0, "Arena Nationala", "Bd. Basarabia 37", "Romania", 100);
        locatieDAO.create(locConcert);
        Concert concert = new Concert(0, "Concert Rock", dataViitoare, locConcert, 200.0, "Trupa X", "Rock");
        evenimentDAO.create(concert);

        // --- Meci de sport ---
        Locatie locSport = new Locatie(0, "Stadionul Steaua", "Bd. Ghencea 35", "Romania", 50);
        locatieDAO.create(locSport);
        SportsMatch meci = new SportsMatch(0, "Derby de Bucuresti", dataViitoare, locSport, 150.0, "FCSB", "Dinamo", "Liga 1");
        evenimentDAO.create(meci);

        // --- Teatru ---
        Locatie locTeatru = new Locatie(0, "Teatrul National", "Bd. Nicolae Balcescu 2", "Romania", 30);
        locatieDAO.create(locTeatru);
        Theatre teatru = new Theatre(0, "O scrisoare pierduta", dataViitoare, locTeatru, 80.0, "O scrisoare pierduta", "Ion Caramitru");
        evenimentDAO.create(teatru);

        // --- Muzeu ---
        Locatie locMuzeu = new Locatie(0, "Muzeul de Arta", "Calea Victoriei 49", "Romania", 200);
        locatieDAO.create(locMuzeu);
        Museum muzeu = new Museum(0, "Expozitie Brancusi", dataViitoare, locMuzeu, 40.0, "Muzeul National de Arta", "Sculptura");
        evenimentDAO.create(muzeu);

        System.out.println("Seed finalizat: 1 utilizator (test@test.ro / test) si 4 evenimente.");
    }
}