package task.trak.app.client.gui;

import task.trak.api.service.ServiceFactory;
import task.trak.app.client.ApiClient;
import task.trak.app.client.gui.controller.TTAppGUI;

import java.util.Arrays;

public class GUIMain {
    public static void main(String[] args) {
        boolean seedTest = Arrays.asList(args).contains("--test");
        boolean local = Arrays.asList(args).contains("--local");
        if (!local) {
            String url = parseServerUrl(args);
            ApiClient.setBaseUrl(url);
            ServiceFactory.registerHttpServices();
        } else {
            ServiceFactory.registerLocalServices();
        }
        TTAppGUI gui = new TTAppGUI(seedTest, local);
        gui.initStore(local);
    }

    private static String parseServerUrl(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("--server-url".equals(args[i]) && i + 1 < args.length) {
                return args[i + 1];
            }
        }
        return "http://localhost:8080";
    }
}
