package task.trak.app.client.cli.cmd.backlog;

import task.trak.api.dto.BacklogDTO;
import task.trak.api.service.BacklogService;
import task.trak.api.service.ServiceFactory;

import java.util.Optional;
import java.util.Scanner;

public class BacklogDeleteCMD extends BacklogCMD {
    private final BacklogService backlogService = ServiceFactory.backlogService();
    private final String backlogName;

    public BacklogDeleteCMD(String[] args) {
        super();
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Backlog name is required for delete command");
        }
        this.backlogName = args[0];
    }

    @Override
    public Optional<BacklogDTO> Execute() throws Exception {
        System.out.println("Confirm deletion of backlog \"" + this.backlogName + "\"? (yes/no)");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("yes")) {
            boolean deleted = backlogService.deleteByName(this.backlogName);
            if (deleted) {
                System.out.println("Backlog \"" + this.backlogName + "\" deleted successfully.");
            } else {
                System.out.println("Backlog \"" + this.backlogName + "\" not found.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
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
