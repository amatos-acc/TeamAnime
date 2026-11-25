package chess.gui;

import chess.model.PieceColor;
import chess.pieces.Piece;
import chess.pieces.PieceType;
import chess.service.Game;
import chess.service.GameStatus;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.logging.Logger;

/**
 * The main window: the board in the center, a side panel with the turn indicator,
 * game status, captured pieces, and move list, a message bar at the bottom, and a
 * Game menu with New Game, Restart, and Exit.
 *
 * <p>This class is the View. It owns no game state: {@link #refresh()} redraws
 * everything from {@link GameController#getGame()}, which is what keeps the
 * display consistent with the application state. It uses a {@link JFrame}
 * rather than extending one (composition over inheritance), so its public
 * methods are only the ones the controller needs.</p>
 */
public final class ChessFrame implements GameView {

    /** Logger for window-level events. */
    private static final Logger LOG = Logger.getLogger(ChessFrame.class.getName());

    /** Color for error messages in the message bar. */
    private static final Color ERROR_COLOR = new Color(183, 28, 28);

    /** Color for normal messages in the message bar. */
    private static final Color INFO_COLOR = new Color(33, 33, 33);

    /**
     * Width at which side-panel text wraps. Swing's HTML renderer treats CSS "px" as points
     * (1 pt = 4/3 screen pixels), so 175 here is about 233 pixels, which fits the 270-pixel
     * side panel minus its 24 pixels of border.
     */
    private static final int SIDE_TEXT_WIDTH = 175;

    /** The top-level window. */
    private final JFrame frame;

    /** Handles user actions and owns the game. */
    private final GameController controller;

    /** The chess board. */
    private final BoardPanel boardPanel;

    /** Shows whose turn it is, with a colored swatch. */
    private final JLabel turnLabel = new JLabel();

    /** Shows check, checkmate, or stalemate. */
    private final JLabel statusLabel = new JLabel(" ");

    /** Shows the White player's name. */
    private final JLabel whitePlayerLabel = new JLabel();

    /** Shows the Black player's name. */
    private final JLabel blackPlayerLabel = new JLabel();

    /** Black pieces that White has captured. */
    private final JLabel capturedByWhite = new JLabel(" ");

    /** White pieces that Black has captured. */
    private final JLabel capturedByBlack = new JLabel(" ");

    /** Contents of the move list. */
    private final DefaultListModel<String> historyModel = new DefaultListModel<>();

    /** The move list. */
    private final JList<String> historyList = new JList<>(historyModel);

    /** One-line feedback, e.g. why a move was rejected. */
    private final JLabel messageLabel = new JLabel(" ");

    /**
     * Builds the window and starts a game with the given settings.
     *
     * @param initial settings for the first game
     */
    public ChessFrame(GameSettings initial) {
        frame = new JFrame("Chess - TeamAnime");
        controller = new GameController(this);
        boardPanel = new BoardPanel(controller);

        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmExit();
            }
        });
        frame.setJMenuBar(buildMenuBar());
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(boardPanel, BorderLayout.CENTER);
        frame.getContentPane().add(buildSidePanel(), BorderLayout.EAST);
        frame.getContentPane().add(buildMessageBar(), BorderLayout.SOUTH);

        controller.newGame(initial);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationRelativeTo(null);
    }

    /**
     * Makes the window visible.
     */
    public void show() {
        frame.setVisible(true);
    }

    /**
     * Builds the Game and Help menus.
     *
     * @return the menu bar
     */
    private JMenuBar buildMenuBar() {
        int shortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        JMenu gameMenu = new JMenu("Game");
        gameMenu.add(menuItem("New Game...", KeyStroke.getKeyStroke(KeyEvent.VK_N, shortcut), this::showNewGameDialog));
        gameMenu.add(menuItem("Restart", KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcut), this::confirmRestart));
        gameMenu.addSeparator();
        gameMenu.add(menuItem("Exit", KeyStroke.getKeyStroke(KeyEvent.VK_Q, shortcut), this::confirmExit));

        JMenu helpMenu = new JMenu("Help");
        helpMenu.add(menuItem("How to Play", null, this::showHelp));

        JMenuBar bar = new JMenuBar();
        bar.add(gameMenu);
        bar.add(helpMenu);
        return bar;
    }

    /**
     * Creates a menu item that runs an action.
     *
     * @param text        the item's text
     * @param accelerator keyboard shortcut, or {@code null}
     * @param action      what to do when chosen
     * @return the menu item
     */
    private static JMenuItem menuItem(String text, KeyStroke accelerator, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        if (accelerator != null) {
            item.setAccelerator(accelerator);
        }
        item.addActionListener(e -> action.run());
        return item;
    }

    /**
     * Builds the side panel: players, turn, status, captured pieces, move list, and buttons.
     *
     * @return the side panel
     */
    private JPanel buildSidePanel() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 16));
        side.setPreferredSize(new Dimension(270, 0));

        turnLabel.setFont(turnLabel.getFont().deriveFont(Font.BOLD, 18f));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 16f));
        statusLabel.setForeground(ERROR_COLOR);
        Font pieceFont = new Font(Font.SERIF, Font.PLAIN, 22);
        capturedByWhite.setFont(pieceFont);
        capturedByBlack.setFont(pieceFont);

        side.add(leftAligned(whitePlayerLabel));
        side.add(leftAligned(blackPlayerLabel));
        side.add(Box.createVerticalStrut(12));
        side.add(leftAligned(turnLabel));
        side.add(Box.createVerticalStrut(4));
        side.add(leftAligned(statusLabel));
        side.add(Box.createVerticalStrut(12));
        side.add(leftAligned(new JLabel("Captured by White:")));
        side.add(leftAligned(capturedByWhite));
        side.add(leftAligned(new JLabel("Captured by Black:")));
        side.add(leftAligned(capturedByBlack));
        side.add(Box.createVerticalStrut(12));
        side.add(leftAligned(new JLabel("Moves:")));
        JScrollPane historyScroll = new JScrollPane(historyList);
        historyScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        historyScroll.setPreferredSize(new Dimension(240, 260));
        side.add(historyScroll);
        side.add(Box.createVerticalStrut(12));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton newGame = new JButton("New Game");
        JButton restart = new JButton("Restart");
        JButton exit = new JButton("Exit");
        newGame.addActionListener(e -> showNewGameDialog());
        restart.addActionListener(e -> confirmRestart());
        exit.addActionListener(e -> confirmExit());
        buttons.add(newGame);
        buttons.add(restart);
        buttons.add(exit);
        side.add(buttons);
        return side;
    }

    /**
     * Sets a component's horizontal alignment so BoxLayout lines it up on the left.
     *
     * @param component the component
     * @param <T>       the component's type
     * @return the same component
     */
    private static <T extends JComponent> T leftAligned(T component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        return component;
    }

    /**
     * Wraps label text in HTML so it breaks onto more lines instead of being cut off with "..."
     * when it is wider than the side panel. A plain {@code JLabel} never wraps.
     *
     * @param text the text to show; player names are limited to safe characters by
     *             {@link GameSettings#checkName}, so no HTML escaping is needed
     * @return the text as HTML with a fixed wrapping width
     */
    private static String wrap(String text) {
        return "<html><div style='width:" + SIDE_TEXT_WIDTH + "px'>" + text + "</div></html>";
    }

    /**
     * Builds the message bar at the bottom of the window.
     *
     * @return the message bar
     */
    private JPanel buildMessageBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(6, 12, 8, 12));
        messageLabel.setFont(messageLabel.getFont().deriveFont(14f));
        bar.add(messageLabel, BorderLayout.CENTER);
        return bar;
    }

    /**
     * Redraws everything from the controller's current game.
     */
    @Override
    public void refresh() {
        Game game = controller.getGame();
        if (game == null) {
            return;
        }
        whitePlayerLabel.setText(wrap("White: " + game.getPlayer(PieceColor.WHITE).getName()));
        blackPlayerLabel.setText(wrap("Black: " + game.getPlayer(PieceColor.BLACK).getName()));

        PieceColor turn = game.getCurrentPlayer().getColor();
        if (game.isGameOver()) {
            turnLabel.setText(wrap("Game over"));
        } else if (controller.isComputerThinking()) {
            turnLabel.setText(wrap(PieceGlyphs.text(PieceType.KING, turn) + " " + turn.displayName() + " (computer) thinking..."));
        } else {
            turnLabel.setText(wrap(PieceGlyphs.text(PieceType.KING, turn) + " " + turn.displayName() + " to move ("
                    + game.getCurrentPlayer().getName() + ")"));
        }

        GameStatus status = game.getStatus();
        if (status == GameStatus.CHECK) {
            statusLabel.setText(wrap("CHECK! " + turn.displayName() + "'s king is attacked."));
        } else if (status.isGameOver()) {
            statusLabel.setText(wrap(game.describeOutcome() + "."));
        } else {
            statusLabel.setText(" ");
        }

        capturedByWhite.setText(wrap(capturedText(game, PieceColor.BLACK)));
        capturedByBlack.setText(wrap(capturedText(game, PieceColor.WHITE)));

        historyModel.clear();
        for (String entry : game.getMoveHistory()) {
            historyModel.addElement(entry);
        }
        if (!historyModel.isEmpty()) {
            historyList.ensureIndexIsVisible(historyModel.size() - 1);
        }
        boardPanel.repaint();
    }

    /**
     * Builds the row of symbols for one color's captured pieces.
     *
     * @param game the game
     * @param lost the color whose pieces were captured
     * @return the symbols separated by spaces (so a long row can wrap), or a no-break space if none
     */
    private static String capturedText(Game game, PieceColor lost) {
        List<Piece> pieces = game.getBoard().getCapturedPieces(lost);
        if (pieces.isEmpty()) {
            return "&nbsp;";
        }
        StringBuilder text = new StringBuilder();
        for (Piece piece : pieces) {
            text.append(PieceGlyphs.text(piece)).append(' ');
        }
        return text.toString().trim();
    }

    /**
     * Shows a message in the message bar.
     *
     * @param text    the message
     * @param isError {@code true} to show it in red
     */
    @Override
    public void showMessage(String text, boolean isError) {
        messageLabel.setForeground(isError ? ERROR_COLOR : INFO_COLOR);
        messageLabel.setText(text);
    }

    /**
     * Asks which piece to promote to.
     *
     * @param color the promoting pawn's color
     * @return the choice, or {@code null} if cancelled
     */
    @Override
    public PieceType askPromotion(PieceColor color) {
        return PromotionDialog.ask(frame, color);
    }

    /**
     * Shows the result of the game in a dialog.
     *
     * @param outcome for example {@code "Checkmate. Black wins"}
     */
    @Override
    public void announceGameOver(String outcome) {
        LOG.info("Game over: " + outcome);
        JOptionPane.showMessageDialog(frame, outcome + ".\n\nUse Restart or New Game to play again.",
                "Game over", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Shows the new-game dialog and starts a game if the player confirms.
     */
    private void showNewGameDialog() {
        GameSettings chosen = NewGameDialog.show(frame, controller.getSettings());
        if (chosen != null) {
            controller.newGame(chosen);
        }
    }

    /**
     * Restarts with the same settings, asking first if moves have been played.
     */
    private void confirmRestart() {
        Game game = controller.getGame();
        if (game.getMoveHistory().isEmpty() || game.isGameOver() || confirm("Restart and lose the current game?")) {
            controller.restart();
        }
    }

    /**
     * Closes the application after asking for confirmation.
     */
    private void confirmExit() {
        if (confirm("Exit Chess?")) {
            LOG.info("Application closed by the user.");
            frame.dispose();
            System.exit(0);
        }
    }

    /**
     * Shows a yes/no question.
     *
     * @param question the question
     * @return {@code true} if the player chose Yes
     */
    private boolean confirm(String question) {
        return JOptionPane.showConfirmDialog(frame, question, "Confirm", JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    /**
     * Shows a short summary of how to play.
     */
    private void showHelp() {
        JOptionPane.showMessageDialog(frame,
                "Move: click a piece, then click a highlighted square, or drag the piece and drop it.\n"
                        + "Dots show empty target squares; rings show captures.\n"
                        + "Castle: move the king two squares toward the rook.\n"
                        + "En passant and promotion are supported; you choose the promotion piece.\n"
                        + "A red square marks a king in check.\n\n"
                        + "Not implemented: draws by repetition, the 50-move rule, or insufficient material.",
                "How to Play", JOptionPane.INFORMATION_MESSAGE);
    }
}
