package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

/**
 * A bishop: moves any number of squares diagonally.
 * Intervening squares must be empty (enforced by {@link Piece#canMoveTo}).
 */
public class Bishop extends Piece {

    /**
     * Creates a bishop.
     *
     * @param color    side the bishop belongs to
     * @param position starting square
     */
    public Bishop(PieceColor color, Position position) {
        super(color, position);
    }

    /**
     * A bishop's move is valid when it travels along a diagonal.
     *
     * @param target destination square
     * @param board  unused; a bishop's pattern does not depend on occupancy
     * @return {@code true} for diagonal moves
     */
    @Override
    public boolean isValidPattern(Position target, Board board) {
        return isDiagonal(target);
    }

    /**
     * Returns the bishop's board code.
     *
     * @return {@code 'B'}
     */
    @Override
    public char getSymbol() {
        return 'B';
    }

    /**
     * Returns the type name.
     *
     * @return {@code "Bishop"}
     */
    @Override
    public String getName() {
        return "Bishop";
    }
}
