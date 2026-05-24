package models;

public class Bilet {
    private int id;
    private Eveniment eveniment;
    private User user;
    private Comanda comanda;
    private TipBilet tip_bilet;
    private boolean valid;

    public Bilet(int id, Eveniment eveniment, User user, TipBilet tip_bilet) {
        this.id = id;
        this.eveniment = eveniment;
        this.user = user;
        this.tip_bilet = tip_bilet;
        this.valid = true;
        eveniment.addBilet(this);
    }

    public int getId() { return id; }
    public Eveniment getEveniment() { return eveniment; }
    public User getUser() { return user; }
    public Comanda getComanda() { return comanda; }
    public TipBilet getTipBilet() { return tip_bilet; }
    public boolean isValid() { return valid; }

    public void setId(int id) { this.id = id; }
    public void setEveniment(Eveniment eveniment) { this.eveniment = eveniment; }
    public void setUser(User user) { this.user = user; }
    public void setComanda(Comanda comanda) { this.comanda = comanda; }
    public void setTipBilet(TipBilet tip_bilet) { this.tip_bilet = tip_bilet; }
    public void setValid(boolean valid) { this.valid = valid; }
}