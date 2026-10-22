package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

/**
 * A rook: moves any number of squares along a rank or file.
 * Intervening squares must be empty (enforced by {@link Piece#canMoveTo}).
 *
 * <p>This class is the fully worked example of a piece subclass. Use it as
 * the model for the other five.</p>
 */
public class Rook extends Piece {

    /**
     * Creates a rook.
     *
     * @param color    side the rook belongs to
     * @param position starting square
     */
    public Rook(PieceColor color, Position position) {
        super(color, position);
    }

    /**
     * A rook's move is valid when it stays on the same rank or the same file.
     *
     * @param target destination square
     * @param board  unused; a rook's pattern does not depend on occupancy
     * @return {@code true} for horizontal or vertical moves
     */
    @Override
    public boolean isValidPattern(Position target, Board board) {
        return isStraightLine(target);
    }

    /**
     * Returns the rook's board code.
     *
     * @return {@code 'R'}
     */
    @Override
    public char getSymbol() {
        return 'R';
    }

    /**
     * Returns the type name.
     *
     * @return {@code "Rook"}
     */
    @Override
    public String getName() {
        return "Rook";
    }
}
