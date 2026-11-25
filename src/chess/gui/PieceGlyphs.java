package chess.gui;

import chess.model.PieceColor;
import chess.pieces.Piece;
import chess.pieces.PieceType;

/**
 * Maps pieces to Unicode chess symbols, so the GUI needs no image files.
 */
public final class PieceGlyphs {

    /** Not instantiable; all methods are static. */
    private PieceGlyphs() {
    }

    /**
     * Returns the solid (filled) symbol for a piece type. The board draws this shape for
     * both colors and fills it white or black, so the two sides look the same size.
     *
     * @param type the piece type
     * @return a one-character string such as {@code "♛"} for a queen
     */
    public static String solid(PieceType type) {
        switch (type) {
            case KING:
                return "♚";
            case QUEEN:
                return "♛";
            case ROOK:
                return "♜";
            case BISHOP:
                return "♝";
            case KNIGHT:
                return "♞";
            case PAWN:
            default:
                return "♟";
        }
    }

    /**
     * Returns the outlined symbol for white pieces and the solid symbol for black pieces,
     * for plain text such as the captured-pieces labels.
     *
     * @param type  the piece type
     * @param color the piece's color
     * @return a one-character string
     */
    public static String text(PieceType type, PieceColor color) {
        if (color == PieceColor.BLACK) {
            return solid(type);
        }
        // The outlined symbols are exactly 6 code points before the solid ones.
        return String.valueOf((char) (solid(type).charAt(0) - 6));
    }

    /**
     * Returns the plain-text symbol for a piece.
     *
     * @param piece the piece
     * @return a one-character string
     */
    public static String text(Piece piece) {
        return text(piece.getType(), piece.getColor());
    }
}
