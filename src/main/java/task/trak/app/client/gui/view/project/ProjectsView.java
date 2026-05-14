package task.trak.app.client.gui.view.project;

import task.trak.model.dto.ProjectDTO;
import task.trak.model.dto.TaskDTO;
import task.trak.model.util.TimeUtil;
import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.view.DataView;
import task.trak.app.client.gui.view.TrakTheme;
import task.trak.app.client.gui.viewmodel.ViewModelChangeListener;
import task.trak.app.client.gui.viewmodel.ViewModelChangeType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectsView extends DataView implements ViewModelChangeListener {

    private final GUIController guiController;

    public ProjectsView(GUIController guiController) {
        this.guiController = guiController;
        setLayout(new BorderLayout());
        setBackground(TrakTheme.BG_DARK);

        guiController.getProjectController().getViewModel().addObserver(this);
    }

    @Override
    public void onViewModelChanged(ViewModelChangeType type) {
        if (type == ViewModelChangeType.PROJECTS) {
            SwingUtilities.invokeLater(this::render);
        }
    }

    public void showProjects(List<ProjectDTO> projects) {
        guiController.getProjectController().getViewModel().setAll(projects);
        render();
    }

    @Override
    public void render() {
        removeAll();
        setLayout(new BorderLayout());

        List<ProjectDTO> projects = guiController.getProjectController().getViewModel().get();

        if (projects == null || projects.isEmpty()) {
            JButton addBtn = new JButton("+ Create a Project");
            TrakTheme.styleButtonPrimary(addBtn);
            addBtn.addActionListener(e -> openCreateDialog());
            JPanel placeholder = new JPanel(new GridBagLayout());
            placeholder.setBackground(TrakTheme.BG_DARK);
            placeholder.add(addBtn);
            add(placeholder, BorderLayout.CENTER);
        } else {
            // Compute total estimate per project
            Map<String, Long> projectEstimateMs = new HashMap<>();
            List<TaskDTO> allTasks = guiController.getTaskController().getViewModel().get();
            if (allTasks != null) {
                for (TaskDTO t : allTasks) {
                    if (t.projectName() != null && t.estimate() != null && !t.estimate().isBlank()) {
                        projectEstimateMs.merge(t.projectName(), TimeUtil.parseDurationToMs(t.estimate()), Long::sum);
                    }
                }
            }

            String[] columns = {"ID", "Name", "Description", "Owner", "Members", "Tasks", "Estimate", "Sprints"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    String colName = getColumnName(col);
                    if (!"Name".equals(colName) && !"Description".equals(colName)) return false;
                    if (row < 0 || row >= projects.size()) return false;
                    String owner = projects.get(row).ownerUsername();
                    String currentUser = guiController.getSession() != null
                            ? guiController.getSession().getLogged_in_user() : null;
                    return owner != null && owner.equals(currentUser);
                }
            };

            for (ProjectDTO p : projects) {
                long estMs = projectEstimateMs.getOrDefault(p.projectName(), 0L);
                String estDisplay = estMs > 0 ? TimeUtil.formatDuration(estMs) : "-";
                model.addRow(new Object[]{
                        p.id(),
                        p.projectName() != null ? p.projectName() : "",
                        p.summary() != null ? p.summary() : "",
                        p.ownerUsername() != null ? p.ownerUsername() : "-",
                        p.memberCount(),
                        p.taskCount(),
                        estDisplay,
                        p.sprintCount()
                });
            }

            JTable table = createCopyableTable(model);
            TrakTheme.styleTable(table);

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = table.rowAtPoint(e.getPoint());
                        int col = table.columnAtPoint(e.getPoint());
                        if (row < 0 || row >= projects.size()) return;
                        ProjectDTO p = projects.get(row);
                        String colName = model.getColumnName(col);
                        switch (colName) {
                            case "Members" -> {
                                new ProjectAddView(ProjectsView.this, guiController.getProjectController(), p).show();
                            }
                            case "Description" -> {
                                String currentUser = guiController.getSession() != null
                                        ? guiController.getSession().getLogged_in_user() : null;
                                if (p.ownerUsername() != null && p.ownerUsername().equals(currentUser)) {
                                    showSummaryEditor(p, model, row);
                                }
                            }
                            case "Tasks" -> {
                                guiController.getTaskController().getViewModel().setProjectFilter(p.projectName());
                                guiController.getTaskController().refreshTasks();
                            }
                        }
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getViewport().setBackground(TrakTheme.BG_DARK);

            JPanel tableWrapper = new JPanel(new BorderLayout());
            tableWrapper.setBackground(TrakTheme.BG_DARK);
            tableWrapper.setBorder(new EmptyBorder(TrakTheme.SP_MD, 0, 0, 0));
            tableWrapper.add(scrollPane, BorderLayout.CENTER);
            add(tableWrapper, BorderLayout.CENTER);

            JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, TrakTheme.SP_SM, TrakTheme.SP_SM));
            bottomBar.setBackground(TrakTheme.BG_SURFACE);
            JButton addBtn = new JButton("+ Add Project");
            TrakTheme.styleButtonPrimary(addBtn);
            addBtn.addActionListener(e -> openCreateDialog());

            JButton saveBtn = new JButton("Save Changes");
            TrakTheme.styleButtonNav(saveBtn);
            saveBtn.addActionListener(e -> saveProjectEdits(table, model, projects));

            bottomBar.add(addBtn);
            bottomBar.add(saveBtn);
            add(bottomBar, BorderLayout.SOUTH);
        }

        revalidate();
        repaint();
    }

    private void openCreateDialog() {
        new ProjectCreateView(this, guiController.getProjectController()).show();
    }

    private void showSummaryEditor(ProjectDTO project, DefaultTableModel model, int row) {
        JTextArea textArea = new JTextArea(project.summary() != null ? project.summary() : "", 8, 40);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(TrakTheme.FONT_BODY);
        JScrollPane scroll = new JScrollPane(textArea);

        int result = JOptionPane.showConfirmDialog(this, scroll,
                "Edit Description \u2014 " + project.projectName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String newSummary = textArea.getText().trim();
            String original = project.summary() != null ? project.summary() : "";
            if (!newSummary.equals(original)) {
                model.setValueAt(newSummary, row, 2);
                guiController.getProjectController().updateProject(project.projectName(), null, newSummary);
            }
        }
    }

    private void saveProjectEdits(JTable table, DefaultTableModel model, List<ProjectDTO> projects) {
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        int saved = 0;
        for (int i = 0; i < model.getRowCount() && i < projects.size(); i++) {
            ProjectDTO original = projects.get(i);
            String newName = model.getValueAt(i, 1).toString().trim();
            String newSummary = model.getValueAt(i, 2).toString().trim();

            boolean nameChanged = !newName.equals(original.projectName() != null ? original.projectName() : "");
            boolean summaryChanged = !newSummary.equals(original.summary() != null ? original.summary() : "");

            if (nameChanged || summaryChanged) {
                guiController.getProjectController().updateProject(
                        original.projectName(),
                        nameChanged ? newName : null,
                        summaryChanged ? newSummary : null
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
