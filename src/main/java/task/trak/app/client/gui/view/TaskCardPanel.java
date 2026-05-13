package task.trak.app.client.gui.view;

import task.trak.api.dto.ProjectDTO;
import task.trak.api.dto.TaskDTO;
import task.trak.api.service.ProjectService;
import task.trak.api.service.ServiceFactory;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.function.Consumer;

public class TaskCardPanel extends JPanel {

    private static final int CARD_WIDTH = 260;
    private static final int CARD_HEIGHT = 180;

    private static final Color STATUS_READY = new Color(0xD3, 0x2F, 0x2F);
    private static final Color STATUS_INPROGRESS = new Color(0xF9, 0xA8, 0x25);
    private static final Color STATUS_COMPLETE = new Color(0x4C, 0xAF, 0x50);
    private static final Color BORDER_COLOR = new Color(0xBD, 0xBD, 0xBD);
    private static final Color HOVER_BORDER = new Color(0x21, 0x96, 0xF3);
    private static final Color NORMAL_BG = Color.WHITE;
    private static final Color HOVER_BG = new Color(0xF5, 0xF8, 0xFF);

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MMM dd, yyyy");

    private final TaskDTO task;
    private final Consumer<String> onCommand;

    public TaskCardPanel(TaskDTO task, Consumer<String> onCommand) {
        this.task = task;
        this.onCommand = onCommand;

        setLayout(new BorderLayout(4, 2));
        setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMinimumSize(new Dimension(200, CARD_HEIGHT));
        setBackground(NORMAL_BG);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(new CompoundBorder(
                new LineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));

        // Hover effect
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(HOVER_BG);
                setBorder(new CompoundBorder(
                        new LineBorder(HOVER_BORDER, 2),
                        new EmptyBorder(7, 9, 7, 9)
                ));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(NORMAL_BG);
                setBorder(new CompoundBorder(
                        new LineBorder(BORDER_COLOR, 1),
                        new EmptyBorder(8, 10, 8, 10)
                ));
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                showEditDialog();
            }
        });

        // --- Top row: title + status combo ---
        JPanel topRow = new JPanel(new BorderLayout(6, 0));
        topRow.setOpaque(false);

        String titleText = task.title() != null ? task.title() : "(untitled)";
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 13f));
        titleLabel.setToolTipText(titleText);
        topRow.add(titleLabel, BorderLayout.CENTER);

        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"READY", "INPROGRESS", "COMPLETE"});
        statusCombo.setSelectedItem(task.status() != null ? task.status() : "READY");
        statusCombo.setFont(statusCombo.getFont().deriveFont(Font.BOLD, 10f));
        statusCombo.setForeground(statusColor(task.status()));
        statusCombo.setBackground(Color.WHITE);
        statusCombo.setFocusable(false);
        statusCombo.addActionListener(e -> {
            String newStatus = (String) statusCombo.getSelectedItem();
            if (newStatus != null && !newStatus.equals(task.status())) {
                statusCombo.setForeground(statusColor(newStatus));
                if ("COMPLETE".equals(newStatus)) {
                    onCommand.accept("complete " + task.id());
                } else {
                    onCommand.accept("task update " + task.id() + " --status " + newStatus);
                }
            }
        });
        topRow.add(statusCombo, BorderLayout.EAST);

        add(topRow, BorderLayout.NORTH);

        // --- Center: project, summary ---
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        String projectText = task.projectName() != null ? task.projectName() : "-";
        JLabel projectLabel = new JLabel(projectText);
        projectLabel.setFont(projectLabel.getFont().deriveFont(Font.PLAIN, 11f));
        projectLabel.setForeground(Color.GRAY);
        projectLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        centerPanel.add(projectLabel);

        centerPanel.add(Box.createVerticalStrut(3));

        String summaryText = task.summary() != null ? task.summary() : "";
        JTextArea summaryArea = new JTextArea(summaryText);
        summaryArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setEditable(false);
        summaryArea.setOpaque(false);
        summaryArea.setBorder(null);
        summaryArea.setAlignmentX(Component.LEFT_ALIGNMENT);
        summaryArea.setToolTipText(task.summary());
        // Click on summary also opens edit dialog
        summaryArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showEditDialog();
            }
        });
        summaryArea.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        centerPanel.add(summaryArea);

        add(centerPanel, BorderLayout.CENTER);

        // --- Bottom: deadline + task ID ---
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        String deadlineText = task.deadline() != null ? "Due: " + DATE_FMT.format(task.deadline()) : "";
        JLabel deadlineLabel = new JLabel(deadlineText);
        deadlineLabel.setFont(deadlineLabel.getFont().deriveFont(Font.ITALIC, 10f));
        deadlineLabel.setForeground(Color.DARK_GRAY);
        JLabel idLabel = new JLabel("#" + task.id());
        idLabel.setFont(idLabel.getFont().deriveFont(Font.PLAIN, 9f));
        idLabel.setForeground(new Color(0xAA, 0xAA, 0xAA));
        bottomRow.add(deadlineLabel, BorderLayout.WEST);
        bottomRow.add(idLabel, BorderLayout.EAST);
        add(bottomRow, BorderLayout.SOUTH);

    }

    private void showEditDialog() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.NORTHWEST;

        JTextField titleField = new JTextField(task.title() != null ? task.title() : "", 30);

        // Populate assignee dropdown from project members
        JComboBox<String> assignedCombo = new JComboBox<>();
        if (task.projectName() != null) {
            ProjectService ps = ServiceFactory.projectService();
            ProjectDTO proj = ps.getByName(task.projectName());
            if (proj != null) {
                if (proj.ownerUsername() != null) assignedCombo.addItem(proj.ownerUsername());
                if (proj.memberUsernames() != null) {
                    for (String m : proj.memberUsernames()) {
                        if (!m.equals(proj.ownerUsername())) assignedCombo.addItem(m);
                    }
                }
            }
        }
        if (assignedCombo.getItemCount() == 0 && task.assignedTo() != null) {
            assignedCombo.addItem(task.assignedTo());
        }
        assignedCombo.setSelectedItem(task.assignedTo());

        JComboBox<String> statusField = new JComboBox<>(new String[]{"READY", "INPROGRESS", "COMPLETE"});
        statusField.setSelectedItem(task.status() != null ? task.status() : "READY");

        JTextArea summaryArea = new JTextArea(task.summary() != null ? task.summary() : "", 8, 40);
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        summaryArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        JScrollPane summaryScroll = new JScrollPane(summaryArea);

        // Row 0: ID
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Task ID:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        JLabel idLabel = new JLabel("#" + task.id() + "  (" + task.projectName() + ")");
        idLabel.setForeground(Color.GRAY);
        panel.add(idLabel, gbc);

        // Row 1: Title
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panel.add(titleField, gbc);

        // Row 2: Assigned
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Assigned To:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panel.add(assignedCombo, gbc);

        // Row 3: Status
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panel.add(statusField, gbc);

        // Row 4: Summary (large)
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Summary:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        panel.add(summaryScroll, gbc);

        panel.setPreferredSize(new Dimension(500, 350));

        int result = JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(this), panel, "Edit Task",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            StringBuilder cmd = new StringBuilder("task update " + task.id());
            boolean changed = false;

            String newTitle = titleField.getText().trim();
            if (!newTitle.equals(task.title() != null ? task.title() : "")) {
                cmd.append(" --title ").append(newTitle);
                changed = true;
            }
            String newAssigned = (String) assignedCombo.getSelectedItem();
            if (newAssigned != null && !newAssigned.equals(task.assignedTo() != null ? task.assignedTo() : "")) {
                cmd.append(" --assigned_to ").append(newAssigned);
                changed = true;
            }
            String newSummary = summaryArea.getText().trim();
            if (!newSummary.equals(task.summary() != null ? task.summary() : "")) {
                cmd.append(" --summary ").append(newSummary);
                changed = true;
            }
            String newStatus = (String) statusField.getSelectedItem();
            if (newStatus != null && !newStatus.equals(task.status())) {
                cmd.append(" --status ").append(newStatus);
                changed = true;
            }

            if (changed) {
                onCommand.accept(cmd.toString());
            }
        }
    }

    private Color statusColor(String status) {
        if (status == null) return STATUS_READY;
        return switch (status.toUpperCase()) {
            case "COMPLETE", "COMPLETED", "DONE" -> STATUS_COMPLETE;
            case "INPROGRESS", "IN_PROGRESS" -> STATUS_INPROGRESS;
            default -> STATUS_READY;
        };
    }
}
