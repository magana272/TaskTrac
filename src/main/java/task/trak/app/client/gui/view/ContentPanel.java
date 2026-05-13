package task.trak.app.client.gui.view;

import task.trak.api.dto.ProjectDTO;
import task.trak.api.dto.SprintDTO;
import task.trak.api.dto.TaskDTO;
import task.trak.api.service.ProjectService;
import task.trak.api.service.ServiceFactory;
import task.trak.api.service.TaskService;
import task.trak.app.App;
import task.trak.app.client.cli.TTApp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ContentPanel extends JPanel {

    private static final String VIEW_TASKS = "tasks";
    private static final String VIEW_PROJECTS = "projects";
    private static final String VIEW_SPRINTS = "sprints";
    private static final String VIEW_OUTPUT = "output";

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("MMM dd, yyyy");

    private final CardLayout cardLayout;
    private final JPanel taskCardsContainer;
    private final JPanel projectsContainer;
    private final JPanel sprintsContainer;
    private final OutputPanel outputPanel;
    private Consumer<String> onCommand;
    private boolean showCompleted = false;
    private String taskSort = "None";
    private String taskProjectFilter = "All";
    private List<TaskDTO> lastTasks;

    // Track current data for editable tables
    private List<ProjectDTO> currentProjects;
    private List<SprintDTO> currentSprints;

    public ContentPanel() {
        this.cardLayout = new CardLayout();
        setLayout(cardLayout);

        taskCardsContainer = new JPanel(new BorderLayout());
        taskCardsContainer.setBackground(new Color(0xF5, 0xF5, 0xF5));
        add(taskCardsContainer, VIEW_TASKS);

        projectsContainer = new JPanel(new BorderLayout());
        projectsContainer.setBackground(Color.WHITE);
        add(projectsContainer, VIEW_PROJECTS);

        sprintsContainer = new JPanel(new BorderLayout());
        sprintsContainer.setBackground(Color.WHITE);
        add(sprintsContainer, VIEW_SPRINTS);

        outputPanel = new OutputPanel();
        JScrollPane outputScroll = new JScrollPane(outputPanel);
        outputScroll.setBorder(BorderFactory.createEmptyBorder());
        add(outputScroll, VIEW_OUTPUT);

        cardLayout.show(this, VIEW_OUTPUT);
    }

    public void setOnCommand(Consumer<String> onCommand) {
        this.onCommand = onCommand;
    }

    private String getCurrentUsername() {
        App app = TTApp.getInstance();
        if (app != null && app.getSession() != null) {
            return app.getSession().getLogged_in_user();
        }
        return null;
    }

    public void showTasks(List<TaskDTO> tasks) {
        this.lastTasks = tasks;
        renderTasks();
    }

    private void renderTasks() {
        taskCardsContainer.removeAll();
        // Remove old resize listeners
        for (var l : taskCardsContainer.getComponentListeners()) {
            taskCardsContainer.removeComponentListener(l);
        }

        List<TaskDTO> visible = lastTasks;
        if (visible == null) visible = List.of();

        // Filter by project
        if (!"All".equals(taskProjectFilter) && !visible.isEmpty()) {
            visible = visible.stream()
                    .filter(t -> taskProjectFilter.equals(t.projectName()))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Filter out completed unless toggle is on
        if (!showCompleted && !visible.isEmpty()) {
            visible = visible.stream()
                    .filter(t -> !"COMPLETE".equals(t.status()))
                    .collect(java.util.stream.Collectors.toList());
        }

        // Sort
        if ("Due Date".equals(taskSort)) {
            visible = new ArrayList<>(visible);
            visible.sort((a, b) -> {
                if (a.deadline() == null && b.deadline() == null) return 0;
                if (a.deadline() == null) return 1;
                if (b.deadline() == null) return -1;
                return a.deadline().compareTo(b.deadline());
            });
        } else if ("Estimate".equals(taskSort)) {
            visible = new ArrayList<>(visible);
            visible.sort((a, b) -> {
                long ea = parseEstimate(a.estimate());
                long eb = parseEstimate(b.estimate());
                return Long.compare(ea, eb);
            });
        }

        long completedCount = (lastTasks != null) ?
                lastTasks.stream().filter(t -> "COMPLETE".equals(t.status())).count() : 0;

        if (visible.isEmpty() && completedCount == 0) {
            taskCardsContainer.setLayout(new BorderLayout());
            taskCardsContainer.add(new AddPlaceholderPanel("Add a Task", this::showAddTaskDialog), BorderLayout.CENTER);
        } else {
            taskCardsContainer.setLayout(new BorderLayout());

            // Top bar with filters, sort, toggle, add
            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
            toolbar.setBackground(new Color(0xF5, 0xF5, 0xF5));

            // Project filter
            java.util.Set<String> projectNames = new java.util.LinkedHashSet<>();
            projectNames.add("All");
            if (lastTasks != null) {
                for (TaskDTO t : lastTasks) {
                    if (t.projectName() != null) projectNames.add(t.projectName());
                }
            }
            toolbar.add(new JLabel("Project:"));
            JComboBox<String> projectFilter = new JComboBox<>(projectNames.toArray(new String[0]));
            projectFilter.setSelectedItem(taskProjectFilter);
            projectFilter.addActionListener(e -> {
                taskProjectFilter = (String) projectFilter.getSelectedItem();
                renderTasks();
            });
            toolbar.add(projectFilter);

            toolbar.add(Box.createHorizontalStrut(8));

            // Sort
            toolbar.add(new JLabel("Sort:"));
            JComboBox<String> sortCombo = new JComboBox<>(new String[]{"None", "Due Date", "Estimate"});
            sortCombo.setSelectedItem(taskSort);
            sortCombo.addActionListener(e -> {
                taskSort = (String) sortCombo.getSelectedItem();
                renderTasks();
            });
            toolbar.add(sortCombo);

            toolbar.add(Box.createHorizontalStrut(8));

            JCheckBox archiveToggle = new JCheckBox("Show completed (" + completedCount + ")");
            archiveToggle.setSelected(showCompleted);
            archiveToggle.setOpaque(false);
            archiveToggle.addActionListener(e -> {
                showCompleted = archiveToggle.isSelected();
                renderTasks();
            });
            toolbar.add(archiveToggle);

            toolbar.add(Box.createHorizontalStrut(8));

            JButton addBtn = new JButton("+ Add Task");
            addBtn.setFocusPainted(false);
            addBtn.addActionListener(e -> showAddTaskDialog());
            toolbar.add(addBtn);

            taskCardsContainer.add(toolbar, BorderLayout.NORTH);

            // Card grid
            JPanel gridPanel = new JPanel(new GridBagLayout());
            gridPanel.setBackground(new Color(0xF5, 0xF5, 0xF5));

            List<JComponent> cards = new ArrayList<>();
            for (TaskDTO task : visible) {
                cards.add(new TaskCardPanel(task, onCommand));
            }

            if (cards.isEmpty()) {
                JLabel emptyLabel = new JLabel("  All tasks completed. Toggle above to view.");
                emptyLabel.setForeground(Color.GRAY);
                emptyLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
                cards.add(emptyLabel);
            }

            layoutCards(gridPanel, cards);

            JScrollPane scroll = new JScrollPane(gridPanel);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            taskCardsContainer.add(scroll, BorderLayout.CENTER);

            taskCardsContainer.addComponentListener(new java.awt.event.ComponentAdapter() {
                @Override
                public void componentResized(java.awt.event.ComponentEvent e) {
                    layoutCards(gridPanel, cards);
                    gridPanel.revalidate();
                    gridPanel.repaint();
                }
            });
        }
        taskCardsContainer.revalidate();
        taskCardsContainer.repaint();
        cardLayout.show(this, VIEW_TASKS);
    }

    public void showProjects(List<ProjectDTO> projects) {
        this.currentProjects = projects;
        projectsContainer.removeAll();
        if (projects == null || projects.isEmpty()) {
            projectsContainer.setLayout(new BorderLayout());
            projectsContainer.add(new AddPlaceholderPanel("Create a Project", this::showAddProjectDialog), BorderLayout.CENTER);
        } else {
            projectsContainer.setLayout(new BorderLayout());
            String[] columns = {"ID", "Name", "Description", "Owner", "Members", "Tasks", "Sprints"};
            String[] editableColumns = {"Name", "Description"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    String colName = getColumnName(col);
                    return colName.equals("Name") || colName.equals("Description");
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
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = table.rowAtPoint(e.getPoint());
                        int col = table.columnAtPoint(e.getPoint());
                        if (row < 0 || row >= projects.size()) return;
                        ProjectDTO p = projects.get(row);
                        String colName = model.getColumnName(col);
                        String currentUser = getCurrentUsername();
                        boolean isOwner = currentUser != null && currentUser.equals(p.ownerUsername());
                        switch (colName) {
                            case "Members" -> showMembersDialog(p, isOwner);
                            case "Tasks" -> showTasksManagerDialog(p, isOwner);
                            case "Sprints" -> showAddSprintDialogForProject(p);
                            case "Description" -> {
                                if (isOwner) showSummaryEditor(p, model, row);
                            }
                        }
                    }
                }
            });
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            projectsContainer.add(scrollPane, BorderLayout.CENTER);

            JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
            JButton addBtn = new JButton("+ Add Project");
            addBtn.setFocusPainted(false);
            addBtn.addActionListener(e -> showAddProjectDialog());
            JButton saveBtn = new JButton("Save Changes");
            saveBtn.setFocusPainted(false);
            saveBtn.addActionListener(e -> saveProjectEdits(table, model));
            bottomBar.add(addBtn);
            bottomBar.add(saveBtn);
            projectsContainer.add(bottomBar, BorderLayout.SOUTH);
        }
        projectsContainer.revalidate();
        projectsContainer.repaint();
        cardLayout.show(this, VIEW_PROJECTS);
    }

    public void showSprints(List<SprintDTO> sprints) {
        this.currentSprints = sprints;
        sprintsContainer.removeAll();
        if (sprints == null || sprints.isEmpty()) {
            sprintsContainer.setLayout(new BorderLayout());
            sprintsContainer.add(new AddPlaceholderPanel("Create a Sprint", this::showAddSprintDialog), BorderLayout.CENTER);
        } else {
            sprintsContainer.setLayout(new BorderLayout());
            String[] columns = {"ID", "Name", "Project", "Tasks", "Start Date", "End Date"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    String colName = getColumnName(col);
                    return colName.equals("Start Date") || colName.equals("End Date");
                }
            };
            for (SprintDTO s : sprints) {
                model.addRow(new Object[]{
                        s.id(),
                        s.name() != null ? s.name() : "",
                        s.projectName() != null ? s.projectName() : "",
                        s.taskIds() != null ? s.taskIds().size() : 0,
                        s.startDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(s.startDate()) : "",
                        s.endDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(s.endDate()) : ""
                });
            }
            JTable table = createCopyableTable(model);
            table.setRowHeight(28);
            table.getTableHeader().setReorderingAllowed(false);
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
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
            sprintsContainer.add(scrollPane, BorderLayout.CENTER);

            JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
            JButton addSprintBtn = new JButton("+ Add Sprint");
            addSprintBtn.setFocusPainted(false);
            addSprintBtn.addActionListener(e -> showAddSprintDialog());
            bottomBar.add(addSprintBtn);
            JButton saveBtn = new JButton("Save Changes");
            saveBtn.setFocusPainted(false);
            saveBtn.addActionListener(e -> saveSprintEdits(table, model));
            bottomBar.add(saveBtn);
            sprintsContainer.add(bottomBar, BorderLayout.SOUTH);
        }
        sprintsContainer.revalidate();
        sprintsContainer.repaint();
        cardLayout.show(this, VIEW_SPRINTS);
    }

    public void showOutput(String text) {
        outputPanel.appendOutput(text);
        cardLayout.show(this, VIEW_OUTPUT);
    }

    public void showCommand(String cmd) {
        outputPanel.appendCommand(cmd);
    }

    public void showError(String error) {
        outputPanel.appendError(error);
        cardLayout.show(this, VIEW_OUTPUT);
    }


    private void layoutCards(JPanel panel, List<JComponent> cards) {
        panel.removeAll();
        int minCardWidth = 240;
        int gap = 10;
        int containerWidth = taskCardsContainer.getWidth() - gap;
        if (containerWidth <= 0) containerWidth = 900;
        int cols = Math.max(1, (containerWidth + gap) / (minCardWidth + gap));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(gap / 2, gap / 2, gap / 2, gap / 2);
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        for (int i = 0; i < cards.size(); i++) {
            gbc.gridx = i % cols;
            gbc.gridy = i / cols;
            gbc.weighty = 0;
            panel.add(cards.get(i), gbc);
        }

        // Vertical filler to push cards to top
        gbc.gridx = 0;
        gbc.gridy = (cards.size() / cols) + 1;
        gbc.gridwidth = cols;
        gbc.weighty = 1.0;
        panel.add(Box.createGlue(), gbc);
    }

    private JSpinner createDateSpinner() {
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner spinner = new JSpinner(dateModel);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);
        return spinner;
    }

    private void showAddTaskDialog() {
        if (onCommand == null) return;

        // Load projects for dropdown
        ProjectService projectService = ServiceFactory.projectService();
        App app = TTApp.getInstance();
        String username = (app != null && app.getSession() != null) ? app.getSession().getLogged_in_user() : null;
        List<ProjectDTO> myProjects = username != null ? projectService.listByUser(username) : projectService.listAll();

        JPanel panel = new JPanel(new GridLayout(6, 2, 4, 4));
        JTextField titleField = new JTextField();
        JComboBox<String> projectCombo = new JComboBox<>();
        for (ProjectDTO p : myProjects) {
            projectCombo.addItem(p.projectName() + "  (#" + p.id() + ")");
        }
        JComboBox<String> assignedCombo = new JComboBox<>();
        // Populate assigned dropdown when project selection changes
        Runnable updateAssignees = () -> {
            assignedCombo.removeAllItems();
            int idx = projectCombo.getSelectedIndex();
            if (idx >= 0 && idx < myProjects.size()) {
                ProjectDTO selected = myProjects.get(idx);
                if (selected.ownerUsername() != null) {
                    assignedCombo.addItem(selected.ownerUsername());
                }
                if (selected.memberUsernames() != null) {
                    for (String member : selected.memberUsernames()) {
                        if (!member.equals(selected.ownerUsername())) {
                            assignedCombo.addItem(member);
                        }
                    }
                }
            }
        };
        updateAssignees.run();
        projectCombo.addActionListener(e -> updateAssignees.run());

        JTextField summaryField = new JTextField();
        JSpinner deadlineSpinner = createDateSpinner();
        JCheckBox deadlineCheck = new JCheckBox("Set deadline");
        deadlineSpinner.setEnabled(false);
        deadlineCheck.addActionListener(e -> deadlineSpinner.setEnabled(deadlineCheck.isSelected()));
        JPanel deadlinePanel = new JPanel(new BorderLayout(4, 0));
        deadlinePanel.add(deadlineCheck, BorderLayout.WEST);
        deadlinePanel.add(deadlineSpinner, BorderLayout.CENTER);
        JTextField estimateField = new JTextField();
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Project:"));
        panel.add(projectCombo);
        panel.add(new JLabel("Assigned To:"));
        panel.add(assignedCombo);
        panel.add(new JLabel("Summary:"));
        panel.add(summaryField);
        panel.add(new JLabel("Deadline:"));
        panel.add(deadlinePanel);
        panel.add(new JLabel("Estimate:"));
        panel.add(estimateField);

        if (myProjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No projects found. Create a project first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Task", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            int selectedIdx = projectCombo.getSelectedIndex();
            if (title.isEmpty() || selectedIdx < 0) {
                JOptionPane.showMessageDialog(this, "Title and Project are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String projectId = String.valueOf(myProjects.get(selectedIdx).id());
            StringBuilder cmd = new StringBuilder("task add --title " + title + " --project " + projectId);
            String assignee = (String) assignedCombo.getSelectedItem();
            if (assignee != null && !assignee.isEmpty()) cmd.append(" --assigned_to ").append(assignee);
            if (!summaryField.getText().trim().isEmpty())
                cmd.append(" --summary ").append(summaryField.getText().trim());
            if (deadlineCheck.isSelected()) {
                java.util.Date d = (java.util.Date) deadlineSpinner.getValue();
                cmd.append(" --deadline ").append(new SimpleDateFormat("yyyy-MM-dd").format(d));
            }
            if (!estimateField.getText().trim().isEmpty())
                cmd.append(" --estimate ").append(estimateField.getText().trim());
            onCommand.accept(cmd.toString());
        }
    }

    private void showAddProjectDialog() {
        if (onCommand == null) return;
        JPanel panel = new JPanel(new GridLayout(2, 2, 4, 4));
        JTextField nameField = new JTextField();
        JTextField summaryField = new JTextField();
        panel.add(new JLabel("Project Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Summary:"));
        panel.add(summaryField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create Project", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Project name is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            StringBuilder cmd = new StringBuilder("project add " + name);
            if (!summaryField.getText().trim().isEmpty())
                cmd.append(" --summary ").append(summaryField.getText().trim());
            onCommand.accept(cmd.toString());
        }
    }

    private void showAddSprintDialog() {
        if (onCommand == null) return;

        // Load projects
        ProjectService projectService = ServiceFactory.projectService();
        App app = TTApp.getInstance();
        String username = (app != null && app.getSession() != null) ? app.getSession().getLogged_in_user() : null;
        List<ProjectDTO> myProjects = username != null ? projectService.listByUser(username) : projectService.listAll();

        if (myProjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No projects found. Create a project first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Load all tasks for task selection
        TaskService taskService = ServiceFactory.taskService();
        List<TaskDTO> allTasks = taskService.listAll();

        // --- Build dialog ---
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Sprint name
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Sprint Name:"), gbc);
        JTextField nameField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        // Project dropdown
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Project:"), gbc);
        JComboBox<String> projectCombo = new JComboBox<>();
        for (ProjectDTO p : myProjects) {
            projectCombo.addItem(p.projectName() + "  (#" + p.id() + ")");
        }
        gbc.gridx = 1;
        panel.add(projectCombo, gbc);

        // Start date
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Start Date:"), gbc);
        JSpinner startSpinner = createDateSpinner();
        gbc.gridx = 1;
        panel.add(startSpinner, gbc);

        // End date
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(new JLabel("End Date:"), gbc);
        JSpinner endSpinner = createDateSpinner();
        gbc.gridx = 1;
        panel.add(endSpinner, gbc);

        // Task selection (checkboxes in a scrollable list)
        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(new JLabel("Add Tasks:"), gbc);

        JPanel taskCheckPanel = new JPanel();
        taskCheckPanel.setLayout(new BoxLayout(taskCheckPanel, BoxLayout.Y_AXIS));
        List<JCheckBox> taskChecks = new java.util.ArrayList<>();

        Runnable updateTaskList = () -> {
            taskCheckPanel.removeAll();
            taskChecks.clear();
            int idx = projectCombo.getSelectedIndex();
            if (idx >= 0 && idx < myProjects.size()) {
                ProjectDTO selectedProj = myProjects.get(idx);
                List<TaskDTO> projectTasks = allTasks.stream()
                        .filter(t -> selectedProj.projectName().equals(t.projectName()))
                        .collect(java.util.stream.Collectors.toList());
                if (projectTasks.isEmpty()) {
                    taskCheckPanel.add(new JLabel("  No tasks in this project."));
                } else {
                    for (TaskDTO t : projectTasks) {
                        String label = t.title() + " [" + t.status() + "]"
                                + (t.assignedTo() != null ? " - " + t.assignedTo() : " - unassigned");
                        JCheckBox cb = new JCheckBox(label);
                        cb.putClientProperty("taskDTO", t);
                        taskChecks.add(cb);
                        taskCheckPanel.add(cb);
                    }
                }
            }
            taskCheckPanel.revalidate();
            taskCheckPanel.repaint();
        };
        updateTaskList.run();
        projectCombo.addActionListener(e -> updateTaskList.run());

        JScrollPane taskScroll = new JScrollPane(taskCheckPanel);
        taskScroll.setPreferredSize(new Dimension(300, 120));
        gbc.gridx = 1;
        panel.add(taskScroll, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create Sprint", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;

        String sprintName = nameField.getText().trim();
        int projIdx = projectCombo.getSelectedIndex();
        if (sprintName.isEmpty() || projIdx < 0) {
            JOptionPane.showMessageDialog(this, "Sprint name and project are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ProjectDTO selectedProject = myProjects.get(projIdx);
        String startDate = new SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) startSpinner.getValue());
        String endDate = new SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) endSpinner.getValue());

        // Collect selected tasks, prompt to assign unassigned ones
        List<TaskDTO> selectedTasks = new java.util.ArrayList<>();
        for (JCheckBox cb : taskChecks) {
            if (cb.isSelected()) {
                selectedTasks.add((TaskDTO) cb.getClientProperty("taskDTO"));
            }
        }

        // Check for unassigned tasks and prompt
        for (TaskDTO t : selectedTasks) {
            if (t.assignedTo() == null || t.assignedTo().isEmpty()) {
                // Build assignee options from project owner + members
                java.util.List<String> assignees = new java.util.ArrayList<>();
                if (selectedProject.ownerUsername() != null) assignees.add(selectedProject.ownerUsername());
                if (selectedProject.memberUsernames() != null) {
                    for (String m : selectedProject.memberUsernames()) {
                        if (!assignees.contains(m)) assignees.add(m);
                    }
                }
                if (assignees.isEmpty()) continue;

                String chosen = (String) JOptionPane.showInputDialog(this,
                        "Task \"" + t.title() + "\" is unassigned.\nAssign to:",
                        "Assign Task", JOptionPane.QUESTION_MESSAGE, null,
                        assignees.toArray(new String[0]), assignees.get(0));
                if (chosen != null) {
                    onCommand.accept("task update " + t.id() + " --assigned_to " + chosen);
                }
            }
        }

        // Create sprint
        onCommand.accept("sprint add " + sprintName + " --project " + selectedProject.projectName());

        // Wait briefly then update dates and add tasks
        // Use the sprint name + project to update since we don't have the ID yet
        onCommand.accept("sprint update " + sprintName + " --project " + selectedProject.projectName()
                + " --start_date " + startDate + " --end_date " + endDate);
        for (TaskDTO t : selectedTasks) {
            onCommand.accept("sprint update " + sprintName + " --project " + selectedProject.projectName()
                    + " --add_task " + t.id());
        }
    }

    // --- Double-click dialogs for project table cells ---

    private void showMembersDialog(ProjectDTO project, boolean isOwner) {
        if (onCommand == null) return;

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Members — " + project.projectName(), java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(350, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 4));

        // Header with owner
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        header.add(new JLabel("Owner:"));
        JLabel ownerLabel = new JLabel(project.ownerUsername() != null ? project.ownerUsername() : "-");
        ownerLabel.setFont(ownerLabel.getFont().deriveFont(Font.BOLD));
        header.add(ownerLabel);
        dialog.add(header, BorderLayout.NORTH);

        // Member list panel
        JPanel memberListPanel = new JPanel();
        memberListPanel.setLayout(new BoxLayout(memberListPanel, BoxLayout.Y_AXIS));
        memberListPanel.setBackground(Color.WHITE);

        List<String> currentMembers = project.memberUsernames() != null
                ? new ArrayList<>(project.memberUsernames()) : new ArrayList<>();

        Runnable refreshList = () -> {
            memberListPanel.removeAll();
            if (currentMembers.isEmpty()) {
                JLabel emptyLabel = new JLabel("  No members yet");
                emptyLabel.setForeground(Color.GRAY);
                emptyLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
                memberListPanel.add(emptyLabel);
            } else {
                for (String member : currentMembers) {
                    JPanel row = new JPanel(new BorderLayout(4, 0));
                    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
                    row.setBackground(Color.WHITE);
                    row.setBorder(new EmptyBorder(4, 12, 4, 8));

                    JLabel nameLabel = new JLabel(member);
                    nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 13f));
                    row.add(nameLabel, BorderLayout.CENTER);

                    // Only owner can remove members
                    if (isOwner && !member.equals(project.ownerUsername())) {
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
                                currentMembers.remove(member);
                                // Update project with remaining members
                                String memberStr = "[" + String.join(",", currentMembers) + "]";
                                onCommand.accept("project update " + project.projectName() + " --members " + memberStr);
                                // Refresh the list visually
                                ((Runnable) () -> {
                                    memberListPanel.removeAll();
                                    showMembersRefresh(memberListPanel, currentMembers, project, dialog);
                                    memberListPanel.revalidate();
                                    memberListPanel.repaint();
                                }).run();
                            }
                        });
                        row.add(removeBtn, BorderLayout.EAST);
                    }

                    memberListPanel.add(row);
                    memberListPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
                }
            }
            memberListPanel.revalidate();
            memberListPanel.repaint();
        };
        refreshList.run();

        JScrollPane scroll = new JScrollPane(memberListPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        dialog.add(scroll, BorderLayout.CENTER);

        // Bottom: add member button (owner only)
        if (isOwner) {
            JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
            JButton addBtn = new JButton("+ Add Member");
            addBtn.setFocusPainted(false);
            addBtn.addActionListener(e -> {
                String username = JOptionPane.showInputDialog(dialog,
                        "Enter username to add:", "Add Member", JOptionPane.PLAIN_MESSAGE);
                if (username != null && !username.trim().isEmpty()) {
                    String trimmed = username.trim();
                    if (!currentMembers.contains(trimmed)) {
                        currentMembers.add(trimmed);
                        onCommand.accept("addmember " + project.projectName() + " " + trimmed);
                        refreshList.run();
                    }
                }
            });
            bottomPanel.add(addBtn);
            dialog.add(bottomPanel, BorderLayout.SOUTH);
        }

        dialog.setVisible(true);
    }

    private void showMembersRefresh(JPanel memberListPanel, List<String> currentMembers, ProjectDTO project, JDialog dialog) {
        if (currentMembers.isEmpty()) {
            JLabel emptyLabel = new JLabel("  No members yet");
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setBorder(new EmptyBorder(8, 8, 8, 8));
            memberListPanel.add(emptyLabel);
        } else {
            for (String member : new ArrayList<>(currentMembers)) {
                JPanel row = new JPanel(new BorderLayout(4, 0));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
                row.setBackground(Color.WHITE);
                row.setBorder(new EmptyBorder(4, 12, 4, 8));

                JLabel nameLabel = new JLabel(member);
                nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 13f));
                row.add(nameLabel, BorderLayout.CENTER);

                if (!member.equals(project.ownerUsername())) {
                    JButton removeBtn = new JButton("\u2715");
                    removeBtn.setFocusPainted(false);
                    removeBtn.setBorderPainted(false);
                    removeBtn.setContentAreaFilled(false);
                    removeBtn.setForeground(new Color(0xC6, 0x28, 0x28));
                    removeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    removeBtn.addActionListener(ev -> {
                        int confirm = JOptionPane.showConfirmDialog(dialog,
                                "Remove \"" + member + "\" from the project?",
                                "Confirm", JOptionPane.YES_NO_OPTION);
                        if (confirm == JOptionPane.YES_OPTION) {
                            currentMembers.remove(member);
                            String memberStr = "[" + String.join(",", currentMembers) + "]";
                            onCommand.accept("project update " + project.projectName() + " --members " + memberStr);
                            memberListPanel.removeAll();
                            showMembersRefresh(memberListPanel, currentMembers, project, dialog);
                            memberListPanel.revalidate();
                            memberListPanel.repaint();
                        }
                    });
                    row.add(removeBtn, BorderLayout.EAST);
                }

                memberListPanel.add(row);
                memberListPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
            }
        }
    }

    private void showTasksManagerDialog(ProjectDTO project, boolean isOwner) {
        if (onCommand == null) return;

        TaskService taskService = ServiceFactory.taskService();
        List<TaskDTO> projectTasks = taskService.listAll().stream()
                .filter(t -> project.projectName().equals(t.projectName()))
                .collect(java.util.stream.Collectors.toList());

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Tasks — " + project.projectName(), java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 4));

        // Task list
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        Runnable[] refreshRef = new Runnable[1];
        refreshRef[0] = () -> {
            listPanel.removeAll();
            List<TaskDTO> fresh = taskService.listAll().stream()
                    .filter(t -> project.projectName().equals(t.projectName()))
                    .collect(java.util.stream.Collectors.toList());

            if (fresh.isEmpty()) {
                JLabel emptyLabel = new JLabel("  No tasks in this project");
                emptyLabel.setForeground(Color.GRAY);
                emptyLabel.setBorder(new EmptyBorder(12, 12, 12, 12));
                listPanel.add(emptyLabel);
            } else {
                for (TaskDTO t : fresh) {
                    JPanel row = new JPanel(new BorderLayout(8, 0));
                    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                    row.setBackground(Color.WHITE);
                    row.setBorder(new EmptyBorder(4, 12, 4, 8));

                    // Status color dot + title
                    String statusStr = t.status() != null ? t.status() : "READY";
                    Color statusColor = switch (statusStr) {
                        case "COMPLETE" -> new Color(0x4C, 0xAF, 0x50);
                        case "INPROGRESS" -> new Color(0xF9, 0xA8, 0x25);
                        default -> new Color(0xD3, 0x2F, 0x2F);
                    };
                    JLabel dot = new JLabel("\u25CF ");
                    dot.setForeground(statusColor);
                    JLabel titleLabel = new JLabel(t.title() + "  [" + statusStr + "]");
                    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 12f));
                    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
                    left.setOpaque(false);
                    left.add(dot);
                    left.add(titleLabel);
                    row.add(left, BorderLayout.CENTER);

                    // Edit + Delete buttons
                    if (isOwner) {
                        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
                        buttons.setOpaque(false);

                        JButton editBtn = new JButton("Edit");
                        editBtn.setFocusPainted(false);
                        editBtn.addActionListener(ev -> {
                            showEditTaskDialog(t, dialog);
                            refreshRef[0].run();
                        });

                        JButton deleteBtn = new JButton("\u2715");
                        deleteBtn.setFocusPainted(false);
                        deleteBtn.setForeground(new Color(0xC6, 0x28, 0x28));
                        deleteBtn.addActionListener(ev -> {
                            int confirm = JOptionPane.showConfirmDialog(dialog,
                                    "Delete task \"" + t.title() + "\"?",
                                    "Confirm", JOptionPane.YES_NO_OPTION);
                            if (confirm == JOptionPane.YES_OPTION) {
                                taskService.deleteById(t.id());
                                refreshRef[0].run();
                            }
                        });

                        buttons.add(editBtn);
                        buttons.add(deleteBtn);
                        row.add(buttons, BorderLayout.EAST);
                    }

                    listPanel.add(row);
                    listPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
                }
            }
            listPanel.revalidate();
            listPanel.repaint();
        };
        refreshRef[0].run();

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        dialog.add(scroll, BorderLayout.CENTER);

        // Bottom: add button (owner only)
        if (!isOwner) {
            dialog.setVisible(true);
            return;
        }
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton addBtn = new JButton("+ Add Task");
        addBtn.setFocusPainted(false);
        addBtn.addActionListener(e -> {
            showAddTaskDialogForProject(project);
            Timer timer = new Timer(500, te -> refreshRef[0].run());
            timer.setRepeats(false);
            timer.start();
        });
        bottomPanel.add(addBtn);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void showSprintTasksDialog(SprintDTO sprint) {
        if (onCommand == null) return;

        TaskService taskService = ServiceFactory.taskService();
        List<Long> taskIds = sprint.taskIds() != null ? sprint.taskIds() : List.of();

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Tasks — " + sprint.name() + " (" + sprint.projectName() + ")",
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(550, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 4));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        if (taskIds.isEmpty()) {
            JLabel emptyLabel = new JLabel("  No tasks in this sprint");
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setBorder(new EmptyBorder(12, 12, 12, 12));
            listPanel.add(emptyLabel);
        } else {
            for (Long tid : taskIds) {
                TaskDTO t = taskService.getById(tid);
                if (t == null) continue;

                JPanel row = new JPanel(new BorderLayout(8, 0));
                row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
                row.setBackground(Color.WHITE);
                row.setBorder(new EmptyBorder(4, 12, 4, 8));

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

                JButton editBtn = new JButton("Edit");
                editBtn.setFocusPainted(false);
                editBtn.addActionListener(ev -> showEditTaskDialog(t, dialog));
                row.add(editBtn, BorderLayout.EAST);

                listPanel.add(row);
                listPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    private void showEditTaskDialog(TaskDTO task, java.awt.Window parent) {
        JPanel panel = new JPanel(new GridLayout(5, 2, 4, 4));
        JTextField titleField = new JTextField(task.title() != null ? task.title() : "");
        JTextField assignedField = new JTextField(task.assignedTo() != null ? task.assignedTo() : "");
        JTextField summaryField = new JTextField(task.summary() != null ? task.summary() : "");
        JComboBox<String> statusField = new JComboBox<>(new String[]{"READY", "INPROGRESS", "COMPLETE"});
        statusField.setSelectedItem(task.status() != null ? task.status() : "READY");
        JLabel idLabel = new JLabel(String.valueOf(task.id()));
        idLabel.setEnabled(false);

        panel.add(new JLabel("Task ID:"));
        panel.add(idLabel);
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Assigned To:"));
        panel.add(assignedField);
        panel.add(new JLabel("Summary:"));
        panel.add(summaryField);
        panel.add(new JLabel("Status:"));
        panel.add(statusField);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Edit Task",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            StringBuilder cmd = new StringBuilder("task update " + task.id());
            boolean changed = false;

            String newTitle = titleField.getText().trim();
            if (!newTitle.equals(task.title() != null ? task.title() : "")) {
                cmd.append(" --title ").append(newTitle);
                changed = true;
            }
            String newAssigned = assignedField.getText().trim();
            if (!newAssigned.equals(task.assignedTo() != null ? task.assignedTo() : "")) {
                cmd.append(" --assigned_to ").append(newAssigned);
                changed = true;
            }
            String newSummary = summaryField.getText().trim();
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

    private void showAddTaskDialogForProject(ProjectDTO project) {
        if (onCommand == null) return;
        JPanel panel = new JPanel(new GridLayout(5, 2, 4, 4));
        JTextField titleField = new JTextField();
        JLabel projectLabel = new JLabel(project.projectName());
        projectLabel.setEnabled(false);

        // Assignee dropdown from owner + members
        JComboBox<String> assignedCombo = new JComboBox<>();
        if (project.ownerUsername() != null) assignedCombo.addItem(project.ownerUsername());
        if (project.memberUsernames() != null) {
            for (String m : project.memberUsernames()) {
                if (!m.equals(project.ownerUsername())) assignedCombo.addItem(m);
            }
        }

        JTextField summaryField = new JTextField();
        JTextField estimateField = new JTextField();
        panel.add(new JLabel("Project:"));
        panel.add(projectLabel);
        panel.add(new JLabel("Title:"));
        panel.add(titleField);
        panel.add(new JLabel("Assigned To:"));
        panel.add(assignedCombo);
        panel.add(new JLabel("Summary:"));
        panel.add(summaryField);
        panel.add(new JLabel("Estimate:"));
        panel.add(estimateField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Task to " + project.projectName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String title = titleField.getText().trim();
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Title is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            StringBuilder cmd = new StringBuilder("task add --title " + title + " --project " + project.id());
            String assignee = (String) assignedCombo.getSelectedItem();
            if (assignee != null && !assignee.isEmpty()) cmd.append(" --assigned_to ").append(assignee);
            if (!summaryField.getText().trim().isEmpty())
                cmd.append(" --summary ").append(summaryField.getText().trim());
            if (!estimateField.getText().trim().isEmpty())
                cmd.append(" --estimate ").append(estimateField.getText().trim());
            onCommand.accept(cmd.toString());
        }
    }

    private void showAddSprintDialogForProject(ProjectDTO project) {
        if (onCommand == null) return;
        JPanel panel = new JPanel(new GridLayout(3, 2, 4, 4));
        JTextField nameField = new JTextField();
        JSpinner startSpinner = createDateSpinner();
        JSpinner endSpinner = createDateSpinner();
        panel.add(new JLabel("Sprint Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Start Date:"));
        panel.add(startSpinner);
        panel.add(new JLabel("End Date:"));
        panel.add(endSpinner);

        int result = JOptionPane.showConfirmDialog(this, panel, "Create Sprint for " + project.projectName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Sprint name is required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String startDate = new SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) startSpinner.getValue());
            String endDate = new SimpleDateFormat("yyyy-MM-dd").format((java.util.Date) endSpinner.getValue());
            onCommand.accept("sprint add " + name + " --project " + project.projectName());
            onCommand.accept("sprint update " + name + " --project " + project.projectName()
                    + " --start_date " + startDate + " --end_date " + endDate);
        }
    }

    private void showSummaryEditor(ProjectDTO project, DefaultTableModel model, int row) {
        JTextArea textArea = new JTextArea(project.summary() != null ? project.summary() : "", 8, 40);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        JScrollPane scroll = new JScrollPane(textArea);

        int result = JOptionPane.showConfirmDialog(this, scroll,
                "Edit Description — " + project.projectName(),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String newSummary = textArea.getText().trim();
            if (!newSummary.equals(project.summary() != null ? project.summary() : "")) {
                model.setValueAt(newSummary, row, 2);
                onCommand.accept("project update " + project.projectName() + " --summary " + newSummary);
            }
        }
    }

    // --- Save edits ---

    private void saveProjectEdits(JTable table, DefaultTableModel model) {
        if (onCommand == null || currentProjects == null) return;
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        int saved = 0;
        for (int i = 0; i < model.getRowCount() && i < currentProjects.size(); i++) {
            ProjectDTO original = currentProjects.get(i);
            String newName = model.getValueAt(i, 1).toString().trim();
            String newSummary = model.getValueAt(i, 2).toString().trim();

            boolean nameChanged = !newName.equals(original.projectName() != null ? original.projectName() : "");
            boolean summaryChanged = !newSummary.equals(original.summary() != null ? original.summary() : "");

            if (nameChanged || summaryChanged) {
                StringBuilder cmd = new StringBuilder("project update " + original.projectName());
                if (nameChanged) cmd.append(" --name ").append(newName);
                if (summaryChanged) cmd.append(" --summary ").append(newSummary);
                onCommand.accept(cmd.toString());
                saved++;
            }
        }
        if (saved == 0) {
            JOptionPane.showMessageDialog(this, "No changes to save.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveSprintEdits(JTable table, DefaultTableModel model) {
        if (onCommand == null || currentSprints == null) return;
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        int saved = 0;
        for (int i = 0; i < model.getRowCount() && i < currentSprints.size(); i++) {
            SprintDTO original = currentSprints.get(i);
            String newStart = model.getValueAt(i, 4).toString().trim();
            String newEnd = model.getValueAt(i, 5).toString().trim();
            String origStart = original.startDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(original.startDate()) : "";
            String origEnd = original.endDate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(original.endDate()) : "";

            boolean startChanged = !newStart.equals(origStart);
            boolean endChanged = !newEnd.equals(origEnd);

            if (startChanged || endChanged) {
                StringBuilder cmd = new StringBuilder("sprint update " + original.id());
                if (startChanged && !newStart.isEmpty()) cmd.append(" --start_date ").append(newStart);
                if (endChanged && !newEnd.isEmpty()) cmd.append(" --end_date ").append(newEnd);
                onCommand.accept(cmd.toString());
                saved++;
            }
        }
        if (saved == 0) {
            JOptionPane.showMessageDialog(this, "No changes to save.", "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private long parseEstimate(String est) {
        if (est == null || est.isEmpty()) return Long.MAX_VALUE;
        try {
            String lower = est.trim().toLowerCase();
            if (lower.endsWith("h")) return (long) (Double.parseDouble(lower.replace("h", "")) * 60);
            if (lower.endsWith("d")) return (long) (Double.parseDouble(lower.replace("d", "")) * 60 * 8);
            if (lower.endsWith("m")) return (long) Double.parseDouble(lower.replace("m", ""));
            return Long.parseLong(lower);
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
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
            public void actionPerformed(java.awt.event.ActionEvent e) {
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();
                if (row >= 0 && col >= 0) {
                    Object val = table.getValueAt(row, col);
                    String text = val != null ? val.toString() : "";
                    java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                            .setContents(new java.awt.datatransfer.StringSelection(text), null);
                }
            }
        };
        table.getInputMap().put(copy, "copy");
        table.getInputMap().put(macCopy, "copy");
        table.getActionMap().put("copy", copyAction);
        return table;
    }
}
