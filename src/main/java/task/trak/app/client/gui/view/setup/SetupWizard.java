package task.trak.app.client.gui.view.setup;

import task.trak.app.client.cli.TTApp;
import task.trak.app.client.config.WorkspaceConfig;
import task.trak.app.client.gui.view.TrakTheme;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * First-run setup wizard. Fixed storage at ~/Library/Application Support/trak1.0.0/.store.
 * On reinstall, existing .store is deleted.
 */
public class SetupWizard {

    private static final String APP_SUPPORT_DIR =
            System.getProperty("user.home") + "/Library/Application Support/trak1.0.0";
    private static final String STORE_DIR = APP_SUPPORT_DIR + "/.store";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private static final String[] STORAGE_OPTIONS = {"DuckDB (default)", "JSON", "Parquet", "Redis", "MongoDB"};
    private static final String[] STORAGE_KEYS    = {"duckdb",           "json", "parquet", "redis", "mongo"};

    private String chosenFormat;
    private String chosenMode;
    private String chosenServerUrl;
    private String chosenRedisUrl;
    private String chosenMongoUri;
    private String chosenMongoDb;
    private boolean confirmed;

    /**
     * Returns true if this is a first run or a reinstall (different build).
     */
    public static boolean isFirstRun() {
        Path ws = Path.of(STORE_DIR, "workspace.json");
        if (!Files.exists(ws)) return true;

        // Compare build timestamp — if different, it's a reinstall
        String currentBuild = getBuildTimestamp();
        if (currentBuild == null) return false; // dev mode, no build.properties
        TTApp.storedir = STORE_DIR;
        WorkspaceConfig saved = WorkspaceConfig.load();
        return !currentBuild.equals(saved.getBuild_timestamp());
    }

    static String getBuildTimestamp() {
        try (InputStream in = SetupWizard.class.getResourceAsStream("/build.properties")) {
            if (in == null) return null;
            Properties p = new Properties();
            p.load(in);
            return p.getProperty("build.timestamp");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Shows the setup wizard. Returns true if confirmed, false if cancelled.
     */
    public boolean show() {
        chosenFormat = "duckdb";
        chosenMode = "local";
        confirmed = false;

        // On reinstall: wipe old store
        deleteDirectory(Path.of(STORE_DIR));

        JDialog dialog = new JDialog((Frame) null, "Trak Setup", true);
        dialog.setUndecorated(true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(TrakTheme.BG_SURFACE);
        dialog.getRootPane().setBorder(BorderFactory.createLineBorder(TrakTheme.BORDER, 1));

        dialog.add(buildHeader(), BorderLayout.NORTH);

        // ── Body ──
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBackground(TrakTheme.BG_SURFACE);
        body.setBorder(new EmptyBorder(TrakTheme.SP_LG, TrakTheme.SP_XL, TrakTheme.SP_SM, TrakTheme.SP_XL));

        // ── Mode selector ──
        addSectionLabel(body, "Connection Mode");
        addHint(body, "Local runs an embedded server. Remote connects to an existing Trak server.");
        body.add(Box.createVerticalStrut(TrakTheme.SP_SM));

        JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, TrakTheme.SP_LG, 0));
        modeRow.setBackground(TrakTheme.BG_SURFACE);
        modeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        modeRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        ButtonGroup modeGroup = new ButtonGroup();
        JRadioButton localRadio = styledRadio("Local", true);
        JRadioButton remoteRadio = styledRadio("Remote", false);
        modeGroup.add(localRadio);
        modeGroup.add(remoteRadio);
        modeRow.add(localRadio);
        modeRow.add(remoteRadio);
        body.add(modeRow);

        // ── Remote: server URL ──
        body.add(Box.createVerticalStrut(TrakTheme.SP_SM));
        JLabel serverLabel = sectionLabel("Server URL");
        serverLabel.setForeground(TrakTheme.TEXT_SECONDARY);
        serverLabel.setVisible(false);
        body.add(serverLabel);
        body.add(Box.createVerticalStrut(TrakTheme.SP_XS));

        JPanel serverRow = new JPanel(new BorderLayout(TrakTheme.SP_SM, 0));
        serverRow.setBackground(TrakTheme.BG_SURFACE);
        serverRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        serverRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        serverRow.setVisible(false);

        JTextField serverField = styledTextField("http://localhost:8080");
        serverRow.add(serverField, BorderLayout.CENTER);

        JButton testServerBtn = new JButton("Test");
        TrakTheme.styleButtonNav(testServerBtn);
        testServerBtn.setPreferredSize(new Dimension(70, 28));
        serverRow.add(testServerBtn, BorderLayout.EAST);
        body.add(serverRow);

        JLabel serverStatus = statusLabel();
        serverStatus.setVisible(false);
        body.add(serverStatus);

        // ── Local: storage backend ──
        body.add(Box.createVerticalStrut(TrakTheme.SP_LG));
        JLabel fmtLabel = sectionLabel("Storage Backend");
        body.add(fmtLabel);
        body.add(Box.createVerticalStrut(TrakTheme.SP_XS));
        JLabel fmtHint = hint("Choose how data is persisted. DuckDB is recommended for most users.");
        body.add(fmtHint);
        body.add(Box.createVerticalStrut(TrakTheme.SP_SM));

        JComboBox<String> fmtCombo = new JComboBox<>(STORAGE_OPTIONS);
        fmtCombo.setSelectedIndex(0);
        fmtCombo.setBackground(TrakTheme.BG_INPUT);
        fmtCombo.setForeground(TrakTheme.TEXT_PRIMARY);
        fmtCombo.setFont(TrakTheme.FONT_BODY);
        fmtCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        fmtCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(fmtCombo);

        // Storage path info
        body.add(Box.createVerticalStrut(TrakTheme.SP_XS));
        JLabel pathInfo = hint("Data stored at: " + STORE_DIR);
        body.add(pathInfo);

        // ── Redis fields ──
        body.add(Box.createVerticalStrut(TrakTheme.SP_SM));
        JLabel redisLabel = sectionLabel("Redis URL");
        redisLabel.setVisible(false);
        body.add(redisLabel);

        JPanel redisRow = new JPanel(new BorderLayout(TrakTheme.SP_SM, 0));
        redisRow.setBackground(TrakTheme.BG_SURFACE);
        redisRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        redisRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        redisRow.setVisible(false);

        JTextField redisField = styledTextField("redis://localhost:6379");
        redisRow.add(redisField, BorderLayout.CENTER);

        JButton testRedisBtn = new JButton("Test");
        TrakTheme.styleButtonNav(testRedisBtn);
        testRedisBtn.setPreferredSize(new Dimension(70, 28));
        redisRow.add(testRedisBtn, BorderLayout.EAST);
        body.add(redisRow);

        JLabel redisStatus = statusLabel();
        redisStatus.setVisible(false);
        body.add(redisStatus);

        // ── MongoDB fields ──
        body.add(Box.createVerticalStrut(TrakTheme.SP_SM));
        JLabel mongoLabel = sectionLabel("MongoDB Connection");
        mongoLabel.setVisible(false);
        body.add(mongoLabel);

        JLabel mongoUriHint = hint("URI (e.g. mongodb://localhost:27017)");
        mongoUriHint.setVisible(false);
        body.add(mongoUriHint);

        JPanel mongoUriRow = new JPanel(new BorderLayout(TrakTheme.SP_SM, 0));
        mongoUriRow.setBackground(TrakTheme.BG_SURFACE);
        mongoUriRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        mongoUriRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        mongoUriRow.setVisible(false);
        JTextField mongoUriField = styledTextField("mongodb://localhost:27017");
        mongoUriRow.add(mongoUriField, BorderLayout.CENTER);
        body.add(mongoUriRow);

        body.add(Box.createVerticalStrut(TrakTheme.SP_XS));
        JLabel mongoDbHint = hint("Database Name");
        mongoDbHint.setVisible(false);
        body.add(mongoDbHint);

        JTextField mongoDbField = styledTextField("trak");
        mongoDbField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        mongoDbField.setAlignmentX(Component.LEFT_ALIGNMENT);
        mongoDbField.setVisible(false);
        body.add(mongoDbField);

        body.add(Box.createVerticalStrut(TrakTheme.SP_XS));
        JPanel mongoTestRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        mongoTestRow.setBackground(TrakTheme.BG_SURFACE);
        mongoTestRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        mongoTestRow.setVisible(false);

        JButton testMongoBtn = new JButton("Test Connection");
        TrakTheme.styleButtonNav(testMongoBtn);
        testMongoBtn.setPreferredSize(new Dimension(130, 28));
        mongoTestRow.add(testMongoBtn);
        body.add(mongoTestRow);

        JLabel mongoStatus = statusLabel();
        mongoStatus.setVisible(false);
        body.add(mongoStatus);

        // ── Local-only components ──
        Component[] localPanels = {fmtLabel, fmtHint, fmtCombo, pathInfo};

        // ── Mode toggle ──
        Runnable updateMode = () -> {
            boolean isLocal = localRadio.isSelected();
            for (Component c : localPanels) c.setVisible(isLocal);
            serverLabel.setVisible(!isLocal);
            serverRow.setVisible(!isLocal);
            serverStatus.setVisible(!isLocal);
            if (!isLocal) {
                redisLabel.setVisible(false); redisRow.setVisible(false); redisStatus.setVisible(false);
                mongoLabel.setVisible(false); mongoUriHint.setVisible(false); mongoUriRow.setVisible(false);
                mongoDbHint.setVisible(false); mongoDbField.setVisible(false);
                mongoTestRow.setVisible(false); mongoStatus.setVisible(false);
            } else if (fmtCombo.getActionListeners().length > 0) {
                fmtCombo.getActionListeners()[0].actionPerformed(null);
            }
            dialog.pack();
        };
        localRadio.addActionListener(e -> updateMode.run());
        remoteRadio.addActionListener(e -> updateMode.run());

        // ── Format toggle ──
        fmtCombo.addActionListener(e -> {
            int idx = fmtCombo.getSelectedIndex();
            boolean r = idx == 3, m = idx == 4;
            redisLabel.setVisible(r); redisRow.setVisible(r); redisStatus.setVisible(r);
            mongoLabel.setVisible(m); mongoUriHint.setVisible(m); mongoUriRow.setVisible(m);
            mongoDbHint.setVisible(m); mongoDbField.setVisible(m); mongoTestRow.setVisible(m); mongoStatus.setVisible(m);
            dialog.pack();
        });

        // ── Test buttons with spinner ──
        testServerBtn.addActionListener(e ->
                runTest(testServerBtn, serverStatus, () -> testHttpConnection(serverField.getText().trim())));
        testRedisBtn.addActionListener(e ->
                runTest(testRedisBtn, redisStatus, () -> testRedisConnection(redisField.getText().trim())));
        testMongoBtn.addActionListener(e ->
                runTest(testMongoBtn, mongoStatus, () -> testMongoConnection(mongoUriField.getText().trim(), mongoDbField.getText().trim())));

        JScrollPane scrollPane = new JScrollPane(body);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(TrakTheme.BG_SURFACE);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // ── Footer ──
        JLabel errorLabel = new JLabel(" ");
        errorLabel.setFont(TrakTheme.FONT_SMALL);
        errorLabel.setForeground(TrakTheme.STATUS_READY);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(TrakTheme.BG_SURFACE);
        footer.setBorder(new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_XL, TrakTheme.SP_LG, TrakTheme.SP_XL));
        footer.add(errorLabel, BorderLayout.WEST);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, TrakTheme.SP_SM, 0));
        btnRow.setBackground(TrakTheme.BG_SURFACE);

        JButton cancelBtn = new JButton("Cancel");
        TrakTheme.styleButtonNav(cancelBtn);
        cancelBtn.setPreferredSize(new Dimension(90, 30));
        cancelBtn.addActionListener(e -> { confirmed = false; dialog.dispose(); });

        JButton okBtn = new JButton("Get Started");
        TrakTheme.styleButtonPrimary(okBtn);
        okBtn.setPreferredSize(new Dimension(110, 30));
        okBtn.addActionListener(e -> {
            errorLabel.setText(" ");
            boolean isRemote = remoteRadio.isSelected();

            if (isRemote) {
                String url = serverField.getText().trim();
                if (url.isEmpty()) { showError(errorLabel, "Server URL is required."); return; }
                okBtn.setEnabled(false);
                startSpinner(errorLabel, "Validating...");
                new Thread(() -> {
                    String result = testHttpConnection(url);
                    SwingUtilities.invokeLater(() -> {
                        stopSpinner(errorLabel);
                        okBtn.setEnabled(true);
                        if (result != null) {
                            showError(errorLabel, "Connection failed.");
                        } else {
                            chosenMode = "remote";
                            chosenServerUrl = url;
                            confirmed = true;
                            dialog.dispose();
                        }
                    });
                }).start();
                return;
            }

            chosenFormat = STORAGE_KEYS[fmtCombo.getSelectedIndex()];
            chosenMode = "local";

            if ("redis".equals(chosenFormat)) {
                String url = redisField.getText().trim();
                if (url.isEmpty()) { showError(errorLabel, "Redis URL is required."); return; }
                chosenRedisUrl = url;
            }
            if ("mongo".equals(chosenFormat)) {
                String uri = mongoUriField.getText().trim();
                if (uri.isEmpty()) { showError(errorLabel, "MongoDB URI is required."); return; }
                chosenMongoUri = uri;
                chosenMongoDb = mongoDbField.getText().trim();
                if (chosenMongoDb.isEmpty()) chosenMongoDb = "trak";
            }

            confirmed = true;
            dialog.dispose();
        });

        btnRow.add(cancelBtn);
        btnRow.add(okBtn);
        footer.add(btnRow, BorderLayout.EAST);
        dialog.add(footer, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setMinimumSize(new Dimension(520, 460));
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);

        return confirmed;
    }

    public void apply() {
        Path storePath = Path.of(STORE_DIR);
        try { Files.createDirectories(storePath); } catch (Exception e) {
            throw new RuntimeException("Failed to create storage directory: " + e.getMessage(), e);
        }
        TTApp.storedir = STORE_DIR;

        WorkspaceConfig config = new WorkspaceConfig();
        config.setMode(chosenMode);
        if ("remote".equals(chosenMode)) {
            config.setServer_url(chosenServerUrl);
        } else {
            config.setStore_format(chosenFormat);
            if (chosenRedisUrl != null) config.setRedis_url(chosenRedisUrl);
            if (chosenMongoUri != null) config.setMongo_uri(chosenMongoUri);
            if (chosenMongoDb != null) config.setMongo_db(chosenMongoDb);
        }
        String buildTs = getBuildTimestamp();
        if (buildTs != null) config.setBuild_timestamp(buildTs);
        config.save();
    }

    public String getChosenMode() { return chosenMode; }
    public String getChosenServerUrl() { return chosenServerUrl; }

    // ── Connection testers (all return null on success, error message on failure) ──

    private static String testHttpConnection(String url) {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).GET().timeout(TIMEOUT).build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            return resp.statusCode() > 0 ? null : "No response";
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private static String testRedisConnection(String url) {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Future<String> future = exec.submit(() -> {
            var jedis = new redis.clients.jedis.JedisPooled(url);
            String pong = jedis.ping();
            jedis.close();
            return "PONG".equalsIgnoreCase(pong) ? null : "Unexpected: " + pong;
        });
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return "Connection timed out";
        } catch (Exception e) {
            return e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        } finally {
            exec.shutdownNow();
        }
    }

    private static String testMongoConnection(String uri, String dbName) {
        ExecutorService exec = Executors.newSingleThreadExecutor();
        Future<String> future = exec.submit(() -> {
            var client = com.mongodb.client.MongoClients.create(uri);
            var db = client.getDatabase(dbName.isEmpty() ? "trak" : dbName);
            db.listCollectionNames().first();
            client.close();
            return null;
        });
        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return "Connection timed out";
        } catch (Exception e) {
            return e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
        } finally {
            exec.shutdownNow();
        }
    }

    // ── Spinner (animated dots on status label) ──

    private static final String[] SPINNER_FRAMES = {"\u25D0", "\u25D3", "\u25D1", "\u25D2"};

    private static void runTest(JButton btn, JLabel status, Callable<String> test) {
        btn.setEnabled(false);
        startSpinner(status, "Connecting");
        new Thread(() -> {
            String result;
            try {
                result = test.call();
            } catch (Exception e) {
                result = e.getMessage();
            }
            String finalResult = result;
            SwingUtilities.invokeLater(() -> {
                stopSpinner(status);
                btn.setEnabled(true);
                if (finalResult == null) {
                    status.setText("Connected.");
                    status.setForeground(TrakTheme.ACCENT_GREEN);
                } else {
                    status.setText("Failed: " + finalResult);
                    status.setForeground(TrakTheme.STATUS_READY);
                }
            });
        }).start();
    }

    private static void startSpinner(JLabel label, String message) {
        label.setForeground(TrakTheme.TEXT_SECONDARY);
        int[] frame = {0};
        stopSpinner(label); // clear any existing
        Timer timer = new Timer(150, e -> {
            label.setText(SPINNER_FRAMES[frame[0] % SPINNER_FRAMES.length] + " " + message + "...");
            frame[0]++;
        });
        timer.start();
        label.putClientProperty("spinnerTimer", timer);
    }

    private static void stopSpinner(JLabel label) {
        Timer existing = (Timer) label.getClientProperty("spinnerTimer");
        if (existing != null) {
            existing.stop();
            label.putClientProperty("spinnerTimer", null);
        }
    }

    private static void showError(JLabel label, String msg) {
        label.setText(msg);
        label.setForeground(TrakTheme.STATUS_READY);
    }

    private static void deleteDirectory(Path dir) {
        if (!Files.exists(dir)) return;
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        } catch (Exception ignored) {}
    }

    // ── UI helpers ──

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(TrakTheme.BG_DARK);
        header.setBorder(new EmptyBorder(TrakTheme.SP_XL, TrakTheme.SP_XL, TrakTheme.SP_LG, TrakTheme.SP_XL));

        try (var in = getClass().getResourceAsStream("/icons/trak-64.png")) {
            if (in != null) {
                JLabel iconLabel = new JLabel(new ImageIcon(ImageIO.read(in)));
                iconLabel.setBorder(new EmptyBorder(0, 0, 0, TrakTheme.SP_LG));
                header.add(iconLabel, BorderLayout.WEST);
            }
        } catch (Exception ignored) {}

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBackground(TrakTheme.BG_DARK);

        JLabel title = new JLabel("Welcome to Trak");
        title.setFont(TrakTheme.FONT_HEADING);
        title.setForeground(TrakTheme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(title);
        text.add(Box.createVerticalStrut(TrakTheme.SP_XS));

        JLabel subtitle = new JLabel("Configure your workspace before getting started.");
        subtitle.setFont(TrakTheme.FONT_BODY);
        subtitle.setForeground(TrakTheme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(subtitle);

        header.add(text, BorderLayout.CENTER);
        return header;
    }

    private static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(TrakTheme.FONT_BODY.deriveFont(Font.BOLD));
        l.setForeground(TrakTheme.TEXT_PRIMARY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JLabel hint(String text) {
        JLabel l = new JLabel(text);
        l.setFont(TrakTheme.FONT_SMALL);
        l.setForeground(TrakTheme.TEXT_MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static JLabel statusLabel() {
        JLabel l = new JLabel(" ");
        l.setFont(TrakTheme.FONT_SMALL);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private static void addSectionLabel(JPanel p, String text) {
        p.add(sectionLabel(text));
        p.add(Box.createVerticalStrut(TrakTheme.SP_XS));
    }

    private static void addHint(JPanel p, String text) { p.add(hint(text)); }

    private static JTextField styledTextField(String initial) {
        JTextField f = new JTextField(initial);
        f.setBackground(TrakTheme.BG_INPUT);
        f.setForeground(TrakTheme.TEXT_PRIMARY);
        f.setCaretColor(TrakTheme.TEXT_PRIMARY);
        f.setFont(TrakTheme.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TrakTheme.BORDER, 1),
                new EmptyBorder(4, 8, 4, 8)));
        return f;
    }

    private static JRadioButton styledRadio(String text, boolean selected) {
        JRadioButton r = new JRadioButton(text, selected);
        r.setBackground(TrakTheme.BG_SURFACE);
        r.setForeground(TrakTheme.TEXT_PRIMARY);
        r.setFont(TrakTheme.FONT_BODY);
        r.setFocusPainted(false);
        return r;
    }
}
