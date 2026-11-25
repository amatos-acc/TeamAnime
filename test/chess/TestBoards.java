package chess;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;
import chess.pieces.PieceFactory;
import chess.pieces.PieceType;

/**
 * Helpers shared by the unit tests for building positions and naming squares.
 */
public final class TestBoards {

    /** Not instantiable; all methods are static. */
    private TestBoards() {
    }

    /**
     * Shorthand for {@link Position#fromAlgebraic(String)}.
     *
     * @param square a square name such as {@code "E2"}
     * @return the matching position
     */
    public static Position sq(String square) {
        return Position.fromAlgebraic(square);
    }

    /**
     * Returns a new empty board.
     *
     * @return a board with no pieces
     */
    public static Board empty() {
        return new Board();
    }

    /**
     * Returns a new board in the standard starting position.
     *
     * @return a fully set-up board
     */
    public static Board standard() {
        Board board = new Board();
        board.initialize();
        return board;
    }

    /**
     * Places a new piece on a board.
     *
     * @param board  the board to change
     * @param type   the kind of piece
     * @param color  the piece's color
     * @param square where to place it, such as {@code "E1"}
     * @return the same board, so calls can be chained
     */
    public static Board put(Board board, PieceType type, PieceColor color, String square) {
        board.placePiece(PieceFactory.create(type, color, sq(square)));
        return board;
    }
}
