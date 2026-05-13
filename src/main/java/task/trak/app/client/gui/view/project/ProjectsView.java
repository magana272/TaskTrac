package task.trak.app.client.gui.view.project;

import task.trak.api.dto.ProjectDTO;
import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.view.DataView;
import task.trak.app.client.gui.view.task.TaskAddView;
import task.trak.app.client.gui.viewmodel.ViewModelChangeListener;
import task.trak.app.client.gui.viewmodel.ViewModelChangeType;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ProjectsView extends DataView implements ViewModelChangeListener {

    private final GUIController guiController;

    public ProjectsView(GUIController guiController) {
        this.guiController = guiController;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

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
            addBtn.addActionListener(e -> openCreateDialog());
            JPanel placeholder = new JPanel(new GridBagLayout());
            placeholder.add(addBtn);
            add(placeholder, BorderLayout.CENTER);
        } else {
            String[] columns = {"ID", "Name", "Description", "Owner", "Members", "Tasks", "Sprints"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    String colName = getColumnName(col);
                    return "Name".equals(colName) || "Description".equals(colName);
                }
            };

            for (ProjectDTO p : projects) {
                model.addRow(new Object[]{
                        p.id(),
                        p.projectName() != null ? p.projectName() : "",
                        p.summary() != null ? p.summary() : "",
                        p.ownerUsername() != null ? p.ownerUsername() : "-",
                        p.memberCount(),
                        p.taskCount(),
                        p.sprintCount()
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
                        if (row < 0 || row >= projects.size()) return;
                        ProjectDTO p = projects.get(row);
                        String colName = model.getColumnName(col);
                        switch (colName) {
                            case "Members" -> {
                                new ProjectAddView(ProjectsView.this, guiController.getProjectController(), p).show();
                            }
                            case "Description" -> {
                                showSummaryEditor(p, model, row);
                            }
                            case "Tasks" -> {
                                new TaskAddView(ProjectsView.this,
                                        guiController.getTaskController(),
                                        guiController.getProjectController().getViewModel().get()).show();
                            }
                        }
                    }
                }
            });

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            add(scrollPane, BorderLayout.CENTER);

            JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
            JButton addBtn = new JButton("+ Add Project");
            addBtn.setFocusPainted(false);
            addBtn.addActionListener(e -> openCreateDialog());

            JButton saveBtn = new JButton("Save Changes");
            saveBtn.setFocusPainted(false);
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
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
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
