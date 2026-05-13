package task.trak.app.client.cli.cmd.workspace;

import task.trak.app.client.cli.cmd.cmdtype.CMD;

import java.util.Optional;

public class InfoCMD implements CMD<Void> {

    public InfoCMD(String[] args) {
    }

    @Override
    public Optional<Void> Execute() throws Exception {
        System.out.println("Trak CLI Commands:");
        System.out.println();
        System.out.println("  Authentication:");
        System.out.println("    login <username> --password <pw>    Log in");
        System.out.println("    logout                              Log out");
        System.out.println();
        System.out.println("  Workspace (requires login):");
        System.out.println("    projects                            List my projects");
        System.out.println("    tasks                               List my tasks (table)");
        System.out.println("    sprints                             List my sprints");
        System.out.println("    detail -s|-p|-t <id>                Full info by sprint/project/task ID");
        System.out.println("    cur                                 Current task + project + elapsed time");
        System.out.println("    start <task_id>                     Start working on a task");
        System.out.println("    end                                 Stop working on current task");
        System.out.println("    complete <task_id>                  Mark task as COMPLETE");
        System.out.println("    addtask <project>                   Add a task (interactive)");
        System.out.println("    addmember <project> <username>      Add member to project");
        System.out.println("    sprintplan                          Plan a sprint (interactive)");
        System.out.println();
        System.out.println("  Entity CRUD:");
        System.out.println("    user     add|get|update|delete      Manage users");
        System.out.println("    project  add|get|update|delete      Manage projects");
        System.out.println("    task     add|get|update|delete      Manage tasks");
        System.out.println("    sprint   add|get|update|delete      Manage sprints");
        System.out.println("    backlog  add|get|update|delete      Manage backlogs");
        return Optional.empty();
    }

    @Override
    public void accept(String[] strings) {
        try {
            this.Execute();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
