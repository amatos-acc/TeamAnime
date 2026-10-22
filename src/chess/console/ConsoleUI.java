package chess.console;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;
import chess.pieces.Piece;
import chess.service.Game;
import chess.service.MoveResult;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * Text interface for the game: prints the board, reads commands, and
 * forwards moves to {@link Game}.
 *
 * <p>All console input and output lives in this class. Streams are injected
 * through the constructor, so the UI can be driven from a file or a string
 * for testing instead of the keyboard.</p>
 */
public class ConsoleUI {

    /** Symbol printed for an empty square, as specified in the assignment. */
    private static final String EMPTY_SQUARE = "##";

    /** The game being played. */
    private final Game game;

    /** Converts typed lines into commands. */
    private final InputParser parser;

    /** Source of user input. */
    private final Scanner in;

    /** Destination for all output. */
    private final PrintStream out;

    /**
     * Creates a console UI.
     *
     * @param game   the game to play; must not be null
     * @param input  where commands are read from (usually {@code System.in})
     * @param output where text is written (usually {@code System.out})
     */
    public ConsoleUI(Game game, InputStream input, PrintStream output) {
        this.game = Objects.requireNonNull(game, "game must not be null");
        this.parser = new InputParser();
        this.in = new Scanner(Objects.requireNonNull(input, "input must not be null"));
        this.out = Objects.requireNonNull(output, "output must not be null");
    }

    /**
     * Runs the read-evaluate-print loop until the user quits or input ends.
     */
    public void run() {
        out.println("=== Console Chess (simplified prototype) ===");
        out.println("Type 'help' for commands.");
        displayBoard();

        boolean running = true;
        while (running) {
            printPrompt();
            if (!in.hasNextLine()) {
                out.println();
                out.println("Input closed. Goodbye!");
                break;
            }
            Command command = parser.parse(in.nextLine());
            running = handle(command);
        }
        game.end();
    }

    /**
     * Executes one command.
     *
     * @param command the parsed command
     * @return {@code false} if the loop should stop, {@code true} otherwise
     */
    private boolean handle(Command command) {
        switch (command.getType()) {
            case QUIT:
                out.println("Thanks for playing. Goodbye!");
                return false;
            case MOVE:
                handleMove(command.getFrom(), command.getTo());
                break;
            case MOVES:
                displayLegalMoves(command.getFrom());
                break;
            case CAPTURED:
                displayCaptured();
                break;
            case HISTORY:
                displayHistory();
                break;
            case BOARD:
                displayBoard();
                break;
            case RESET:
                game.start();
                out.println("Board reset. White moves first.");
                displayBoard();
                break;
            case HELP:
                displayHelp();
                break;
            case INVALID:
            default:
                out.println("Invalid input: " + command.getErrorMessage());
                break;
        }
        return true;
    }

    /**
     * Asks the game to make a move and reports the result.
     *
     * @param from source square
     * @param to   destination square
     */
    private void handleMove(Position from, Position to) {
        MoveResult result = game.tryMove(from, to);
        if (result.isSuccess()) {
            out.println(result.getMessage());
            displayBoard();
        } else {
            out.println("Illegal move: " + result.getMessage());
            out.println("The board is unchanged. It is still "
                    + game.getCurrentPlayer().getColor().displayName() + "'s turn.");
        }
    }

    /**
     * Prints the prompt for the current player, or a game-over notice.
     */
    private void printPrompt() {
        if (game.isGameOver()) {
            out.print("Game over. Type 'reset' to play again or 'quit' to exit > ");
        } else {
            out.print(game.getCurrentPlayer() + " to move > ");
        }
    }

    /**
     * Prints the board with files A-H across the top and ranks 8-1 down the left,
     * using {@code ##} for empty squares and symbols like {@code wK} for pieces.
     */
    public void displayBoard() {
        // TODO(16): Print the board in the format shown on page 8 of the instructions:
        //
        //        A  B  C  D  E  F  G  H
        //     8  bR bN bB bQ bK bB bN bR  8
        //     7  bp bp bp bp bp bp bp bp  7
        //     6  ## ## ## ## ## ## ## ##  6
        //     ...
        //     1  wR wN wB wQ wK wB wN wR  1
        //        A  B  C  D  E  F  G  H
        //
        //   Hints:
        //   - Board board = game.getBoard();
        //   - Outer loop: row 0..7, printing rank label (8 - row). Inner loop: col 0..7.
        //   - Piece p = board.getPiece(new Position(row, col));
        //     print p == null ? EMPTY_SQUARE : p.getDisplaySymbol()
        //   - Use out.print / out.println (NOT System.out) so tests can capture the output.
        //   - Repeating the labels on the right/bottom is optional, but it makes the board easier to read.
        out.println("[board display not implemented yet - TODO 16 in ConsoleUI.displayBoard]");
    }

    /**
     * Prints the squares the piece on {@code square} could move to.
     *
     * @param square the square to inspect
     */
    private void displayLegalMoves(Position square) {
        Piece piece = game.getBoard().getPiece(square);
        if (piece == null) {
            out.println("There is no piece on " + square + ".");
            return;
        }
        List<Position> moves = game.getLegalMoves(square);
        if (moves.isEmpty()) {
            out.println(piece + " has no available moves.");
        } else {
            out.println(piece + " can move to: " + joinPositions(moves));
        }
    }

    /**
     * Prints the captured pieces for each color.
     */
    private void displayCaptured() {
        Board board = game.getBoard();
        for (PieceColor color : PieceColor.values()) {
            List<Piece> lost = board.getCapturedPieces(color);
            StringBuilder line = new StringBuilder(color.displayName() + " pieces captured ("
                    + lost.size() + "):");
            for (Piece piece : lost) {
                line.append(' ').append(piece.getDisplaySymbol());
            }
            out.println(line);
        }
    }

    /**
     * Prints the move history, one move per line.
     */
    private void displayHistory() {
        List<String> history = game.getMoveHistory();
        if (history.isEmpty()) {
            out.println("No moves have been played yet.");
            return;
        }
        for (String entry : history) {
            out.println(entry);
        }
    }

    /**
     * Prints the list of commands.
     */
    private void displayHelp() {
        out.println("Commands:");
        out.println("  E2 E4       move the piece on E2 to E4 (captures are detected automatically)");
        out.println("  moves E2    list the squares the piece on E2 can move to");
        out.println("  captured    show captured pieces");
        out.println("  history     show the moves played so far");
        out.println("  board       print the board again");
        out.println("  reset       start a new game");
        out.println("  help        show this message");
        out.println("  quit        exit the program");
        out.println("Simplified rules: no check/checkmate, castling, en passant, or promotion.");
        out.println("Capturing the opposing king wins the game.");
    }

    /**
     * Joins positions into a comma-separated string such as {@code "F3, H3"}.
     *
     * @param positions the positions to join
     * @return the joined text
     */
    private static String joinPositions(List<Position> positions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < positions.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(positions.get(i));
        }
        return sb.toString();
    }
}
