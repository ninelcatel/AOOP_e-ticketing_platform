package db;
import models.Eveniment;
import models.Concert;
import models.SportsMatch;
import models.Theatre;
import models.Museum;
import models.Locatie;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class EvenimentDAO extends AbstractDAO<Eveniment> {
    private static EvenimentDAO instance;

    private EvenimentDAO() {}

    public static EvenimentDAO getInstance() {
        if (instance == null) {
            instance = new EvenimentDAO();
        }
        return instance;
    }

    @Override
    protected String getNumeTabela() {
        return "eveniment";
    }

    @Override
    protected Eveniment mapeazaRand(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String nume = rs.getString("nume");
        Date data = new Date(rs.getTimestamp("data").getTime());
        double pret = rs.getDouble("pret");
        String tip = rs.getString("tip_eveniment");

        Locatie locatie = LocatieDAO.getInstance().read(rs.getInt("locatie_id"));

        Eveniment eveniment;
        switch (tip) {
            case "CONCERT":
                eveniment = new Concert(id, nume, data, locatie, pret, rs.getString("camp1"), rs.getString("camp2"));
                break;
            case "SPORTS_MATCH":
                eveniment = new SportsMatch(id, nume, data, locatie, pret, rs.getString("camp1"), rs.getString("camp2"), rs.getString("camp3"));
                break;
            case "THEATRE":
                eveniment = new Theatre(id, nume, data, locatie, pret, rs.getString("camp1"), rs.getString("camp2"));
                break;
            case "MUSEUM":
                eveniment = new Museum(id, nume, data, locatie, pret, rs.getString("camp1"), rs.getString("camp2"));
                break;
            default:
                return null;
        }
        if (locatie != null) {
            locatie.addEveniment(eveniment);
        }
        return eveniment;
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO eveniment (nume, data, pret, tip_eveniment, locatie_id, camp1, camp2, camp3) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void seteazaParametriInsert(PreparedStatement ps, Eveniment eveniment) throws SQLException {
        seteazaCampuriComune(ps, eveniment);
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE eveniment SET nume = ?, data = ?, pret = ?, tip_eveniment = ?, locatie_id = ?, camp1 = ?, camp2 = ?, camp3 = ? WHERE id = ?";
    }

    @Override
    protected void seteazaParametriUpdate(PreparedStatement ps, Eveniment eveniment) throws SQLException {
        seteazaCampuriComune(ps, eveniment);
        ps.setInt(9, eveniment.getId());
    }

    // Mapam campurile specifice fiecarui subtip in camp1/camp2/camp3
    private void seteazaCampuriComune(PreparedStatement ps, Eveniment eveniment) throws SQLException {
        ps.setString(1, eveniment.getNume());
        ps.setTimestamp(2, new Timestamp(eveniment.getData().getTime()));
        ps.setDouble(3, eveniment.getPret());
        ps.setString(4, eveniment.getTipEveniment());
        if (eveniment.getLocatie() != null) {
            ps.setInt(5, eveniment.getLocatie().getId());
        } else {
            ps.setNull(5, java.sql.Types.INTEGER);
        }

        String camp1 = null;
        String camp2 = null;
        String camp3 = null;

        if (eveniment instanceof Concert) {
            Concert c = (Concert) eveniment;
            camp1 = c.getArtist();
            camp2 = c.getGen_muzical();
        } else if (eveniment instanceof SportsMatch) {
            SportsMatch s = (SportsMatch) eveniment;
            camp1 = s.getEchipa_gazda();
            camp2 = s.getEchipa_oaspete();
            camp3 = s.getCompetitie();
        } else if (eveniment instanceof Theatre) {
            Theatre t = (Theatre) eveniment;
            camp1 = t.getPiesa();
            camp2 = t.getRegizor();
        } else if (eveniment instanceof Museum) {
            Museum m = (Museum) eveniment;
            camp1 = m.getNume_muzeu();
            camp2 = m.getTip_expozitie();
        }

        ps.setString(6, camp1);
        ps.setString(7, camp2);
        ps.setString(8, camp3);
    }

    @Override
    protected int getId(Eveniment eveniment) {
        return eveniment.getId();
    }

    @Override
    protected void setId(Eveniment eveniment, int id) {
        eveniment.setId(id);
    }
}
