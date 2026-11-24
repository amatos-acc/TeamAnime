package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

/**
 * A rook: moves any number of squares along a rank or file.
 * Intervening squares must be empty (enforced by {@link Piece#checkMove}).
 * A rook also takes part in castling; see {@link King}.
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
     * Identifies this piece as a rook; the symbol and name come from {@link PieceType#ROOK}.
     *
     * @return {@link PieceType#ROOK}
     */
    @Override
    public PieceType getType() {
        return PieceType.ROOK;
    }
}
