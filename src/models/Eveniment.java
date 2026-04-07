abstract class Eveniment{
    private String nume;
    private Date data;
    private Locatie locatie;
    
    public Eveniment(String nume, Date data, Locatie locatie){
        this.nume = nume;
        this.data = data;
        this.locatie = locatie;
    }
    
    public String getNume(){
        return nume;
    }
    
    public Date getData(){
        return data;
    }
    
    public Locatie getLocatie(){
        return locatie;
    }
    abstract String getTipEveniment();
}