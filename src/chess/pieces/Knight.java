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
        // TODO(8): Let dr = |rowDelta(target)| and dc = |colDelta(target)|.
        //   The move is an L when (dr == 2 && dc == 1) || (dr == 1 && dc == 2).
        //   Test from G1: F3 and H3 valid; G3, E2 (friendly pawn - rejected elsewhere), and G1 itself invalid.
        return false;
    }

    /**
     * Knights jump, so blocking pieces never matter.
     *
     * @return {@code false}
     */
    @Override
    public boolean requiresClearPath() {
        // TODO(9): Return the value that lets the knight jump. Then explain (in your report) why
        //   overriding this one method is better than adding "if (piece instanceof Knight)" in Game.
        return true;
    }

    /**
     * Returns the knight's board code.
     *
     * @return {@code 'N'} (K is taken by the king)
     */
    @Override
    public char getSymbol() {
        return 'N';
    }

    /**
     * Returns the type name.
     *
     * @return {@code "Knight"}
     */
    @Override
    public String getName() {
        return "Knight";
    }
}
