package task.trak.app.client.gui.viewmodel.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommandEventBus {
    private static final List<CommandListener> listeners = new CopyOnWriteArrayList<>();

    public static void addListener(CommandListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(CommandListener listener) {
        listeners.remove(listener);
    }

    public static void fire(CommandEvent event) {
        for (CommandListener listener : listeners) {
            listener.onCommandExecuted(event);
        }
    }

    public static boolean hasListeners() {
        return !listeners.isEmpty();
    }
}
