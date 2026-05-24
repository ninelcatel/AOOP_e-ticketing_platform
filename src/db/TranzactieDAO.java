package db;
import models.Tranzactie;
import models.Comanda;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class TranzactieDAO extends AbstractDAO<Tranzactie> {
    private static TranzactieDAO instance;

    private TranzactieDAO() {}

    public static TranzactieDAO getInstance() {
        if (instance == null) {
            instance = new TranzactieDAO();
        }
        return instance;
    }

    @Override
    protected String getNumeTabela() {
        return "tranzactie";
    }

    @Override
    protected Tranzactie mapeazaRand(ResultSet rs) throws SQLException {
        Comanda comanda = ComandaDAO.getInstance().read(rs.getInt("comanda_id"));
        Date dataTranzactie = new Date(rs.getTimestamp("data_tranzactie").getTime());
        Tranzactie tranzactie = new Tranzactie(rs.getInt("id"), comanda, rs.getDouble("suma"), dataTranzactie);
        tranzactie.setStatus(rs.getString("status"));
        return tranzactie;
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO tranzactie (comanda_id, suma, status, data_tranzactie) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected void seteazaParametriInsert(PreparedStatement ps, Tranzactie tranzactie) throws SQLException {
        if (tranzactie.getComanda() != null) {
            ps.setInt(1, tranzactie.getComanda().getId());
        } else {
            ps.setNull(1, java.sql.Types.INTEGER);
        }
        ps.setDouble(2, tranzactie.getSuma());
        ps.setString(3, tranzactie.getStatus());
        ps.setTimestamp(4, new Timestamp(tranzactie.getDataTranzactie().getTime()));
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE tranzactie SET comanda_id = ?, suma = ?, status = ?, data_tranzactie = ? WHERE id = ?";
    }

    @Override
    protected void seteazaParametriUpdate(PreparedStatement ps, Tranzactie tranzactie) throws SQLException {
        if (tranzactie.getComanda() != null) {
            ps.setInt(1, tranzactie.getComanda().getId());
        } else {
            ps.setNull(1, java.sql.Types.INTEGER);
        }
        ps.setDouble(2, tranzactie.getSuma());
        ps.setString(3, tranzactie.getStatus());
        ps.setTimestamp(4, new Timestamp(tranzactie.getDataTranzactie().getTime()));
        ps.setInt(5, tranzactie.getId());
    }

    @Override
    protected int getId(Tranzactie tranzactie) {
        return tranzactie.getId();
    }

    @Override
    protected void setId(Tranzactie tranzactie, int id) {
        tranzactie.setId(id);
    }
}
