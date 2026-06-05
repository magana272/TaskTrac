package task.trak.app.client.gui.viewmodel;

import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Abstract base for all ViewModels. Implements ViewModel and Serializable.
 * Handles listener management and serialization to .cache/ directory.
 */
public abstract class ObservableViewModel<T> implements ViewModel<T>, Serializable {

    public static String CACHE_DIR = ".cache";

    private transient List<ViewModelChangeListener> listeners = new CopyOnWriteArrayList<>();
    private final String cacheFile;

    protected ObservableViewModel(String cacheFile) {
        this.cacheFile = cacheFile;
    }

    public void addObserver(ViewModelChangeListener observer) {
        if (listeners == null) listeners = new CopyOnWriteArrayList<>();
        listeners.add(observer);
    }

    public void removeObserver(ViewModelChangeListener observer) {
        if (listeners != null) listeners.remove(observer);
    }

    public void notifyObservers(ViewModelChangeType type) {
        if (listeners == null) return;
        for (ViewModelChangeListener listener : listeners) {
            try {
                listener.onViewModelChanged(type);
            } catch (Exception e) {
                System.err.println("Observer notification failed: " + e.getMessage());
            }
        }
    }

    @Override
    public void save() {
        File dir = new File(CACHE_DIR);
        if (!dir.exists()) dir.mkdirs();
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(dir, cacheFile)))) {
            out.writeObject(this);
        } catch (IOException e) {
            System.err.println("Failed to save " + cacheFile + ": " + e.getMessage());
        }
    }

    @Override
    public void load() {
        File file = new File(CACHE_DIR, cacheFile);
        if (!file.exists()) return;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            ObservableViewModel<?> loaded = (ObservableViewModel<?>) in.readObject();
            loadFrom(loaded);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load " + cacheFile + ": " + e.getMessage());
        }
    }

    public void clearCache() {
        File file = new File(CACHE_DIR, cacheFile);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Subclasses implement this to copy state from a deserialized instance.
     */
    protected abstract void loadFrom(ObservableViewModel<?> loaded);
}
