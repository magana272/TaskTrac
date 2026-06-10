package task.trak.app.client.gui.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

/**
 * Bottom command input bar.
 */
public class CommandInputPanel extends JPanel {

    private final JTextField inputField;

    public CommandInputPanel(Consumer<String> onSubmit) {
        setLayout(new BorderLayout(TrakTheme.SP_SM, 0));
        setBackground(TrakTheme.BG_SURFACE);
        setBorder(TrakTheme.pad(TrakTheme.SP_SM, TrakTheme.SP_XL));

        JLabel prompt = new JLabel(" > ");
        prompt.setFont(TrakTheme.FONT_MONO.deriveFont(Font.BOLD));
        prompt.setForeground(TrakTheme.ACCENT);

        inputField = new JTextField();
        inputField.setFont(TrakTheme.FONT_MONO);
        inputField.setBackground(TrakTheme.BG_INPUT);
        inputField.setForeground(TrakTheme.TEXT_PRIMARY);
        inputField.setCaretColor(TrakTheme.ACCENT);
        inputField.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(TrakTheme.BORDER, 1, true),
                new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_MD, TrakTheme.SP_SM, TrakTheme.SP_MD)
        ));

        JButton submitButton = new JButton("Run");
        TrakTheme.styleButtonAccent(submitButton);

        JButton helpButton = new JButton("?");
        helpButton.setFont(TrakTheme.FONT_BODY.deriveFont(Font.BOLD));
        helpButton.setForeground(TrakTheme.ACCENT_GREEN);
        helpButton.setBackground(TrakTheme.BG_SURFACE);
        helpButton.setOpaque(true);
        helpButton.setBorderPainted(false);
        helpButton.setFocusPainted(false);
        helpButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        helpButton.setBorder(new EmptyBorder(2, 10, 2, 10));
        helpButton.setToolTipText("Command Reference");
        helpButton.addActionListener(e -> showHelpPanel());

        ActionListener action = e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                onSubmit.accept(text);
                inputField.setText("");
            }
        };
        inputField.addActionListener(action);
        submitButton.addActionListener(action);

        JPanel eastPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, TrakTheme.SP_XS, 0));
        eastPanel.setOpaque(false);
        eastPanel.add(submitButton);
        eastPanel.add(helpButton);

        add(prompt, BorderLayout.WEST);
        add(inputField, BorderLayout.CENTER);
        add(eastPanel, BorderLayout.EAST);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        inputField.setEnabled(enabled);
    }

    private void showHelpPanel() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner instanceof Frame ? (Frame) owner : null, "Command Reference", false);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(TrakTheme.BG_SURFACE);

        String[][] cmds = {
            {"Workspace", null},
            {"projects", "List my projects"},
            {"tasks", "List my tasks"},
            {"sprints", "List my sprints"},
            {"detail -p|-t|-s <id>", "e.g. detail -t 3"},
            {"cur", "Current task + elapsed time"},
            {"start <task_id>", "e.g. start 3"},
            {"end", "Stop working on current task"},
            {"complete <task_id>", "e.g. complete 3"},
            {"", ""},
            {"Create", null},
            {"addtask <project>", "e.g. addtask WebApp"},
            {"addmember <project> <user>", "e.g. addmember WebApp alice"},
            {"sprintplan", "Plan a sprint (interactive)"},
            {"", ""},
            {"Entity CRUD", null},
            {"project add <name>", "e.g. project add WebApp --summary \"My app\""},
            {"project get|update|delete <name>", "e.g. project delete WebApp"},
            {"task add --title <t> --project <p>", "e.g. task add --title \"Fix bug\" --project WebApp"},
            {"task get|update|delete <id>", "e.g. task update 3 --status COMPLETE"},
            {"sprint add <name> --project <p>", "e.g. sprint add Sprint1 --project WebApp"},
            {"sprint update <name> --project <p>", "e.g. sprint update Sprint1 --project WebApp --add_task 3"},
            {"", ""},
            {"Auth", null},
            {"login <user> --password <pw>", "e.g. login guest --password Guest1!"},
            {"logout", "Log out"},
        };

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(TrakTheme.BG_SURFACE);
        content.setBorder(new EmptyBorder(TrakTheme.SP_SM, TrakTheme.SP_LG, TrakTheme.SP_SM, TrakTheme.SP_LG));
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(1, 0, 1, TrakTheme.SP_LG);

        for (int i = 0; i < cmds.length; i++) {
            gc.gridy = i;
            if (cmds[i][1] == null) {
                // Section header
                gc.gridx = 0; gc.gridwidth = 2;
                JLabel header = new JLabel(cmds[i][0]);
                header.setFont(TrakTheme.FONT_BODY.deriveFont(Font.BOLD));
                header.setForeground(TrakTheme.ACCENT);
                header.setBorder(new EmptyBorder(TrakTheme.SP_XS, 0, 2, 0));
                content.add(header, gc);
                gc.gridwidth = 1;
            } else if (cmds[i][0].isEmpty()) {
                gc.gridx = 0;
                content.add(Box.createVerticalStrut(TrakTheme.SP_XS), gc);
            } else {
                gc.gridx = 0;
                JLabel cmd = new JLabel(cmds[i][0]);
                cmd.setFont(TrakTheme.FONT_MONO);
                cmd.setForeground(TrakTheme.ACCENT_GREEN);
                content.add(cmd, gc);

                gc.gridx = 1;
                JLabel desc = new JLabel(cmds[i][1]);
                desc.setFont(TrakTheme.FONT_CAPTION);
                desc.setForeground(TrakTheme.TEXT_SECONDARY);
                content.add(desc, gc);
            }
        }

        JScrollPane scroll = new JScrollPane(content);
        TrakTheme.styleScrollPane(scroll);
        scroll.getViewport().setBackground(TrakTheme.BG_SURFACE);
        dialog.add(scroll, BorderLayout.CENTER);

        dialog.setSize(680, 420);
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }
}
