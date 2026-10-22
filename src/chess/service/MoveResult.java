package chess.service;

import chess.pieces.Piece;

/**
 * The outcome of an attempted move, returned by {@link Game#tryMove}.
 *
 * <p>The service layer never prints anything. It hands back a
 * {@code MoveResult} and lets whichever interface is in use (the console now,
 * a GUI in Phase 2) decide how to show it.</p>
 *
 * <p>Instances are immutable and are created through the static factory
 * methods {@link #success} and {@link #failure}.</p>
 */
public final class MoveResult {

    /** Whether the move was accepted. */
    private final boolean success;

    /** Human-readable explanation of what happened (or why it was rejected). */
    private final String message;

    /** The piece captured by the move, or {@code null} if none. */
    private final Piece capturedPiece;

    /**
     * Private constructor; use the factory methods.
     *
     * @param success       whether the move was accepted
     * @param message       explanation for the user
     * @param capturedPiece captured piece, or {@code null}
     */
    private MoveResult(boolean success, String message, Piece capturedPiece) {
        this.success = success;
        this.message = message;
        this.capturedPiece = capturedPiece;
    }

    /**
     * Creates a result for an accepted move.
     *
     * @param message       description of the move
     * @param capturedPiece the captured piece, or {@code null} if nothing was captured
     * @return a successful result
     */
    public static MoveResult success(String message, Piece capturedPiece) {
        return new MoveResult(true, message, capturedPiece);
    }

    /**
     * Creates a result for a rejected move. The game state is unchanged.
     *
     * @param reason why the move was rejected
     * @return a failed result
     */
    public static MoveResult failure(String reason) {
        return new MoveResult(false, reason, null);
    }

    /**
     * Returns whether the move was accepted.
     *
     * @return {@code true} if the board changed
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the message for the user.
     *
     * @return description or rejection reason
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the captured piece, if any.
     *
     * @return the captured piece, or {@code null}
     */
    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    /**
     * Returns whether the move captured a piece.
     *
     * @return {@code true} if {@link #getCapturedPiece()} is non-null
     */
    public boolean isCapture() {
        return capturedPiece != null;
    }
}
