package chess.console;

import chess.model.Position;

/**
 * Converts raw text typed by the user into a {@link Command}.
 *
 * <p>Parsing never throws: any unrecognized or malformed input becomes an
 * {@link Command.Type#INVALID INVALID} command with a message explaining the
 * problem. Input is case-insensitive and surrounding whitespace is ignored.</p>
 *
 * <p>Accepted input:</p>
 * <ul>
 *   <li>{@code E2 E4} - move</li>
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
     * Parses a move written as {@code FROM TO}, e.g. {@code E2 E4}.
     *
     * @param tokens the split input
     * @return a MOVE command, or INVALID with an explanation
     */
    private Command parseMove(String[] tokens) {
        // TODO(15): Turn ["E2", "E4"] into Command.move(from, to).
        //   1. If tokens.length != 2, return Command.invalid(...) explaining the expected format.
        //      (Covers the "E2" test case - one token - and things like "E2 to E4".)
        //   2. Inside a try block, call Position.fromAlgebraic on each token.
        //   3. catch (IllegalArgumentException e) -> return Command.invalid(e.getMessage());
        //      (Covers "Z9 E4".) Never let the exception escape: the UI must not crash.
        //   4. return Command.move(from, to);
        //   Look at parseMovesHint() above - it is the same idea with one square.
        return Command.invalid("Move parsing is not implemented yet (TODO 15 in InputParser).");
    }
}
