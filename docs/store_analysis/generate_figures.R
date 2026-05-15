library(ggplot2)
library(dplyr)
library(scales)

df <- read.csv("docs/store_analysis/results.csv", stringsAsFactors = FALSE)

store_colors <- c(
  JSON    = "#00B8A9",
  DUCKDB  = "#6C63FF",
  PARQUET = "#FF9F1C",
  REDIS   = "#EF476F",
  MONGO   = "#7570B3"
)

store_order <- c("JSON", "DUCKDB", "PARQUET", "REDIS", "MONGO")

op_labels <- c(
  create    = "Create (1K ops)",
  loadAll   = "Load All (10 ops)",
  loadByKey = "Load by Key (100 ops)",
  delete    = "Delete (100 ops)"
)

theme_bench <- theme_bw(base_size = 12) +
  theme(
    plot.title = element_text(face = "bold", size = 14, margin = margin(b = 4)),
    plot.subtitle = element_text(color = "grey45", size = 10, margin = margin(b = 12)),
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
    legend.text = element_text(size = 9),
    plot.margin = margin(12, 16, 8, 12),
    plot.background = element_rect(fill = "white", color = NA)
  )

df <- df %>%
  mutate(
    Store     = factor(Store, levels = store_order),
    Operation = factor(Operation, levels = names(op_labels), labels = op_labels)
  )

p1 <- df %>%
  ggplot(aes(x = Operation, y = Avg_ms, fill = Store)) +
  geom_col(position = position_dodge(width = 0.75), width = 0.65) +
  geom_text(
    aes(label = ifelse(Avg_ms >= 0.01, sprintf("%.1f", Avg_ms), "<0.01")),
    position = position_dodge(width = 0.75),
    vjust = -0.5, size = 2.8, color = "grey25"
  ) +
  scale_fill_manual(values = store_colors) +
  scale_y_continuous(
    trans  = pseudo_log_trans(base = 10),
    breaks = c(0, 1, 10, 100),
    labels = label_number(drop0trailing = TRUE),
    expand = expansion(mult = c(0, 0.15))
  ) +
  labs(
    title = "Average Latency by Operation",
    subtitle = "Lower is better",
    x = NULL, y = "Avg latency (ms)"
  ) +
  theme_bench

ggsave("docs/store_analysis/avg_latency.png", p1,
       width = 8, height = 5, dpi = 300, bg = "white")

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
    vjust = -0.4, size = 2.5, color = "grey25"
  ) +
  facet_grid(Percentile ~ Operation, scales = "free_y", switch = "y") +
  scale_fill_manual(values = store_colors) +
  scale_y_continuous(expand = expansion(mult = c(0, 0.18))) +
  labs(
    title = "Tail Latency (P95 & P99)",
    subtitle = "Per-operation breakdown",
    x = NULL, y = "Latency (ms)"
  ) +
  theme_bench +
  theme(
    strip.text.x = element_text(face = "bold", size = 10, margin = margin(b = 6)),
    strip.text.y.left = element_text(face = "bold", size = 10, angle = 0),
    strip.background = element_rect(fill = "grey96", color = NA),
    strip.placement = "outside",
    panel.spacing.x = unit(1, "lines"),
    panel.spacing.y = unit(0.8, "lines"),
    axis.text.x = element_text(size = 8)
  )

ggsave("docs/store_analysis/tail_latency.png", p2,
       width = 10, height = 5.5, dpi = 300, bg = "white")

p3 <- df %>%
  ggplot(aes(x = Store, y = Total_ms, fill = Operation)) +
  geom_col(width = 0.55) +
  geom_text(
    aes(label = ifelse(Total_ms >= 100, paste0(format(Total_ms, big.mark = ","), " ms"), "")),
    position = position_stack(vjust = 0.5),
    size = 3, fontface = "bold", color = "white",
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
    title = "Total Elapsed Time by Store",
    subtitle = "Stacked by operation",
    x = NULL, y = "Total elapsed (ms)"
  ) +
  theme_bench +
  scale_y_continuous(
    labels = label_comma(),
    expand = expansion(mult = c(0, 0.05))
  )

ggsave("docs/store_analysis/total_time.png", p3,
       width = 7, height = 5, dpi = 300, bg = "white")

cat("Done\n")
