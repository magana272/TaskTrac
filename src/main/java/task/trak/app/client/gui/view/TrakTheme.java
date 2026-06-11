package task.trak.app.client.gui.view;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Theme constants and styling methods for the Trak GUI.
 * Supports dark and light modes with runtime switching.
 */
public final class TrakTheme {

    public enum Theme { DARK, LIGHT }
    private static Theme currentTheme = Theme.DARK;

    // ── Spacing scale (8px grid) — theme-independent ──
    public static final int SP_XS = 4;
    public static final int SP_SM = 8;
    public static final int SP_MD = 12;
    public static final int SP_LG = 16;
    public static final int SP_XL = 24;
    public static final int SP_2XL = 32;
    public static final int SP_3XL = 48;

    // ── Backgrounds ──
    public static Color BG_DARK = new Color(0x12, 0x12, 0x16);
    public static Color BG_SURFACE = new Color(0x1C, 0x1C, 0x22);
    public static Color BG_ELEVATED = new Color(0x26, 0x26, 0x2E);
    public static Color BG_INPUT = new Color(0x18, 0x18, 0x1E);
    public static Color BG_GLASS_TOP = new Color(0x22, 0x22, 0x2A);
    public static Color BG_GLASS_BOT = new Color(0x18, 0x18, 0x20);
    // ── Borders ──
    public static Color BORDER = new Color(0x2E, 0x2E, 0x36);
    public static Color BORDER_SUBTLE = new Color(255, 255, 255, 6);
    public static Color BORDER_HOVER = new Color(0xFF, 0xD5, 0x4F);
    // ── Text ──
    public static Color TEXT_PRIMARY = new Color(0xF2, 0xF2, 0xF2);
    public static Color TEXT_SECONDARY = new Color(0x8A, 0x8A, 0x94);
    public static Color TEXT_MUTED = new Color(0x52, 0x52, 0x5C);
    // ── Accent ──
    public static Color ACCENT = new Color(0xFF, 0xD5, 0x4F);
    public static Color ACCENT_DIM = new Color(0xFF, 0xD5, 0x4F, 30);
    public static Color ACCENT_GREEN = new Color(0x34, 0xC7, 0x59);
    public static Color ACCENT_BLUE = new Color(0x64, 0xB5, 0xF6);
    public static Color PRIMARY_BLUE = new Color(0x5B, 0x9B, 0xD5);
    // ── Status ──
    public static Color STATUS_READY = new Color(0xEF, 0x53, 0x50);
    public static Color STATUS_INPROGRESS = new Color(0xFF, 0xB7, 0x4D);
    public static Color STATUS_COMPLETE = new Color(0x66, 0xBB, 0x6A);
    // ── Table ──
    public static Color TABLE_ROW_ALT = new Color(0x19, 0x19, 0x20);
    public static Color TABLE_HEADER_BG = new Color(0x20, 0x20, 0x28);
    public static Color TABLE_SELECTION = new Color(0x2E, 0x2E, 0x48);
    // ── Card ──
    public static Color CARD_BG = new Color(0x1C, 0x1C, 0x24);
    public static Color CARD_HOVER_BG = new Color(0x24, 0x24, 0x2E);
    public static Color CARD_GLOW = new Color(0xFF, 0xD5, 0x4F, 18);
    public static Color CARD_GRADIENT_BOT = new Color(0x19, 0x19, 0x21);
    public static Color CARD_HOVER_GRADIENT_BOT = new Color(0x20, 0x20, 0x2A);
    public static Color CARD_HIGHLIGHT = new Color(255, 255, 255, 8);

    // ── Typography — theme-independent ──
    public static final Font FONT_DISPLAY = new Font("SansSerif", Font.BOLD, 22);
    public static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 15);
    public static final Font FONT_HEADING = new Font("SansSerif", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_SMALL = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_CAPTION = new Font("SansSerif", Font.PLAIN, 10);
    public static final Font FONT_MONO = new Font(Font.MONOSPACED, Font.PLAIN, 13);
    // ── Corner radii — theme-independent ──
    public static final int RADIUS_SM = 6;
    public static final int RADIUS_MD = 10;
    public static final int RADIUS_LG = 16;

    private TrakTheme() {
    }

    public static Theme getTheme() { return currentTheme; }
    public static boolean isDark() { return currentTheme == Theme.DARK; }

    /**
     * Switch theme. Builds a color transition map, reassigns all fields, and updates UIManager.
     * Call {@link #recolorTree(Component)} on the root window after this to update existing components.
     */
    public static Map<Color, Color> setTheme(Theme theme) {
        if (theme == currentTheme) return Map.of();
        Map<Color, Color> map = buildTransitionMap(theme);
        currentTheme = theme;
        assignColors(theme);
        applyDefaults();
        return map;
    }

    private static Map<Color, Color> buildTransitionMap(Theme target) {
        Map<Color, Color> map = new HashMap<>();
        Color[] darkColors = darkPalette();
        Color[] lightColors = lightPalette();
        Color[] from = (target == Theme.LIGHT) ? darkColors : lightColors;
        Color[] to = (target == Theme.LIGHT) ? lightColors : darkColors;
        for (int i = 0; i < from.length; i++) {
            map.put(from[i], to[i]);
        }
        return map;
    }

    private static Color[] darkPalette() {
        return new Color[]{
            new Color(0x12, 0x12, 0x16),       // BG_DARK
            new Color(0x1C, 0x1C, 0x22),       // BG_SURFACE
            new Color(0x26, 0x26, 0x2E),       // BG_ELEVATED
            new Color(0x18, 0x18, 0x1E),       // BG_INPUT
            new Color(0x22, 0x22, 0x2A),       // BG_GLASS_TOP
            new Color(0x18, 0x18, 0x20),       // BG_GLASS_BOT
            new Color(0x2E, 0x2E, 0x36),       // BORDER
            new Color(255, 255, 255, 6),        // BORDER_SUBTLE
            new Color(0xFF, 0xD5, 0x4F),        // BORDER_HOVER
            new Color(0xF2, 0xF2, 0xF2),        // TEXT_PRIMARY
            new Color(0x8A, 0x8A, 0x94),        // TEXT_SECONDARY
            new Color(0x52, 0x52, 0x5C),        // TEXT_MUTED
            new Color(0xFF, 0xD5, 0x4F),        // ACCENT
            new Color(0xFF, 0xD5, 0x4F, 30),    // ACCENT_DIM
            new Color(0x34, 0xC7, 0x59),        // ACCENT_GREEN
            new Color(0x64, 0xB5, 0xF6),        // ACCENT_BLUE
            new Color(0x5B, 0x9B, 0xD5),        // PRIMARY_BLUE
            new Color(0xEF, 0x53, 0x50),        // STATUS_READY
            new Color(0xFF, 0xB7, 0x4D),        // STATUS_INPROGRESS
            new Color(0x66, 0xBB, 0x6A),        // STATUS_COMPLETE
            new Color(0x19, 0x19, 0x20),        // TABLE_ROW_ALT
            new Color(0x20, 0x20, 0x28),        // TABLE_HEADER_BG
            new Color(0x2E, 0x2E, 0x48),        // TABLE_SELECTION
            new Color(0x1C, 0x1C, 0x24),        // CARD_BG
            new Color(0x24, 0x24, 0x2E),        // CARD_HOVER_BG
            new Color(0xFF, 0xD5, 0x4F, 18),    // CARD_GLOW
            new Color(0x19, 0x19, 0x21),        // CARD_GRADIENT_BOT
            new Color(0x20, 0x20, 0x2A),        // CARD_HOVER_GRADIENT_BOT
            new Color(255, 255, 255, 8),         // CARD_HIGHLIGHT
        };
    }

    private static Color[] lightPalette() {
        return new Color[]{
            new Color(0xF5, 0xF5, 0xF7),        // BG_DARK
            new Color(0xFF, 0xFF, 0xFF),         // BG_SURFACE
            new Color(0xE8, 0xE8, 0xEC),         // BG_ELEVATED
            new Color(0xFF, 0xFF, 0xFF),         // BG_INPUT
            new Color(0xFF, 0xFF, 0xFF),         // BG_GLASS_TOP
            new Color(0xF0, 0xF0, 0xF4),         // BG_GLASS_BOT
            new Color(0xD1, 0xD1, 0xD6),         // BORDER
            new Color(0, 0, 0, 8),                // BORDER_SUBTLE
            new Color(0xD4, 0xA0, 0x17),          // BORDER_HOVER
            new Color(0x1C, 0x1C, 0x1E),          // TEXT_PRIMARY
            new Color(0x6B, 0x6B, 0x73),          // TEXT_SECONDARY
            new Color(0xA0, 0xA0, 0xA8),          // TEXT_MUTED
            new Color(0xD4, 0xA0, 0x17),          // ACCENT
            new Color(0xD4, 0xA0, 0x17, 25),      // ACCENT_DIM
            new Color(0x28, 0xA7, 0x45),           // ACCENT_GREEN
            new Color(0x29, 0x79, 0xFF),           // ACCENT_BLUE
            new Color(0x1A, 0x73, 0xE8),           // PRIMARY_BLUE
            new Color(0xD3, 0x2F, 0x2F),           // STATUS_READY
            new Color(0xE6, 0x8A, 0x00),           // STATUS_INPROGRESS
            new Color(0x2E, 0x7D, 0x32),           // STATUS_COMPLETE
            new Color(0xF0, 0xF0, 0xF4),           // TABLE_ROW_ALT
            new Color(0xE0, 0xE0, 0xE6),           // TABLE_HEADER_BG
            new Color(0xBB, 0xDE, 0xFB),           // TABLE_SELECTION
            new Color(0xFF, 0xFF, 0xFF),            // CARD_BG
            new Color(0xF0, 0xF0, 0xF4),            // CARD_HOVER_BG
            new Color(0xD4, 0xA0, 0x17, 20),        // CARD_GLOW
            new Color(0xE8, 0xE8, 0xEC),             // CARD_GRADIENT_BOT
            new Color(0xF0, 0xF0, 0xF4),             // CARD_HOVER_GRADIENT_BOT
            new Color(0, 0, 0, 8),                    // CARD_HIGHLIGHT
        };
    }

    private static void assignColors(Theme theme) {
        Color[] palette = (theme == Theme.DARK) ? darkPalette() : lightPalette();
        int i = 0;
        BG_DARK = palette[i++];
        BG_SURFACE = palette[i++];
        BG_ELEVATED = palette[i++];
        BG_INPUT = palette[i++];
        BG_GLASS_TOP = palette[i++];
        BG_GLASS_BOT = palette[i++];
        BORDER = palette[i++];
        BORDER_SUBTLE = palette[i++];
        BORDER_HOVER = palette[i++];
        TEXT_PRIMARY = palette[i++];
        TEXT_SECONDARY = palette[i++];
        TEXT_MUTED = palette[i++];
        ACCENT = palette[i++];
        ACCENT_DIM = palette[i++];
        ACCENT_GREEN = palette[i++];
        ACCENT_BLUE = palette[i++];
        PRIMARY_BLUE = palette[i++];
        STATUS_READY = palette[i++];
        STATUS_INPROGRESS = palette[i++];
        STATUS_COMPLETE = palette[i++];
        TABLE_ROW_ALT = palette[i++];
        TABLE_HEADER_BG = palette[i++];
        TABLE_SELECTION = palette[i++];
        CARD_BG = palette[i++];
        CARD_HOVER_BG = palette[i++];
        CARD_GLOW = palette[i++];
        CARD_GRADIENT_BOT = palette[i++];
        CARD_HOVER_GRADIENT_BOT = palette[i++];
        CARD_HIGHLIGHT = palette[i++];
    }

    /**
     * Recursively recolor an entire component tree using the given color transition map.
     */
    public static void recolorTree(Component root, Map<Color, Color> colorMap) {
        recolorComponent(root, colorMap);
        if (root instanceof Container c) {
            for (Component child : c.getComponents()) {
                recolorTree(child, colorMap);
            }
        }
        if (root instanceof JComponent jc) {
            jc.revalidate();
            jc.repaint();
        }
    }

    private static void recolorComponent(Component c, Map<Color, Color> map) {
        Color bg = c.getBackground();
        Color fg = c.getForeground();
        if (bg != null && map.containsKey(bg)) c.setBackground(map.get(bg));
        if (fg != null && map.containsKey(fg)) c.setForeground(map.get(fg));
        if (c instanceof JComponent jc) {
            Border border = jc.getBorder();
            if (border instanceof CompoundBorder cb) {
                Border outer = cb.getOutsideBorder();
                if (outer instanceof LineBorder lb && map.containsKey(lb.getLineColor())) {
                    jc.setBorder(new CompoundBorder(
                            new LineBorder(map.get(lb.getLineColor()), lb.getThickness(), lb.getRoundedCorners()),
                            cb.getInsideBorder()));
                }
            } else if (border instanceof LineBorder lb && map.containsKey(lb.getLineColor())) {
                jc.setBorder(new LineBorder(map.get(lb.getLineColor()), lb.getThickness(), lb.getRoundedCorners()));
            }
        }
        if (c instanceof JTextComponent tc) {
            Color caret = tc.getCaretColor();
            if (caret != null && map.containsKey(caret)) tc.setCaretColor(map.get(caret));
            Color sel = tc.getSelectionColor();
            if (sel != null && map.containsKey(sel)) tc.setSelectionColor(map.get(sel));
        }
    }

    /**
     * Apply global UIManager defaults for the current theme. Call before creating any Swing components.
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
        UIManager.put("PasswordField.selectionBackground", TABLE_SELECTION);
        UIManager.put("PasswordField.selectionForeground", TEXT_PRIMARY);
        UIManager.put("PasswordField.border", BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(SP_XS, SP_SM, SP_XS, SP_SM)));

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

    public static void styleButtonPrimary(JButton btn) {
        btn.setBackground(PRIMARY_BLUE);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BODY.deriveFont(Font.BOLD));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(SP_XS + 2, SP_LG, SP_XS + 2, SP_LG));
    }

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

    public static Border cardBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(SP_MD, SP_LG, SP_MD, SP_LG)
        );
    }

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
        combo.setForeground(statusColor((String) combo.getSelectedItem()));
    }

    // ── Table styling ──

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

    public static Color statusColor(String status) {
        if (status == null) return STATUS_READY;
        return switch (status.toUpperCase()) {
            case "COMPLETE", "COMPLETED", "DONE" -> STATUS_COMPLETE;
            case "INPROGRESS", "IN_PROGRESS" -> STATUS_INPROGRESS;
            default -> STATUS_READY;
        };
    }

    public static void styleScrollPane(JScrollPane sp) {
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(BG_DARK);
        for (JScrollBar bar : new JScrollBar[]{sp.getVerticalScrollBar(), sp.getHorizontalScrollBar()}) {
            bar.setOpaque(false);
            bar.setPreferredSize(new Dimension(8, 8));
            bar.setUnitIncrement(16);
            bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
                @Override protected void configureScrollBarColors() {
                    thumbColor = TEXT_MUTED;
                    trackColor = new Color(0, 0, 0, 0);
                }
                @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
                @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }
                private JButton zeroButton() {
                    JButton b = new JButton();
                    b.setPreferredSize(new Dimension(0, 0));
                    return b;
                }
                @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) { /* transparent */ }
                @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                    if (r.isEmpty() || !c.isEnabled()) return;
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(TEXT_MUTED);
                    int arc = Math.min(r.width, r.height);
                    g2.fillRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, arc, arc);
                    g2.dispose();
                }
            });
        }
    }

    public static EmptyBorder pad(int all) {
        return new EmptyBorder(all, all, all, all);
    }

    public static EmptyBorder pad(int v, int h) {
        return new EmptyBorder(v, h, v, h);
    }
}
