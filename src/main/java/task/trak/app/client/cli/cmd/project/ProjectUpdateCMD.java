package task.trak.app.client.cli.cmd.project;

import task.trak.api.dto.ProjectDTO;
import task.trak.api.dto.request.UpdateProjectRequest;
import task.trak.api.service.ProjectService;
import task.trak.api.service.ServiceFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ProjectUpdateCMD extends ProjectCMD {
    private final ProjectService projectService = ServiceFactory.projectService();
    private String projectName;
    private HashMap<String, String> options;

    public ProjectUpdateCMD(String[] args) {
        super();
        this.parse(args);
    }

    public HashMap<String, String> parse(String[] args) {
        if (args == null || args.length < 3) {
            throw new IllegalArgumentException(
                    "Usage: project update <project_name> --<field> <value>");
        }
        this.projectName = args[0];
        HashMap<String, String> parsed = new HashMap<>();
        int i = 1;
        while (i < args.length) {
            if (args[i].startsWith("--")) {
                String key = args[i].replaceFirst("^--", "");
                StringBuilder value = new StringBuilder();
                i++;
                while (i < args.length && !args[i].startsWith("--")) {
                    if (value.length() > 0) value.append(" ");
                    value.append(args[i]);
                    i++;
                }
                parsed.put(key, value.toString());
            } else {
                i++;
            }
        }
        this.options = parsed;
        return parsed;
    }

    @Override
    public Optional<ProjectDTO> Execute() throws Exception {
        String newName = this.options.get("name");
        String newSummary = this.options.get("summary");

        List<String> newMemberUsernames = null;
        String membersStr = this.options.get("members");
        if (membersStr != null && !membersStr.isEmpty()) {
            newMemberUsernames = ProjectAddCMD.parseMemberUsernames(membersStr);
        }

        ProjectDTO updated = projectService.updateByName(
                new UpdateProjectRequest(this.projectName, newName, newSummary, newMemberUsernames));

        String displayName = newName != null ? newName : this.projectName;
        System.out.println("Project \"" + displayName + "\" updated successfully.");
        return Optional.of(updated);
    }

    @Override
    public void accept(String[] strings) {
        try {
            this.Execute();
        } catch (Exception e) {
            System.err.println("Error updating project: " + e.getMessage());
        }
    }
}
