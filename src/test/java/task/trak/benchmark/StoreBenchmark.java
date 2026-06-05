package task.trak.benchmark;

import task.trak.app.client.cli.TTApp;
import task.trak.app.server.dao.DAOFactory;
import task.trak.app.server.dao.EntityDAO;
import task.trak.app.server.model.task.Task;
import task.trak.api.service.STATE;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Benchmarks CRUD operations across storage backends at multiple scale points.
 *
 * Environment variables:
 *   BENCH_STORE   — single store to benchmark (JSON, PARQUET, DUCKDB, REDIS).
 *                   If unset, benchmarks all available stores.
 *   BENCH_SCALES  — comma-separated scale points (default: 10,100,1000,2000,4000,10000,100000).
 *   BENCH_OUTPUT  — output directory (default: docs/store_analysis).
 */
public class StoreBenchmark {

    private static final int[] DEFAULT_SCALES = {10, 100, 1000, 2000, 4000, 10000, 100000};
    private static final String TEST_STORE = "src/test/.store_benchmark";
    private String originalStoreDir;

    @Before
    public void setUp() throws Exception {
        originalStoreDir = TTApp.storedir;
        TTApp.storedir = TEST_STORE;
        Files.createDirectories(Path.of(TEST_STORE));
    }

    @After
    public void tearDown() {
        deleteDir(new File(TEST_STORE));
        TTApp.storedir = originalStoreDir;
    }

    private int[] getScalePoints() {
        String env = System.getenv("BENCH_SCALES");
        if (env == null || env.isBlank()) return DEFAULT_SCALES;
        return Arrays.stream(env.split(","))
                .mapToInt(s -> Integer.parseInt(s.trim()))
                .toArray();
    }

    private String getOutputDir() {
        String env = System.getenv("BENCH_OUTPUT");
        return (env != null && !env.isBlank()) ? env : "docs/store_analysis";
    }

    @Test
    public void benchmarkAllStores() throws Exception {
        int[] scales = getScalePoints();
        String outputDir = getOutputDir();
        String targetStore = System.getenv("BENCH_STORE");

        List<String[]> results = new ArrayList<>();
        results.add(new String[]{"Store", "Operation", "N", "Total_ms", "Avg_ms", "P50_ms", "P95_ms", "P99_ms"});

        List<String[]> trialData = new ArrayList<>();
        trialData.add(new String[]{"Store", "Operation", "N", "Request", "Latency_us"});

        if (targetStore != null && !targetStore.isBlank()) {
            // Single-store mode (for parallel Docker containers)
            benchmarkStore(targetStore.toUpperCase(), scales, results, trialData);
        } else {
            // All stores
            for (String store : new String[]{"JSON", "PARQUET", "DUCKDB"}) {
                benchmarkStore(store, scales, results, trialData);
            }
            if (isRedisAvailable()) {
                benchmarkStore("REDIS", scales, results, trialData);
            } else {
                System.out.println("\n=== REDIS: skipped (server not available) ===");
            }
        }

        new File(outputDir).mkdirs();
        String suffix = (targetStore != null && !targetStore.isBlank())
                ? "_" + targetStore.toLowerCase() : "";
        writeCSV(outputDir + "/results" + suffix + ".csv", results);
        writeCSV(outputDir + "/trial_data" + suffix + ".csv", trialData);
        if (suffix.isEmpty()) {
            writeSummary(outputDir, scales, results);
        }
        writeSystemSpecs(outputDir);
        System.out.printf("%n  trial_data%s.csv: %d data points%n", suffix, trialData.size() - 1);
        System.out.println("Results written to " + outputDir + "/");
    }

    private void benchmarkStore(String storeName, int[] scales,
                                List<String[]> results, List<String[]> trialData) {
        System.out.println("\n=== Benchmarking: " + storeName + " ===");
        DAOFactory.Format fmt = DAOFactory.Format.valueOf(storeName);

        for (int n : scales) {
            System.out.printf("  --- N = %,d ---%n", n);
            if (storeName.equals("REDIS")) {
                DAOFactory.setFormat(fmt);
                flushRedis();
                benchmarkAtScale(storeName, n, results, trialData);
                flushRedis();
            } else if (storeName.equals("MONGO")) {
                DAOFactory.setFormat(fmt);
                flushMongo();
                benchmarkAtScale(storeName, n, results, trialData);
                flushMongo();
            } else {
                cleanStore();
                DAOFactory.setFormat(fmt);
                benchmarkAtScale(storeName, n, results, trialData);
            }
        }
    }

    private void benchmarkAtScale(String store, int n, List<String[]> results, List<String[]> trialData) {
        EntityDAO<Task> dao = DAOFactory.taskDAO();
        Random rand = new Random(42);
        int lookupCount = Math.max(10, n / 10);

        // --- CREATE N ---
        long[] createNanos = new long[n];
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            long id = System.currentTimeMillis() + i;
            ids.add(id);
            Task task = new Task(id, "Project" + (i % 10), "user" + (i % 20),
                    "Task #" + i, STATE.READY, new Date(), null, "Summary " + i);
            task.setEstimate("2h");
            long start = System.nanoTime();
            dao.save(task);
            createNanos[i] = System.nanoTime() - start;
        }
        record(results, trialData, store, "create", n, createNanos);

        // --- LOAD ALL (10 iterations) ---
        long[] loadAllNanos = new long[10];
        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            dao.loadAll();
            loadAllNanos[i] = System.nanoTime() - start;
        }
        record(results, trialData, store, "loadAll", n, loadAllNanos);

        // --- LOAD BY KEY ---
        long[] lookupNanos = new long[lookupCount];
        for (int i = 0; i < lookupCount; i++) {
            long id = ids.get(rand.nextInt(ids.size()));
            long start = System.nanoTime();
            dao.loadByKey(String.valueOf(id));
            lookupNanos[i] = System.nanoTime() - start;
        }
        record(results, trialData, store, "loadByKey", n, lookupNanos);

        // --- DELETE ---
        long[] deleteNanos = new long[lookupCount];
        for (int i = 0; i < lookupCount; i++) {
            long id = ids.get(i);
            long start = System.nanoTime();
            dao.deleteByKey(String.valueOf(id));
            deleteNanos[i] = System.nanoTime() - start;
        }
        record(results, trialData, store, "delete", n, deleteNanos);
    }

    private void record(List<String[]> results, List<String[]> trialData,
                        String store, String op, int n, long[] nanos) {
        long[] ms = new long[nanos.length];
        for (int i = 0; i < nanos.length; i++) ms[i] = nanos[i] / 1_000_000;
        Arrays.sort(ms);
        results.add(new String[]{
                store, op, String.valueOf(n),
                String.valueOf(sum(ms)),
                String.format("%.2f", avg(ms)),
                String.valueOf(pct(ms, 50)),
                String.valueOf(pct(ms, 95)),
                String.valueOf(pct(ms, 99))
        });

        for (int i = 0; i < nanos.length; i++) {
            trialData.add(new String[]{
                    store, op, String.valueOf(n),
                    String.valueOf(i + 1),
                    String.valueOf(nanos[i] / 1_000)
            });
        }

        System.out.printf("    %-10s avg=%.3fms%n", op, avg(ms));
    }

    // --- I/O ---

    private void writeCSV(String path, List<String[]> rows) throws Exception {
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            for (String[] row : rows) pw.println(String.join(",", row));
        }
    }

    private void writeSummary(String outputDir, int[] scales, List<String[]> rows) throws Exception {
        try (PrintWriter pw = new PrintWriter(new FileWriter(outputDir + "/README.md"))) {
            pw.println("# Store Benchmark Results");
            pw.println();
            pw.println("Scale points: " + Arrays.toString(scales));
            pw.println("MongoDB excluded (requires external server). Redis included if available.");
            pw.println();
            pw.println("| Store | Operation | N | Total (ms) | Avg (ms) | P50 (ms) | P95 (ms) | P99 (ms) |");
            pw.println("|-------|-----------|---|------------|----------|----------|----------|----------|");
            for (int i = 1; i < rows.size(); i++) {
                String[] r = rows.get(i);
                pw.printf("| %s | %s | %s | %s | %s | %s | %s | %s |%n",
                        r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7]);
            }
            pw.println();
            pw.println("*Generated by `StoreBenchmark.java`*");
        }
    }

    private void writeSystemSpecs(String outputDir) throws Exception {
        try (PrintWriter pw = new PrintWriter(new FileWriter(outputDir + "/system_specs.txt"))) {
            pw.println("=== System Specifications ===");
            pw.println("Date: " + new Date());
            pw.println();
            pw.printf("OS: %s %s (%s)%n",
                    System.getProperty("os.name"),
                    System.getProperty("os.version"),
                    System.getProperty("os.arch"));
            pw.printf("Java: %s (%s)%n",
                    System.getProperty("java.version"),
                    System.getProperty("java.vendor"));
            pw.printf("JVM: %s%n", System.getProperty("java.vm.name"));
            pw.printf("Available processors: %d%n", Runtime.getRuntime().availableProcessors());
            pw.printf("Max heap: %d MB%n", Runtime.getRuntime().maxMemory() / (1024 * 1024));
            pw.printf("Total memory: %d MB%n", Runtime.getRuntime().totalMemory() / (1024 * 1024));
            boolean inContainer = new File("/.dockerenv").exists()
                    || System.getenv("DOCKER_CONTAINER") != null;
            pw.printf("Container: %s%n", inContainer ? "Docker" : "bare-metal");
        }
    }

    // --- Math ---

    private double avg(long[] arr) {
        return arr.length == 0 ? 0 : (double) sum(arr) / arr.length;
    }

    private long sum(long[] arr) {
        long s = 0; for (long v : arr) s += v; return s;
    }

    private long pct(long[] sorted, int p) {
        if (sorted.length == 0) return 0;
        int idx = (int) Math.ceil(p / 100.0 * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(idx, sorted.length - 1))];
    }

    // --- Infra ---

    private boolean isRedisAvailable() {
        try {
            var client = task.trak.app.server.dao.redis.RedisConnection.getClient();
            client.ping();
            return true;
        } catch (Exception e) { return false; }
    }

    private void flushRedis() {
        try {
            var client = task.trak.app.server.dao.redis.RedisConnection.getClient();
            for (String key : client.keys("trak:tasks:*")) client.del(key);
        } catch (Exception ignored) {}
    }

    private void flushMongo() {
        try {
            var db = task.trak.app.server.dao.mongo.MongoConnection.getDatabase();
            db.getCollection("tasks").drop();
        } catch (Exception ignored) {}
    }

    private void cleanStore() {
        deleteDir(new File(TEST_STORE));
        new File(TEST_STORE).mkdirs();
    }

    private void deleteDir(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) for (File f : files) {
                if (f.isDirectory()) deleteDir(f); else f.delete();
            }
            dir.delete();
        }
    }
}
