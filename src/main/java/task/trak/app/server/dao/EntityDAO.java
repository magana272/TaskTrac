package task.trak.app.server.dao;

import java.util.List;

public interface EntityDAO<T> {
    void save(T entity);

    T loadByKey(String key);

    boolean deleteByKey(String key);

    List<T> loadAll();
}
