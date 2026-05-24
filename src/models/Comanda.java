package models;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

public class Comanda {
    private int id;
    private User user;
    private List<Bilet> bilete;
    private CodPromo cod_promo;
    private Tranzactie tranzactie;
    private Date data_comanda;
    private String status;

    public Comanda(int id, User user, Date data_comanda) {
        this.id = id;
        this.user = user;
        this.data_comanda = data_comanda;
        user.addComanda(this);
        this.bilete = new ArrayList<>();
    }

    public int getId() { return id; }
    public User getUser() { return user; }
    public List<Bilet> getBilete() { return bilete; }
    public CodPromo getCodPromo() { return cod_promo; }
    public Tranzactie getTranzactie() { return tranzactie; }
    public Date getDataComanda() { return data_comanda; }
    public String getStatus() { return status; }

    public void setId(int id) {this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setBilete(List<Bilet> bilete) { this.bilete = bilete; }
    public void setCodPromo(CodPromo cod_promo) { this.cod_promo = cod_promo; }
    public void setTranzactie(Tranzactie tranzactie) { this.tranzactie = tranzactie; }
    public void setDataComanda(Date data_comanda) { this.data_comanda = data_comanda; }
    public void setStatus(String status) { this.status = status; }

    public void addBilet(Bilet bilet) { this.bilete.add(bilet); }
}