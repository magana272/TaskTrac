library(ggplot2)
library(dplyr)
library(scales)

df <- read.csv("docs/store_analysis/results.csv", stringsAsFactors = FALSE)

# --- Okabe-Ito palette (colorblind-safe, publication standard) ---
store_colors <- c(
  "JSON"    = "#0072B2",
  "PARQUET" = "#E69F00",
  "DUCKDB"  = "#009E73",
  "REDIS"   = "#D55E00"
)

store_order <- c("JSON", "DUCKDB", "PARQUET", "REDIS")

op_labels <- c(
  create    = "Create (1K ops)",
  loadAll   = "Load All (10 ops)",
  loadByKey = "Load by Key (100 ops)",
  delete    = "Delete (100 ops)"
)

# --- Reusable publication theme ---
theme_bench <- theme_minimal(base_size = 12, base_family = "sans") +
  theme(
    plot.title          = element_text(face = "bold", size = 14, margin = margin(b = 4)),
    plot.subtitle       = element_text(color = "grey45", size = 10, margin = margin(b = 12)),
    plot.caption        = element_text(color = "grey55", size = 8, hjust = 0,
                                       margin = margin(t = 10)),
    plot.title.position = "plot",
    axis.title.x        = element_text(size = 10, margin = margin(t = 8)),
    axis.title.y        = element_text(size = 10, margin = margin(r = 8)),
    axis.text           = element_text(color = "grey30", size = 9),
    panel.grid.major.x  = element_blank(),
    panel.grid.minor    = element_blank(),
    panel.grid.major.y  = element_line(color = "grey90", linewidth = 0.3),
    legend.position     = "bottom",
    legend.title        = element_blank(),
    legend.text         = element_text(size = 9),
    legend.key.size     = unit(0.8, "lines"),
    legend.margin       = margin(t = 4),
    plot.margin         = margin(t = 12, r = 16, b = 8, l = 12),
    plot.background     = element_rect(fill = "white", color = NA),
    panel.background    = element_rect(fill = "white", color = NA),
    axis.line.x         = element_line(color = "grey70", linewidth = 0.3)
  )

# Prepare base data frame
df <- df %>%
  mutate(
    Store     = factor(Store, levels = store_order),
    Operation = factor(Operation, levels = names(op_labels), labels = op_labels)
  )

# ============================================================
# Figure 1: Average latency — pseudo-log scale grouped bar
# ============================================================
p1 <- df %>%
  ggplot(aes(x = Operation, y = Avg_ms, fill = Store)) +
  geom_col(position = position_dodge(width = 0.75), width = 0.65) +
  geom_text(
    aes(label = ifelse(Avg_ms >= 0.01, sprintf("%.1f", Avg_ms), "<0.01")),
    position = position_dodge(width = 0.75),
    vjust = -0.5, size = 2.8, color = "grey25", family = "sans"
  ) +
  scale_fill_manual(values = store_colors) +
  scale_y_continuous(
    trans  = pseudo_log_trans(base = 10),
    breaks = c(0, 1, 10, 100),
    labels = label_number(drop0trailing = TRUE),
    expand = expansion(mult = c(0, 0.15))
  ) +
  labs(
    title    = "Average Latency by Operation",
    subtitle = "Lower is better \u2014 milliseconds per operation",
    caption  = "Y-axis: pseudo-log scale (linear near zero, logarithmic for large values)",
    x = NULL, y = "Avg latency (ms)"
  ) +
  theme_bench

ggsave("docs/store_analysis/avg_latency.png", p1,
       width = 8, height = 5, dpi = 300, bg = "white")

# ============================================================
# Figure 2: Tail latency — P95 & P99 faceted
# ============================================================
tail_df <- rbind(
  df %>% mutate(Percentile = "P95", Latency = P95_ms) %>%
    select(Store, Operation, Percentile, Latency),
  df %>% mutate(Percentile = "P99", Latency = P99_ms) %>%
    select(Store, Operation, Percentile, Latency)
) %>%
  mutate(Percentile = factor(Percentile, levels = c("P95", "P99")))

p2 <- tail_df %>%
  ggplot(aes(x = Store, y = Latency, fill = Store)) +
  geom_col(width = 0.55) +
  geom_text(
    aes(label = ifelse(Latency > 0, sprintf("%.0f", Latency), "")),
    vjust = -0.4, size = 2.5, color = "grey25", family = "sans"
  ) +
  facet_grid(Percentile ~ Operation, scales = "free_y", switch = "y") +
  scale_fill_manual(values = store_colors) +
  scale_y_continuous(expand = expansion(mult = c(0, 0.18))) +
  labs(
    title    = "Tail Latency \u2014 P95 & P99",
    subtitle = "Per-operation breakdown across storage backends",
    caption  = "Bars with zero latency omitted for clarity",
    x = NULL, y = "Latency (ms)"
  ) +
  theme_bench +
  theme(
    strip.text.x      = element_text(face = "bold", size = 10, margin = margin(b = 6)),
    strip.text.y.left  = element_text(face = "bold", size = 10, angle = 0),
    strip.background   = element_rect(fill = "grey96", color = NA),
    strip.placement    = "outside",
    panel.spacing.x    = unit(1, "lines"),
    panel.spacing.y    = unit(0.8, "lines"),
    axis.text.x        = element_text(size = 8)
  )

ggsave("docs/store_analysis/tail_latency.png", p2,
       width = 10, height = 5.5, dpi = 300, bg = "white")

# ============================================================
# Figure 3: Total time stacked bar
# ============================================================
p3 <- df %>%
  ggplot(aes(x = Store, y = Total_ms, fill = Operation)) +
  geom_col(width = 0.55) +
  geom_text(
    aes(
      label = ifelse(Total_ms >= 100, paste0(format(Total_ms, big.mark = ","), " ms"), "")
    ),
    position  = position_stack(vjust = 0.5),
    size      = 3, fontface = "bold", color = "white", family = "sans",
    show.legend = FALSE
  ) +
  scale_fill_manual(
    values = c(
      "Create (1K ops)"       = "#66C2A5",
      "Load All (10 ops)"     = "#FC8D62",
      "Load by Key (100 ops)" = "#8DA0CB",
      "Delete (100 ops)"      = "#E78AC3"
    )
  ) +
  labs(
    title    = "Total Elapsed Time by Store",
    subtitle = "Stacked by operation \u2014 shows where each backend spends time",
    caption  = "Labels shown for segments \u2265 100 ms",
    x = NULL, y = "Total elapsed (ms)"
  ) +
  theme_bench +
  scale_y_continuous(
    labels = label_comma(),
    expand = expansion(mult = c(0, 0.05))
  )

ggsave("docs/store_analysis/total_time.png", p3,
       width = 7, height = 5, dpi = 300, bg = "white")

cat("Done — 3 figures saved to docs/store_analysis/\n")
