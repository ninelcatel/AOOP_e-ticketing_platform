package db;
import models.Recenzie;
import models.User;
import models.Eveniment;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RecenzieDAO extends AbstractDAO<Recenzie> {
    private static RecenzieDAO instance;

    private RecenzieDAO() {}

    public static RecenzieDAO getInstance() {
        if (instance == null) {
            instance = new RecenzieDAO();
        }
        return instance;
    }

    @Override
    protected String getNumeTabela() {
        return "recenzie";
    }

    @Override
    protected Recenzie mapeazaRand(ResultSet rs) throws SQLException {
        User user = UserDAO.getInstance().read(rs.getInt("user_id"));
        Eveniment eveniment = EvenimentDAO.getInstance().read(rs.getInt("eveniment_id"));
        Date dataRecenzie = new Date(rs.getTimestamp("data_recenzie").getTime());
        return new Recenzie(
            rs.getInt("id"),
            user,
            eveniment,
            rs.getInt("nota"),
            rs.getString("comentariu"),
            dataRecenzie
        );
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO recenzie (user_id, eveniment_id, nota, comentariu, data_recenzie) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected void seteazaParametriInsert(PreparedStatement ps, Recenzie recenzie) throws SQLException {
        ps.setInt(1, recenzie.getUser().getId());
        ps.setInt(2, recenzie.getEveniment().getId());
        ps.setInt(3, recenzie.getNota());
        ps.setString(4, recenzie.getComentariu());
        ps.setTimestamp(5, new Timestamp(recenzie.getDataRecenzie().getTime()));
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE recenzie SET user_id = ?, eveniment_id = ?, nota = ?, comentariu = ?, data_recenzie = ? WHERE id = ?";
    }

    @Override
    protected void seteazaParametriUpdate(PreparedStatement ps, Recenzie recenzie) throws SQLException {
        ps.setInt(1, recenzie.getUser().getId());
        ps.setInt(2, recenzie.getEveniment().getId());
        ps.setInt(3, recenzie.getNota());
        ps.setString(4, recenzie.getComentariu());
        ps.setTimestamp(5, new Timestamp(recenzie.getDataRecenzie().getTime()));
        ps.setInt(6, recenzie.getId());
    }

    @Override
    protected int getId(Recenzie recenzie) {
        return recenzie.getId();
    }

    @Override
    protected void setId(Recenzie recenzie, int id) {
        recenzie.setId(id);
    }

    // Interogare specifica - recenziile unui eveniment
    public List<Recenzie> readByEveniment(int evenimentId) {
        List<Recenzie> rezultate = new ArrayList<>();
        String sql = "SELECT * FROM recenzie WHERE eveniment_id = ? ORDER BY id";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, evenimentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rezultate.add(mapeazaRand(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Eroare la readByEveniment in recenzie: " + e.getMessage());
        }
        return rezultate;
    }
}
