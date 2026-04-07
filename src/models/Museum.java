package models;
public class Museum extends Eveniment{
    private String nume_muzeu;
    private String tip_expozitie;
    public Museum(int id, String nume, java.util.Date data, Locatie locatie, double pret, String nume_muzeu, String tip_expozitie) {
        super(id, nume, data, locatie, pret);
        this.nume_muzeu = nume_muzeu;
        this.tip_expozitie = tip_expozitie;
    }
    public String getNume_muzeu() { return nume_muzeu; }
    public String getTip_expozitie() { return tip_expozitie; }
    public void setNume_muzeu(String nume_muzeu) { this.nume_muzeu = nume_muzeu; }
    public void setTip_expozitie(String tip_expozitie) { this.tip_expozitie = tip_expozitie; }
    @Override
    public String getTipEveniment() {
        return "MUSEUM";
    }
}