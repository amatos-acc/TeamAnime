package chess.model;

import java.util.Objects;

/**
 * An immutable square on the 8 x 8 board.
 *
 * <h2>Coordinate mapping</h2>
 * <ul>
 *   <li>{@code row 0} is rank 8 (Black's back rank); {@code row 7} is rank 1 (White's back rank).</li>
 *   <li>{@code col 0} is file A; {@code col 7} is file H.</li>
 *   <li>Therefore {@code rank = 8 - row} and {@code file = 'A' + col}.</li>
 *   <li>Example: {@code E2} maps to row 6, column 4.</li>
 * </ul>
 *
 * <p>A {@code Position} can never hold out-of-bounds coordinates: the
 * constructor rejects them. This keeps every other class free of repeated
 * bounds checks.</p>
 */
public final class Position {

    /** Number of rows and columns on the board. */
    public static final int BOARD_SIZE = 8;

    /** Array row index, 0 (rank 8) through 7 (rank 1). */
    private final int row;

    /** Array column index, 0 (file A) through 7 (file H). */
    private final int col;

    /**
     * Creates a position from array indices.
     *
     * @param row array row, 0-7 (0 = rank 8)
     * @param col array column, 0-7 (0 = file A)
     * @throws IllegalArgumentException if either index is outside the board
     */
    public Position(int row, int col) {
        if (!isInBounds(row, col)) {
            throw new IllegalArgumentException(
                    "Coordinates (" + row + ", " + col + ") are outside the board.");
        }
        this.row = row;
        this.col = col;
    }

    /**
     * Checks whether a row/column pair lies on the board.
     *
     * @param row array row to test
     * @param col array column to test
     * @return {@code true} if both indices are in the range 0-7
     */
    public static boolean isInBounds(int row, int col) {
        return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
    }

    /**
     * Parses algebraic notation such as {@code "E2"} or {@code "h8"} into a position.
     *
     * @param text two-character square name; letter A-H (either case) followed by digit 1-8
     * @return the matching position
     * @throws IllegalArgumentException if {@code text} is null, the wrong length,
     *                                  or names a square that is not on the board
     */
    public static Position fromAlgebraic(String text) {
        //  Reject null or anything that is not exactly 2 characters after trimming
        //  (throw IllegalArgumentException with a helpful message like "Expected a square like E2").
        
        if (text == null) {
            throw new IllegalArgumentException("Expected a square like E2 but got '" + text + "'");
        } 
        
        String square = text.trim().toUpperCase();
        
        if (square.length() != 2) {
            throw new IllegalArgumentException("Expected a square like E2 but got '" + text + "'");
        }
        
        // Compute col = first character - 'A'.
        int col = square.charAt(0) - 'A';
        
        // Convert the second character to a digit and compute row = 8 - rankDigit.
        int row = 8 - (square.charAt(1) - '0');
        
        // If the file isn't A-H or the rank isn't 1-8, throw IllegalArgumentException
        // naming the bad square, e.g. "Z9 is not on the board (files A-H, ranks 1-8)".
        if (col > 7 || col < 0) {
            throw new IllegalArgumentException(square + " is not on the board (files A-H, ranks 1-8)");
        }
        
        if (row > 7 || row < 0) {
            throw new IllegalArgumentException(square + " is not on the board (files A-H, ranks 1-8)");
        }
        
        return new Position(row, col);
    }

    /**
     * Returns this square in algebraic notation, e.g. {@code "E2"}.
     *
     * @return upper-case file letter followed by rank digit
     */
    public String toAlgebraic() {
        char file = (char) ('A' + col);
        int rank = BOARD_SIZE - row;
        return "" + file + rank;
    }

    /**
     * Returns the array row.
     *
     * @return row index 0-7
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the array column.
     *
     * @return column index 0-7
     */
    public int getCol() {
        return col;
    }

    /**
     * Returns the position offset by the given deltas, if that square is on the board.
     *
     * @param rowDelta rows to move (negative is toward rank 8)
     * @param colDelta columns to move (negative is toward file A)
     * @return the offset position, or {@code null} if it would fall off the board
     */
    public Position offset(int rowDelta, int colDelta) {
        int newRow = row + rowDelta;
        int newCol = col + colDelta;
        return isInBounds(newRow, newCol) ? new Position(newRow, newCol) : null;
    }

    /**
     * Two positions are equal when they name the same square.
     *
     * @param other object to compare with
     * @return {@code true} if {@code other} is a position with the same row and column
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Position)) {
            return false;
        }
        Position that = (Position) other;
        return row == that.row && col == that.col;
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     *
     * @return hash of row and column
     */
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    /**
     * Returns the algebraic name of this square.
     *
     * @return same value as {@link #toAlgebraic()}
     */
    @Override
    public String toString() {
        return toAlgebraic();
    }
}
