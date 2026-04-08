package models;
public class Concert extends Eveniment {
    private String artist;
    private String gen_muzical;

    public Concert(int id, String nume, java.util.Date data, Locatie locatie, double pret, String artist, String gen_muzical) {
        super(id, nume, data, locatie, pret);
        this.artist = artist;
        this.gen_muzical = gen_muzical;
    }

    public String getArtist() { return artist; }
    public String getGen_muzical() { return gen_muzical; }

    public void setArtist(String artist) { this.artist = artist; }
    public void setGen_muzical(String gen_muzical) { this.gen_muzical = gen_muzical; }

    @Override
    public String getTipEveniment() {
        return "CONCERT";
    }

    @Override
    public String getDetaliiSpecifice() {
        return "Artist: " + artist + " - Gen: " + gen_muzical;
    }
}