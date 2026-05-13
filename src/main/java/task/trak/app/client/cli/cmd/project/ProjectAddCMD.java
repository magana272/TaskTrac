package task.trak.app.client.cli.cmd.project;

import task.trak.api.dto.ProjectDTO;
import task.trak.api.model.Session;
import task.trak.api.service.ProjectService;
import task.trak.api.service.ServiceFactory;
import task.trak.app.App;
import task.trak.app.client.cli.TTApp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class ProjectAddCMD extends ProjectCMD {
    private final ProjectService projectService = ServiceFactory.projectService();
    String project_name;
    HashMap<String, String> options;

    public ProjectAddCMD(String[] options) {
        super();
        this.project_name = options[0];
        this.parse(options);
    }

    static List<String> parseMemberUsernames(String membersStr) {
        List<String> result = new ArrayList<>();
        String inner = membersStr.trim();
        if (inner.startsWith("[")) inner = inner.substring(1);
        if (inner.endsWith("]")) inner = inner.substring(0, inner.length() - 1);

        String[] usernames = inner.split(",");
        for (String username : usernames) {
            String trimmed = username.trim().replaceAll("^\"|\"$", "");
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    public HashMap<String, String> parse(String[] options) {
        HashMap<String, String> parsed = new HashMap<>();
        parsed.put("name", options[0]);
        int i = 1;
        while (i < options.length) {
            if (options[i].startsWith("--")) {
                String key = options[i].replaceFirst("^--", "");
                StringBuilder value = new StringBuilder();
                i++;
                while (i < options.length && !options[i].startsWith("--")) {
                    if (value.length() > 0) value.append(" ");
                    value.append(options[i]);
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
        String ownerUsername = this.options.get("owner");
        if (ownerUsername == null || ownerUsername.isEmpty()) {
            App app = TTApp.getInstance();
            Session session = app != null ? app.getSession() : null;
            if (session != null && session.getLogged_in_user() != null) {
                ownerUsername = session.getLogged_in_user();
            } else {
                throw new IllegalArgumentException("--owner is required. Usage: project add <name> --owner <username>");
            }
        }

        String summary = this.options.get("summary");

        List<String> memberUsernames = null;
        String membersStr = this.options.get("members");
        if (membersStr != null && !membersStr.isEmpty()) {
            memberUsernames = parseMemberUsernames(membersStr);
        }

        ProjectDTO project = projectService.create(this.project_name, summary, ownerUsername, memberUsernames);
        System.out.println("Project " + project.id() + " \"" + this.project_name + "\" created successfully.");
        return Optional.of(project);
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
