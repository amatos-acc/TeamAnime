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
     * Identifies this piece as a bishop; the symbol and name come from {@link PieceType#BISHOP}.
     *
     * @return {@link PieceType#BISHOP}
     */
    @Override
    public PieceType getType() {
        return PieceType.BISHOP;
    }
}
