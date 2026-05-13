package task.trak.app.client.cli.cmd.sprint;

import task.trak.api.dto.SprintDTO;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.SprintService;

import java.util.Optional;
import java.util.Scanner;

public class SprintDeleteCMD extends SprintCMD {
    private final SprintService sprintService = ServiceFactory.sprintService();
    private final String sprintName;

    public SprintDeleteCMD(String[] args) {
        super();
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("Sprint name is required for delete command");
        }
        this.sprintName = args[0];
    }

    @Override
    public Optional<SprintDTO> Execute() throws Exception {
        System.out.println("Confirm deletion of sprint \"" + this.sprintName + "\"? (yes/no)");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("yes")) {
            boolean deleted = sprintService.deleteByName(this.sprintName);
            if (deleted) {
                System.out.println("Sprint \"" + this.sprintName + "\" deleted successfully.");
            } else {
                System.out.println("Sprint \"" + this.sprintName + "\" not found.");
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
