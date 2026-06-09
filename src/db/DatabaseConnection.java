package db;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private String url;
    private String user;
    private String pass;

    private DatabaseConnection() {
        // config din environment,  valori default ca fallback
        this.url = getEnvOrDefault("DB_URL", "jdbc:mysql://localhost:3306/paoj_db");
        this.user = getEnvOrDefault("DB_USER", "root");
        this.pass = getEnvOrDefault("DB_PASS", "ninel4");
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (this.connection == null || this.connection.isClosed()) {
                this.connection = DriverManager.getConnection(this.url, this.user, this.pass);
            }
        } catch (SQLException e) {
            System.out.println("nu m am putut conecta la baza de date: " + e.getMessage());
            throw new RuntimeException(e);
        }
        return this.connection;
    }

    public void closeConnection() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Nu am putut inchide conexiunea: " + e.getMessage());
        }
    }
    
    private String getEnvOrDefault(String key, String valoareDefault) {
        String valoare = System.getenv(key);
        return valoare != null && !valoare.trim().isEmpty() ? valoare : valoareDefault;
    }
}
