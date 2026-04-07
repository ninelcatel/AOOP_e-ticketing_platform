package models;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public abstract class Eveniment {
    private int id;
    private String nume;
    private Date data;
    private Locatie locatie;
    private double pret;
    private List<Bilet> bilete;
    private List<Recenzie> recenzii;

    public Eveniment(int id, String nume, Date data, Locatie locatie, double pret) {
        this.id = id;
        this.nume = nume;
        this.data = data;
        this.locatie = locatie;
        this.pret = pret;
        this.bilete = new ArrayList<>();
        this.recenzii = new ArrayList<>();
    }

    public int getId() { return id; }
    public String getNume() { return nume; }
    public Date getData() { return data; }
    public Locatie getLocatie() { return locatie; }
    public double getPret() { return pret; }
    public List<Bilet> getBilete() { return bilete; }
    public List<Recenzie> getRecenzii() { return recenzii; }

    public void setId(int id) { this.id = id; }
    public void setNume(String nume) { this.nume = nume; }
    public void setData(Date data) { this.data = data; }
    public void setLocatie(Locatie locatie) { this.locatie = locatie; }
    public void setPret(double pret) { this.pret = pret; }

    public void addBilet(Bilet bilet) { this.bilete.add(bilet); }
    public void addRecenzie(Recenzie recenzie) { this.recenzii.add(recenzie); }

    public abstract String getTipEveniment();
}