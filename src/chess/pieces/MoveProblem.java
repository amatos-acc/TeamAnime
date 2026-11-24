package chess.pieces;

/**
 * The result of a piece's own movement check, returned by
 * {@link Piece#checkMove(chess.model.Position, chess.model.Board)}.
 *
 * <p>In Phase 1, {@code Piece.canMoveTo} and {@code Game.tryMove} each ran
 * the same sequence of checks: one returned a boolean, the other built an
 * error message. Returning <em>which</em> check failed lets both callers
 * share a single sequence (see {@link chess.service.MoveValidator}).</p>
 */
public enum MoveProblem {

    /** Every piece-level check passed. */
    NONE,

    /** The destination is the square the piece already stands on. */
    SAME_SQUARE,

    /** The destination holds a piece of the same color. */
    OWN_PIECE,

    /** The move does not fit this piece type's movement pattern. */
    ILLEGAL_PATTERN,

    /** A piece stands on a square between the source and the destination. */
    BLOCKED_PATH
}
