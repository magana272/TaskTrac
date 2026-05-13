package task.trak.app.server.model.user;

public interface UserBuilder {
    UserBuilder setFirstName(String first_name);

    UserBuilder setLastName(String last_name);

    UserBuilder setID(Long id);

    UserBuilder setUserName(String user_name);

    UserBuilder setEmail(String email);

    UserBuilder setPassword(String password);

    User build();
}
