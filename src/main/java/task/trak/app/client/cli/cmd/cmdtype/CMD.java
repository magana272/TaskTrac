package task.trak.app.client.cli.cmd.cmdtype;

import java.util.Optional;
import java.util.function.Consumer;

public interface CMD<T> extends Consumer<String[]> {
    default Optional<T> Execute() throws Exception {
        throw new Exception("Not Implemented");
    }
}
