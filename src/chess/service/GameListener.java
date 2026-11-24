package chess.service;

/**
 * Receives notifications when a {@link Game} changes (Observer pattern).
 *
 * <p>{@code Game} is the subject: it keeps a list of listeners and calls them
 * after it changes state. The Swing window registers one, so it redraws after
 * every move, whether the move came from a mouse click or from the computer
 * opponent. {@code Game} never refers to any Swing class, so the rules can
 * still be used by the console version and by the unit tests.</p>
 */
public interface GameListener {

    /**
     * Called after a game starts or restarts, or after a custom position is loaded.
     *
     * @param game the game that started
     */
    void onGameStarted(Game game);

    /**
     * Called after a move has been accepted and the board, turn, and status are updated.
     * Not called for rejected moves; those change nothing.
     *
     * @param game   the game that changed
     * @param result the accepted move's result
     */
    void onMoveMade(Game game, MoveResult result);
}
