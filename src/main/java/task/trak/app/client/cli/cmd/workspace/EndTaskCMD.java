package task.trak.app.client.cli.cmd.workspace;

import task.trak.api.dto.TaskDTO;
import task.trak.api.dto.request.UpdateTaskRequest;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.TaskService;
import task.trak.api.util.TimeUtil;

import java.util.Optional;

public class EndTaskCMD extends WorkspaceCMD {
    private final TaskService taskService = ServiceFactory.taskService();

    public EndTaskCMD(String[] args) {
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
            System.out.println("Task " + taskId + " not found.");
            this.session.setCurrent_task_id(null);
            this.session.setTask_started_at(null);
            return Optional.empty();
        }

        // Stop the task via service
        taskService.updateById(new UpdateTaskRequest(taskId, null, "READY", null, null, null));

        // Calculate accumulated time for display
        long accumulated = task.timeSpentMs();
        Long started = this.session.getTask_started_at();
        if (started != null) {
            accumulated += (System.currentTimeMillis() - started);
        }

        // Clear session
        this.session.setCurrent_task_id(null);
        this.session.setTask_started_at(null);

        System.out.println("Stopped: " + task.title() + " - Time spent: " + TimeUtil.formatDuration(accumulated));
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
