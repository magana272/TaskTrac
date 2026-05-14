package task.trak.model.exception;

public class TrakException extends RuntimeException {
    public TrakException(String message) {
        super(message);
    }

    public TrakException(String message, Throwable cause) {
        super(message, cause);
    }
}
