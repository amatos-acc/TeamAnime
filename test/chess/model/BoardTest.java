package chess.model;

import static chess.TestBoards.put;
import static chess.TestBoards.sq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import chess.TestBoards;
import chess.pieces.PieceType;
import org.junit.jupiter.api.Test;

/**
 * Tests board setup, copying, paths, and attack detection.
 */
class BoardTest {

    @Test
    void startingPositionHasSixteenPiecesPerSide() {
        Board board = TestBoards.standard();
        assertEquals(16, board.getActivePieces(PieceColor.WHITE).size());
        assertEquals(16, board.getActivePieces(PieceColor.BLACK).size());
    }

    @Test
    void startingPositionPutsPiecesOnTheRightSquares() {
        Board board = TestBoards.standard();
        assertEquals(PieceType.KING, board.getPiece(sq("E1")).getType());
        assertEquals(PieceType.QUEEN, board.getPiece(sq("D8")).getType());
        assertEquals(PieceColor.BLACK, board.getPiece(sq("A7")).getColor());
        assertTrue(board.isEmpty(sq("E4")));
    }

    @Test
    void findKingLocatesBothKings() {
        Board board = TestBoards.standard();
        assertEquals(sq("E1"), board.findKing(PieceColor.WHITE));
        assertEquals(sq("E8"), board.findKing(PieceColor.BLACK));
    }

    @Test
    void copyIsIndependentOfTheOriginal() {
        Board original = TestBoards.standard();
        Board copy = original.copy();
        copy.movePiece(sq("E2"), sq("E4"));

        assertNotNull(original.getPiece(sq("E2")));
        assertTrue(original.isEmpty(sq("E4")));
        assertTrue(copy.isEmpty(sq("E2")));
    }

    @Test
    void pathIsBlockedByPiecesInBetween() {
        Board board = TestBoards.standard();
        assertFalse(board.isPathClear(sq("A1"), sq("A3")));
        assertTrue(board.isPathClear(sq("A2"), sq("A4")));
    }

    @Test
    void capturedPiecesAreRecorded() {
        Board board = TestBoards.empty();
        put(board, PieceType.ROOK, PieceColor.WHITE, "A1");
        put(board, PieceType.KNIGHT, PieceColor.BLACK, "A8");

        board.movePiece(sq("A1"), sq("A8"));

        assertEquals(1, board.getCapturedPieces().size());
        assertEquals(PieceType.KNIGHT, board.getCapturedPieces().get(0).getType());
    }

    @Test
    void squareAttackDetection() {
        Board board = TestBoards.empty();
        put(board, PieceType.ROOK, PieceColor.BLACK, "E8");
        put(board, PieceType.KING, PieceColor.WHITE, "E1");

        assertTrue(board.isSquareAttacked(sq("E4"), PieceColor.BLACK));
        assertFalse(board.isSquareAttacked(sq("D4"), PieceColor.BLACK));
        assertTrue(board.isInCheck(PieceColor.WHITE));
    }

    @Test
    void emptyBoardHasNoKing() {
        assertNull(TestBoards.empty().findKing(PieceColor.WHITE));
    }
}
