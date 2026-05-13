package task.trak.app.server;

import task.trak.api.service.ServiceFactory;
import task.trak.app.server.server.TrakServer;

public class ServerMain {
    public static void main(String[] args) {
        ServiceFactory.registerLocalServices();
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
            }
        }
        try {
            TrakServer server = new TrakServer(port);
            server.start();
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}
