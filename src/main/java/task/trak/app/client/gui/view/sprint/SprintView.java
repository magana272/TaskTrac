package task.trak.app.client.gui.view.sprint;

import task.trak.api.dto.SprintDTO;
import task.trak.api.dto.TaskDTO;
import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.view.DataView;
import task.trak.app.client.gui.viewmodel.ViewModelChangeListener;
import task.trak.app.client.gui.viewmodel.ViewModelChangeType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;

public class SprintView extends DataView implements ViewModelChangeListener {

    private final GUIController guiController;

    public SprintView(GUIController guiController) {
        this.guiController = guiController;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        guiController.getSprintController().getViewModel().addObserver(this);
        guiController.getProjectController().getViewModel().addObserver(this);
        guiController.getTaskController().getViewModel().addObserver(this);
    }

    @Override
    public void onViewModelChanged(ViewModelChangeType type) {
        if (type == ViewModelChangeType.SPRINTS) {
            SwingUtilities.invokeLater(this::render);
        }
    }

    public void showSprints(List<SprintDTO> sprints) {
        guiController.getSprintController().getViewModel().setAll(sprints);
        render();
    }

    @Override
    public void render() {
        removeAll();
        setLayout(new BorderLayout());

        List<SprintDTO> sprints = guiController.getSprintController().getViewModel().get();

        if (sprints == null || sprints.isEmpty()) {
            JButton addBtn = new JButton("+ Create a Sprint");
            addBtn.addActionListener(e -> openAddSprintDialog());
            JPanel placeholder = new JPanel(new GridBagLayout());
            placeholder.add(addBtn);
            add(placeholder, BorderLayout.CENTER);
        } else {
            String[] columns = {"ID", "Name", "Project", "Tasks", "Start Date", "End Date"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    String colName = getColumnName(col);
                    return "Start Date".equals(colName) || "End Date".equals(colName);
                }
            };

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            for (SprintDTO s : sprints) {
                model.addRow(new Object[]{
                        s.id(),
                        s.name() != null ? s.name() : "",
                        s.projectName() != null ? s.projectName() : "",
                        s.taskIds() != null ? s.taskIds().size() : 0,
                        s.startDate() != null ? sdf.format(s.startDate()) : "",
                        s.endDate() != null ? sdf.format(s.endDate()) : ""
                });
            }

            JTable table = createCopyableTable(model);
            table.setRowHeight(28);
            table.getTableHeader().setReorderingAllowed(false);

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = table.rowAtPoint(e.getPoint());
                        int col = table.columnAtPoint(e.getPoint());
                        if (row < 0 || row >= sprints.size()) return;
                        SprintDTO s = sprints.get(row);
                        String colName = model.getColumnName(col);
                        if ("Tasks".equals(colName)) {
                            showSprintTasksDialog(s);
                        }
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            add(scrollPane, BorderLayout.CENTER);

            JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
            JButton addSprintBtn = new JButton("+ Add Sprint");
            addSprintBtn.setFocusPainted(false);
            addSprintBtn.addActionListener(e -> openAddSprintDialog());
            bottomBar.add(addSprintBtn);

            JButton saveBtn = new JButton("Save Changes");
            saveBtn.setFocusPainted(false);
            saveBtn.addActionListener(e -> saveSprintEdits(table, model, sprints));
            bottomBar.add(saveBtn);

            add(bottomBar, BorderLayout.SOUTH);
        }

        revalidate();
        repaint();
    }

    private void openAddSprintDialog() {
        var projects = guiController.getProjectController().getProjectsForUser(
                guiController.getSession() != null ? guiController.getSession().getLogged_in_user() : null);
        var tasks = guiController.getTaskController().getViewModel().get();
        new SprintAddView(this, guiController.getSprintController(), projects, tasks).show();
    }

    private void showSprintTasksDialog(SprintDTO sprint) {
        List<Long> taskIds = sprint.taskIds() != null ? sprint.taskIds() : List.of();

        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner,
                "Tasks \u2014 " + sprint.name() + " (" + sprint.projectName() + ")",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(550, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 4));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        if (taskIds.isEmpty()) {
            JLabel emptyLabel = new JLabel("  No tasks in this sprint");
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            listPanel.add(emptyLabel);
        } else {
            for (Long tid : taskIds) {
                TaskDTO t = guiController.getTaskController().getTaskById(tid);
                if (t == null) continue;

                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                row.setBackground(Color.WHITE);
                row.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 8));

                String statusStr = t.status() != null ? t.status() : "READY";
                Color statusColor = switch (statusStr) {
                    case "COMPLETE" -> new Color(0x4C, 0xAF, 0x50);
                    case "INPROGRESS" -> new Color(0xF9, 0xA8, 0x25);
                    default -> new Color(0xD3, 0x2F, 0x2F);
                };

                JLabel dot = new JLabel("\u25CF ");
                dot.setForeground(statusColor);
                String assignee = t.assignedTo() != null ? " \u2014 " + t.assignedTo() : "";
                JLabel titleLabel = new JLabel(t.title() + "  [" + statusStr + "]" + assignee);
                titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 12f));

                JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                left.setOpaque(false);
                left.add(dot);
                left.add(titleLabel);
                row.add(left, BorderLayout.CENTER);

                listPanel.add(row);
                listPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void saveSprintEdits(JTable table, DefaultTableModel model, List<SprintDTO> sprints) {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int saved = 0;

        for (int i = 0; i < model.getRowCount() && i < sprints.size(); i++) {
            SprintDTO original = sprints.get(i);
            String newStart = model.getValueAt(i, 4).toString().trim();
            String newEnd = model.getValueAt(i, 5).toString().trim();
            String origStart = original.startDate() != null ? sdf.format(original.startDate()) : "";
            String origEnd = original.endDate() != null ? sdf.format(original.endDate()) : "";

            boolean startChanged = !newStart.equals(origStart);
            boolean endChanged = !newEnd.equals(origEnd);

            if (startChanged || endChanged) {
                guiController.getSprintController().updateSprint(
                        String.valueOf(original.id()),
                        startChanged && !newStart.isEmpty() ? newStart : null,
                        endChanged && !newEnd.isEmpty() ? newEnd : null
                );
                saved++;
            }
        }
        if (saved == 0) {
            JOptionPane.showMessageDialog(this, "No changes to save.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private JTable createCopyableTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setCellSelectionEnabled(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        KeyStroke copy = KeyStroke.getKeyStroke("control C");
        KeyStroke macCopy = KeyStroke.getKeyStroke("meta C");
        Action copyAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();
                if (row >= 0 && col >= 0) {
                    Object val = table.getValueAt(row, col);
                    String text = val != null ? val.toString() : "";
                    Toolkit.getDefaultToolkit().getSystemClipboard()
                            .setContents(new StringSelection(text), null);
                }
            }
        };
        table.getInputMap().put(copy, "copy");
        table.getInputMap().put(macCopy, "copy");
        table.getActionMap().put("copy", copyAction);
        return table;
    }
}
