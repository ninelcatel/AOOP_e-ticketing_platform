package db;
import models.Comanda;
import models.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ComandaDAO extends AbstractDAO<Comanda> {
    private static ComandaDAO instance;

    private ComandaDAO() {}

    public static ComandaDAO getInstance() {
        if (instance == null) {
            instance = new ComandaDAO();
        }
        return instance;
    }

    @Override
    protected String getNumeTabela() {
        return "comanda";
    }

    @Override
    protected Comanda mapeazaRand(ResultSet rs) throws SQLException {
        User user = UserDAO.getInstance().read(rs.getInt("user_id"));
        Date dataComanda = new Date(rs.getTimestamp("data_comanda").getTime());
        Comanda comanda = new Comanda(rs.getInt("id"), user, dataComanda);
        comanda.setStatus(rs.getString("status"));
        return comanda;
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO comanda (user_id, data_comanda, status) VALUES (?, ?, ?)";
    }

    @Override
    protected void seteazaParametriInsert(PreparedStatement ps, Comanda comanda) throws SQLException {
        ps.setInt(1, comanda.getUser().getId());
        ps.setTimestamp(2, new Timestamp(comanda.getDataComanda().getTime()));
        ps.setString(3, comanda.getStatus());
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE comanda SET user_id = ?, data_comanda = ?, status = ? WHERE id = ?";
    }

    @Override
    protected void seteazaParametriUpdate(PreparedStatement ps, Comanda comanda) throws SQLException {
        ps.setInt(1, comanda.getUser().getId());
        ps.setTimestamp(2, new Timestamp(comanda.getDataComanda().getTime()));
        ps.setString(3, comanda.getStatus());
        ps.setInt(4, comanda.getId());
    }

    @Override
    protected int getId(Comanda comanda) {
        return comanda.getId();
    }

    @Override
    protected void setId(Comanda comanda, int id) {
        comanda.setId(id);
    }

    // Interogare specifica - comenzile unui utilizator
    public List<Comanda> readByUser(int userId) {
        List<Comanda> rezultate = new ArrayList<>();
        String sql = "SELECT * FROM comanda WHERE user_id = ? ORDER BY id";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rezultate.add(mapeazaRand(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Eroare la readByUser in comanda: " + e.getMessage());
        }
        return rezultate;
    }
}
