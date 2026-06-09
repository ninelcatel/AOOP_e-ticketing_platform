package db;
import java.util.List;

// CRUD comun pentru DAO.
public interface GenericDAO<T> {
    int create(T obiect);
    T read(int id);
    List<T> readAll();
    void update(T obiect);
    void delete(int id);
}
