package db;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDAO<T> implements GenericDAO<T> {

    protected Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    protected abstract String getNumeTabela();
    protected abstract T mapeazaRand(ResultSet rs) throws SQLException;
    protected abstract String getInsertSql();
    protected abstract void seteazaParametriInsert(PreparedStatement ps, T obiect) throws SQLException;
    protected abstract String getUpdateSql();
    protected abstract void seteazaParametriUpdate(PreparedStatement ps, T obiect) throws SQLException;
    protected abstract int getId(T obiect);
    protected abstract void setId(T obiect, int id);

    @Override
    public int create(T obiect) {
        if (obiect == null) {
            return -1;
        }
        String sql = getInsertSql();
        try (PreparedStatement ps = getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            seteazaParametriInsert(ps, obiect);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerat = rs.getInt(1);
                    setId(obiect, idGenerat);
                    return idGenerat;
                }
            }
        } catch (SQLException e) {
            System.out.println("Nu am putut salva in " + getNumeTabela() + ": " + e.getMessage());
        }
        return -1;
    }

    @Override
    public T read(int id) {
        String sql = "SELECT * FROM " + getNumeTabela() + " WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapeazaRand(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Nu am putut citi din " + getNumeTabela() + ": " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<T> readAll() {
        List<T> rezultate = new ArrayList<>();
        String sql = "SELECT * FROM " + getNumeTabela() + " ORDER BY id";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rezultate.add(mapeazaRand(rs));
            }
        } catch (SQLException e) {
            System.out.println("Nu am putut citi tot din " + getNumeTabela() + ": " + e.getMessage());
        }
        return rezultate;
    }

    @Override
    public void update(T obiect) {
        if (obiect == null) {
            return;
        }
        String sql = getUpdateSql();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            seteazaParametriUpdate(ps, obiect);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Nu am putut actualiza in " + getNumeTabela() + ": " + e.getMessage());
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM " + getNumeTabela() + " WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Nu am putut sterge din " + getNumeTabela() + ": " + e.getMessage());
        }
    }
}
