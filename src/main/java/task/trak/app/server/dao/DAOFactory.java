package task.trak.app.server.dao;

import task.trak.app.server.dao.json.*;
import task.trak.app.server.dao.mongo.*;
import task.trak.app.server.dao.parquet.*;
import task.trak.app.server.model.backlog.BackLog;
import task.trak.app.server.model.project.Project;
import task.trak.app.server.model.sprint.Sprint;
import task.trak.app.server.model.task.Task;
import task.trak.app.server.model.user.User;

public class DAOFactory {

    private static Format format = Format.PARQUET;

    public static Format getFormat() {
        return format;
    }

    public static void setFormat(Format f) {
        format = f;
    }

    public static EntityDAO<User> userDAO() {
        return switch (format) {
            case PARQUET -> new ParquetUserDAO();
            case JSON -> new JsonUserDAO();
            case MONGO -> new MongoUserDAO();
        };
    }

    public static EntityDAO<Project> projectDAO() {
        return switch (format) {
            case PARQUET -> new ParquetProjectDAO();
            case JSON -> new JsonProjectDAO();
            case MONGO -> new MongoProjectDAO();
        };
    }

    public static EntityDAO<Task> taskDAO() {
        return switch (format) {
            case PARQUET -> new ParquetTaskDAO();
            case JSON -> new JsonTaskDAO();
            case MONGO -> new MongoTaskDAO();
        };
    }

    public static EntityDAO<Sprint> sprintDAO() {
        return switch (format) {
            case PARQUET -> new ParquetSprintDAO();
            case JSON -> new JsonSprintDAO();
            case MONGO -> new MongoSprintDAO();
        };
    }

    public static EntityDAO<BackLog> backlogDAO() {
        return switch (format) {
            case PARQUET -> new ParquetBacklogDAO();
            case JSON -> new JsonBacklogDAO();
            case MONGO -> new MongoBacklogDAO();
        };
    }

    public enum Format {JSON, PARQUET, MONGO}
}
