package models;
import java.util.Date;

public class CodPromo {
    private int id;
    private String cod;
    private double valoare;
    private boolean procentual;
    private Date data_start;
    private Date data_end;
    private boolean activ;

    public CodPromo(int id, String cod, double valoare, boolean procentual, Date data_start, Date data_end) {
        this.id = id;
        this.cod = cod;
        this.valoare = valoare;
        this.procentual = procentual;
        if(data_start.after(data_end)) {
            throw new IllegalArgumentException("Data de start trebuie sa fie inainte de data de end");
        }
        this.data_start = data_start;
        this.data_end = data_end;
        this.activ = data_start.before(new Date()) && data_end.after(new Date());
    }

    public int getId() { return id; }
    public String getCod() { return cod; }
    public double getValoare() { return valoare; }
    public boolean isProcentual() { return procentual; }
    public Date getDataStart() { return data_start; }
    public Date getDataEnd() { return data_end; }
    public boolean isActiv() { return activ; }

    public void setId(int id) { this.id = id; }
    public void setCod(String cod) { this.cod = cod; }
    public void setValoare(double valoare) { this.valoare = valoare; }
    public void setProcentual(boolean procentual) { this.procentual = procentual; }
    public void setDataStart(Date data_start) { this.data_start = data_start; }
    public void setDataEnd(Date data_end) { this.data_end = data_end; }
    public void setActiv(boolean activ) { this.activ = activ; }
    
    public boolean checkActiv() { if (new Date().after(data_start) && new Date().before(data_end)) {
            this.activ = true;
        } else {
            this.activ = false;
        }
        return this.activ;
    }
}