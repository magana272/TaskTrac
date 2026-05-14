package task.trak.app.client.cli.cmd.workspace;

import task.trak.model.dto.TaskDTO;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.TaskService;
import task.trak.model.util.TimeUtil;

import java.util.Optional;

public class CurrentTaskCMD extends WorkspaceCMD {
    private final TaskService taskService = ServiceFactory.taskService();

    public CurrentTaskCMD(String[] args) {
        super();
    }

    @Override
    public Optional<Object> Execute() throws Exception {
        Long taskId = this.session.getCurrent_task_id();
        if (taskId == null) {
            System.out.println("No task in progress.");
            return Optional.empty();
        }

        TaskDTO task = taskService.getById(taskId);
        if (task == null) {
            System.out.println("Current task " + taskId + " not found.");
            return Optional.empty();
        }

        System.out.println("Task: " + task.id());
        System.out.println("Project: " + task.projectName());
        System.out.println(task.title() + " - Time spent: " + TimeUtil.formatDuration(task.timeSpentMs()));
        if (task.deadline() != null) {
            System.out.println("Deadline: " + TimeUtil.formatDeadlineRemaining(task.deadline()));
        }
        return Optional.of(task);
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
