package services;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

// Singleton. scrie fiecare actiune in audit.csv (nume_actiune, timestamp).
public class AuditService {
    private static AuditService instance;
    private static final String FISIER_AUDIT = "audit.csv";
    private SimpleDateFormat formatData;

    private AuditService() {
        this.formatData = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public static AuditService getInstance() {
        if (instance == null) {
            instance = new AuditService();
        }
        return instance;
    }

    public void logActiune(String numeActiune) {
        if (numeActiune == null || numeActiune.trim().isEmpty()) {
            return;
        }
        try (FileWriter writer = new FileWriter(FISIER_AUDIT, true)) {
            String timestamp = formatData.format(new Date());
            writer.write(numeActiune + "," + timestamp + "\n");
        } catch (IOException e) {
            System.out.println("nu am putut scrie in fisierul de audit: " + e.getMessage());
        }
    }
}
