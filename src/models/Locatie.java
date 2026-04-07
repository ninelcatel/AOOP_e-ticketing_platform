import java.util.List;

public class Locatie{
    private int id;
    private String nume;
    private String adresa;
    private String country;
    private int capacitate;
    private List<Eveniment> evenimente;
    
    public Locatie(int id, String nume, String adresa, String country, int capacitate) {
        this.id = id;
        this.nume = nume;
        this.adresa = adresa;
        this.country = country;
        this.capacitate = capacitate < 0 ? 0 : capacitate; // Capacitatea nu poate fi negativă
        this.evenimente = new java.util.ArrayList<>();
    }

    public int getId() { return id; }
    public String getNume() { return nume; }
    public String getAdresa() { return adresa; }
    public String getCountry() { return country; }
    public int getCapacitate() { return capacitate; }
    public List<Eveniment> getEvenimente() { return evenimente; }

    public void setId(int id) { this.id = id; }
    public void setNume(String nume) { this.nume = nume; }
    public void setAdresa(String adresa) { this.adresa = adresa; }
    public void setCountry(String country) { this.country = country; }
    public void setCapacitate(int capacitate) { this.capacitate = capacitate < 0 ? 0 : capacitate; }
}