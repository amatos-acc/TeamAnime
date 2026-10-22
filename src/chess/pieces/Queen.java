package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

/**
 * A queen: combines rook and bishop movement.
 * Intervening squares must be empty (enforced by {@link Piece#canMoveTo}).
 *
 * <p>Design note: Java has no multiple inheritance of classes, so
 * {@code Queen} cannot extend both {@link Rook} and {@link Bishop}. Instead
 * the shared geometry lives in protected helpers on {@link Piece}, and the
 * queen reuses both.</p>
 */
public class Queen extends Piece {

    /**
     * Creates a queen.
     *
     * @param color    side the queen belongs to
     * @param position starting square
     */
    public Queen(PieceColor color, Position position) {
        super(color, position);
    }

    /**
     * A queen's move is valid when it is either straight or diagonal.
     *
     * @param target destination square
     * @param board  unused; a queen's pattern does not depend on occupancy
     * @return {@code true} for straight or diagonal moves
     */
    @Override
    public boolean isValidPattern(Position target, Board board) {
        // TODO(7): Combine the two helpers you used for Rook and Bishop.
        return false;
    }

    /**
     * Returns the queen's board code.
     *
     * @return {@code 'Q'}
     */
    @Override
    public char getSymbol() {
        return 'Q';
    }

    /**
     * Returns the type name.
     *
     * @return {@code "Queen"}
     */
    @Override
    public String getName() {
        return "Queen";
    }
}
