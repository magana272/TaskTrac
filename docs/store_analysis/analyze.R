library(ggplot2)
library(dplyr)
library(scales)

# ── Data ─────────────────────────────────────────────────────
trial <- read.csv("trial_data.csv", stringsAsFactors = FALSE) %>%
  mutate(
    Latency_ms = Latency_us / 1000,
    Store      = factor(Store, levels = c("JSON", "DUCKDB", "PARQUET", "REDIS"))
  )

summary_df <- read.csv("results.csv", stringsAsFactors = FALSE)

# ── Palette (Okabe-Ito, colorblind-safe) ─────────────────────
store_colors <- c(
  "JSON"    = "#0072B2",
  "DUCKDB"  = "#009E73",
  "PARQUET" = "#E69F00",
  "REDIS"   = "#D55E00"
)

# ── Theme ────────────────────────────────────────────────────
theme_bench <- theme_minimal(base_size = 12, base_family = "sans") +
  theme(
    plot.title          = element_text(face = "bold", size = 14, margin = margin(b = 4)),
    plot.subtitle       = element_text(color = "grey45", size = 10, margin = margin(b = 12)),
    plot.caption        = element_text(color = "grey55", size = 8, hjust = 0,
                                       margin = margin(t = 10)),
    plot.title.position = "plot",
    axis.title          = element_text(size = 10),
    axis.title.x        = element_text(margin = margin(t = 8)),
    axis.title.y        = element_text(margin = margin(r = 8)),
    axis.text           = element_text(color = "grey30", size = 9),
    panel.grid.major    = element_line(color = "grey92", linewidth = 0.3),
    panel.grid.minor    = element_blank(),
    legend.position     = "bottom",
    legend.title        = element_blank(),
    legend.text         = element_text(size = 9),
    legend.key.size     = unit(0.8, "lines"),
    plot.margin         = margin(t = 12, r = 16, b = 8, l = 12),
    plot.background     = element_rect(fill = "white", color = NA),
    panel.background    = element_rect(fill = "white", color = NA),
    axis.line           = element_line(color = "grey70", linewidth = 0.3)
  )

# ── Figure 1: Create — scatter + LOESS ──────────────────────
create_df <- trial %>% filter(Operation == "create")

p1 <- ggplot(create_df, aes(x = Request, y = Latency_ms, color = Store)) +
  geom_point(alpha = 0.15, size = 0.6, shape = 16) +
  geom_smooth(method = "loess", se = FALSE, linewidth = 1, span = 0.2) +
  scale_color_manual(values = store_colors) +
  scale_y_continuous(
    trans  = pseudo_log_trans(base = 10),
    breaks = c(0, 0.01, 0.1, 1, 10),
    labels = label_number(drop0trailing = TRUE)
  ) +
  labs(
    title    = "Create Latency Over 1,000 Sequential Inserts",
    subtitle = "Each point is one request \u2014 LOESS trend shows degradation patterns",
    caption  = "Y-axis: pseudo-log scale | Points: individual requests, Lines: LOESS smooth (span = 0.2)",
    x = "Request number", y = "Latency (ms)"
  ) +
  theme_bench

ggsave("create_scatter.png", p1, width = 10, height = 5.5, dpi = 300, bg = "white")

# ── Figure 2: Load by Key — scatter + LOESS ─────────────────
lookup_df <- trial %>% filter(Operation == "loadByKey")

p2 <- ggplot(lookup_df, aes(x = Request, y = Latency_ms, color = Store)) +
  geom_point(alpha = 0.3, size = 1.2, shape = 16) +
  geom_smooth(method = "loess", se = FALSE, linewidth = 1, span = 0.3) +
  scale_color_manual(values = store_colors) +
  scale_y_continuous(
    trans  = pseudo_log_trans(base = 10),
    breaks = c(0, 0.01, 0.1, 1, 10),
    labels = label_number(drop0trailing = TRUE)
  ) +
  labs(
    title    = "Point Lookup Latency Over 100 Random Reads",
    subtitle = "loadByKey against 1,000 stored tasks \u2014 tests key-based retrieval",
    caption  = "Y-axis: pseudo-log scale | Reads are random-order by key",
    x = "Request number", y = "Latency (ms)"
  ) +
  theme_bench

ggsave("lookup_scatter.png", p2, width = 10, height = 5.5, dpi = 300, bg = "white")

# ── Figure 3: Delete — scatter + LOESS ──────────────────────
delete_df <- trial %>% filter(Operation == "delete")

p3 <- ggplot(delete_df, aes(x = Request, y = Latency_ms, color = Store)) +
  geom_point(alpha = 0.3, size = 1.2, shape = 16) +
  geom_smooth(method = "loess", se = FALSE, linewidth = 1, span = 0.3) +
  scale_color_manual(values = store_colors) +
  scale_y_continuous(
    trans  = pseudo_log_trans(base = 10),
    breaks = c(0, 0.01, 0.1, 1, 10),
    labels = label_number(drop0trailing = TRUE)
  ) +
  labs(
    title    = "Delete Latency Over 100 Sequential Deletes",
    subtitle = "Deleting first 100 of 1,000 tasks \u2014 reveals compaction or rewrite cost",
    caption  = "Y-axis: pseudo-log scale",
    x = "Request number", y = "Latency (ms)"
  ) +
  theme_bench

ggsave("delete_scatter.png", p3, width = 10, height = 5.5, dpi = 300, bg = "white")

# ── Figure 4: Load All — scatter ────────────────────────────
loadall_df <- trial %>% filter(Operation == "loadAll")

p4 <- ggplot(loadall_df, aes(x = Request, y = Latency_ms, color = Store)) +
  geom_point(size = 2.5, shape = 16) +
  geom_line(linewidth = 0.6, alpha = 0.6) +
  scale_color_manual(values = store_colors) +
  labs(
    title    = "Load All Latency Over 10 Full Scans",
    subtitle = "Full table scan of 1,000 tasks \u2014 shows caching and I/O patterns",
    caption  = "10 iterations | Linear y-axis (all values are in ms range)",
    x = "Iteration", y = "Latency (ms)"
  ) +
  theme_bench

ggsave("loadall_scatter.png", p4, width = 10, height = 5.5, dpi = 300, bg = "white")

# ── Figure 5: All operations combined — faceted scatter ─────
op_labels <- c(
  create    = "Create (1K ops)",
  loadAll   = "Load All (10 ops)",
  loadByKey = "Lookup (100 ops)",
  delete    = "Delete (100 ops)"
)

combined_df <- trial %>%
  mutate(OpLabel = factor(Operation, levels = names(op_labels), labels = op_labels))

p5 <- ggplot(combined_df, aes(x = Request, y = Latency_ms, color = Store)) +
  geom_point(alpha = 0.2, size = 0.5, shape = 16) +
  geom_smooth(method = "loess", se = FALSE, linewidth = 0.8, span = 0.3) +
  facet_wrap(~ OpLabel, scales = "free", nrow = 2) +
  scale_color_manual(values = store_colors) +
  scale_y_continuous(
    trans  = pseudo_log_trans(base = 10),
    breaks = c(0, 0.01, 0.1, 1, 10, 100),
    labels = label_number(drop0trailing = TRUE)
  ) +
  labs(
    title    = "Per-Request Latency Across All Operations",
    subtitle = "Scatter with LOESS trend \u2014 each point is one request, colored by store",
    caption  = "Y-axis: pseudo-log scale | Panels have independent x/y ranges",
    x = "Request number", y = "Latency (ms)"
  ) +
  theme_bench +
  theme(
    strip.text       = element_text(face = "bold", size = 10),
    strip.background = element_rect(fill = "grey96", color = NA),
    panel.spacing    = unit(1, "lines")
  )

ggsave("combined_scatter.png", p5, width = 10, height = 8, dpi = 300, bg = "white")

cat("Done — 5 figures generated\n")
