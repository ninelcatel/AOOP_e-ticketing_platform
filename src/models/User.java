import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String nume;
    private String email;
    private String parola;
    private List<Comanda> comenzi;
    private List<Recenzie> recenzii;
    private List<Tranzactie> tranzactii;

    public User(int id, String nume, String email, String parola) {
        this.id = id;
        this.nume = nume;
        this.email = email;
        this.parola = parola;
        this.comenzi = new ArrayList<>();
        this.recenzii = new ArrayList<>();
        this.tranzactii = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getNume() { return nume; }
    public String getEmail() { return email; }
    public String getParola() { return parola; }
    public List<Comanda> getComenzi() { return comenzi; }
    public List<Recenzie> getRecenzii() { return recenzii; }
    public List<Tranzactie> getTranzactii() { return tranzactii; }

    public void setId(int id) { this.id = id; }
    public void setNume(String nume) { this.nume = nume; }
    public void setEmail(String email) { this.email = email; }
    public void setParola(String parola) { this.parola = parola; }

    public void addTranzactie(Tranzactie tranzactie) { this.tranzactii.add(tranzactie); }
    public void addComanda(Comanda comanda) { this.comenzi.add(comanda); }
    public void addRecenzie(Recenzie recenzie) { this.recenzii.add(recenzie); }
}