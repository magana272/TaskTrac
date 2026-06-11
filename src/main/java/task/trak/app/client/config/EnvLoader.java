package task.trak.app.client.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Loads .env properties from multiple locations (first match wins):
 *   1. Classpath resource /env.properties (bundled in jar/jpackage)
 *   2. ~/Library/Application Support/trak1.0.0/.env
 *   3. .env in working directory (development)
 */
public class EnvLoader {

    private static final String APP_SUPPORT =
            System.getProperty("user.home") + "/Library/Application Support/trak1.0.0";
    private static Properties props;

    public static synchronized Properties load() {
        if (props != null) return props;
        props = new Properties();

        // 1. Classpath resource (bundled)
        try (InputStream in = EnvLoader.class.getResourceAsStream("/env.properties")) {
            if (in != null) {
                props.load(in);
                return props;
            }
        } catch (IOException ignored) {}

        // 2. App support directory
        Path appSupport = Path.of(APP_SUPPORT, ".env");
        if (Files.exists(appSupport)) {
            try { props.load(Files.newBufferedReader(appSupport)); return props; }
            catch (IOException ignored) {}
        }

        // 3. Working directory (development)
        Path cwd = Path.of(".env");
        if (Files.exists(cwd)) {
            try { props.load(Files.newBufferedReader(cwd)); }
            catch (IOException ignored) {}
        }

        return props;
    }

    /**
     * Get a value: System env var first, then .env file, then default.
     */
    public static String get(String key, String defaultValue) {
        String env = System.getenv(key);
        if (env != null && !env.isBlank()) return env;

        String val = load().getProperty(key);
        return val != null && !val.isBlank() ? val : defaultValue;
    }

    /**
     * Get a required value. Throws if not found anywhere.
     */
    public static String getRequired(String key) {
        String val = get(key, null);
        if (val == null) {
            throw new IllegalStateException(key + " not configured. Set as env var or in .env file.");
        }
        return val;
    }
}