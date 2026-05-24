package db;
import java.util.List;

// Contractul generic CRUD pe care il expun toate DAO-urile.
public interface GenericDAO<T> {
    int create(T obiect);
    T read(int id);
    List<T> readAll();
    void update(T obiect);
    void delete(int id);
}
