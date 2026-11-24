package chess.model;

/**
 * What kind of move a {@link Move} turns out to be on a particular board.
 * {@link Board#classify(Move)} decides this; {@link Board#applyMove(Move)}
 * uses it to know which extra pieces to move or remove.
 */
public enum MoveType {

    /** An ordinary move or capture. */
    NORMAL,

    /** The king moves two squares toward the H-file rook, which jumps over it. */
    CASTLE_KINGSIDE,

    /** The king moves two squares toward the A-file rook, which jumps over it. */
    CASTLE_QUEENSIDE,

    /** A pawn captures a pawn that just advanced two squares, by moving onto the skipped square. */
    EN_PASSANT,

    /** A pawn reaches the last rank and is replaced by a queen, rook, bishop, or knight. */
    PROMOTION
}
