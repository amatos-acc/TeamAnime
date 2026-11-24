package chess.service;

import chess.model.Move;
import chess.model.MoveType;
import chess.pieces.Piece;

/**
 * The outcome of an attempted move, returned by {@link Game#tryMove}.
 *
 * <p>The service layer never prints anything. It hands back a
 * {@code MoveResult} and lets whichever interface is in use (the console or
 * the Swing GUI) decide how to show it.</p>
 *
 * <p>Instances are immutable and are created through the static factory
 * methods {@link #success} and {@link #failure}.</p>
 */
public final class MoveResult {

    /** Whether the move was accepted. */
    private final boolean success;

    /** Human-readable explanation of what happened (or why it was rejected). */
    private final String message;

    /** The move that was played (with its promotion choice filled in), or {@code null} on failure. */
    private final Move move;

    /** What kind of move was played, or {@code null} on failure. */
    private final MoveType moveType;

    /** The piece captured by the move, or {@code null} if none. */
    private final Piece capturedPiece;

    /** The game status after the move, or {@code null} on failure. */
    private final GameStatus status;

    /**
     * Private constructor; use the factory methods.
     *
     * @param success       whether the move was accepted
     * @param message       explanation for the user
     * @param move          the move played, or {@code null}
     * @param moveType      the kind of move, or {@code null}
     * @param capturedPiece captured piece, or {@code null}
     * @param status        status after the move, or {@code null}
     */
    private MoveResult(boolean success, String message, Move move, MoveType moveType,
                       Piece capturedPiece, GameStatus status) {
        this.success = success;
        this.message = message;
        this.move = move;
        this.moveType = moveType;
        this.capturedPiece = capturedPiece;
        this.status = status;
    }

    /**
     * Creates a result for an accepted move.
     *
     * @param message       description of the move
     * @param move          the move as played
     * @param moveType      what kind of move it was
     * @param capturedPiece the captured piece, or {@code null} if nothing was captured
     * @param status        the game status after the move
     * @return a successful result
     */
    public static MoveResult success(String message, Move move, MoveType moveType,
                                     Piece capturedPiece, GameStatus status) {
        return new MoveResult(true, message, move, moveType, capturedPiece, status);
    }

    /**
     * Creates a result for a rejected move. The game state is unchanged.
     *
     * @param reason why the move was rejected
     * @return a failed result
     */
    public static MoveResult failure(String reason) {
        return new MoveResult(false, reason, null, null, null, null);
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
     * Returns the move as it was played.
     *
     * @return the move, or {@code null} for a rejected move
     */
    public Move getMove() {
        return move;
    }

    /**
     * Returns what kind of move was played.
     *
     * @return the move type, or {@code null} for a rejected move
     */
    public MoveType getMoveType() {
        return moveType;
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

    /**
     * Returns the game status after the move.
     *
     * @return the new status, or {@code null} for a rejected move
     */
    public GameStatus getStatus() {
        return status;
    }
}
