package chess.gui;

import chess.ai.MoveStrategy;
import chess.ai.RandomStrategy;
import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;
import chess.model.Position;
import chess.pieces.Piece;
import chess.pieces.PieceType;
import chess.service.Game;
import chess.service.GameListener;
import chess.service.MoveResult;

import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connects the Swing window to the game (the Controller in Model-View-Controller).
 *
 * <p>Mouse handlers in {@link BoardPanel} call {@link #squareClicked} or
 * {@link #pieceDropped}. This class turns those clicks into a
 * {@link Move}, asks {@link Game#tryMove(Move)} whether it is legal, and
 * tells the {@link GameView} what to show. It never checks chess rules
 * itself; every rule lives in the back end.</p>
 *
 * <p>The controller is also a {@link GameListener}: after any accepted move
 * (the human's or the computer's), the game calls {@link #onMoveMade}, which
 * refreshes the view and, if it is now the computer's turn, starts the
 * computer thinking on a background thread.</p>
 *
 * <p>All methods must be called on the Swing event dispatch thread.</p>
 */
public final class GameController implements GameListener {

    /** Logger for GUI-level events. */
    private static final Logger LOG = Logger.getLogger(GameController.class.getName());

    /** The window this controller updates. */
    private final GameView view;

    /** The game being played; replaced by {@link #newGame}. */
    private Game game;

    /** The settings the current game was started with (used by {@link #restart()}). */
    private GameSettings settings;

    /** The computer's strategy, or {@code null} in a two-player game. */
    private MoveStrategy computer;

    /** The square of the piece the player has selected, or {@code null}. */
    private Position selected;

    /** Legal destinations of the selected piece, for highlighting. */
    private List<Position> selectedTargets = Collections.emptyList();

    /** {@code true} while the computer is choosing a move. */
    private boolean computerThinking;

    /**
     * Increases with every new game, so a computer move that finishes after the
     * player restarted can be recognized as stale and ignored.
     */
    private int generation;

    /**
     * Creates a controller for a view. Call {@link #newGame} to start playing.
     *
     * @param view the window to update; must not be null
     */
    public GameController(GameView view) {
        this.view = Objects.requireNonNull(view, "view must not be null");
    }

    /**
     * Starts a new game with the given settings, replacing any game in progress.
     *
     * @param newSettings player names and computer options; must not be null
     */
    public void newGame(GameSettings newSettings) {
        settings = Objects.requireNonNull(newSettings, "settings must not be null");
        generation++;
        computerThinking = false;
        if (game != null) {
            game.removeListener(this);
        }
        game = new Game(settings.getWhiteName(), settings.getBlackName());
        game.addListener(this);
        computer = settings.isVsComputer() ? settings.getDifficulty().createStrategy() : null;
        clearSelection();
        LOG.info("GUI started a new game: " + game.getPlayer(PieceColor.WHITE) + " vs "
                + game.getPlayer(PieceColor.BLACK)
                + (computer == null ? "" : ", computer plays " + settings.getComputerColor().displayName()
                + " using " + computer.getName()));
        view.refresh();
        view.showMessage("New game. " + game.getCurrentPlayer() + " moves first.", false);
        startComputerTurnIfNeeded();
    }

    /**
     * Restarts with the same players and options.
     */
    public void restart() {
        newGame(settings);
    }

    /**
     * Handles a click on a square: selects one of the current player's pieces, changes the
     * selection, deselects, or tries to move the selected piece to the clicked square.
     *
     * @param square the clicked square; ignored if {@code null} (a click outside the board)
     */
    public void squareClicked(Position square) {
        if (square == null || !acceptingInput()) {
            return;
        }
        Piece clicked = game.getBoard().getPiece(square);
        PieceColor turn = game.getCurrentPlayer().getColor();

        if (selected == null) {
            if (clicked == null) {
                view.showMessage("Select one of " + turn.displayName() + "'s pieces first.", true);
            } else if (clicked.getColor() != turn) {
                view.showMessage("That " + clicked.getName() + " belongs to " + clicked.getColor().displayName()
                        + ". It is " + turn.displayName() + "'s turn.", true);
            } else {
                select(square);
            }
        } else if (square.equals(selected)) {
            clearSelection();
            view.refresh();
        } else if (clicked != null && clicked.getColor() == turn) {
            select(square);   // switch to another of your own pieces
        } else {
            attemptMove(selected, square);
        }
    }

    /**
     * Handles a drag-and-drop: tries to move the piece from {@code from} to {@code to}.
     *
     * @param from the square the drag started on
     * @param to   the square the piece was dropped on, or {@code null} if off the board
     */
    public void pieceDropped(Position from, Position to) {
        if (from == null || !acceptingInput()) {
            return;
        }
        if (to == null || to.equals(from)) {
            squareClicked(from);   // treat a drop back on the same square as a click
            return;
        }
        Piece piece = game.getBoard().getPiece(from);
        if (piece == null || piece.getColor() != game.getCurrentPlayer().getColor()) {
            squareClicked(from);   // reports the problem
            return;
        }
        attemptMove(from, to);
    }

    /**
     * Checks whether a drag may start on {@code square}.
     *
     * @param square the square under the mouse
     * @return {@code true} if it holds a piece of the side to move and input is accepted
     */
    public boolean canPickUp(Position square) {
        if (square == null || game == null || !isHumanTurn() || game.isGameOver()) {
            return false;
        }
        Piece piece = game.getBoard().getPiece(square);
        return piece != null && piece.getColor() == game.getCurrentPlayer().getColor();
    }

    /**
     * Asks for a promotion piece if needed, then asks the game to play the move. On
     * failure the reason is shown and nothing else changes; on success the game notifies
     * {@link #onMoveMade}, which redraws the board.
     *
     * @param from source square
     * @param to   destination square
     */
    private void attemptMove(Position from, Position to) {
        PieceType promotion = null;
        if (game.isPromotionMove(from, to)) {
            promotion = view.askPromotion(game.getCurrentPlayer().getColor());
            if (promotion == null) {
                view.showMessage("Promotion cancelled; choose a move again.", false);
                return;
            }
        }
        clearSelection();
        MoveResult result = game.tryMove(new Move(from, to, promotion));
        if (!result.isSuccess()) {
            view.refresh();   // removes the selection highlight; the board itself is unchanged
            view.showMessage(result.getMessage(), true);
        }
    }

    /**
     * Selects a piece and remembers its legal destinations for highlighting.
     *
     * @param square the square of one of the current player's pieces
     */
    private void select(Position square) {
        selected = square;
        selectedTargets = game.getLegalMoves(square);
        Piece piece = game.getBoard().getPiece(square);
        if (selectedTargets.isEmpty()) {
            view.showMessage("The " + piece.getName() + " on " + square + " has no legal moves.", true);
        } else {
            view.showMessage(piece.getName() + " on " + square + " selected. Click a highlighted square.", false);
        }
        view.refresh();
    }

    /**
     * Forgets the current selection.
     */
    private void clearSelection() {
        selected = null;
        selectedTargets = Collections.emptyList();
    }

    /**
     * Checks whether a human click should be processed now, explaining why not if it shouldn't.
     *
     * @return {@code true} if a human may move
     */
    private boolean acceptingInput() {
        if (game == null) {
            return false;
        }
        if (game.isGameOver()) {
            view.showMessage(game.describeOutcome() + ". Use Restart or New Game to play again.", true);
            return false;
        }
        if (!isHumanTurn()) {
            view.showMessage("Please wait: the computer is thinking.", true);
            return false;
        }
        return true;
    }

    /**
     * Checks whether the side to move is controlled by a person.
     *
     * @return {@code false} while it is the computer's turn
     */
    private boolean isHumanTurn() {
        return computer == null || game.getCurrentPlayer().getColor() != settings.getComputerColor();
    }

    /**
     * If it is the computer's turn, chooses its move on a background thread so the window stays
     * responsive, then plays it back on the event dispatch thread. If the strategy fails or
     * returns an illegal move, a random legal move is played instead and the problem is logged.
     */
    private void startComputerTurnIfNeeded() {
        if (computer == null || game.isGameOver() || isHumanTurn() || computerThinking) {
            return;
        }
        computerThinking = true;
        view.showMessage(computer.getName() + " is thinking...", false);
        view.refresh();

        final int myGeneration = generation;
        final Game myGame = game;
        final MoveStrategy strategy = computer;
        final Board snapshot = game.getBoard().copy();
        final PieceColor side = game.getCurrentPlayer().getColor();

        SwingWorker<Move, Void> worker = new SwingWorker<Move, Void>() {
            @Override
            protected Move doInBackground() {
                long start = System.nanoTime();
                Move choice = strategy.chooseMove(snapshot, side);
                LOG.info(strategy.getName() + " chose " + choice + " in "
                        + (System.nanoTime() - start) / 1_000_000 + " ms");
                return choice;
            }

            @Override
            protected void done() {
                if (myGeneration != generation) {
                    LOG.fine("Discarded a computer move from a game that was replaced.");
                    return;
                }
                computerThinking = false;
                Move choice = null;
                try {
                    choice = get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    LOG.log(Level.SEVERE, "The computer strategy failed; falling back to a random move.", e.getCause());
                }
                playComputerMove(myGame, choice);
            }
        };
        worker.execute();
    }

    /**
     * Plays the computer's chosen move, falling back to a random legal move if the choice is
     * missing or rejected.
     *
     * @param target the game the move was chosen for
     * @param choice the strategy's move, or {@code null}
     */
    private void playComputerMove(Game target, Move choice) {
        MoveResult result = choice == null ? null : target.tryMove(choice);
        if (result == null || !result.isSuccess()) {
            LOG.warning("Computer move " + choice + " was unusable"
                    + (result == null ? "" : " (" + result.getMessage() + ")") + "; playing a random move.");
            Move fallback = new RandomStrategy().chooseMove(target.getBoard().copy(),
                    target.getCurrentPlayer().getColor());
            if (fallback != null) {
                target.tryMove(fallback);
            }
        }
        view.refresh();
    }

    /**
     * Called by the game after a restart; redraws the window.
     *
     * @param changed the game that started
     */
    @Override
    public void onGameStarted(Game changed) {
        clearSelection();
        view.refresh();
    }

    /**
     * Called by the game after every accepted move: redraws, reports the move, announces
     * checkmate or stalemate, and lets the computer reply if it is its turn.
     *
     * @param changed the game that changed
     * @param result  the accepted move's result
     */
    @Override
    public void onMoveMade(Game changed, MoveResult result) {
        view.refresh();
        view.showMessage(result.getMessage(), false);
        if (result.getStatus().isGameOver()) {
            view.announceGameOver(changed.describeOutcome());
        } else {
            startComputerTurnIfNeeded();
        }
    }

    /**
     * Returns the game being played, for the view to draw.
     *
     * @return the current game
     */
    public Game getGame() {
        return game;
    }

    /**
     * Returns the selected square.
     *
     * @return the selected square, or {@code null}
     */
    public Position getSelected() {
        return selected;
    }

    /**
     * Returns the selected piece's legal destinations.
     *
     * @return an unmodifiable list (empty if nothing is selected)
     */
    public List<Position> getSelectedTargets() {
        return Collections.unmodifiableList(new ArrayList<>(selectedTargets));
    }

    /**
     * Returns whether the computer is currently choosing a move.
     *
     * @return {@code true} while the computer is thinking
     */
    public boolean isComputerThinking() {
        return computerThinking;
    }

    /**
     * Returns the settings of the current game.
     *
     * @return the settings
     */
    public GameSettings getSettings() {
        return settings;
    }
}
