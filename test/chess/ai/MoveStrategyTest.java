package chess.ai;

import static chess.TestBoards.put;
import static chess.TestBoards.sq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import chess.TestBoards;
import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;
import chess.model.Position;
import chess.pieces.Piece;
import chess.pieces.PieceType;
import chess.service.Game;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests the computer opponent's strategies and the board evaluator.
 */
class MoveStrategyTest {

    /** Stalemate: Black to move with no legal moves and not in check. */
    private static Board stalematePosition() {
        Board board = TestBoards.empty();
        put(board, PieceType.KING, PieceColor.BLACK, "A8");
        put(board, PieceType.KING, PieceColor.WHITE, "B6");
        put(board, PieceType.QUEEN, PieceColor.WHITE, "C7");
        return board;
    }

    /**
     * Summarizes every square so two boards can be compared.
     *
     * @param board the board to describe
     * @return one character per square
     */
    private static String snapshot(Board board) {
        StringBuilder text = new StringBuilder();
        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                Piece piece = board.getPiece(new Position(row, col));
                text.append(piece == null ? '.' : piece.getDisplaySymbol().charAt(0));
            }
        }
        return text.toString();
    }

    @Test
    void randomStrategyAlwaysReturnsALegalMove() {
        RandomStrategy strategy = new RandomStrategy(new Random(42));
        Game game = new Game();
        for (int i = 0; i < 40 && !game.isGameOver(); i++) {
            PieceColor side = game.getCurrentPlayer().getColor();
            Move move = strategy.chooseMove(game.getBoard().copy(), side);
            assertNotNull(move);
            assertTrue(game.getAllLegalMoves().contains(move), "illegal choice " + move);
            assertTrue(game.tryMove(move).isSuccess());
        }
    }

    @ParameterizedTest
    @EnumSource(Difficulty.class)
    void everyDifficultyReturnsNullWhenThereIsNoLegalMove(Difficulty difficulty) {
        assertNull(difficulty.createStrategy().chooseMove(stalematePosition(), PieceColor.BLACK));
    }

    @Test
    void easyUsesTheRandomStrategy() {
        assertInstanceOf(RandomStrategy.class, Difficulty.EASY.createStrategy());
    }

    @Test
    void minimaxCapturesAHangingQueen() {
        Board board = TestBoards.empty();
        put(board, PieceType.KING, PieceColor.WHITE, "A1");
        put(board, PieceType.ROOK, PieceColor.WHITE, "D1");
        put(board, PieceType.QUEEN, PieceColor.BLACK, "D5");
        put(board, PieceType.KING, PieceColor.BLACK, "H8");

        Move move = new MinimaxStrategy(2).chooseMove(board, PieceColor.WHITE);

        assertEquals(new Move(sq("D1"), sq("D5")), move);
    }

    @Test
    void minimaxFindsMateInOne() {
        Board board = TestBoards.empty();
        put(board, PieceType.KING, PieceColor.BLACK, "H8");
        put(board, PieceType.PAWN, PieceColor.BLACK, "G7");
        put(board, PieceType.PAWN, PieceColor.BLACK, "H7");
        put(board, PieceType.ROOK, PieceColor.WHITE, "A1");
        put(board, PieceType.KING, PieceColor.WHITE, "G1");

        Move move = new MinimaxStrategy(2).chooseMove(board, PieceColor.WHITE);

        assertEquals(new Move(sq("A1"), sq("A8")), move);
    }

    @Test
    void strategiesDoNotChangeTheBoardTheyAreGiven() {
        Board board = TestBoards.standard();
        String before = snapshot(board);

        new MinimaxStrategy(2).chooseMove(board, PieceColor.WHITE);
        new RandomStrategy(new Random(1)).chooseMove(board, PieceColor.WHITE);

        assertEquals(before, snapshot(board));
    }

    @Test
    void evaluatorScoresMaterial() {
        BoardEvaluator evaluator = new BoardEvaluator();
        Board board = TestBoards.standard();
        assertEquals(0, evaluator.evaluate(board, PieceColor.WHITE), "the start is balanced");

        board.movePiece(sq("D1"), sq("D8"));
        assertTrue(evaluator.evaluate(board, PieceColor.WHITE) > 0, "White is up a queen");
        assertTrue(evaluator.evaluate(board, PieceColor.BLACK) < 0, "Black is down a queen");
    }
}
