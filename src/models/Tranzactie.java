import java.util.Date;

public class Tranzactie {
    private int id;
    private Comanda comanda;
    private double suma;
    private String metoda_plata;
    private String status;
    private Date data_tranzactie;

    public Tranzactie(int id, Comanda comanda, double suma, String metoda_plata, Date data_tranzactie) {
        this.id = id;
        this.comanda = comanda;
        this.suma = suma;
        this.metoda_plata = metoda_plata;
        this.data_tranzactie = data_tranzactie;
    }

    public int getId() { return id; }
    public Comanda getComanda() { return comanda; }
    public double getSuma() { return suma; }
    public String getMetodaPlata() { return metoda_plata; }
    public String getStatus() { return status; }
    public Date getDataTranzactie() { return data_tranzactie; }

    public void setId(int id) { this.id = id; }
    public void setComanda(Comanda comanda) { this.comanda = comanda; }
    public void setSuma(double suma) { this.suma = suma; }
    public void setMetodaPlata(String metoda_plata) { this.metoda_plata = metoda_plata; }
    public void setStatus(String status) { this.status = status; }
    public void setDataTranzactie(Date data_tranzactie) { this.data_tranzactie = data_tranzactie; }
}