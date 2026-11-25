package chess.pieces;

import static chess.TestBoards.put;
import static chess.TestBoards.sq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import chess.TestBoards;
import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests each piece's movement pattern and the shared piece-level checks.
 */
class PieceMovementTest {

    /**
     * Counts the moves of a lone piece on an otherwise empty board.
     *
     * @param type   the piece to place
     * @param square where to place it
     * @return how many squares it can reach
     */
    private static int movesOnEmptyBoard(PieceType type, String square) {
        Board board = TestBoards.empty();
        put(board, type, PieceColor.WHITE, square);
        return board.getPiece(sq(square)).possibleMoves(board).size();
    }

    @ParameterizedTest(name = "{0} on {1} has {2} moves")
    @CsvSource({
        "QUEEN, D4, 27",
        "ROOK, D4, 14",
        "BISHOP, D4, 13",
        "KNIGHT, D4, 8",
        "KNIGHT, A1, 2",
        "KING, D4, 8",
        "KING, A1, 3"
    })
    void loneMoveCountsOnAnEmptyBoard(PieceType type, String square, int expected) {
        assertEquals(expected, movesOnEmptyBoard(type, square));
    }

    @Test
    void knightJumpsOverPieces() {
        Board board = TestBoards.standard();
        assertEquals(MoveProblem.NONE, board.getPiece(sq("B1")).checkMove(sq("C3"), board));
    }

    @Test
    void rookIsBlockedByItsOwnPawn() {
        Board board = TestBoards.standard();
        assertEquals(MoveProblem.OWN_PIECE, board.getPiece(sq("A1")).checkMove(sq("A2"), board));
        assertEquals(MoveProblem.BLOCKED_PATH, board.getPiece(sq("A1")).checkMove(sq("A3"), board));
    }

    @Test
    void bishopCannotMoveStraight() {
        Board board = TestBoards.standard();
        assertEquals(MoveProblem.ILLEGAL_PATTERN, board.getPiece(sq("C1")).checkMove(sq("C3"), board));
    }

    @Test
    void sameSquareIsRejected() {
        Board board = TestBoards.standard();
        assertEquals(MoveProblem.SAME_SQUARE, board.getPiece(sq("E2")).checkMove(sq("E2"), board));
    }

    @Test
    void pawnMovesOneOrTwoFromTheStart() {
        Board board = TestBoards.standard();
        Piece pawn = board.getPiece(sq("E2"));
        assertTrue(pawn.canMoveTo(sq("E3"), board));
        assertTrue(pawn.canMoveTo(sq("E4"), board));
        assertFalse(pawn.canMoveTo(sq("E5"), board));
    }

    @Test
    void pawnCannotMoveTwoAfterLeavingTheStartRow() {
        Board board = TestBoards.empty();
        put(board, PieceType.PAWN, PieceColor.WHITE, "E3");
        Piece pawn = board.getPiece(sq("E3"));
        assertTrue(pawn.canMoveTo(sq("E4"), board));
        assertFalse(pawn.canMoveTo(sq("E5"), board));
    }

    @Test
    void pawnCannotMoveBackward() {
        Board board = TestBoards.empty();
        put(board, PieceType.PAWN, PieceColor.WHITE, "E4");
        put(board, PieceType.PAWN, PieceColor.BLACK, "D5");
        assertFalse(board.getPiece(sq("E4")).canMoveTo(sq("E3"), board));
        assertFalse(board.getPiece(sq("D5")).canMoveTo(sq("D6"), board));
    }

    @Test
    void pawnCapturesDiagonallyButNotStraight() {
        Board board = TestBoards.empty();
        put(board, PieceType.PAWN, PieceColor.WHITE, "E4");
        put(board, PieceType.PAWN, PieceColor.BLACK, "D5");
        put(board, PieceType.PAWN, PieceColor.BLACK, "E5");
        Piece pawn = board.getPiece(sq("E4"));

        assertTrue(pawn.canMoveTo(sq("D5"), board));
        assertFalse(pawn.canMoveTo(sq("E5"), board));
        assertFalse(pawn.canMoveTo(sq("F5"), board));
    }

    @Test
    void pawnTwoStepIsBlockedByAPieceInFront() {
        Board board = TestBoards.standard();
        put(board, PieceType.KNIGHT, PieceColor.BLACK, "E3");
        assertFalse(board.getPiece(sq("E2")).canMoveTo(sq("E4"), board));
    }

    @Test
    void queenCombinesRookAndBishopPatterns() {
        Board board = TestBoards.empty();
        put(board, PieceType.QUEEN, PieceColor.WHITE, "D4");
        Piece queen = board.getPiece(sq("D4"));
        assertTrue(queen.canMoveTo(sq("D8"), board));
        assertTrue(queen.canMoveTo(sq("H8"), board));
        assertFalse(queen.canMoveTo(sq("E6"), board));
    }

    @Test
    void factoryCreatesTheRequestedType() {
        for (PieceType type : PieceType.values()) {
            Piece piece = PieceFactory.create(type, PieceColor.BLACK, new Position(0, 0));
            assertEquals(type, piece.getType());
            assertEquals(PieceColor.BLACK, piece.getColor());
        }
    }

    @Test
    void promotionLettersParseCaseInsensitively() {
        assertEquals(PieceType.KNIGHT, PieceType.fromLetter('n'));
        assertEquals(PieceType.QUEEN, PieceType.fromLetter('Q'));
    }
}
