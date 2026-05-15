library(ggplot2)
library(dplyr)
library(scales)

# ── Data ─────────────────────────────────────────────────────
trial <- read.csv("trial_data.csv", stringsAsFactors = FALSE) %>%
  mutate(
    Latency_ms = Latency_us / 1000,
    Store      = factor(Store, levels = c("JSON", "DUCKDB", "PARQUET", "REDIS"))
  )

agg <- trial %>%
  group_by(Store, Operation, N) %>%
  summarise(
    Avg_ms    = mean(Latency_ms),
    Median_ms = median(Latency_ms),
    SD_ms     = sd(Latency_ms),
    Q25_ms    = quantile(Latency_ms, 0.25),
    Q75_ms    = quantile(Latency_ms, 0.75),
    P5_ms     = quantile(Latency_ms, 0.05),
    P95_ms    = quantile(Latency_ms, 0.95),
    Total_ms  = sum(Latency_ms),
    .groups   = "drop"
  )

# ── Palette (Okabe-Ito) ─────────────────────────────────────
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
    legend.text         = element_text(size = 10),
    legend.key.size     = unit(1.2, "lines"),
    plot.margin         = margin(t = 12, r = 16, b = 8, l = 12),
    plot.background     = element_rect(fill = "white", color = NA),
    panel.background    = element_rect(fill = "white", color = NA),
    axis.line           = element_line(color = "grey70", linewidth = 0.3)
  )

# Detect scale points from data
all_n <- sort(unique(agg$N))
x_breaks <- all_n
x_labels <- ifelse(all_n >= 1000, paste0(all_n / 1000, "K"), as.character(all_n))

# ── Helper: build a scaling plot with error bars ─────────────
make_scaling_plot <- function(data, title, subtitle, caption, ylab,
                              y_breaks = c(0, 0.01, 0.1, 1, 10, 100, 1000)) {
  ggplot(data, aes(x = N, y = Avg_ms, color = Store, fill = Store)) +
    # IQR ribbon (25th–75th percentile)
    geom_ribbon(aes(ymin = Q25_ms, ymax = Q75_ms), alpha = 0.15, color = NA) +
    # P5–P95 whiskers
    geom_errorbar(aes(ymin = P5_ms, ymax = P95_ms), width = 0.06, linewidth = 0.4, alpha = 0.5) +
    # Line + points
    geom_line(linewidth = 0.9) +
    geom_point(size = 2.5, shape = 21, stroke = 0.6) +
    scale_color_manual(values = store_colors) +
    scale_fill_manual(values = store_colors) +
    scale_x_log10(breaks = x_breaks, labels = x_labels) +
    scale_y_continuous(
      trans  = pseudo_log_trans(base = 10),
      breaks = y_breaks,
      labels = label_number(drop0trailing = TRUE)
    ) +
    labs(title = title, subtitle = subtitle, caption = caption,
         x = "Number of requests (N)", y = ylab) +
    theme_bench
}

# ============================================================
# Figure 1: Create scaling
# ============================================================
p1 <- make_scaling_plot(
  agg %>% filter(Operation == "create"),
  title    = "Create \u2014 Average Latency vs Request Count",
  subtitle = "Per-insert cost as dataset grows | Shaded band = IQR, whiskers = P5\u2013P95",
  caption  = "Y-axis: pseudo-log scale | X-axis: log scale",
  ylab     = "Avg latency per request (ms)"
)
ggsave("create_scaling.png", p1, width = 10, height = 5.5, dpi = 300, bg = "white")

# ============================================================
# Figure 2: Load All scaling
# ============================================================
p2 <- make_scaling_plot(
  agg %>% filter(Operation == "loadAll"),
  title    = "Load All \u2014 Average Latency vs Dataset Size",
  subtitle = "Full table scan of N tasks (10 iterations) | Shaded band = IQR, whiskers = P5\u2013P95",
  caption  = "Y-axis: pseudo-log scale | X-axis: log scale",
  ylab     = "Avg latency per scan (ms)",
  y_breaks = c(0, 1, 10, 100, 1000, 10000)
)
ggsave("loadall_scaling.png", p2, width = 10, height = 5.5, dpi = 300, bg = "white")

# ============================================================
# Figure 3: Point Lookup scaling
# ============================================================
p3 <- make_scaling_plot(
  agg %>% filter(Operation == "loadByKey"),
  title    = "Point Lookup \u2014 Average Latency vs Dataset Size",
  subtitle = "Random key lookups (N/10) | Shaded band = IQR, whiskers = P5\u2013P95",
  caption  = "Y-axis: pseudo-log scale | X-axis: log scale",
  ylab     = "Avg latency per lookup (ms)"
)
ggsave("lookup_scaling.png", p3, width = 10, height = 5.5, dpi = 300, bg = "white")

# ============================================================
# Figure 4: Delete scaling
# ============================================================
p4 <- make_scaling_plot(
  agg %>% filter(Operation == "delete"),
  title    = "Delete \u2014 Average Latency vs Dataset Size",
  subtitle = "Sequential deletes (N/10) | Shaded band = IQR, whiskers = P5\u2013P95",
  caption  = "Y-axis: pseudo-log scale | X-axis: log scale",
  ylab     = "Avg latency per delete (ms)"
)
ggsave("delete_scaling.png", p4, width = 10, height = 5.5, dpi = 300, bg = "white")

# ============================================================
# Figure 5: Combined faceted
# ============================================================
op_labels <- c(
  create    = "Create",
  loadAll   = "Load All",
  loadByKey = "Lookup",
  delete    = "Delete"
)

agg_combined <- agg %>%
  mutate(OpLabel = factor(Operation, levels = names(op_labels), labels = op_labels))

p5 <- ggplot(agg_combined, aes(x = N, y = Avg_ms, color = Store, fill = Store)) +
  geom_ribbon(aes(ymin = Q25_ms, ymax = Q75_ms), alpha = 0.15, color = NA) +
  geom_errorbar(aes(ymin = P5_ms, ymax = P95_ms), width = 0.06, linewidth = 0.3, alpha = 0.4) +
  geom_line(linewidth = 0.9) +
  geom_point(size = 2, shape = 21, stroke = 0.5) +
  facet_wrap(~ OpLabel, scales = "free_y", nrow = 2) +
  scale_color_manual(values = store_colors) +
  scale_fill_manual(values = store_colors) +
  scale_x_log10(breaks = x_breaks, labels = x_labels) +
  scale_y_continuous(
    trans  = pseudo_log_trans(base = 10),
    breaks = c(0, 0.01, 0.1, 1, 10, 100, 1000, 10000),
    labels = label_number(drop0trailing = TRUE)
  ) +
  labs(
    title    = "Per-Request Latency Scaling \u2014 All Operations",
    subtitle = "Shaded band = IQR (25th\u201375th), whiskers = P5\u2013P95",
    caption  = "Both axes: log scale | Each panel has independent y-range",
    x = "Number of requests (N)", y = "Avg latency (ms)"
  ) +
  theme_bench +
  theme(
    strip.text       = element_text(face = "bold", size = 11),
    strip.background = element_rect(fill = "grey96", color = NA),
    panel.spacing    = unit(1.2, "lines")
  )

ggsave("combined_scaling.png", p5, width = 10, height = 8, dpi = 300, bg = "white")

# ============================================================
# Figure 6: Total elapsed time for creates
# ============================================================
total_agg <- agg %>%
  filter(Operation == "create") %>%
  select(Store, N, Total_ms)

p6 <- ggplot(total_agg, aes(x = N, y = Total_ms, color = Store)) +
  geom_line(linewidth = 0.9) +
  geom_point(size = 3, shape = 16) +
  scale_color_manual(values = store_colors) +
  scale_x_log10(breaks = x_breaks, labels = x_labels) +
  scale_y_continuous(labels = label_comma()) +
  labs(
    title    = "Total Create Time vs Request Count",
    subtitle = "Wall-clock time to insert N tasks \u2014 shows absolute cost",
    caption  = "X-axis: log scale | Linear y-axis (total milliseconds)",
    x = "Number of requests (N)", y = "Total time (ms)"
  ) +
  theme_bench

ggsave("total_scaling.png", p6, width = 10, height = 5.5, dpi = 300, bg = "white")

cat("Done \u2014 6 figures generated\n")
