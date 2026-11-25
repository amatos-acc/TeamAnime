package chess.gui;

import chess.ai.Difficulty;
import chess.model.PieceColor;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * The "New Game" dialog: game mode, player names, and computer options.
 *
 * <p>Input is validated with {@link GameSettings#checkName(String)} when the
 * player clicks OK. If a name is invalid, the problem is explained and the
 * dialog reopens with everything the player typed still filled in.</p>
 */
public final class NewGameDialog {

    /** Not instantiable; use {@link #show}. */
    private NewGameDialog() {
    }

    /**
     * Shows the dialog until the player enters valid settings or cancels.
     *
     * @param parent  component to center the dialog over
     * @param current settings used to pre-fill the fields; may be null
     * @return the new settings, or {@code null} if the player cancelled
     */
    public static GameSettings show(Component parent, GameSettings current) {
        JRadioButton twoPlayers = new JRadioButton("Two players (same computer)");
        JRadioButton vsComputer = new JRadioButton("Play against the computer");
        ButtonGroup mode = new ButtonGroup();
        mode.add(twoPlayers);
        mode.add(vsComputer);

        JTextField whiteName = new JTextField(16);
        JTextField blackName = new JTextField(16);
        JComboBox<PieceColor> computerColor = new JComboBox<>(PieceColor.values());
        JComboBox<Difficulty> difficulty = new JComboBox<>(Difficulty.values());

        boolean computerMode = current != null && current.isVsComputer();
        twoPlayers.setSelected(!computerMode);
        vsComputer.setSelected(computerMode);
        computerColor.setSelectedItem(computerMode ? current.getComputerColor() : PieceColor.BLACK);
        difficulty.setSelectedItem(current == null ? Difficulty.MEDIUM : current.getDifficulty());
        if (current != null && !computerMode) {
            whiteName.setText(current.getWhiteName() == null ? "" : current.getWhiteName());
            blackName.setText(current.getBlackName() == null ? "" : current.getBlackName());
        }

        JLabel whiteLabel = new JLabel("White player's name:");
        JLabel blackLabel = new JLabel("Black player's name:");
        JLabel colorLabel = new JLabel("Computer plays:");
        JLabel levelLabel = new JLabel("Difficulty:");

        Runnable updateEnabled = () -> {
            boolean vs = vsComputer.isSelected();
            PieceColor cpu = (PieceColor) computerColor.getSelectedItem();
            whiteName.setEnabled(!vs || cpu != PieceColor.WHITE);
            blackName.setEnabled(!vs || cpu != PieceColor.BLACK);
            computerColor.setEnabled(vs);
            difficulty.setEnabled(vs);
            colorLabel.setEnabled(vs);
            levelLabel.setEnabled(vs);
        };
        twoPlayers.addActionListener(e -> updateEnabled.run());
        vsComputer.addActionListener(e -> updateEnabled.run());
        computerColor.addActionListener(e -> updateEnabled.run());
        updateEnabled.run();

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridwidth = 2;
        c.gridy = 0;
        form.add(twoPlayers, c);
        c.gridy = 1;
        form.add(vsComputer, c);
        c.gridwidth = 1;
        addRow(form, c, 2, whiteLabel, whiteName);
        addRow(form, c, 3, blackLabel, blackName);
        addRow(form, c, 4, colorLabel, computerColor);
        addRow(form, c, 5, levelLabel, difficulty);
        c.gridx = 0;
        c.gridy = 6;
        c.gridwidth = 2;
        form.add(new JLabel("Leave a name blank to use \"White\" or \"Black\". Max "
                + GameSettings.MAX_NAME_LENGTH + " characters."), c);

        while (true) {
            int answer = JOptionPane.showConfirmDialog(parent, form, "New Game",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (answer != JOptionPane.OK_OPTION) {
                return null;
            }
            try {
                if (vsComputer.isSelected()) {
                    PieceColor cpu = (PieceColor) computerColor.getSelectedItem();
                    String human = cpu == PieceColor.WHITE ? blackName.getText() : whiteName.getText();
                    return GameSettings.vsComputer(human, cpu, (Difficulty) difficulty.getSelectedItem());
                }
                return GameSettings.twoPlayer(whiteName.getText(), blackName.getText());
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(parent, e.getMessage(), "Invalid name", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Adds a label and a field on one row of the form.
     *
     * @param form  the form panel
     * @param c     reusable layout constraints
     * @param row   the row index
     * @param label the label for column 0
     * @param field the input for column 1
     */
    private static void addRow(JPanel form, GridBagConstraints c, int row, JLabel label, Component field) {
        c.gridy = row;
        c.gridx = 0;
        form.add(label, c);
        c.gridx = 1;
        form.add(field, c);
    }
}
