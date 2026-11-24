package chess.console;

import chess.model.Position;
import chess.pieces.PieceType;

/**
 * Converts raw text typed by the user into a {@link Command}.
 *
 * <p>Parsing never throws: any unrecognized or malformed input becomes an
 * {@link Command.Type#INVALID INVALID} command with a message explaining the
 * problem. Input is case-insensitive and surrounding whitespace is ignored.</p>
 *
 * <p>Accepted input:</p>
 * <ul>
 *   <li>{@code E2 E4} - move; {@code E7 E8 N} - move and promote to a knight</li>
 *   <li>{@code moves E2} - list legal destinations</li>
 *   <li>{@code captured}, {@code history}, {@code board}, {@code reset}, {@code help}</li>
 *   <li>{@code quit} or {@code exit}</li>
 * </ul>
 */
public class InputParser {

    /**
     * Creates a parser. The parser is stateless, so one instance can be reused for every line.
     */
    public InputParser() {
    }

    /**
     * Parses one line of input.
     *
     * @param line the raw text; may be null (treated as empty)
     * @return the parsed command, never null
     */
    public Command parse(String line) {
        if (line == null || line.isBlank()) {
            return Command.invalid("Please enter a move like E2 E4, or type 'help'.");
        }
        String[] tokens = line.trim().split("\\s+");
        String keyword = tokens[0].toLowerCase();

        switch (keyword) {
            case "quit":
            case "exit":
                return keywordOnly(tokens, Command.Type.QUIT);
            case "help":
            case "?":
                return keywordOnly(tokens, Command.Type.HELP);
            case "captured":
                return keywordOnly(tokens, Command.Type.CAPTURED);
            case "history":
                return keywordOnly(tokens, Command.Type.HISTORY);
            case "board":
                return keywordOnly(tokens, Command.Type.BOARD);
            case "reset":
                return keywordOnly(tokens, Command.Type.RESET);
            case "moves":
                return parseMovesHint(tokens);
            default:
                return parseMove(tokens);
        }
    }

    /**
     * Builds a no-argument command, rejecting extra tokens such as {@code "quit now"}.
     *
     * @param tokens the split input
     * @param type   the command type to create
     * @return the command, or INVALID if extra tokens were present
     */
    private Command keywordOnly(String[] tokens, Command.Type type) {
        if (tokens.length != 1) {
            return Command.invalid("'" + tokens[0] + "' does not take any arguments.");
        }
        return Command.simple(type);
    }

    /**
     * Parses {@code moves <square>}.
     *
     * @param tokens the split input; tokens[0] is "moves"
     * @return a MOVES command, or INVALID with an explanation
     */
    private Command parseMovesHint(String[] tokens) {
        if (tokens.length != 2) {
            return Command.invalid("Usage: moves <square>, for example: moves G1");
        }
        try {
            return Command.moves(Position.fromAlgebraic(tokens[1]));
        } catch (IllegalArgumentException e) {
            return Command.invalid(e.getMessage());
        }
    }

    /**
     * Parses a move written as {@code FROM TO [PIECE]}, e.g. {@code E2 E4} or {@code E7 E8 N}.
     *
     * @param tokens the split input
     * @return a MOVE command, or INVALID with an explanation
     */
    private Command parseMove(String[] tokens) {
        // ["E2", "E4"] or, for a promotion, ["E7", "E8", "N"].
        if (tokens.length != 2 && tokens.length != 3) {
            return Command.invalid("Usage: <FROM> <TO> [Q|R|B|N], for example: E2 E4 or E7 E8 N");
        }

        try {
            Position from = Position.fromAlgebraic(tokens[0]);
            Position to = Position.fromAlgebraic(tokens[1]);
            if (tokens.length == 2) {
                return Command.move(from, to);
            }
            return Command.move(from, to, parsePromotion(tokens[2]));
        } catch (IllegalArgumentException e) {
            return Command.invalid(e.getMessage());
        }
    }

    /**
     * Parses a promotion letter: Q, R, B, or N (either case).
     *
     * @param token the third word of a move command
     * @return the chosen piece type
     * @throws IllegalArgumentException if the token is not one of those letters
     */
    private PieceType parsePromotion(String token) {
        if (token.length() == 1) {
            PieceType type = PieceType.fromLetter(token.charAt(0));
            if (type.isPromotionChoice()) {
                return type;
            }
        }
        throw new IllegalArgumentException("A pawn can be promoted to Q, R, B, or N, not '" + token + "'.");
    }
}
