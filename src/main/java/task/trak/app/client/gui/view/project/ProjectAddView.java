package task.trak.app.client.gui.view.project;

import task.trak.model.dto.ProjectDTO;
import task.trak.app.client.gui.controller.ProjectController;
import task.trak.app.client.gui.view.TrakTheme;
import task.trak.app.client.gui.view.form.FormDialogView;
import task.trak.app.client.gui.view.form.FormPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectAddView extends FormDialogView {

    private final ProjectController projectController;
    private final ProjectDTO project;
    private final List<String> currentMembers;

    public ProjectAddView(Component parent, ProjectController projectController, ProjectDTO project) {
        super(parent, "Members \u2014 " + project.projectName());
        this.projectController = projectController;
        this.project = project;
        this.currentMembers = project.memberUsernames() != null
                ? new ArrayList<>(project.memberUsernames())
                : new ArrayList<>();
    }

    @Override
    protected FormPanel buildPanel() {
        // Not used -- show() creates a JDialog directly
        return new FormPanel();
    }

    @Override
    protected void onConfirm() {
        // Not used -- interactions are handled inline via the JDialog
    }

    @Override
    public void show() {
        Window owner = parent instanceof Window
                ? (Window) parent
                : SwingUtilities.getWindowAncestor(parent);

        JDialog dialog = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(350, 400);
        dialog.setLocationRelativeTo(parent);
        dialog.setLayout(new BorderLayout(0, 4));

        // Header with owner
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        header.setBackground(TrakTheme.BG_SURFACE);
        JLabel ownerTitle = new JLabel("Owner:");
        ownerTitle.setForeground(TrakTheme.TEXT_SECONDARY);
        header.add(ownerTitle);
        JLabel ownerLabel = new JLabel(project.ownerUsername() != null ? project.ownerUsername() : "-");
        ownerLabel.setFont(TrakTheme.FONT_HEADING);
        ownerLabel.setForeground(TrakTheme.TEXT_PRIMARY);
        header.add(ownerLabel);
        dialog.add(header, BorderLayout.NORTH);

        // Member list panel
        JPanel memberListPanel = new JPanel();
        memberListPanel.setLayout(new BoxLayout(memberListPanel, BoxLayout.Y_AXIS));
        memberListPanel.setBackground(TrakTheme.BG_DARK);

        Runnable[] refreshRef = new Runnable[1];
        refreshRef[0] = () -> {
            memberListPanel.removeAll();
            if (currentMembers.isEmpty()) {
                JLabel emptyLabel = new JLabel("  No members yet");
                emptyLabel.setForeground(TrakTheme.TEXT_MUTED);
                emptyLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
                memberListPanel.add(emptyLabel);
            } else {
                for (String member : new ArrayList<>(currentMembers)) {
                    JPanel row = new JPanel(new BorderLayout(4, 0));
                    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
                    row.setBackground(TrakTheme.BG_DARK);
                    row.setBorder(new EmptyBorder(4, 12, 4, 8));

                    JLabel nameLabel = new JLabel(member);
                    nameLabel.setFont(TrakTheme.FONT_BODY);
                    nameLabel.setForeground(TrakTheme.TEXT_PRIMARY);
                    row.add(nameLabel, BorderLayout.CENTER);

                    if (!member.equals(project.ownerUsername())) {
                        JButton removeBtn = new JButton("\u2715");
                        removeBtn.setFocusPainted(false);
                        removeBtn.setBorderPainted(false);
                        removeBtn.setContentAreaFilled(false);
                        removeBtn.setForeground(new Color(0xC6, 0x28, 0x28));
                        removeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        removeBtn.setToolTipText("Remove " + member);
                        removeBtn.addActionListener(ev -> {
                            int confirm = JOptionPane.showConfirmDialog(dialog,
                                    "Remove \"" + member + "\" from the project?",
                                    "Confirm", JOptionPane.YES_NO_OPTION);
                            if (confirm == JOptionPane.YES_OPTION) {
                                try {
                                    currentMembers.remove(member);
                                    projectController.removeMember(project.projectName(),
                                            new ArrayList<>(currentMembers));
                                    refreshRef[0].run();
                                } catch (Exception ex) {
                                    JOptionPane.showMessageDialog(dialog,
                                            ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        });
                        row.add(removeBtn, BorderLayout.EAST);
                    }

                    memberListPanel.add(row);
                    JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL) {
                        @Override
                        protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setColor(TrakTheme.ACCENT);
                            g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{4, 4}, 0));
                            g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
                            g2.dispose();
                        }
                    };
                    sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
                    sep.setPreferredSize(new Dimension(0, 2));
                    memberListPanel.add(sep);
                }
            }
            memberListPanel.revalidate();
            memberListPanel.repaint();
        };
        refreshRef[0].run();

        JScrollPane scroll = new JScrollPane(memberListPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(TrakTheme.BG_DARK);
        dialog.add(scroll, BorderLayout.CENTER);

        // Bottom: add member button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bottomPanel.setBackground(TrakTheme.BG_SURFACE);
        JButton addBtn = new JButton("+ Add Member");
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> {
            String username = JOptionPane.showInputDialog(dialog,
                    "Enter username to add:", "Add Member", JOptionPane.PLAIN_MESSAGE);
            if (username != null && !username.trim().isEmpty()) {
                String trimmed = username.trim();
                if (!currentMembers.contains(trimmed)) {
                    try {
                        projectController.addMember(project.projectName(), trimmed);
                        currentMembers.add(trimmed);
                        refreshRef[0].run();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dialog,
                                ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        bottomPanel.add(addBtn);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
