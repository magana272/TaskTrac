package task.trak.app.client.cli.cmd;

import task.trak.api.util.TeeOutputStream;
import task.trak.app.client.cli.cmd.backlog.BacklogCMD;
import task.trak.app.client.cli.cmd.cmdtype.CMD;
import task.trak.app.client.cli.cmd.project.ProjectCMD;
import task.trak.app.client.cli.cmd.sprint.SprintCMD;
import task.trak.app.client.cli.cmd.task.TaskCMD;
import task.trak.app.client.cli.cmd.user.UserCMD;
import task.trak.app.client.cli.cmd.workspace.*;
import task.trak.app.client.gui.viewmodel.event.CommandEvent;
import task.trak.app.client.gui.viewmodel.event.CommandEventBus;
import task.trak.app.client.gui.viewmodel.event.CommandEventType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Consumer;

public class CMD_Factory implements CMD<Consumer<String[]>> {

    String cmd;
    String[] options;
    HashMap<String, Consumer<String[]>> cmds;
    private Object lastResult;

    public CMD_Factory(String[] option) throws Exception {
        if (this.verify(option)) {
            throw new Exception("Options are incorrect" + option);
        }
        this.cmds = new HashMap<>();
        this.cmd = option[0];
        this.options = option;
        cmds.put("project", (String[] options) -> new ProjectCMD(options));
        cmds.put("user", (String[] options) -> new UserCMD(options));
        cmds.put("task", (String[] options) -> new TaskCMD(options));
        cmds.put("sprint", (String[] options) -> new SprintCMD(options));
        cmds.put("backlog", (String[] options) -> new BacklogCMD(options));
        cmds.put("login", (String[] options) -> {
            try {
                lastResult = new LoginCMD(options).Execute().orElse(null);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        cmds.put("signup", (String[] options) -> {
            try {
                lastResult = new SignupCMD(options).Execute().orElse(null);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        cmds.put("logout", (String[] options) -> {
            try {
                lastResult = new LogoutCMD(options).Execute().orElse(null);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        cmds.put("projects", (String[] options) -> {
            try {
                lastResult = new ProjectsListCMD(options).Execute().orElse(null);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        cmds.put("tasks", (String[] options) -> {
            try {
                lastResult = new TasksListCMD(options).Execute().orElse(null);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        cmds.put("detail", (String[] options) -> {
            try {
                lastResult = new DetailCMD(options).Execute().orElse(null);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        cmds.put("cur", (String[] options) -> {
            try {
                lastResult = new CurrentTaskCMD(options).Execute().orElse(null);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        cmds.put("start", (String[] options) -> {
            try {
                lastResult = new StartTaskCMD(options).Execute().orElse(null);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        cmds.put("end", (String[] options) -> {
            try {
                lastResult = new EndTaskCMD(options).Execute().orElse(null);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        cmds.put("info", (String[] options) -> {
            try {
                lastResult = new InfoCMD(options).Execute().orElse(null);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        cmds.put("addmember", (String[] options) -> {
            try {
                lastResult = new AddMemberCMD(options).Execute().orElse(null);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        cmds.put("addtask", (String[] options) -> {
            try {
                lastResult = new AddTaskCMD(options).Execute().orElse(null);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        cmds.put("sprintplan", (String[] options) -> {
            try {
                lastResult = new SprintPlanCMD(options).Execute().orElse(null);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        cmds.put("sprints", (String[] options) -> {
            try {
                lastResult = new SprintsListCMD(options).Execute().orElse(null);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
        cmds.put("complete", (String[] options) -> {
            try {
                lastResult = new CompleteCMD(options).Execute().orElse(null);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        });
    }

    private boolean verify(String[] option) {
        return option == null || option.length == 0;
    }

    public void create() {
        String[] subArgs = Arrays.copyOfRange(this.options, 1, this.options.length);
        this.cmds.get(this.cmd).accept(subArgs);
    }

    @Override
    public void accept(String[] strings) {
        String cmd = strings[0];
        String[] subArgs = Arrays.copyOfRange(strings, 1, strings.length);
        Consumer<String[]> handler = this.cmds.get(cmd);
        if (handler == null) {
            System.err.println("Unknown command: " + cmd);
            return;
        }

        if (CommandEventBus.hasListeners()) {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.PrintStream original = System.out;
            System.setOut(new java.io.PrintStream(new TeeOutputStream(original, baos)));
            lastResult = null;
            try {
                handler.accept(subArgs);
                CommandEventType type = resolveEventType(cmd, subArgs);
                CommandEventBus.fire(new CommandEvent(type, cmd, lastResult, baos.toString(), true, null));
            } catch (Exception e) {
                CommandEventBus.fire(new CommandEvent(CommandEventType.ERROR, cmd, null, baos.toString(), false, e.getMessage()));
            } finally {
                System.setOut(original);
            }
        } else {
            handler.accept(subArgs);
        }
    }

    private CommandEventType resolveEventType(String cmd, String[] subArgs) {
        String sub = (subArgs != null && subArgs.length > 0) ? subArgs[0] : "";
        return switch (cmd) {
            case "task" -> switch (sub) {
                case "add" -> CommandEventType.TASK_CREATED;
                case "get" -> CommandEventType.TASK_RETRIEVED;
                case "update" -> CommandEventType.TASK_UPDATED;
                case "delete" -> CommandEventType.TASK_DELETED;
                default -> CommandEventType.UNKNOWN;
            };
            case "project" -> switch (sub) {
                case "add" -> CommandEventType.PROJECT_CREATED;
                case "get" -> CommandEventType.PROJECT_RETRIEVED;
                case "update" -> CommandEventType.PROJECT_UPDATED;
                case "delete" -> CommandEventType.PROJECT_DELETED;
                default -> CommandEventType.UNKNOWN;
            };
            case "sprint" -> switch (sub) {
                case "add" -> CommandEventType.SPRINT_CREATED;
                case "get" -> CommandEventType.SPRINT_RETRIEVED;
                case "update" -> CommandEventType.SPRINT_UPDATED;
                case "delete" -> CommandEventType.SPRINT_DELETED;
                default -> CommandEventType.UNKNOWN;
            };
            case "backlog" -> switch (sub) {
                case "add" -> CommandEventType.BACKLOG_CREATED;
                case "get" -> CommandEventType.BACKLOG_RETRIEVED;
                case "update" -> CommandEventType.BACKLOG_UPDATED;
                case "delete" -> CommandEventType.BACKLOG_DELETED;
                default -> CommandEventType.UNKNOWN;
            };
            case "user" -> switch (sub) {
                case "add" -> CommandEventType.USER_CREATED;
                case "get" -> CommandEventType.USER_RETRIEVED;
                case "update" -> CommandEventType.USER_UPDATED;
                case "delete" -> CommandEventType.USER_DELETED;
                default -> CommandEventType.UNKNOWN;
            };
            case "login" -> CommandEventType.LOGIN;
            case "logout" -> CommandEventType.LOGOUT;
            case "projects" -> CommandEventType.PROJECT_LIST;
            case "tasks" -> CommandEventType.TASK_LIST;
            case "sprints" -> CommandEventType.SPRINT_LIST;
            case "detail" -> CommandEventType.DETAIL;
            case "cur" -> CommandEventType.CURRENT_TASK;
            case "start" -> CommandEventType.START_TASK;
            case "end" -> CommandEventType.END_TASK;
            case "complete" -> CommandEventType.COMPLETE_TASK;
            case "info" -> CommandEventType.INFO;
            case "addtask" -> CommandEventType.ADD_TASK;
            case "addmember" -> CommandEventType.ADD_MEMBER;
            case "sprintplan" -> CommandEventType.SPRINT_PLAN;
            default -> CommandEventType.UNKNOWN;
        };
    }
}
