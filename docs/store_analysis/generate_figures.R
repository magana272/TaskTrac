library(ggplot2)
library(dplyr)
library(scales)

df <- read.csv("docs/store_analysis/results.csv", stringsAsFactors = FALSE)

# Consistent store colors and theme
store_colors <- c(
  "JSON"    = "#2196F3",
  "PARQUET" = "#FF9800",
  "DUCKDB"  = "#4CAF50",
  "REDIS"   = "#F44336"
)

theme_bench <- theme_minimal(base_size = 13) +
  theme(
    plot.title       = element_text(face = "bold", size = 15),
    plot.subtitle    = element_text(color = "grey40", size = 11),
    panel.grid.minor = element_blank(),
    legend.position  = "bottom",
    legend.title     = element_blank(),
    plot.background  = element_rect(fill = "white", color = NA)
  )

op_labels <- c(
  create    = "Create (1000)",
  loadAll   = "Load All (10)",
  loadByKey = "Load by Key (100)",
  delete    = "Delete (100)"
)

# --- Figure 1: Average latency per operation (grouped bar) ---
p1 <- df %>%
  mutate(Operation = factor(Operation, levels = names(op_labels), labels = op_labels)) %>%
  ggplot(aes(x = Operation, y = Avg_ms, fill = Store)) +
  geom_col(position = position_dodge(width = 0.75), width = 0.65) +
  geom_text(
    aes(label = ifelse(Avg_ms >= 0.5, sprintf("%.1f", Avg_ms), "")),
    position = position_dodge(width = 0.75),
    vjust = -0.4, size = 3
  ) +
  scale_fill_manual(values = store_colors) +
  labs(
    title    = "Average Latency by Operation",
    subtitle = "Lower is better — milliseconds per operation",
    x = NULL, y = "Avg (ms)"
  ) +
  theme_bench +
  scale_y_continuous(expand = expansion(mult = c(0, 0.15)))

ggsave("docs/store_analysis/avg_latency.png", p1, width = 8, height = 5, dpi = 150)

# --- Figure 2: P95 vs P99 tail latency (faceted) ---
tail_df <- rbind(
  df %>% mutate(Percentile = "P95", Latency = P95_ms) %>% select(Store, Operation, Percentile, Latency),
  df %>% mutate(Percentile = "P99", Latency = P99_ms) %>% select(Store, Operation, Percentile, Latency)
) %>% mutate(Operation = factor(Operation, levels = names(op_labels), labels = op_labels))

p2 <- tail_df %>%
  ggplot(aes(x = Store, y = Latency, fill = Store)) +
  geom_col(width = 0.6) +
  facet_grid(Percentile ~ Operation, scales = "free_y") +
  scale_fill_manual(values = store_colors) +
  labs(
    title    = "Tail Latency — P95 & P99",
    subtitle = "Per-operation breakdown across storage backends",
    x = NULL, y = "Latency (ms)"
  ) +
  theme_bench +
  theme(
    axis.text.x  = element_text(angle = 45, hjust = 1, size = 9),
    strip.text    = element_text(face = "bold")
  )

ggsave("docs/store_analysis/tail_latency.png", p2, width = 10, height = 5.5, dpi = 150)

# --- Figure 3: Total time per operation (stacked bar per store) ---
p3 <- df %>%
  mutate(Operation = factor(Operation, levels = names(op_labels), labels = op_labels)) %>%
  ggplot(aes(x = Store, y = Total_ms, fill = Operation)) +
  geom_col(width = 0.6) +
  geom_text(
    aes(label = ifelse(Total_ms > 0, paste0(Total_ms, "ms"), "")),
    position = position_stack(vjust = 0.5), size = 3, color = "white", fontface = "bold"
  ) +
  scale_fill_brewer(palette = "Set2") +
  labs(
    title    = "Total Time by Store",
    subtitle = "Stacked by operation — shows where each backend spends time",
    x = NULL, y = "Total (ms)"
  ) +
  theme_bench +
  scale_y_continuous(expand = expansion(mult = c(0, 0.05)))

ggsave("docs/store_analysis/total_time.png", p3, width = 7, height = 5, dpi = 150)

cat("Figures saved to docs/store_analysis/\n")
