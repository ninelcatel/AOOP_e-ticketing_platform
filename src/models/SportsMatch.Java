package models;
public class SportsMatch extends Eveniment {
    private String echipa_gazda;
    private String echipa_oaspete;
    private String competitie;

    public SportsMatch(int id, String nume, java.util.Date data, Locatie locatie, double pret, String echipa_gazda, String echipa_oaspete, String competitie) {
        super(id, nume, data, locatie, pret);
        this.echipa_gazda = echipa_gazda;
        this.echipa_oaspete = echipa_oaspete;
        this.competitie = competitie;
    }

    public String getEchipa_gazda() { return echipa_gazda; }
    public String getEchipa_oaspete() { return echipa_oaspete; }
    public String getCompetitie() { return competitie; }

    public void setEchipa_gazda(String echipa_gazda) { this.echipa_gazda = echipa_gazda; }
    public void setEchipa_oaspete(String echipa_oaspete) { this.echipa_oaspete = echipa_oaspete; }
    public void setCompetitie(String competitie) { this.competitie = competitie; }

    @Override
    public String getTipEveniment() {
        return "SPORTS_MATCH";
    }

    @Override
    public String getDetaliiSpecifice() {
        return echipa_gazda + " vs " + echipa_oaspete + " - " + competitie;
    }
}