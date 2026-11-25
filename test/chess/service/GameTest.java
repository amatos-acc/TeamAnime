package chess.service;

import static chess.TestBoards.put;
import static chess.TestBoards.sq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import chess.TestBoards;
import chess.model.Board;
import chess.model.Move;
import chess.model.MoveType;
import chess.model.PieceColor;
import chess.pieces.Piece;
import chess.pieces.PieceType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests the full game rules: turns, invalid moves, check, game outcomes, and special moves.
 */
class GameTest {

    private Game game;

    @BeforeEach
    void newGame() {
        game = new Game();
    }

    /**
     * Plays moves that must all succeed, such as {@code "E2 E4"}.
     *
     * @param moves the moves to play in order
     */
    private void play(String... moves) {
        for (String move : moves) {
            String[] squares = move.split(" ");
            MoveResult result = game.tryMove(sq(squares[0]), sq(squares[1]));
            assertTrue(result.isSuccess(), move + " should be legal but was rejected: " + result.getMessage());
        }
    }

    /**
     * Attempts a move that must fail and checks that nothing changed.
     *
     * @param from source square
     * @param to   destination square
     * @return the failure message
     */
    private String rejected(String from, String to) {
        PieceColor turnBefore = game.getCurrentPlayer().getColor();
        Piece moverBefore = game.getBoard().getPiece(sq(from));
        Piece targetBefore = game.getBoard().getPiece(sq(to));
        int historyBefore = game.getMoveHistory().size();

        MoveResult result = game.tryMove(sq(from), sq(to));

        assertFalse(result.isSuccess(), from + " " + to + " should have been rejected");
        assertEquals(turnBefore, game.getCurrentPlayer().getColor(), "turn must not change");
        assertEquals(moverBefore, game.getBoard().getPiece(sq(from)), "source square must not change");
        assertEquals(targetBefore, game.getBoard().getPiece(sq(to)), "destination must not change");
        assertEquals(historyBefore, game.getMoveHistory().size(), "history must not change");
        return result.getMessage();
    }

    @Nested
    class NormalPlay {

        @Test
        void whiteMovesFirstAndTurnsAlternate() {
            assertEquals(PieceColor.WHITE, game.getCurrentPlayer().getColor());
            play("E2 E4");
            assertEquals(PieceColor.BLACK, game.getCurrentPlayer().getColor());
            play("E7 E5");
            assertEquals(PieceColor.WHITE, game.getCurrentPlayer().getColor());
            assertEquals(2, game.getMoveHistory().size());
        }

        @Test
        void startingPositionHasTwentyLegalMoves() {
            assertEquals(20, game.getAllLegalMoves().size());
        }

        @Test
        void legalMovesForAPawnAtTheStart() {
            List<?> targets = game.getLegalMoves(sq("E2"));
            assertEquals(2, targets.size());
            assertTrue(targets.contains(sq("E3")));
            assertTrue(targets.contains(sq("E4")));
        }

        @Test
        void captureRemovesTheEnemyPiece() {
            play("E2 E4", "D7 D5");
            MoveResult result = game.tryMove(sq("E4"), sq("D5"));

            assertTrue(result.isCapture());
            assertEquals(PieceType.PAWN, result.getCapturedPiece().getType());
            assertEquals(PieceColor.WHITE, game.getBoard().getPiece(sq("D5")).getColor());
            assertEquals(1, game.getBoard().getCapturedPieces(PieceColor.BLACK).size());
        }

        @Test
        void restartRestoresTheStartingPosition() {
            play("E2 E4", "E7 E5");
            game.start();

            assertEquals(PieceColor.WHITE, game.getCurrentPlayer().getColor());
            assertTrue(game.getMoveHistory().isEmpty());
            assertNotNull(game.getBoard().getPiece(sq("E2")));
            assertTrue(game.getBoard().isEmpty(sq("E4")));
            assertEquals(GameStatus.IN_PROGRESS, game.getStatus());
        }

        @Test
        void listenersAreNotifiedOfMoves() {
            List<MoveResult> seen = new ArrayList<>();
            game.addListener(new GameListener() {
                @Override
                public void onGameStarted(Game g) {
                }

                @Override
                public void onMoveMade(Game g, MoveResult result) {
                    seen.add(result);
                }
            });

            play("E2 E4");
            game.tryMove(sq("E2"), sq("E4"));

            assertEquals(1, seen.size(), "only the successful move should be reported");
        }
    }

    @Nested
    class InvalidMoves {

        @Test
        void emptySourceSquare() {
            assertTrue(rejected("E4", "E5").contains("no piece"));
        }

        @Test
        void movingTheOpponentsPiece() {
            assertTrue(rejected("E7", "E5").contains("White's turn"));
        }

        @Test
        void capturingYourOwnPiece() {
            assertTrue(rejected("A1", "A2").contains("your own"));
        }

        @Test
        void sameSquare() {
            assertTrue(rejected("E2", "E2").contains("same square"));
        }

        @Test
        void illegalPattern() {
            assertTrue(rejected("E2", "E5").contains("cannot move"));
        }

        @Test
        void blockedPath() {
            assertTrue(rejected("A1", "A5").contains("blocked"));
        }

        @Test
        void pinnedPieceCannotExposeItsKing() {
            Board setup = TestBoards.empty();
            put(setup, PieceType.KING, PieceColor.WHITE, "E1");
            put(setup, PieceType.BISHOP, PieceColor.WHITE, "E2");
            put(setup, PieceType.ROOK, PieceColor.BLACK, "E8");
            put(setup, PieceType.KING, PieceColor.BLACK, "A8");
            game.loadPosition(setup, PieceColor.WHITE);

            assertTrue(rejected("E2", "D3").contains("own king in check"));
        }

        @Test
        void promotionChoiceOnANonPromotionMove() {
            MoveResult result = game.tryMove(new Move(sq("E2"), sq("E4"), PieceType.QUEEN));
            assertFalse(result.isSuccess());
            assertTrue(result.getMessage().contains("last rank"));
        }
    }

    @Nested
    class CheckAndOutcomes {

        @Test
        void checkIsDetectedAndMustBeAnswered() {
            play("E2 E4", "F7 F6", "D1 H5");

            assertEquals(GameStatus.CHECK, game.getStatus());
            assertTrue(game.isInCheck(PieceColor.BLACK));
            assertTrue(rejected("A7", "A6").contains("does not get it out of check"));
            play("G7 G6");
            assertFalse(game.isInCheck(PieceColor.BLACK));
        }

        @Test
        void foolsMateEndsWithBlackWinning() {
            play("F2 F3", "E7 E5", "G2 G4", "D8 H4");

            assertEquals(GameStatus.CHECKMATE, game.getStatus());
            assertTrue(game.isGameOver());
            assertEquals(PieceColor.BLACK, game.getWinner().getColor());
            assertTrue(game.describeOutcome().contains("Checkmate"));
            assertTrue(game.getMoveHistory().get(3).endsWith("#"));
        }

        @Test
        void noMovesAreAcceptedAfterCheckmate() {
            play("F2 F3", "E7 E5", "G2 G4", "D8 H4");

            assertTrue(rejected("A2", "A3").contains("game is over"));
            assertTrue(game.getAllLegalMoves().isEmpty());
        }

        @Test
        void stalemateIsADraw() {
            Board setup = TestBoards.empty();
            put(setup, PieceType.KING, PieceColor.BLACK, "A8");
            put(setup, PieceType.KING, PieceColor.WHITE, "B6");
            put(setup, PieceType.QUEEN, PieceColor.WHITE, "D7");
            game.loadPosition(setup, PieceColor.WHITE);

            play("D7 C7");

            assertEquals(GameStatus.STALEMATE, game.getStatus());
            assertTrue(game.isGameOver());
            assertNull(game.getWinner());
            assertFalse(game.isInCheck(PieceColor.BLACK));
        }

        @Test
        void loadingAMatedPositionIsAlreadyOver() {
            Board setup = TestBoards.empty();
            put(setup, PieceType.KING, PieceColor.BLACK, "H8");
            put(setup, PieceType.PAWN, PieceColor.BLACK, "G7");
            put(setup, PieceType.PAWN, PieceColor.BLACK, "H7");
            put(setup, PieceType.ROOK, PieceColor.WHITE, "A8");
            put(setup, PieceType.KING, PieceColor.WHITE, "G1");
            game.loadPosition(setup, PieceColor.BLACK);

            assertEquals(GameStatus.CHECKMATE, game.getStatus());
            assertEquals(PieceColor.WHITE, game.getWinner().getColor());
        }
    }

    @Nested
    class SpecialMoves {

        /** Kings and both white rooks on their starting squares, White to move. */
        private void loadCastlingPosition() {
            Board setup = TestBoards.empty();
            put(setup, PieceType.KING, PieceColor.WHITE, "E1");
            put(setup, PieceType.ROOK, PieceColor.WHITE, "A1");
            put(setup, PieceType.ROOK, PieceColor.WHITE, "H1");
            put(setup, PieceType.KING, PieceColor.BLACK, "D8");
            game.loadPosition(setup, PieceColor.WHITE);
        }

        @Test
        void kingsideCastlingMovesTheRook() {
            loadCastlingPosition();
            MoveResult result = game.tryMove(sq("E1"), sq("G1"));

            assertTrue(result.isSuccess(), result.getMessage());
            assertEquals(MoveType.CASTLE_KINGSIDE, result.getMoveType());
            assertEquals(PieceType.KING, game.getBoard().getPiece(sq("G1")).getType());
            assertEquals(PieceType.ROOK, game.getBoard().getPiece(sq("F1")).getType());
            assertTrue(game.getBoard().isEmpty(sq("H1")));
        }

        @Test
        void queensideCastlingMovesTheRook() {
            loadCastlingPosition();
            MoveResult result = game.tryMove(sq("E1"), sq("C1"));

            assertTrue(result.isSuccess(), result.getMessage());
            assertEquals(MoveType.CASTLE_QUEENSIDE, result.getMoveType());
            assertEquals(PieceType.ROOK, game.getBoard().getPiece(sq("D1")).getType());
            assertTrue(game.getBoard().isEmpty(sq("A1")));
        }

        @Test
        void cannotCastleThroughAnAttackedSquare() {
            Board setup = TestBoards.empty();
            put(setup, PieceType.KING, PieceColor.WHITE, "E1");
            put(setup, PieceType.ROOK, PieceColor.WHITE, "H1");
            put(setup, PieceType.ROOK, PieceColor.BLACK, "F8");
            put(setup, PieceType.KING, PieceColor.BLACK, "A8");
            game.loadPosition(setup, PieceColor.WHITE);

            assertTrue(rejected("E1", "G1").contains("attacked"));
        }

        @Test
        void cannotCastleOutOfCheck() {
            Board setup = TestBoards.empty();
            put(setup, PieceType.KING, PieceColor.WHITE, "E1");
            put(setup, PieceType.ROOK, PieceColor.WHITE, "H1");
            put(setup, PieceType.ROOK, PieceColor.BLACK, "E5");
            put(setup, PieceType.KING, PieceColor.BLACK, "A8");
            game.loadPosition(setup, PieceColor.WHITE);

            assertTrue(rejected("E1", "G1").contains("in check"));
        }

        @Test
        void cannotCastleAfterTheKingHasMoved() {
            loadCastlingPosition();
            play("E1 F1", "D8 C8", "F1 E1", "C8 D8");

            rejected("E1", "G1");
        }

        @Test
        void enPassantCapturesThePassedPawn() {
            play("E2 E4", "A7 A6", "E4 E5", "D7 D5");
            MoveResult result = game.tryMove(sq("E5"), sq("D6"));

            assertTrue(result.isSuccess(), result.getMessage());
            assertEquals(MoveType.EN_PASSANT, result.getMoveType());
            assertTrue(result.isCapture());
            assertTrue(game.getBoard().isEmpty(sq("D5")), "the captured pawn is removed");
            assertEquals(PieceType.PAWN, game.getBoard().getPiece(sq("D6")).getType());
        }

        @Test
        void enPassantMustBePlayedImmediately() {
            play("E2 E4", "A7 A6", "E4 E5", "D7 D5", "H2 H3", "H7 H6");

            rejected("E5", "D6");
        }

        /** A white pawn one step from promotion, White to move. */
        private void loadPromotionPosition() {
            Board setup = TestBoards.empty();
            put(setup, PieceType.PAWN, PieceColor.WHITE, "A7");
            put(setup, PieceType.KING, PieceColor.WHITE, "E1");
            put(setup, PieceType.KING, PieceColor.BLACK, "H6");
            game.loadPosition(setup, PieceColor.WHITE);
        }

        @Test
        void pawnPromotesToAQueenByDefault() {
            loadPromotionPosition();
            assertTrue(game.isPromotionMove(sq("A7"), sq("A8")));

            MoveResult result = game.tryMove(sq("A7"), sq("A8"));

            assertEquals(MoveType.PROMOTION, result.getMoveType());
            assertEquals(PieceType.QUEEN, game.getBoard().getPiece(sq("A8")).getType());
        }

        @Test
        void pawnCanPromoteToAKnight() {
            loadPromotionPosition();
            MoveResult result = game.tryMove(new Move(sq("A7"), sq("A8"), PieceType.KNIGHT));

            assertTrue(result.isSuccess(), result.getMessage());
            assertEquals(PieceType.KNIGHT, game.getBoard().getPiece(sq("A8")).getType());
            assertEquals(PieceColor.WHITE, game.getBoard().getPiece(sq("A8")).getColor());
        }

        @Test
        void ordinaryPawnMoveIsNotAPromotion() {
            assertFalse(game.isPromotionMove(sq("E2"), sq("E4")));
        }
    }
}
