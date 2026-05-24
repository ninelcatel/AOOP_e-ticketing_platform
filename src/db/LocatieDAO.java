package db;
import models.Locatie;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LocatieDAO extends AbstractDAO<Locatie> {
    private static LocatieDAO instance;

    private LocatieDAO() {}

    public static LocatieDAO getInstance() {
        if (instance == null) {
            instance = new LocatieDAO();
        }
        return instance;
    }

    @Override
    protected String getNumeTabela() {
        return "locatie";
    }

    @Override
    protected Locatie mapeazaRand(ResultSet rs) throws SQLException {
        return new Locatie(
            rs.getInt("id"),
            rs.getString("nume"),
            rs.getString("adresa"),
            rs.getString("country"),
            rs.getInt("capacitate")
        );
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO locatie (nume, adresa, country, capacitate) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected void seteazaParametriInsert(PreparedStatement ps, Locatie locatie) throws SQLException {
        ps.setString(1, locatie.getNume());
        ps.setString(2, locatie.getAdresa());
        ps.setString(3, locatie.getCountry());
        ps.setInt(4, locatie.getCapacitate());
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE locatie SET nume = ?, adresa = ?, country = ?, capacitate = ? WHERE id = ?";
    }

    @Override
    protected void seteazaParametriUpdate(PreparedStatement ps, Locatie locatie) throws SQLException {
        ps.setString(1, locatie.getNume());
        ps.setString(2, locatie.getAdresa());
        ps.setString(3, locatie.getCountry());
        ps.setInt(4, locatie.getCapacitate());
        ps.setInt(5, locatie.getId());
    }

    @Override
    protected int getId(Locatie locatie) {
        return locatie.getId();
    }

    @Override
    protected void setId(Locatie locatie, int id) {
        locatie.setId(id);
    }
}
