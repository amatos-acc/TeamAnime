package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

/**
 * A knight: moves in an "L" (two squares on one axis, one on the other)
 * and may jump over any intervening pieces.
 */
public class Knight extends Piece {

    /**
     * Creates a knight.
     *
     * @param color    side the knight belongs to
     * @param position starting square
     */
    public Knight(PieceColor color, Position position) {
        super(color, position);
    }

    /**
     * A knight's move is valid when it changes one coordinate by 2 and the other by 1.
     *
     * @param target destination square
     * @param board  unused; a knight's pattern does not depend on occupancy
     * @return {@code true} for L-shaped moves
     */
    @Override
    public boolean isValidPattern(Position target, Board board) {
        // The move is an L when (dr == 2 && dc == 1) || (dr == 1 && dc == 2).
        int dr = Math.abs(rowDelta(target));
        int dc = Math.abs(colDelta(target));
        return (dr == 2 && dc == 1) || (dr == 1 && dc == 2);
    }

    /**
     * Knights jump, so blocking pieces never matter.
     *
     * @return {@code false}
     */
    @Override
    public boolean requiresClearPath() {
        return false;
    }

    /**
     * Identifies this piece as a knight; the symbol and name come from {@link PieceType#KNIGHT}.
     *
     * @return {@link PieceType#KNIGHT}
     */
    @Override
    public PieceType getType() {
        return PieceType.KNIGHT;
    }
}
