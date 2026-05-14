package task.trak.app.client.cli.cmd.sprint;

import task.trak.model.dto.SprintDTO;
import task.trak.app.client.cli.cmd.cmdtype.CMD;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;

public class SprintCMD implements CMD<SprintDTO> {
    public HashMap<String, Consumer<String[]>> fns = new HashMap<>();

    protected SprintCMD() {
        this.fns.put("add", (String[] args) -> {
            try {
                new SprintAddCMD(args).Execute();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        this.fns.put("get", (String[] args) -> {
            try {
                new SprintGetCMD(args).Execute();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        this.fns.put("update", (String[] args) -> {
            try {
                new SprintUpdateCMD(args).Execute();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        this.fns.put("delete", (String[] args) -> {
            try {
                new SprintDeleteCMD(args).Execute();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
    }

    public SprintCMD(String[] options) {
        this();
        if (options != null && options.length > 0) {
            String subCommand = options[0];
            String[] subArgs = Arrays.copyOfRange(options, 1, options.length);
            Consumer<String[]> handler = this.fns.get(subCommand);
            if (handler != null) {
                handler.accept(subArgs);
            } else {
                System.err.println("Unknown sprint sub-command: " + subCommand);
            }
        }
    }

    @Override
    public void accept(String[] strings) {
        String subCommand = strings[0];
        String[] subArgs = Arrays.copyOfRange(strings, 1, strings.length);
        Consumer<String[]> handler = this.fns.get(subCommand);
        if (handler != null) {
            handler.accept(subArgs);
        } else {
            System.err.println("Unknown sprint sub-command: " + subCommand);
        }
    }
}
