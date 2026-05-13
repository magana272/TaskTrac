package task.trak.app.client.gui.viewmodel.event;

public record CommandEvent(CommandEventType type, String commandName, Object data, String textOutput, boolean success,
                           String errorMessage) {
    public CommandEvent(CommandEventType type, String commandName, String textOutput, boolean success, String errorMessage) {
        this(type, commandName, null, textOutput, success, errorMessage);
    }

}
