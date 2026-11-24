package chess.pieces;

import chess.model.PieceColor;
import chess.model.Position;

import java.util.Objects;

/**
 * Creates pieces from a {@link PieceType} (Factory pattern).
 *
 * <p>Board setup, pawn promotion, and {@link Piece#copy()} all need to turn
 * "a type, a color, and a square" into the right subclass. Without a factory
 * each of those places would need its own chain of {@code new Queen(...)},
 * {@code new Rook(...)} calls. With it, adding a new piece type means changing
 * only this switch.</p>
 */
public final class PieceFactory {

    /** Not instantiable; all methods are static. */
    private PieceFactory() {
    }

    /**
     * Creates a new piece of the given type.
     *
     * @param type     which subclass to build; must not be null
     * @param color    side the piece belongs to; must not be null
     * @param position square the piece starts on; must not be null
     * @return a new, unmoved piece
     * @throws NullPointerException if any argument is null
     */
    public static Piece create(PieceType type, PieceColor color, Position position) {
        Objects.requireNonNull(type, "type must not be null");
        switch (type) {
            case PAWN:
                return new Pawn(color, position);
            case KNIGHT:
                return new Knight(color, position);
            case BISHOP:
                return new Bishop(color, position);
            case ROOK:
                return new Rook(color, position);
            case QUEEN:
                return new Queen(color, position);
            case KING:
                return new King(color, position);
            default:
                throw new IllegalArgumentException("Unknown piece type: " + type);
        }
    }
}
