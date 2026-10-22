package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

/**
 * A king: moves exactly one square in any direction.
 *
 * <p>Simplified prototype: king-safety (check) is not enforced, and castling
 * is not supported. Capturing the opposing king ends the game instead.</p>
 */
public class King extends Piece {

    /**
     * Creates a king.
     *
     * @param color    side the king belongs to
     * @param position starting square
     */
    public King(PieceColor color, Position position) {
        super(color, position);
    }

    /**
     * A king's move is valid when it changes each coordinate by at most one.
     *
     * @param target destination square (different from the current one)
     * @param board  unused; a king's pattern does not depend on occupancy
     * @return {@code true} for one-square moves in any direction
     */
    @Override
    public boolean isValidPattern(Position target, Board board) {
        // TODO(10): Valid when |rowDelta| <= 1 and |colDelta| <= 1.
        //   (canMoveTo already rejects target == current square, so you don't need to check for 0,0.)
        //   Optional advanced rule: castling would go here (and is "O-O"/"O-O-O" input) - not required.
        return false;
    }

    /**
     * Returns the king's board code.
     *
     * @return {@code 'K'}
     */
    @Override
    public char getSymbol() {
        return 'K';
    }

    /**
     * Returns the type name.
     *
     * @return {@code "King"}
     */
    @Override
    public String getName() {
        return "King";
    }
}
