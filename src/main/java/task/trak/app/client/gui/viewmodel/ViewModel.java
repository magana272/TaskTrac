package task.trak.app.client.gui.viewmodel;

import java.util.List;

public interface ViewModel<T> {
    List<T> get();
    void create(T item);
    void update(T item);
    void delete(T item);
    void save();
    void load();
}
