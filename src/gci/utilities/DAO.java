package gci.utilities;

import java.sql.SQLException;
import java.util.List;

public interface DAO<T> {

    T retrieve(int index);

    List<T> retrieveAll() throws SQLException;

    void create(T t);

    void update(T t);

    void delete(T t) throws SQLException;
}
