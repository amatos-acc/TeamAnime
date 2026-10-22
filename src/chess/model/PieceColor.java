package chess.model;

/**
 * The two sides in a chess game.
 *
 * <p>Each color knows its display prefix ({@code w} or {@code b}), the row
 * direction its pawns advance in, and which row its pawns start on. Keeping
 * this information in the enum means pieces never need {@code if (WHITE)}
 * branches for direction-dependent logic.</p>
 *
 * <p>Row numbering follows the mapping documented in {@link Position}:
 * array row 0 is rank 8 and array row 7 is rank 1.</p>
 */
public enum PieceColor {

    /** The white side; moves first and advances toward rank 8 (row 0). */
    WHITE('w', -1, 6),

    /** The black side; advances toward rank 1 (row 7). */
    BLACK('b', 1, 1);

    /** Single-character prefix used when printing a piece, e.g. {@code w} in {@code wK}. */
    private final char prefix;

    /** Row delta for one step "forward" for this color's pawns. */
    private final int forwardDirection;

    /** The array row this color's pawns start on. */
    private final int pawnStartRow;

    /**
     * Creates a color constant.
     *
     * @param prefix           display prefix character
     * @param forwardDirection row delta for one forward pawn step
     * @param pawnStartRow     array row on which this color's pawns begin
     */
    PieceColor(char prefix, int forwardDirection, int pawnStartRow) {
        this.prefix = prefix;
        this.forwardDirection = forwardDirection;
        this.pawnStartRow = pawnStartRow;
    }

    /**
     * Returns the display prefix for this color.
     *
     * @return {@code 'w'} for white, {@code 'b'} for black
     */
    public char getPrefix() {
        return prefix;
    }

    /**
     * Returns the row delta that moves one square forward for this color.
     *
     * @return {@code -1} for white (toward row 0), {@code +1} for black
     */
    public int getForwardDirection() {
        return forwardDirection;
    }

    /**
     * Returns the array row on which this color's pawns start.
     *
     * @return 6 for white (rank 2), 1 for black (rank 7)
     */
    public int getPawnStartRow() {
        return pawnStartRow;
    }

    /**
     * Returns the array row of this color's back rank (where the king starts).
     *
     * @return 7 for white (rank 1), 0 for black (rank 8)
     */
    public int getBackRankRow() {
        return pawnStartRow - forwardDirection;
    }

    /**
     * Returns the opposing color.
     *
     * @return {@link #BLACK} for white and {@link #WHITE} for black
     */
    public PieceColor opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

    /**
     * Returns a human-friendly name such as {@code "White"}.
     *
     * @return the capitalized color name
     */
    public String displayName() {
        return this == WHITE ? "White" : "Black";
    }
}
