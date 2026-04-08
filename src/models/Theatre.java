package models;
public class Theatre extends Eveniment {
    private String piesa;
    private String regizor;

    public Theatre(int id, String nume, java.util.Date data, Locatie locatie, double pret, String piesa, String regizor) {
        super(id, nume, data, locatie, pret);
        this.piesa = piesa;
        this.regizor = regizor;
    }

    public String getPiesa() { return piesa; }
    public String getRegizor() { return regizor; }

    public void setPiesa(String piesa) { this.piesa = piesa; }
    public void setRegizor(String regizor) { this.regizor = regizor; }

    @Override
    public String getTipEveniment() {
        return "THEATRE";
    }

    @Override
    public String getDetaliiSpecifice() {
        return "Piesa: " + piesa + " - Regizor: " + regizor;
    }
}