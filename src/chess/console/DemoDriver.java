package chess.console;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;
import chess.pieces.King;
import chess.pieces.Pawn;
import chess.pieces.Piece;
import chess.pieces.Rook;
import chess.service.Game;
import chess.service.MoveResult;


/**
 * Non-interactive demonstration driver that runs labeled test scenarios
 * against {@link Game} and {@link InputParser}, printing the input, the
 * expected result, the observed result, and PASS/FAIL for each check.
 *
 * <p>The scenarios include every case suggested in the assignment plus
 * boundary cases for each piece type. The game is reset before each scenario.
 * Run it with {@code java -cp out chess.console.DemoDriver}.</p>
 */
public final class DemoDriver {

    /** Shared parser used by the input-validation scenarios. */
    private final InputParser parser = new InputParser();

    /** Game under test; reset before each scenario. */
    private final Game game = new Game();

    /** Number of checks that passed. */
    private int passed;

    /** Number of checks that failed. */
    private int failed;

    /** Not instantiable from outside; use {@link #main(String[])}. */
    private DemoDriver() {
    }

    /**
     * Runs every scenario and prints a summary.
     *
     * @param args unused
     */
    public static void main(String[] args) {
        DemoDriver driver = new DemoDriver();
        driver.runAll();
    }

    /**
     * Runs all scenarios in order and prints the totals.
     */
    private void runAll() {
        Runnable[] scenarios = {
            this::startingPosition,
            this::openingMoves,
            this::pawnCapture,
            this::rookBlockedKnightJumps,
            this::invalidMoveCases,
            this::inputValidation,
            this::pawnBoundaries,
            this::bishopAndQueen,
            this::kingMoves,
            this::legalMovesHint,
            this::lastRankPawn,
            this::kingCaptureEndsGame,
        };
        for (Runnable scenario : scenarios) {
            try {
                scenario.run();
            } catch (RuntimeException e) {
                record(false, "Scenario crashed", "no exception", e.toString());
            }
        }

        System.out.println();
        System.out.println("==========================================");
        System.out.println("Summary: " + passed + " passed, " + failed + " failed");
        System.out.println("==========================================");
    }

    // ------------------------------------------------------------------
    // Scenarios
    // ------------------------------------------------------------------

    /** Verifies the starting arrangement and that White moves first. */
    private void startingPosition() {
        header("Starting arrangement");
        game.start();
        Board board = game.getBoard();
        check("White king on E1", "wK", symbolAt("E1"));
        check("Black queen on D8", "bQ", symbolAt("D8"));
        check("White knight on G1", "wN", symbolAt("G1"));
        check("Black pawn on A7", "bp", symbolAt("A7"));
        check("E4 is empty", "##", symbolAt("E4"));
        check("White has 16 pieces", 16, board.getActivePieces(PieceColor.WHITE).size());
        check("Black has 16 pieces", 16, board.getActivePieces(PieceColor.BLACK).size());
        check("White moves first", PieceColor.WHITE, game.getCurrentPlayer().getColor());
    }

    /** Suggested test: E2 E4, then E7 E5. */
    private void openingMoves() {
        header("E2 E4, then E7 E5");
        game.start();
        checkMove("E2 E4", true);
        checkMove("E7 E5", true);
        check("E4 holds the white pawn", "wp", symbolAt("E4"));
        check("E5 holds the black pawn", "bp", symbolAt("E5"));
        check("E2 is now empty", "##", symbolAt("E2"));
        check("Pawn's own position updated", "E4", game.getBoard().getPiece(pos("E4")).getPosition().toString());
        check("White moves next", PieceColor.WHITE, game.getCurrentPlayer().getColor());
    }

    /** Suggested test: E2 E4, D7 D5, E4 D5 (white pawn captures). */
    private void pawnCapture() {
        header("E2 E4, D7 D5, E4 D5 (capture)");
        game.start();
        checkMove("E2 E4", true);
        checkMove("D7 D5", true);
        MoveResult result = checkMove("E4 D5", true);
        check("Move reported a capture", true, result.isCapture());
        check("D5 holds the white pawn", "wp", symbolAt("D5"));
        check("E4 is now empty", "##", symbolAt("E4"));
        check("One black piece captured", 1, game.getBoard().getCapturedPieces(PieceColor.BLACK).size());
        check("Black has 15 pieces", 15, game.getBoard().getActivePieces(PieceColor.BLACK).size());
        check("Black moves next", PieceColor.BLACK, game.getCurrentPlayer().getColor());
    }

    /** Suggested tests: A1 A3 is blocked; G1 F3 is a legal knight jump. */
    private void rookBlockedKnightJumps() {
        header("A1 A3 (rook blocked)");
        game.start();
        checkMove("A1 A3", false);
        check("Rook still on A1", "wR", symbolAt("A1"));

        header("G1 F3 (knight jumps)");
        game.start();
        checkMove("G1 F3", true);
        check("Knight on F3", "wN", symbolAt("F3"));

        header("B1 B3 (knight cannot move straight)");
        game.start();
        checkMove("B1 B3", false);
    }

    /** Suggested tests: invalid movement, wrong owner, friendly destination, empty source, same square. */
    private void invalidMoveCases() {
        String[][] cases = {
            {"E2 E5", "invalid pawn movement"},
            {"E7 E5", "wrong player's piece"},
            {"E2 E1", "friendly destination"},
            {"E1 E2", "king onto friendly pawn"},
            {"E3 E4", "empty source square"},
            {"E2 E2", "same square"},
        };
        for (String[] c : cases) {
            header(c[0] + " (" + c[1] + ")");
            game.start();
            checkMove(c[0], false);
            checkStateUnchanged();
        }
    }

    /** Suggested tests: Z9 E4; E2; quit - plus other malformed input. */
    private void inputValidation() {
        header("Input validation");
        checkParse("Z9 E4", Command.Type.INVALID);
        checkParse("E2", Command.Type.INVALID);
        checkParse("E2 E4 E6", Command.Type.INVALID);
        checkParse("I1 A1", Command.Type.INVALID);
        checkParse("A0 A1", Command.Type.INVALID);
        checkParse("", Command.Type.INVALID);
        checkParse("hello", Command.Type.INVALID);
        checkParse("  e2   e4  ", Command.Type.MOVE);
        checkParse("A1 H8", Command.Type.MOVE);
        checkParse("moves g1", Command.Type.MOVES);
        checkParse("QUIT", Command.Type.QUIT);
        checkParse("quit now", Command.Type.INVALID);

        Command lower = parser.parse("e2 e4");
        if (lower.getType() == Command.Type.MOVE) {
            check("'e2 e4' parses to E2 -> E4", "E2 E4", lower.getFrom() + " " + lower.getTo());
        }
    }

    /** Pawn boundary cases: backward, blocked, diagonal into empty square, double step after moving. */
    private void pawnBoundaries() {
        header("Pawn: single step E2 E3");
        game.start();
        checkMove("E2 E3", true);

        header("Pawn: backward move rejected");
        game.start();
        checkMove("E2 E4", true);
        checkMove("A7 A6", true);
        checkMove("E4 E3", false);

        header("Pawn: double step only from start rank");
        game.start();
        checkMove("E2 E3", true);
        checkMove("A7 A6", true);
        checkMove("E3 E5", false);

        header("Pawn: blocked straight ahead");
        game.start();
        checkMove("E2 E4", true);
        checkMove("E7 E5", true);
        checkMove("E4 E5", false);

        header("Pawn: double step blocked by piece in between");
        game.start();
        checkMove("G1 F3", true);
        checkMove("A7 A6", true);
        checkMove("F2 F4", false);

        header("Pawn: cannot move diagonally into empty square");
        game.start();
        checkMove("E2 D3", false);

        header("Pawn: black advances toward rank 1");
        game.start();
        checkMove("A2 A3", true);
        checkMove("H7 H5", true);
        checkMove("A3 A4", true);
        checkMove("H5 H6", false);
    }

    /** Bishop and queen movement, including a capture by the queen. */
    private void bishopAndQueen() {
        header("Bishop: F1 C4 after opening the diagonal");
        game.start();
        checkMove("E2 E4", true);
        checkMove("E7 E5", true);
        checkMove("F1 C4", true);
        checkMove("A7 A6", true);
        checkMove("C4 C5", false);

        header("Bishop: blocked at start");
        game.start();
        checkMove("C1 E3", false);

        header("Queen: D1 H5, then H5 F7 captures");
        game.start();
        checkMove("E2 E4", true);
        checkMove("E7 E5", true);
        checkMove("D1 H5", true);
        checkMove("B8 C6", true);
        MoveResult capture = checkMove("H5 F7", true);
        check("Queen move captured a piece", true, capture.isCapture());

        header("Queen: L-shaped move rejected");
        game.start();
        checkMove("E2 E4", true);
        checkMove("E7 E5", true);
        checkMove("D1 E3", false);
    }

    /** King moves exactly one square. */
    private void kingMoves() {
        header("King: one square OK, two squares rejected");
        game.start();
        checkMove("E2 E4", true);
        checkMove("E7 E5", true);
        checkMove("E1 E3", false);
        checkMove("E1 E2", true);
    }

    /** The "moves" hint uses possibleMoves() polymorphically. */
    private void legalMovesHint() {
        header("Legal-move hints");
        game.start();
        check("Knight on G1 can reach", "[F3, H3]", game.getLegalMoves(pos("G1")).toString());
        check("Pawn on E2 can reach", "[E4, E3]", game.getLegalMoves(pos("E2")).toString());
        check("Rook on A1 has no moves", "[]", game.getLegalMoves(pos("A1")).toString());
    }

    /** Documents the last-rank pawn behavior using a custom position. */
    private void lastRankPawn() {
        header("Pawn reaching the last rank (custom position)");
        game.start();
        Board board = game.getBoard();
        board.clear();
        board.placePiece(new King(PieceColor.WHITE, pos("E1")));
        board.placePiece(new King(PieceColor.BLACK, pos("H8")));
        board.placePiece(new Pawn(PieceColor.WHITE, pos("A7")));
        MoveResult result = checkMove("A7 A8", true);
        System.out.println("    message : " + result.getMessage());
        Piece onA8 = board.getPiece(pos("A8"));
        System.out.println("    A8 now  : " + (onA8 == null ? "empty" : onA8.getDisplaySymbol()));
    }

    /** Capturing the king ends the game and blocks further moves. */
    private void kingCaptureEndsGame() {
        header("Capturing the king ends the game (custom position)");
        game.start();
        Board board = game.getBoard();
        board.clear();
        board.placePiece(new King(PieceColor.WHITE, pos("E1")));
        board.placePiece(new Rook(PieceColor.WHITE, pos("A1")));
        board.placePiece(new King(PieceColor.BLACK, pos("A8")));
        checkMove("A1 A8", true);
        check("Game is over", true, game.isGameOver());
        check("White is the winner", PieceColor.WHITE,
                game.getWinner() == null ? null : game.getWinner().getColor());
        checkMove("E1 E2", false);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Prints a scenario heading.
     *
     * @param title scenario name
     */
    private void header(String title) {
        System.out.println();
        System.out.println("--- " + title + " ---");
    }

    /**
     * Parses and plays a move, checking whether it was accepted.
     *
     * @param input          move text such as {@code "E2 E4"}
     * @param expectAccepted whether the move should succeed
     * @return the move result (a failure result if parsing failed)
     */
    private MoveResult checkMove(String input, boolean expectAccepted) {
        Command command = parser.parse(input);
        MoveResult result;
        if (command.getType() != Command.Type.MOVE) {
            result = MoveResult.failure("Parse error: " + command.getErrorMessage());
        } else {
            result = game.tryMove(command.getFrom(), command.getTo());
        }
        String expected = expectAccepted ? "accepted" : "rejected";
        String observed = (result.isSuccess() ? "accepted" : "rejected") + " - " + result.getMessage();
        record(expected.equals(result.isSuccess() ? "accepted" : "rejected"),
                "Input: " + input, expected, observed);
        return result;
    }

    /**
     * Parses text and checks the resulting command type.
     *
     * @param input    text to parse
     * @param expected the expected command type
     */
    private void checkParse(String input, Command.Type expected) {
        Command command = parser.parse(input);
        String observed = command.getType()
                + (command.getErrorMessage() != null ? " - " + command.getErrorMessage() : "");
        record(command.getType() == expected, "Input: \"" + input + "\"", expected.toString(), observed);
    }

    /**
     * Checks that a rejected move left the starting position and turn intact.
     */
    private void checkStateUnchanged() {
        check("Board unchanged (E2 still wp)", "wp", symbolAt("E2"));
        check("Board unchanged (E1 still wK)", "wK", symbolAt("E1"));
        check("No pieces captured", 0, game.getBoard().getCapturedPieces().size());
        check("Still White's turn", PieceColor.WHITE, game.getCurrentPlayer().getColor());
    }

    /**
     * Compares an expected and observed value.
     *
     * @param label    what is being checked
     * @param expected expected value
     * @param observed actual value
     */
    private void check(String label, Object expected, Object observed) {
        boolean ok = expected == null ? observed == null : expected.equals(observed);
        record(ok, label, String.valueOf(expected), String.valueOf(observed));
    }

    /**
     * Records and prints one check.
     *
     * @param ok       whether the check passed
     * @param label    description of the check
     * @param expected expected result text
     * @param observed observed result text
     */
    private void record(boolean ok, String label, String expected, String observed) {
        if (ok) {
            passed++;
        } else {
            failed++;
        }
        System.out.printf("[%s] %s%n    expected: %s%n    observed: %s%n",
                ok ? "PASS" : "FAIL", label, expected, observed);
    }

    /**
     * Returns the display symbol on a square, or {@code ##} if it is empty.
     *
     * @param square algebraic square name
     * @return two-character symbol
     */
    private String symbolAt(String square) {
        Position p = pos(square);
        if (p == null) {
            return "(unparseable square " + square + ")";
        }
        Piece piece = game.getBoard().getPiece(p);
        return piece == null ? "##" : piece.getDisplaySymbol();
    }

    /**
     * Converts a square name to a position without letting a stubbed parser crash the driver.
     *
     * @param square algebraic square name
     * @return the position, or {@code null} if parsing failed
     */
    private static Position pos(String square) {
        try {
            return Position.fromAlgebraic(square);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
