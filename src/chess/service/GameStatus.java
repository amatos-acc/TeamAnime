package chess.service;

/**
 * The state of a game from the point of view of the player whose turn it is.
 * {@link Game} recomputes it after every accepted move.
 */
public enum GameStatus {

    /** The player to move is not in check and has at least one legal move. */
    IN_PROGRESS,

    /** The player to move is in check but can get out of it. */
    CHECK,

    /** The player to move is in check and has no legal move: the other player wins. */
    CHECKMATE,

    /** The player to move is not in check but has no legal move: the game is a draw. */
    STALEMATE;

    /**
     * Checks whether this status ends the game.
     *
     * @return {@code true} for checkmate and stalemate
     */
    public boolean isGameOver() {
        return this == CHECKMATE || this == STALEMATE;
    }
}
