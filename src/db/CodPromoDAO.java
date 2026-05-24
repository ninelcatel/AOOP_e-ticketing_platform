package db;
import models.CodPromo;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class CodPromoDAO extends AbstractDAO<CodPromo> {
    private static CodPromoDAO instance;

    private CodPromoDAO() {}

    public static CodPromoDAO getInstance() {
        if (instance == null) {
            instance = new CodPromoDAO();
        }
        return instance;
    }

    @Override
    protected String getNumeTabela() {
        return "cod_promo";
    }

    @Override
    protected CodPromo mapeazaRand(ResultSet rs) throws SQLException {
        Date dataStart = new Date(rs.getTimestamp("data_start").getTime());
        Date dataEnd = new Date(rs.getTimestamp("data_end").getTime());
        CodPromo codPromo = new CodPromo(
            rs.getInt("id"),
            rs.getString("cod"),
            rs.getDouble("valoare"),
            rs.getBoolean("procentual"),
            dataStart,
            dataEnd
        );
        codPromo.setActiv(rs.getBoolean("activ"));
        return codPromo;
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO cod_promo (cod, valoare, procentual, data_start, data_end, activ) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected void seteazaParametriInsert(PreparedStatement ps, CodPromo codPromo) throws SQLException {
        ps.setString(1, codPromo.getCod());
        ps.setDouble(2, codPromo.getValoare());
        ps.setBoolean(3, codPromo.isProcentual());
        ps.setTimestamp(4, new Timestamp(codPromo.getDataStart().getTime()));
        ps.setTimestamp(5, new Timestamp(codPromo.getDataEnd().getTime()));
        ps.setBoolean(6, codPromo.isActiv());
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE cod_promo SET cod = ?, valoare = ?, procentual = ?, data_start = ?, data_end = ?, activ = ? WHERE id = ?";
    }

    @Override
    protected void seteazaParametriUpdate(PreparedStatement ps, CodPromo codPromo) throws SQLException {
        ps.setString(1, codPromo.getCod());
        ps.setDouble(2, codPromo.getValoare());
        ps.setBoolean(3, codPromo.isProcentual());
        ps.setTimestamp(4, new Timestamp(codPromo.getDataStart().getTime()));
        ps.setTimestamp(5, new Timestamp(codPromo.getDataEnd().getTime()));
        ps.setBoolean(6, codPromo.isActiv());
        ps.setInt(7, codPromo.getId());
    }

    @Override
    protected int getId(CodPromo codPromo) {
        return codPromo.getId();
    }

    @Override
    protected void setId(CodPromo codPromo, int id) {
        codPromo.setId(id);
    }
}
