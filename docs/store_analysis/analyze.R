library(ggplot2)
library(dplyr)
library(scales)

store_levels <- c("JSON", "DUCKDB", "PARQUET", "REDIS", "MONGO")

store_colors <- c(
  JSON    = "#00B8A9",
  DUCKDB  = "#6C63FF",
  PARQUET = "#FF9F1C",
  REDIS   = "#EF476F",
  MONGO   = "#7570B3"
)

trial <- read.csv("trial_data.csv", stringsAsFactors = FALSE) %>%
  mutate(
    Latency_ms = pmax(Latency_us / 1000, 0.01),
    Store = factor(Store, levels = store_levels)
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

all_n <- sort(unique(agg$N))

log_label <- function(x) {
  sapply(x, function(v) {
    if (is.na(v) || v <= 0) return("")
    p <- log10(v)
    if (abs(p - round(p)) < 1e-6) {
      as.expression(bquote(10^.(as.integer(round(p)))))
    } else {
      label_number(big.mark = ",")(v)
    }
  })
}

scale_x_bench <- scale_x_log10(
  breaks = all_n,
  labels = log_label
)

scale_y_bench <- function(max_y) {
  upper <- max(1, ceiling(log10(max(max_y, 1))))
  scale_y_log10(
    breaks = 10^seq(-2, upper),
    labels = log_label
  )
}

theme_bench <- theme_bw(base_size = 12) +
  theme(
    plot.title = element_text(face = "bold", size = 14, margin = margin(b = 4)),
    plot.subtitle = element_text(size = 10, color = "grey45", margin = margin(b = 12)),
    plot.title.position = "plot",
    axis.title = element_text(size = 10),
    axis.text = element_text(size = 9, color = "grey30"),
    axis.title.x = element_text(margin = margin(t = 8)),
    axis.title.y = element_text(margin = margin(r = 8)),
    axis.line = element_line(linewidth = 0.4),
    panel.grid.major = element_line(color = "grey85", linewidth = 0.3),
    panel.grid.minor = element_line(color = "grey92", linewidth = 0.2),
    panel.border = element_rect(fill = NA, linewidth = 0.4),
    legend.position = "bottom",
    legend.title = element_blank(),
    legend.text = element_text(size = 10),
    plot.margin = margin(12, 16, 8, 12),
    plot.background = element_rect(fill = "white", color = NA)
  )

make_plot <- function(data, title, subtitle, ylab) {
  ggplot(data, aes(N, Avg_ms, color = Store, fill = Store)) +
    geom_ribbon(
      aes(ymin = Q25_ms, ymax = Q75_ms),
      alpha = 0.15,
      color = NA
    ) +
    geom_errorbar(
      aes(ymin = P5_ms, ymax = P95_ms),
      width = 0.06,
      linewidth = 0.4,
      alpha = 0.5
    ) +
    geom_line(linewidth = 0.9) +
    geom_point(size = 2.5, shape = 21, stroke = 0.6) +
    scale_color_manual(values = store_colors) +
    scale_fill_manual(values = store_colors) +
    scale_x_bench +
    scale_y_bench(max(data$P95_ms, na.rm = TRUE)) +
    labs(
      title = title,
      subtitle = subtitle,
      x = "N",
      y = ylab
    ) +
    theme_bench
}

plots <- list(
  list(
    op = "create",
    title = "Create",
    subtitle = "Per-insert latency | Band = IQR, whiskers = P5-P95",
    file = "create_scaling.png"
  ),
  list(
    op = "loadAll",
    title = "Load All",
    subtitle = "Full scan of N tasks (10 iterations) | Band = IQR, whiskers = P5-P95",
    file = "loadall_scaling.png"
  ),
  list(
    op = "loadByKey",
    title = "Point Lookup",
    subtitle = "Random key lookups (N/10) | Band = IQR, whiskers = P5-P95",
    file = "lookup_scaling.png"
  ),
  list(
    op = "delete",
    title = "Delete",
    subtitle = "Sequential deletes (N/10) | Band = IQR, whiskers = P5-P95",
    file = "delete_scaling.png"
  )
)

for (cfg in plots) {
  p <- make_plot(
    agg %>% filter(Operation == cfg$op),
    cfg$title,
    cfg$subtitle,
    "Avg latency (ms)"
  )

  ggsave(
    cfg$file,
    p,
    width = 10,
    height = 5.5,
    dpi = 300,
    bg = "white"
  )
}

op_labels <- c(
  create = "Create",
  loadAll = "Load All",
  loadByKey = "Lookup",
  delete = "Delete"
)

agg_combined <- agg %>%
  mutate(
    OpLabel = factor(
      Operation,
      levels = names(op_labels),
      labels = op_labels
    )
  )

combined_max <- max(agg_combined$P95_ms, na.rm = TRUE)

p_combined <- ggplot(
  agg_combined,
  aes(N, Avg_ms, color = Store, fill = Store)
) +
  geom_ribbon(
    aes(ymin = Q25_ms, ymax = Q75_ms),
    alpha = 0.15,
    color = NA
  ) +
  geom_errorbar(
    aes(ymin = P5_ms, ymax = P95_ms),
    width = 0.06,
    linewidth = 0.3,
    alpha = 0.4
  ) +
  geom_line(linewidth = 0.9) +
  geom_point(size = 2, shape = 21, stroke = 0.5) +
  facet_wrap(~OpLabel, nrow = 2) +
  scale_color_manual(values = store_colors) +
  scale_fill_manual(values = store_colors) +
  scale_x_bench +
  scale_y_bench(combined_max) +
  labs(
    title = "Per-Request Latency Scaling",
    subtitle = "Band = IQR, whiskers = P5-P95",
    x = "N",
    y = "Avg latency (ms)"
  ) +
  theme_bench +
  theme(
    strip.text = element_text(face = "bold", size = 11),
    strip.background = element_rect(fill = "grey96", color = NA),
    panel.spacing = unit(1.2, "lines")
  )

ggsave(
  "combined_scaling.png",
  p_combined,
  width = 10,
  height = 8,
  dpi = 300,
  bg = "white"
)

cat("Done\n")
