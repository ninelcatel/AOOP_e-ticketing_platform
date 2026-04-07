import java.util.Date;

public class Recenzie {
    private int id;
    private User user;
    private Eveniment eveniment;
    private int nota;
    private String comentariu;
    private Date data_recenzie;

    public Recenzie(int id, User user, Eveniment eveniment, int nota, String comentariu, Date data_recenzie) {
        this.id = id;
        this.user = user;
        this.eveniment = eveniment;
        this.nota = nota > 5 ? 5 : (nota < 1 ? 1 : nota); 
        this.comentariu = comentariu;
        this.data_recenzie = data_recenzie;
        user.addRecenzie(this);
        eveniment.addRecenzie(this);
    }

    public int getId() { return id; }
    public User getUser() { return user; }
    public Eveniment getEveniment() { return eveniment; }
    public int getNota() { return nota; }
    public String getComentariu() { return comentariu; }
    public Date getDataRecenzie() { return data_recenzie; }

    public void setId(int id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setEveniment(Eveniment eveniment) { this.eveniment = eveniment; }
    public void setNota(int nota) { this.nota = nota > 5 ? 5 : (nota < 1 ? 1 : nota); }
    public void setComentariu(String comentariu) { this.comentariu = comentariu; }
    public void setDataRecenzie(Date data_recenzie) { this.data_recenzie = data_recenzie; }
}