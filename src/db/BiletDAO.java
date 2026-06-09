package db;
import models.Bilet;
import models.Eveniment;
import models.User;
import models.Comanda;
import models.TipBilet;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BiletDAO extends AbstractDAO<Bilet> {
    private static BiletDAO instance;

    private BiletDAO() {}

    public static BiletDAO getInstance() {
        if (instance == null) {
            instance = new BiletDAO();
        }
        return instance;
    }

    @Override
    protected String getNumeTabela() {
        return "bilet";
    }

    @Override
    protected Bilet mapeazaRand(ResultSet rs) throws SQLException {
        Eveniment eveniment = EvenimentDAO.getInstance().read(rs.getInt("eveniment_id"));
        User user = UserDAO.getInstance().read(rs.getInt("user_id"));
        TipBilet tip = TipBilet.valueOf(rs.getString("tip_bilet"));

        Bilet bilet = new Bilet(rs.getInt("id"), eveniment, user, tip);
        bilet.setValid(rs.getBoolean("valid"));

        int comandaId = rs.getInt("comanda_id");
        if (!rs.wasNull()) {
            Comanda comanda = ComandaDAO.getInstance().read(comandaId);
            bilet.setComanda(comanda);
        }
        return bilet;
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO bilet (eveniment_id, user_id, comanda_id, tip_bilet, valid) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected void seteazaParametriInsert(PreparedStatement ps, Bilet bilet) throws SQLException {
        seteazaCampuri(ps, bilet);
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE bilet SET eveniment_id = ?, user_id = ?, comanda_id = ?, tip_bilet = ?, valid = ? WHERE id = ?";
    }

    @Override
    protected void seteazaParametriUpdate(PreparedStatement ps, Bilet bilet) throws SQLException {
        seteazaCampuri(ps, bilet);
        ps.setInt(6, bilet.getId());
    }

    private void seteazaCampuri(PreparedStatement ps, Bilet bilet) throws SQLException {
        ps.setInt(1, bilet.getEveniment().getId());
        ps.setInt(2, bilet.getUser().getId());
        if (bilet.getComanda() != null) {
            ps.setInt(3, bilet.getComanda().getId());
        } else {
            ps.setNull(3, java.sql.Types.INTEGER);
        }
        ps.setString(4, bilet.getTipBilet().name());
        ps.setBoolean(5, bilet.isValid());
    }

    @Override
    protected int getId(Bilet bilet) {
        return bilet.getId();
    }

    @Override
    protected void setId(Bilet bilet, int id) {
        bilet.setId(id);
    }

    // Interogare specifica - biletele valide ale unui utilizator
    public List<Bilet> readByUser(int userId) {
        List<Bilet> rezultate = new ArrayList<>();
        String sql = "SELECT * FROM bilet WHERE user_id = ? AND valid = TRUE ORDER BY id";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rezultate.add(mapeazaRand(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Eroare la readByUser in bilet: " + e.getMessage());
        }
        return rezultate;
    }

    // Interogare specifica - biletele unui eveniment
    public List<Bilet> readByEveniment(int evenimentId) {
        List<Bilet> rezultate = new ArrayList<>();
        String sql = "SELECT * FROM bilet WHERE eveniment_id = ? ORDER BY id";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, evenimentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rezultate.add(mapeazaRand(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Eroare la readByEveniment in bilet: " + e.getMessage());
        }
        return rezultate;
    }
}
