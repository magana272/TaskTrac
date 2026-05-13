package task.trak.app.server.service.project;

import task.trak.api.dto.ProjectDTO;
import task.trak.api.service.ProjectService;
import task.trak.app.server.dao.DAOFactory;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.project.Project;
import task.trak.app.server.model.project.TrakProjectBuilder;
import task.trak.app.server.model.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TrakProjectService implements ProjectService {

    private final EntityDAO<Project> store = DAOFactory.projectDAO();

    @Override
    public ProjectDTO create(String name) {
        Project project = new TrakProjectBuilder()
                .setID(System.currentTimeMillis())
                .setProjectName(name)
                .build();
        store.save(project);
        return toDTO(project);
    }

    @Override
    public ProjectDTO create(String name, String summary, String ownerUsername, List<String> memberUsernames) {
        TrakProjectBuilder builder = new TrakProjectBuilder();
        builder.setID(System.currentTimeMillis());
        builder.setProjectName(name);
        if (summary != null) builder.setSummary(summary);
        if (ownerUsername != null) {
            User owner = DAOFactory.userDAO().loadByKey(ownerUsername);
            if (owner == null) {
                throw new IllegalArgumentException("Owner user \"" + ownerUsername + "\" not found.");
            }
            builder.setOwner(owner);
        }
        if (memberUsernames != null) {
            List<User> members = new ArrayList<>();
            for (String username : memberUsernames) {
                User member = DAOFactory.userDAO().loadByKey(username);
                if (member == null) {
                    throw new IllegalArgumentException("Member user \"" + username + "\" not found.");
                }
                members.add(member);
            }
            builder.setMembers(members);
        }
        Project project = builder.build();
        store.save(project);
        return toDTO(project);
    }

    @Override
    public ProjectDTO getById(Long id) {
        Project project = store.loadAll().stream()
                .filter(p -> id.equals(p.getId()))
                .findFirst()
                .orElse(null);
        return project != null ? toDTO(project) : null;
    }

    @Override
    public ProjectDTO getByName(String name) {
        Project project = store.loadByKey(name);
        return project != null ? toDTO(project) : null;
    }

    @Override
    public boolean deleteByName(String name) {
        return store.deleteByKey(name);
    }

    @Override
    public ProjectDTO updateByName(String projectName, String newName, String newSummary, List<String> newMemberUsernames) {
        Project project = store.loadByKey(projectName);
        if (project == null) {
            throw new IllegalArgumentException("Project \"" + projectName + "\" not found.");
        }

        if (newName != null && !newName.equals(projectName)) {
            store.deleteByKey(projectName);
            project.setName(newName);
        }

        if (newSummary != null) {
            project.setSummary(newSummary);
        }

        if (newMemberUsernames != null) {
            List<User> members = new ArrayList<>();
            for (String username : newMemberUsernames) {
                User member = DAOFactory.userDAO().loadByKey(username);
                if (member == null) {
                    throw new IllegalArgumentException("Member user \"" + username + "\" not found.");
                }
                members.add(member);
            }
            project.setMembers(members);
        }

        store.save(project);
        return toDTO(project);
    }

    @Override
    public List<ProjectDTO> listAll() {
        return store.loadAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectDTO> listByUser(String username) {
        return store.loadAll().stream()
                .filter(p -> {
                    if (p.getOwner() != null && username.equals(p.getOwner().getUser_name())) return true;
                    if (p.getMembers() != null) {
                        return p.getMembers().stream()
                                .anyMatch(m -> username.equals(m.getUser_name()));
                    }
                    return false;
                })
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectDTO addMember(String projectName, String username) {
        Project project = store.loadByKey(projectName);
        if (project == null) {
            throw new IllegalArgumentException("Project \"" + projectName + "\" not found.");
        }
        User user = DAOFactory.userDAO().loadByKey(username);
        if (user == null) {
            throw new IllegalArgumentException("User \"" + username + "\" not found.");
        }
        if (project.getMembers() == null) {
            project.setMembers(new ArrayList<>());
        }
        project.getMembers().add(user);
        store.save(project);
        return toDTO(project);
    }

    private ProjectDTO toDTO(Project p) {
        String ownerUsername = p.getOwner() != null ? p.getOwner().getUser_name() : null;
        List<String> memberUsernames = p.getMembers() != null
                ? p.getMembers().stream().map(User::getUser_name).collect(Collectors.toList())
                : Collections.emptyList();
        int memberCount = p.getMembers() != null ? p.getMembers().size() : 0;
        int taskCount = (int) DAOFactory.taskDAO().loadAll().stream()
                .filter(t -> p.getName() != null && p.getName().equals(t.getProject_name()))
                .count();
        int sprintCount = (int) DAOFactory.sprintDAO().loadAll().stream()
                .filter(s -> p.getName() != null && p.getName().equals(s.getProject_name()))
                .count();
        return new ProjectDTO(p.getId(), p.getName(), p.getSummary(), p.getCreated_at(),
                ownerUsername, memberUsernames, memberCount, taskCount, sprintCount);
    }
}
