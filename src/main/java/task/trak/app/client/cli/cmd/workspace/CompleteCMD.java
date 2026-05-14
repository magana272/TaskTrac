package task.trak.app.client.cli.cmd.workspace;

import task.trak.model.dto.TaskDTO;
import task.trak.model.dto.request.UpdateTaskRequest;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.TaskService;
import task.trak.model.util.TimeUtil;

import java.util.Optional;

public class CompleteCMD extends WorkspaceCMD {
    private final Long taskId;
    private final TaskService taskService = ServiceFactory.taskService();

    public CompleteCMD(String[] args) {
        super();
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Usage: complete <task_id>");
        }
        this.taskId = Long.parseLong(args[0]);
    }

    @Override
    public Optional<Object> Execute() throws Exception {
        TaskDTO task = taskService.getById(this.taskId);
        if (task == null) {
            System.out.println("Task " + this.taskId + " not found.");
            return Optional.empty();
        }

        if ("COMPLETE".equals(task.status())) {
            System.out.println("Task \"" + task.title() + "\" is already complete.");
            return Optional.empty();
        }

        // If this was the current task, clear session
        if (this.session.getCurrent_task_id() != null && this.session.getCurrent_task_id().equals(this.taskId)) {
            this.session.setCurrent_task_id(null);
            this.session.setTask_started_at(null);
        }

        // Complete the task via service
        taskService.updateById(new UpdateTaskRequest(this.taskId, null, "COMPLETE", null, null, null, null));

        String timeStr = task.timeSpentMs() > 0
                ? " - Total time: " + TimeUtil.formatDuration(task.timeSpentMs())
                : "";
        System.out.println("Completed: " + task.title() + timeStr);
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
