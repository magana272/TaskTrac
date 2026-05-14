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

/**
 * Benchmarks CRUD operations across all embedded storage backends.
 * Skips MongoDB and Redis (require external servers).
 * Results written to docs/store_analysis/results.csv and stdout.
 */
public class StoreBenchmark {

    private static final int CREATE_COUNT = 1000;
    private static final int LOOKUP_COUNT = 100;
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

    @Test
    public void benchmarkAllStores() throws Exception {
        DAOFactory.Format[] embeddedFormats = {
                DAOFactory.Format.JSON,
                DAOFactory.Format.PARQUET,
                DAOFactory.Format.DUCKDB
        };

        List<String[]> results = new ArrayList<>();
        results.add(new String[]{"Store", "Operation", "Count", "Total_ms", "Avg_ms", "P50_ms", "P95_ms", "P99_ms"});

        for (DAOFactory.Format fmt : embeddedFormats) {
            System.out.println("\n=== Benchmarking: " + fmt + " ===");
            cleanStore();
            DAOFactory.setFormat(fmt);
            results.addAll(benchmarkFormat(fmt.name()));
        }

        // Redis — skip if server not available
        if (isRedisAvailable()) {
            System.out.println("\n=== Benchmarking: REDIS ===");
            DAOFactory.setFormat(DAOFactory.Format.REDIS);
            // Flush test keys before benchmark
            try {
                var client = task.trak.app.server.dao.redis.RedisConnection.getClient();
                for (String key : client.keys("trak:tasks:*")) client.del(key);
            } catch (Exception ignored) {}
            results.addAll(benchmarkFormat("REDIS"));
            // Cleanup
            try {
                var client = task.trak.app.server.dao.redis.RedisConnection.getClient();
                for (String key : client.keys("trak:tasks:*")) client.del(key);
            } catch (Exception ignored) {}
        } else {
            System.out.println("\n=== REDIS: skipped (server not available) ===");
        }

        writeCSV(results);
        writeSummary(results);
        System.out.println("\nResults written to docs/store_analysis/");
    }

    private List<String[]> benchmarkFormat(String storeName) {
        List<String[]> rows = new ArrayList<>();
        EntityDAO<Task> dao = DAOFactory.taskDAO();
        Random rand = new Random(42);

        // --- CREATE ---
        long[] createTimes = new long[CREATE_COUNT];
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < CREATE_COUNT; i++) {
            long id = System.currentTimeMillis() + i;
            ids.add(id);
            Task task = new Task(id, "Project" + (i % 10), "user" + (i % 20),
                    "Task #" + i, STATE.READY, new Date(), null, "Summary " + i);
            task.setEstimate("2h");
            long start = System.nanoTime();
            dao.save(task);
            createTimes[i] = (System.nanoTime() - start) / 1_000_000;
        }
        rows.add(formatRow(storeName, "create", CREATE_COUNT, createTimes));
        System.out.printf("  create %d: avg=%.2fms%n", CREATE_COUNT, avg(createTimes));

        // --- LOAD ALL ---
        long[] loadAllTimes = new long[10];
        for (int i = 0; i < 10; i++) {
            long start = System.nanoTime();
            dao.loadAll();
            loadAllTimes[i] = (System.nanoTime() - start) / 1_000_000;
        }
        rows.add(formatRow(storeName, "loadAll", 10, loadAllTimes));
        System.out.printf("  loadAll (10x): avg=%.2fms%n", avg(loadAllTimes));

        // --- LOAD BY KEY ---
        long[] lookupTimes = new long[LOOKUP_COUNT];
        for (int i = 0; i < LOOKUP_COUNT; i++) {
            long id = ids.get(rand.nextInt(ids.size()));
            long start = System.nanoTime();
            dao.loadByKey(String.valueOf(id));
            lookupTimes[i] = (System.nanoTime() - start) / 1_000_000;
        }
        rows.add(formatRow(storeName, "loadByKey", LOOKUP_COUNT, lookupTimes));
        System.out.printf("  loadByKey (100x): avg=%.2fms%n", avg(lookupTimes));

        // --- DELETE ---
        long[] deleteTimes = new long[LOOKUP_COUNT];
        for (int i = 0; i < LOOKUP_COUNT; i++) {
            long id = ids.get(i);
            long start = System.nanoTime();
            dao.deleteByKey(String.valueOf(id));
            deleteTimes[i] = (System.nanoTime() - start) / 1_000_000;
        }
        rows.add(formatRow(storeName, "delete", LOOKUP_COUNT, deleteTimes));
        System.out.printf("  delete (100x): avg=%.2fms%n", avg(deleteTimes));

        return rows;
    }

    private String[] formatRow(String store, String op, int count, long[] times) {
        Arrays.sort(times);
        return new String[]{
                store, op, String.valueOf(count),
                String.valueOf(sum(times)),
                String.format("%.2f", avg(times)),
                String.valueOf(percentile(times, 50)),
                String.valueOf(percentile(times, 95)),
                String.valueOf(percentile(times, 99))
        };
    }

    private void writeCSV(List<String[]> rows) throws Exception {
        File dir = new File("docs/store_analysis");
        dir.mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter("docs/store_analysis/results.csv"))) {
            for (String[] row : rows) {
                pw.println(String.join(",", row));
            }
        }
    }

    private void writeSummary(List<String[]> rows) throws Exception {
        try (PrintWriter pw = new PrintWriter(new FileWriter("docs/store_analysis/README.md"))) {
            pw.println("# Store Benchmark Results");
            pw.println();
            pw.println("Benchmarked " + CREATE_COUNT + " task CRUD operations across all available stores.");
            pw.println("MongoDB excluded (requires external server). Redis included if available.");
            pw.println();
            pw.println("| Store | Operation | Count | Total (ms) | Avg (ms) | P50 (ms) | P95 (ms) | P99 (ms) |");
            pw.println("|-------|-----------|-------|------------|----------|----------|----------|----------|");
            for (int i = 1; i < rows.size(); i++) {
                String[] r = rows.get(i);
                pw.printf("| %s | %s | %s | %s | %s | %s | %s | %s |%n",
                        r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7]);
            }
            pw.println();
            pw.println("*Generated by `StoreBenchmark.java`*");
        }
    }

    private double avg(long[] arr) {
        return arr.length == 0 ? 0 : (double) sum(arr) / arr.length;
    }

    private long sum(long[] arr) {
        long s = 0;
        for (long v : arr) s += v;
        return s;
    }

    private long percentile(long[] sorted, int p) {
        if (sorted.length == 0) return 0;
        int idx = (int) Math.ceil(p / 100.0 * sorted.length) - 1;
        return sorted[Math.max(0, Math.min(idx, sorted.length - 1))];
    }

    private boolean isRedisAvailable() {
        try {
            var client = task.trak.app.server.dao.redis.RedisConnection.getClient();
            client.ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void cleanStore() {
        deleteDir(new File(TEST_STORE));
        new File(TEST_STORE).mkdirs();
    }

    private void deleteDir(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) for (File f : files) {
                if (f.isDirectory()) deleteDir(f);
                else f.delete();
            }
            dir.delete();
        }
    }
}
