package task.trak.app.client.gui.view;

import task.trak.app.client.gui.controller.GUIController;
import task.trak.app.client.gui.view.error.ErrorView;
import task.trak.app.client.gui.view.panel.StatusPanel;
import task.trak.app.client.gui.viewmodel.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class MainFrame extends JFrame implements ViewModelChangeListener {

    private final GUIController controller;
    private final UserViewModel userViewModel;

    private final StatusPanel statusPanel;
    private final DashboardView dashboardView;

    public MainFrame(GUIController controller,
                     TaskViewModel taskViewModel,
                     ProjectViewModel projectViewModel,
                     SprintViewModel sprintViewModel,
                     UserViewModel userViewModel) {
        super("Trak");
        setUndecorated(true);
        this.controller = controller;
        this.userViewModel = userViewModel;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1060, 700);
        setMinimumSize(new Dimension(800, 500));
        setLayout(new BorderLayout());
        getContentPane().setBackground(TrakTheme.BG_DARK);

        userViewModel.addObserver(this);

        // ── Top section: status bar ──
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setBackground(TrakTheme.BG_SURFACE);

        this.statusPanel = new StatusPanel(controller);
        topSection.add(statusPanel);

        add(topSection, BorderLayout.NORTH);

        // ── Center: Dashboard ──
        this.dashboardView = new DashboardView(controller, taskViewModel, projectViewModel, sprintViewModel);
        add(dashboardView, BorderLayout.CENTER);

        // ── Bottom: command input ──
        CommandInputPanel inputPanel = new CommandInputPanel(this::onCommandSubmitted);
        add(inputPanel, BorderLayout.SOUTH);

        updateStatus();
        setLocationRelativeTo(null);

        // Edge-drag resize for undecorated frame
        final int EDGE = 6;
        final Point[] resizeStart = {null};
        final Dimension[] startSize = {null};
        final Point[] startLoc = {null};
        final int[] resizeDir = {0}; // bitmask: 1=N, 2=S, 4=W, 8=E

        JPanel glassPane = new JPanel(null);
        glassPane.setOpaque(false);
        setGlassPane(glassPane);
        glassPane.setVisible(true);

        glassPane.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int dir = getResizeDir(e.getPoint(), getSize(), EDGE);
                if (dir != 0) {
                    resizeDir[0] = dir;
                    resizeStart[0] = e.getLocationOnScreen();
                    startSize[0] = getSize();
                    startLoc[0] = getLocation();
                } else {
                    // Pass through to components below
                    glassPane.setVisible(false);
                    Component target = SwingUtilities.getDeepestComponentAt(
                            getContentPane(), e.getX(), e.getY());
                    if (target != null) {
                        target.dispatchEvent(SwingUtilities.convertMouseEvent(glassPane, e, target));
                    }
                    glassPane.setVisible(true);
                }
            }
            public void mouseReleased(MouseEvent e) {
                resizeDir[0] = 0;
                resizeStart[0] = null;
            }
        });

        glassPane.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                int dir = getResizeDir(e.getPoint(), getSize(), EDGE);
                if (dir == 0) glassPane.setCursor(Cursor.getDefaultCursor());
                else if (dir == (1|4) || dir == (2|8)) glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                else if (dir == (1|8) || dir == (2|4)) glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                else if ((dir & 1) != 0 || (dir & 2) != 0) glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                else glassPane.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
            }
            public void mouseDragged(MouseEvent e) {
                if (resizeStart[0] == null) return;
                Point now = e.getLocationOnScreen();
                int dx = now.x - resizeStart[0].x;
                int dy = now.y - resizeStart[0].y;
                int dir = resizeDir[0];
                Rectangle r = new Rectangle(startLoc[0], startSize[0]);
                Dimension min = getMinimumSize();
                if ((dir & 8) != 0) r.width = Math.max(min.width, startSize[0].width + dx);
                if ((dir & 4) != 0) { r.width = Math.max(min.width, startSize[0].width - dx); r.x = startLoc[0].x + startSize[0].width - r.width; }
                if ((dir & 2) != 0) r.height = Math.max(min.height, startSize[0].height + dy);
                if ((dir & 1) != 0) { r.height = Math.max(min.height, startSize[0].height - dy); r.y = startLoc[0].y + startSize[0].height - r.height; }
                setBounds(r);
            }
        });
    }

    private static int getResizeDir(Point p, Dimension size, int edge) {
        int dir = 0;
        if (p.y < edge) dir |= 1;
        if (p.y > size.height - edge) dir |= 2;
        if (p.x < edge) dir |= 4;
        if (p.x > size.width - edge) dir |= 8;
        return dir;
    }

    @Override
    public void onViewModelChanged(ViewModelChangeType type) {
        SwingUtilities.invokeLater(() -> {
            switch (type) {
                case SESSION -> {
                    updateStatus();
                    if (userViewModel.getSession() == null) {
                        controller.clearViewModels();
                    } else {
                        // Refresh all data off-EDT, fires notifications when done
                        new Thread(() -> controller.refreshAll(), "session-refresh").start();
                    }
                }
                case ERROR -> {
                    String error = userViewModel.getLastError();
                    if (error != null) {
                        new ErrorView(error).show(this);
                    }
                }
                case OUTPUT -> {
                    String output = userViewModel.getLastOutput();
                    if (output != null) {
                        System.err.println(output);
                    }
                }
                default -> {
                    // TASKS, PROJECTS, SPRINTS handled by DashboardView's own observer
                }
            }
        });
    }

    private void onCommandSubmitted(String command) {
        controller.executeCommand(command);
    }

    private void updateStatus() {
        statusPanel.update(userViewModel.getSession());
    }
}
