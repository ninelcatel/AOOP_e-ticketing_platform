package db;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

// Creeaza automat tabelele la pornire (daca nu exista deja).
// Astfel aplicatia se conecteaza si retine datele fara SQL manual.
public class DatabaseInitializer {

    public static void initializeaza() {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement st = conn.createStatement()) {

            st.execute(
                "CREATE TABLE IF NOT EXISTS utilizator (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "nume VARCHAR(255), " +
                "email VARCHAR(255) UNIQUE, " +
                "parola VARCHAR(255), " +
                "balanta DOUBLE DEFAULT 0" +
                ")"
            );

            st.execute(
                "CREATE TABLE IF NOT EXISTS locatie (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "nume VARCHAR(255), " +
                "adresa VARCHAR(255), " +
                "country VARCHAR(255), " +
                "capacitate INT" +
                ")"
            );

            st.execute(
                "CREATE TABLE IF NOT EXISTS eveniment (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "nume VARCHAR(255), " +
                "data DATETIME, " +
                "pret DOUBLE, " +
                "tip_eveniment VARCHAR(50), " +
                "locatie_id INT, " +
                "camp1 VARCHAR(255), " +
                "camp2 VARCHAR(255), " +
                "camp3 VARCHAR(255), " +
                "FOREIGN KEY (locatie_id) REFERENCES locatie(id)" +
                ")"
            );

            st.execute(
                "CREATE TABLE IF NOT EXISTS comanda (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT, " +
                "data_comanda DATETIME, " +
                "status VARCHAR(50), " +
                "FOREIGN KEY (user_id) REFERENCES utilizator(id)" +
                ")"
            );

            st.execute(
                "CREATE TABLE IF NOT EXISTS tranzactie (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "comanda_id INT, " +
                "suma DOUBLE, " +
                "status VARCHAR(50), " +
                "data_tranzactie DATETIME, " +
                "FOREIGN KEY (comanda_id) REFERENCES comanda(id)" +
                ")"
            );

            st.execute(
                "CREATE TABLE IF NOT EXISTS bilet (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "eveniment_id INT, " +
                "user_id INT, " +
                "comanda_id INT, " +
                "tip_bilet VARCHAR(50), " +
                "valid BOOLEAN, " +
                "FOREIGN KEY (eveniment_id) REFERENCES eveniment(id), " +
                "FOREIGN KEY (user_id) REFERENCES utilizator(id), " +
                "FOREIGN KEY (comanda_id) REFERENCES comanda(id)" +
                ")"
            );

            st.execute(
                "CREATE TABLE IF NOT EXISTS recenzie (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT, " +
                "eveniment_id INT, " +
                "nota INT, " +
                "comentariu VARCHAR(1000), " +
                "data_recenzie DATETIME, " +
                "FOREIGN KEY (user_id) REFERENCES utilizator(id), " +
                "FOREIGN KEY (eveniment_id) REFERENCES eveniment(id)" +
                ")"
            );

            st.execute(
                "CREATE TABLE IF NOT EXISTS cod_promo (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "cod VARCHAR(255), " +
                "valoare DOUBLE, " +
                "procentual BOOLEAN, " +
                "data_start DATETIME, " +
                "data_end DATETIME, " +
                "activ BOOLEAN" +
                ")"
            );

            System.out.println("Baza de date a fost initializata cu succes.");
        } catch (SQLException e) {
            System.out.println("Eroare la initializarea bazei de date: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
