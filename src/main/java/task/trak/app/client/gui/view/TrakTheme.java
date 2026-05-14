package task.trak.app.client.gui.view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Dark theme constants and styling methods for the Trak GUI.
 */
public final class TrakTheme {

    // ── Spacing scale (8px grid) ──
    public static final int SP_XS = 4;
    public static final int SP_SM = 8;
    public static final int SP_MD = 12;
    public static final int SP_LG = 16;
    public static final int SP_XL = 24;
    public static final int SP_2XL = 32;
    public static final int SP_3XL = 48;
    // ── Backgrounds ──
    public static final Color BG_DARK = new Color(0x12, 0x12, 0x16);
    public static final Color BG_SURFACE = new Color(0x1C, 0x1C, 0x22);
    public static final Color BG_ELEVATED = new Color(0x26, 0x26, 0x2E);
    public static final Color BG_INPUT = new Color(0x18, 0x18, 0x1E);
    public static final Color BG_GLASS_TOP = new Color(0x22, 0x22, 0x2A);
    public static final Color BG_GLASS_BOT = new Color(0x18, 0x18, 0x20);
    // ── Borders ──
    public static final Color BORDER = new Color(0x2E, 0x2E, 0x36);
    public static final Color BORDER_SUBTLE = new Color(255, 255, 255, 6);
    public static final Color BORDER_HOVER = new Color(0xFF, 0xD5, 0x4F);
    // ── Text ──
    public static final Color TEXT_PRIMARY = new Color(0xF2, 0xF2, 0xF2);
    public static final Color TEXT_SECONDARY = new Color(0x8A, 0x8A, 0x94);
    public static final Color TEXT_MUTED = new Color(0x52, 0x52, 0x5C);
    // ── Accent ──
    public static final Color ACCENT = new Color(0xFF, 0xD5, 0x4F);
    public static final Color ACCENT_DIM = new Color(0xFF, 0xD5, 0x4F, 30); // glow
    public static final Color ACCENT_GREEN = new Color(0x34, 0xC7, 0x59);
    public static final Color ACCENT_BLUE = new Color(0x64, 0xB5, 0xF6);
    // ── Status ──
    public static final Color STATUS_READY = new Color(0xEF, 0x53, 0x50);
    public static final Color STATUS_INPROGRESS = new Color(0xFF, 0xB7, 0x4D);
    public static final Color STATUS_COMPLETE = new Color(0x66, 0xBB, 0x6A);
    // ── Table ──
    public static final Color TABLE_ROW_ALT = new Color(0x19, 0x19, 0x20);
    public static final Color TABLE_HEADER_BG = new Color(0x20, 0x20, 0x28);
    public static final Color TABLE_SELECTION = new Color(0x2E, 0x2E, 0x48);
    // ── Card ──
    public static final Color CARD_BG = new Color(0x1C, 0x1C, 0x24);
    public static final Color CARD_HOVER_BG = new Color(0x24, 0x24, 0x2E);
    public static final Color CARD_GLOW = new Color(0xFF, 0xD5, 0x4F, 18);
    // ── Typography ──
    // Display: large branding / hero
    public static final Font FONT_DISPLAY = new Font("SansSerif", Font.BOLD, 22);
    // Title: section headings
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 15);
    // Heading: card titles, table headers
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 13);
    // Body: default content
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 12);
    // Small: metadata, labels
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    // Caption: muted metadata
    public static final Font FONT_CAPTION = new Font("SansSerif", Font.PLAIN, 10);
    // Mono: terminal / command input
    public static final Font FONT_MONO = new Font(Font.MONOSPACED, Font.PLAIN, 13);
    // ── Corner radii ──
    public static final int RADIUS_SM = 6;
    public static final int RADIUS_MD = 10;
    public static final int RADIUS_LG = 16;

    private TrakTheme() {
    }

    /**
     * Apply global dark UIManager defaults. Call before creating any Swing components.
     */
    public static void applyDefaults() {
        UIManager.put("Panel.background", BG_DARK);
        UIManager.put("Panel.foreground", TEXT_PRIMARY);

        UIManager.put("OptionPane.background", BG_SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);

        UIManager.put("Label.foreground", TEXT_PRIMARY);
        UIManager.put("Label.font", FONT_BODY);

        UIManager.put("TextField.background", BG_INPUT);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", ACCENT);
        UIManager.put("TextField.selectionBackground", TABLE_SELECTION);
        UIManager.put("TextField.selectionForeground", TEXT_PRIMARY);
        UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(SP_XS, SP_SM, SP_XS, SP_SM)));

        UIManager.put("PasswordField.background", BG_INPUT);
        UIManager.put("PasswordField.foreground", TEXT_PRIMARY);
        UIManager.put("PasswordField.caretForeground", ACCENT);

        UIManager.put("TextArea.background", BG_INPUT);
        UIManager.put("TextArea.foreground", TEXT_PRIMARY);
        UIManager.put("TextArea.caretForeground", ACCENT);
        UIManager.put("TextArea.selectionBackground", TABLE_SELECTION);
        UIManager.put("TextArea.selectionForeground", TEXT_PRIMARY);

        UIManager.put("ComboBox.background", BG_ELEVATED);
        UIManager.put("ComboBox.foreground", TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground", TABLE_SELECTION);
        UIManager.put("ComboBox.selectionForeground", TEXT_PRIMARY);
        UIManager.put("ComboBox.disabledBackground", BG_ELEVATED);
        UIManager.put("ComboBox.disabledForeground", TEXT_MUTED);
        // Flatten Metal L&F 3D effect — all shadow/highlight colors match background
        UIManager.put("ComboBox.selectionBorder", BorderFactory.createLineBorder(BORDER, 0));
        UIManager.put("ComboBox.buttonBackground", BG_ELEVATED);
        UIManager.put("ComboBox.buttonDarkShadow", BG_ELEVATED);
        UIManager.put("ComboBox.buttonHighlight", BG_ELEVATED);
        UIManager.put("ComboBox.buttonShadow", BG_ELEVATED);
        UIManager.put("ComboBox.selectionOutline", BG_ELEVATED);

        UIManager.put("Button.background", BG_ELEVATED);
        UIManager.put("Button.foreground", TEXT_PRIMARY);
        UIManager.put("Button.select", BG_SURFACE);
        UIManager.put("Button.font", FONT_BODY);

        UIManager.put("ToggleButton.background", BG_ELEVATED);
        UIManager.put("ToggleButton.foreground", TEXT_PRIMARY);

        UIManager.put("CheckBox.background", BG_DARK);
        UIManager.put("CheckBox.foreground", TEXT_PRIMARY);

        UIManager.put("Spinner.background", BG_INPUT);
        UIManager.put("Spinner.foreground", TEXT_PRIMARY);

        UIManager.put("Table.background", BG_DARK);
        UIManager.put("Table.foreground", TEXT_PRIMARY);
        UIManager.put("Table.gridColor", BORDER);
        UIManager.put("Table.selectionBackground", TABLE_SELECTION);
        UIManager.put("Table.selectionForeground", TEXT_PRIMARY);
        UIManager.put("Table.font", FONT_BODY);

        UIManager.put("TableHeader.background", TABLE_HEADER_BG);
        UIManager.put("TableHeader.foreground", ACCENT);

        UIManager.put("ScrollPane.background", BG_DARK);
        UIManager.put("ScrollBar.background", BG_DARK);
        UIManager.put("ScrollBar.thumb", BG_ELEVATED);
        UIManager.put("ScrollBar.thumbDarkShadow", BG_DARK);
        UIManager.put("ScrollBar.thumbHighlight", BG_ELEVATED);
        UIManager.put("ScrollBar.thumbShadow", BG_DARK);
        UIManager.put("ScrollBar.track", BG_DARK);
        UIManager.put("ScrollBar.trackHighlight", BG_DARK);
        UIManager.put("ScrollBar.width", 10);

        UIManager.put("Viewport.background", BG_DARK);
        UIManager.put("Separator.foreground", BORDER);

        UIManager.put("List.background", BG_DARK);
        UIManager.put("List.foreground", TEXT_PRIMARY);
        UIManager.put("List.selectionBackground", TABLE_SELECTION);
        UIManager.put("List.selectionForeground", TEXT_PRIMARY);

        UIManager.put("TitledBorder.titleColor", TEXT_PRIMARY);
        UIManager.put("ToolTip.background", BG_ELEVATED);
        UIManager.put("ToolTip.foreground", TEXT_PRIMARY);

        UIManager.put("InternalFrame.background", BG_DARK);
    }

    // ── Button styling ──

    /**
     * Primary action button (green).
     */
    public static void styleButtonPrimary(JButton btn) {
        btn.setBackground(ACCENT_GREEN);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BODY.deriveFont(Font.BOLD));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(SP_XS + 2, SP_LG, SP_XS + 2, SP_LG));
    }

    /**
     * Secondary button.
     */
    public static void styleButtonNav(JButton btn) {
        btn.setBackground(BG_ELEVATED);
        btn.setForeground(TEXT_SECONDARY);
        btn.setFont(FONT_BODY);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(SP_XS + 2, SP_MD, SP_XS + 2, SP_MD));
    }

    /**
     * Accent button (gold).
     */
    public static void styleButtonAccent(JButton btn) {
        btn.setBackground(ACCENT);
        btn.setForeground(BG_DARK);
        btn.setFont(FONT_BODY.deriveFont(Font.BOLD));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(SP_XS + 2, SP_LG, SP_XS + 2, SP_LG));
    }

    // ── Card borders ──

    /**
     * Card border (rounded + padding).
     */
    public static Border cardBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(SP_MD, SP_LG, SP_MD, SP_LG)
        );
    }

    /**
     * Card hover border.
     */
    public static Border cardBorderHover() {
        return new CompoundBorder(
                new LineBorder(BORDER_HOVER, 1, true),
                new EmptyBorder(SP_MD, SP_LG, SP_MD, SP_LG)
        );
    }

    // ── ComboBox styling ──

    public static void styleComboBox(JComboBox<?> combo) {
        combo.setFocusable(false);
        combo.setOpaque(true);
        combo.setBackground(BG_ELEVATED);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        // Replace Aqua UI with basic to respect our colors
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton btn = new JButton("\u25BE");
                btn.setBorder(BorderFactory.createEmptyBorder());
                btn.setBackground(BG_ELEVATED);
                btn.setForeground(TEXT_SECONDARY);
                btn.setFocusable(false);
                return btn;
            }
        });
    }

    public static void styleStatusComboBox(JComboBox<String> combo) {
        styleComboBox(combo);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? TABLE_SELECTION : BG_ELEVATED);
                setForeground(statusColor((String) value));
                setFont(FONT_CAPTION.deriveFont(Font.BOLD));
                return this;
            }
        });
        // Set initial foreground to match selected status
        combo.setForeground(statusColor((String) combo.getSelectedItem()));
    }

    // ── Table styling ──

    /**
     * Style a JTable with alternating dark rows and dark header.
     */
    public static void styleTable(JTable table) {
        table.setBackground(BG_DARK);
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(BORDER);
        table.setSelectionBackground(TABLE_SELECTION);
        table.setSelectionForeground(TEXT_PRIMARY);
        table.setRowHeight(36);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setFont(FONT_BODY);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? BG_DARK : TABLE_ROW_ALT);
                }
                setForeground(TEXT_PRIMARY);
                setBorder(new EmptyBorder(SP_XS, SP_MD, SP_XS, SP_MD));
                return this;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(ACCENT);
        header.setFont(FONT_HEADING);
        header.setBorder(new LineBorder(BORDER, 1));
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 20));
        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.LEFT);
    }

    /**
     * Get status color for a task status string.
     */
    public static Color statusColor(String status) {
        if (status == null) return STATUS_READY;
        return switch (status.toUpperCase()) {
            case "COMPLETE", "COMPLETED", "DONE" -> STATUS_COMPLETE;
            case "INPROGRESS", "IN_PROGRESS" -> STATUS_INPROGRESS;
            default -> STATUS_READY;
        };
    }

    /**
     * Create an inset padding border.
     */
    public static EmptyBorder pad(int all) {
        return new EmptyBorder(all, all, all, all);
    }

    /**
     * Create an inset padding border.
     */
    public static EmptyBorder pad(int v, int h) {
        return new EmptyBorder(v, h, v, h);
    }
}
