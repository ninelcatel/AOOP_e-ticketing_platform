package db;
import models.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO extends AbstractDAO<User> {
    private static UserDAO instance;

    private UserDAO() {}

    public static UserDAO getInstance() {
        if (instance == null) {
            instance = new UserDAO();
        }
        return instance;
    }

    @Override
    protected String getNumeTabela() {
        return "utilizator";
    }

    @Override
    protected User mapeazaRand(ResultSet rs) throws SQLException {
        User user = new User(
            rs.getInt("id"),
            rs.getString("nume"),
            rs.getString("email"),
            rs.getString("parola")
        );
        user.setBalanta(rs.getDouble("balanta"));
        return user;
    }

    @Override
    protected String getInsertSql() {
        return "INSERT INTO utilizator (nume, email, parola, balanta) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected void seteazaParametriInsert(PreparedStatement ps, User user) throws SQLException {
        ps.setString(1, user.getNume());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getParola());
        ps.setDouble(4, user.getBalanta());
    }

    @Override
    protected String getUpdateSql() {
        return "UPDATE utilizator SET nume = ?, email = ?, parola = ?, balanta = ? WHERE id = ?";
    }

    @Override
    protected void seteazaParametriUpdate(PreparedStatement ps, User user) throws SQLException {
        ps.setString(1, user.getNume());
        ps.setString(2, user.getEmail());
        ps.setString(3, user.getParola());
        ps.setDouble(4, user.getBalanta());
        ps.setInt(5, user.getId());
    }

    @Override
    protected int getId(User user) {
        return user.getId();
    }

    @Override
    protected void setId(User user, int id) {
        user.setId(id);
    }

    // Interogare specifica - cautare dupa email
    public User readByEmail(String email) {
        if (email == null) {
            return null;
        }
        String sql = "SELECT * FROM utilizator WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapeazaRand(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("nu am putut cauta userul dupa email: " + e.getMessage());
        }
        return null;
    }
}
