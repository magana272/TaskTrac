package task.trak.app.server.model.user;

import task.trak.app.server.util.PasswordUtil;

public class TrakBuilderUser implements UserBuilder {
    private String first_name;
    private String last_name;
    private Long id;
    private String user_name;
    private String email;
    private String password_hash;

    @Override
    public UserBuilder setFirstName(String first_name) {
        this.first_name = first_name;
        return this;
    }

    @Override
    public UserBuilder setLastName(String last_name) {
        this.last_name = last_name;
        return this;
    }

    @Override
    public UserBuilder setID(Long id) {
        this.id = id;
        return this;
    }

    @Override
    public UserBuilder setUserName(String user_name) {
        this.user_name = user_name;
        return this;
    }

    @Override
    public UserBuilder setEmail(String email) {
        this.email = email;
        return this;
    }

    @Override
    public UserBuilder setPassword(String password) {
        this.password_hash = password != null ? PasswordUtil.hash(password) : null;
        return this;
    }

    @Override
    public User build() {
        return new User(id, first_name, last_name, user_name, email, password_hash, null, null);
    }
}
