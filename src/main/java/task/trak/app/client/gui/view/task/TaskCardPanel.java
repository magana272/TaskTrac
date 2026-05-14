package task.trak.app.client.gui.view.task;

import task.trak.api.dto.TaskDTO;
import task.trak.app.client.gui.controller.TaskController;
import task.trak.app.client.gui.view.TrakTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Cinematic task card with custom-painted rounded corners,
 * subtle gradient background, and gold glow hover state.
 */
public class TaskCardPanel extends JPanel {

    private static final int CARD_WIDTH = 270;
    private static final int CARD_HEIGHT = 190;
    private static final int CORNER = TrakTheme.RADIUS_MD;
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MMM dd");

    private final TaskDTO task;
    private final TaskController taskController;
    private final List<String> assignees;
    private boolean hovered = false;

    public TaskCardPanel(TaskDTO task, TaskController taskController, List<String> assignees) {
        this.task = task;
        this.taskController = taskController;
        this.assignees = assignees;

        setLayout(new BorderLayout(4, 4));
        setMinimumSize(new Dimension(210, CARD_HEIGHT));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, CARD_HEIGHT));
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new EmptyBorder(TrakTheme.SP_MD, TrakTheme.SP_LG, TrakTheme.SP_MD, TrakTheme.SP_LG));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showEditDialog();
            }
        });

        buildContent();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Glow behind card on hover
        if (hovered) {
            g2.setColor(TrakTheme.CARD_GLOW);
            g2.fill(new RoundRectangle2D.Float(-2, -2, w + 4, h + 4, CORNER + 4, CORNER + 4));
        }

        // Card background with subtle gradient
        Color bgTop = hovered ? TrakTheme.CARD_HOVER_BG : TrakTheme.CARD_BG;
        Color bgBot = hovered
                ? new Color(0x20, 0x20, 0x2A)
                : new Color(0x19, 0x19, 0x21);
        GradientPaint gp = new GradientPaint(0, 0, bgTop, 0, h, bgBot);
        g2.setPaint(gp);
        g2.fill(new RoundRectangle2D.Float(0, 0, w, h, CORNER, CORNER));

        // Top edge highlight
        g2.setColor(new Color(255, 255, 255, hovered ? 10 : 5));
        g2.fill(new RoundRectangle2D.Float(0, 0, w, 30, CORNER, CORNER));

        // Border
        g2.setColor(hovered ? TrakTheme.BORDER_HOVER : TrakTheme.BORDER);
        g2.setStroke(new BasicStroke(hovered ? 1.5f : 1f));
        g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, w - 1, h - 1, CORNER, CORNER));

        g2.dispose();
        super.paintComponent(g);
    }

    private void buildContent() {
        add(buildTopRow(), BorderLayout.NORTH);
        add(buildCenterPanel(), BorderLayout.CENTER);
        add(buildBottomRow(), BorderLayout.SOUTH);
    }

    private JPanel buildTopRow() {
        JPanel topRow = new JPanel(new BorderLayout(6, 0));
        topRow.setOpaque(false);

        String titleText = task.title() != null ? task.title() : "(untitled)";
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(TrakTheme.FONT_HEADING);
        titleLabel.setForeground(TrakTheme.TEXT_PRIMARY);
        titleLabel.setToolTipText(titleText);
        topRow.add(titleLabel, BorderLayout.CENTER);

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"READY", "INPROGRESS", "COMPLETE"});
        statusCombo.setSelectedItem(task.status() != null ? task.status() : "READY");
        TrakTheme.styleStatusComboBox(statusCombo);
        statusCombo.addActionListener(e -> {
            String newStatus = (String) statusCombo.getSelectedItem();
            if (newStatus != null && !newStatus.equals(task.status())) {
                statusCombo.setForeground(TrakTheme.statusColor(newStatus));
                if ("COMPLETE".equals(newStatus)) {
                    taskController.completeTask(task.id());
                } else {
                    taskController.updateTask(task.id(), null, newStatus, null, null, null);
                }
            }
        });
        topRow.add(statusCombo, BorderLayout.EAST);
        return topRow;
    }

    private JPanel buildCenterPanel() {
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        String projectText = task.projectName() != null ? task.projectName() : "-";
        JLabel projectLabel = new JLabel(projectText);
        projectLabel.setFont(TrakTheme.FONT_CAPTION);
        projectLabel.setForeground(TrakTheme.TEXT_MUTED);
        projectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(projectLabel);
        centerPanel.add(Box.createVerticalStrut(TrakTheme.SP_XS));

        String summaryText = task.summary() != null ? task.summary() : "";
        JTextArea summaryArea = new JTextArea(summaryText);
        summaryArea.setFont(TrakTheme.FONT_SMALL);
        summaryArea.setForeground(TrakTheme.TEXT_SECONDARY);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setEditable(false);
        summaryArea.setOpaque(false);
        summaryArea.setBorder(null);
        summaryArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryArea.setToolTipText(task.summary());
        summaryArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showEditDialog();
            }
        });
        summaryArea.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        centerPanel.add(summaryArea);
        return centerPanel;
    }

    private JPanel buildBottomRow() {
        JPanel bottomRow = new JPanel();
        bottomRow.setLayout(new BoxLayout(bottomRow, BoxLayout.X_AXIS));
        bottomRow.setOpaque(false);

        if (task.deadline() != null) {
            JLabel deadlineLabel = new JLabel("Due " + DATE_FMT.format(task.deadline()));
            deadlineLabel.setFont(TrakTheme.FONT_CAPTION.deriveFont(Font.ITALIC));
            deadlineLabel.setForeground(TrakTheme.TEXT_MUTED);
            bottomRow.add(deadlineLabel);
        }

        if (task.estimate() != null && !task.estimate().isBlank()) {
            if (task.deadline() != null) bottomRow.add(Box.createHorizontalStrut(TrakTheme.SP_SM));
            JLabel estimateLabel = new JLabel("Est " + task.estimate());
            estimateLabel.setFont(TrakTheme.FONT_CAPTION.deriveFont(Font.ITALIC));
            estimateLabel.setForeground(TrakTheme.ACCENT_BLUE);
            bottomRow.add(estimateLabel);
        }

        bottomRow.add(Box.createHorizontalGlue());

        JLabel idLabel = new JLabel("#" + task.id());
        idLabel.setFont(TrakTheme.FONT_CAPTION);
        idLabel.setForeground(TrakTheme.TEXT_MUTED);
        bottomRow.add(idLabel);
        return bottomRow;
    }

    private void showEditDialog() {
        new TaskEditView(SwingUtilities.getWindowAncestor(this), taskController, task, assignees).show();
    }
}
