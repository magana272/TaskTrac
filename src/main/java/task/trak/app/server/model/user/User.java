package task.trak.app.server.model.user;

import java.util.ArrayList;
import java.util.List;

public class User {
    private Long id;
    private String first_name;
    private String last_name;
    private String user_name;
    private String email;
    private String password_hash;
    private List<Long> tasks;
    private List<Long> projects;

    public User(Long id, String firstName, String lastName, String userName, String email, String passwordHash, List<Long> tasks, List<Long> projects) {
        this.id = id;
        this.first_name = firstName;
        this.last_name = lastName;
        this.user_name = userName;
        this.email = email;
        this.password_hash = passwordHash;
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.projects = projects != null ? projects : new ArrayList<>();
    }

    public Long getID() {
        return this.id;
    }

    public String getFirst_name() {
        return this.first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return this.last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getUser_name() {
        return this.user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword_hash() {
        return this.password_hash;
    }

    public void setPassword_hash(String password_hash) {
        this.password_hash = password_hash;
    }

    public List<Long> getTasks() {
        return this.tasks;
    }

    public void setTasks(List<Long> tasks) {
        this.tasks = tasks;
    }

    public List<Long> getProjects() {
        return this.projects;
    }

    public void setProjects(List<Long> projects) {
        this.projects = projects;
    }

    public String getName() {
        return this.first_name + this.last_name;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
