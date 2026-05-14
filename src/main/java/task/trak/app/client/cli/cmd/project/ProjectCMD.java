package task.trak.app.client.cli.cmd.project;

import task.trak.model.dto.ProjectDTO;
import task.trak.app.client.cli.cmd.cmdtype.CMD;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;


public class ProjectCMD implements CMD<ProjectDTO> {
    public HashMap<String, Consumer<String[]>> fns = new HashMap<>();

    protected ProjectCMD() {
        this.fns.put("add", (String[] args) -> {
            try {
                new ProjectAddCMD(args).Execute();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        this.fns.put("get", (String[] args) -> {
            try {
                new ProjectGetCMD(args).Execute();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        this.fns.put("delete", (String[] args) -> {
            try {
                new ProjectDeleteCMD(args).Execute();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        this.fns.put("update", (String[] args) -> {
            try {
                new ProjectUpdateCMD(args).Execute();
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
    }

    public ProjectCMD(String[] options) {
        this();
        if (options != null && options.length > 0) {
            String subCommand = options[0];
            String[] subArgs = Arrays.copyOfRange(options, 1, options.length);
            Consumer<String[]> handler = this.fns.get(subCommand);
            if (handler != null) {
                handler.accept(subArgs);
            } else {
                System.err.println("Unknown project sub-command: " + subCommand);
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
            System.err.println("Unknown project sub-command: " + subCommand);
        }
    }
}
