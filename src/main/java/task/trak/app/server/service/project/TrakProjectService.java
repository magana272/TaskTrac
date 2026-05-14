package task.trak.app.server.service.project;

import task.trak.model.dto.ProjectDTO;
import task.trak.model.dto.request.CreateProjectRequest;
import task.trak.model.dto.request.UpdateProjectRequest;
import task.trak.model.exception.EntityNotFoundException;
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
    public ProjectDTO create(CreateProjectRequest request) {
        request.validate();
        TrakProjectBuilder builder = new TrakProjectBuilder();
        builder.setID(System.currentTimeMillis());
        builder.setProjectName(request.name());
        if (request.summary() != null) builder.setSummary(request.summary());
        if (request.ownerUsername() != null) {
            User owner = DAOFactory.userDAO().loadByKey(request.ownerUsername());
            if (owner == null) {
                throw new EntityNotFoundException("Owner user \"" + request.ownerUsername() + "\" not found.");
            }
            builder.setOwner(owner);
        }
        if (request.memberUsernames() != null) {
            List<User> members = new ArrayList<>();
            for (String username : request.memberUsernames()) {
                User member = DAOFactory.userDAO().loadByKey(username);
                if (member == null) {
                    throw new EntityNotFoundException("Member user \"" + username + "\" not found.");
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
    public ProjectDTO updateByName(UpdateProjectRequest request) {
        request.validate();
        Project project = store.loadByKey(request.projectName());
        if (project == null) {
            throw new EntityNotFoundException("Project \"" + request.projectName() + "\" not found.");
        }

        if (request.newName() != null && !request.newName().equals(request.projectName())) {
            store.deleteByKey(request.projectName());
            project.setName(request.newName());
        }

        if (request.newSummary() != null) {
            project.setSummary(request.newSummary());
        }

        if (request.newMemberUsernames() != null) {
            List<User> members = new ArrayList<>();
            for (String username : request.newMemberUsernames()) {
                User member = DAOFactory.userDAO().loadByKey(username);
                if (member == null) {
                    throw new EntityNotFoundException("Member user \"" + username + "\" not found.");
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
            throw new EntityNotFoundException("Project \"" + projectName + "\" not found.");
        }
        User user = DAOFactory.userDAO().loadByKey(username);
        if (user == null) {
            throw new EntityNotFoundException("User \"" + username + "\" not found.");
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
