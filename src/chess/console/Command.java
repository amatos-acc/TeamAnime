package chess.console;

import chess.model.Position;

/**
 * A single parsed line of user input, produced by {@link InputParser}.
 *
 * <p>Immutable. Use the static factory methods to create instances.</p>
 */
public final class Command {

    /** The kinds of command the console understands. */
    public enum Type {
        /** Move a piece: {@code E2 E4}. */
        MOVE,
        /** Show legal destinations for a piece: {@code moves E2}. */
        MOVES,
        /** List captured pieces. */
        CAPTURED,
        /** List the moves played so far. */
        HISTORY,
        /** Print the board again. */
        BOARD,
        /** Restart from the starting position. */
        RESET,
        /** Show the help text. */
        HELP,
        /** Exit the program. */
        QUIT,
        /** Input that could not be understood. */
        INVALID
    }

    /** What kind of command this is. */
    private final Type type;

    /** Source square for MOVE / MOVES; otherwise {@code null}. */
    private final Position from;

    /** Destination square for MOVE; otherwise {@code null}. */
    private final Position to;

    /** Explanation for INVALID commands; otherwise {@code null}. */
    private final String errorMessage;

    /**
     * Private constructor; use the factory methods.
     *
     * @param type         command type
     * @param from         source square or null
     * @param to           destination square or null
     * @param errorMessage error text or null
     */
    private Command(Type type, Position from, Position to, String errorMessage) {
        this.type = type;
        this.from = from;
        this.to = to;
        this.errorMessage = errorMessage;
    }

    /**
     * Creates a MOVE command.
     *
     * @param from source square
     * @param to   destination square
     * @return the command
     */
    public static Command move(Position from, Position to) {
        return new Command(Type.MOVE, from, to, null);
    }

    /**
     * Creates a MOVES (hint) command for one square.
     *
     * @param square the square whose piece should be inspected
     * @return the command
     */
    public static Command moves(Position square) {
        return new Command(Type.MOVES, square, null, null);
    }

    /**
     * Creates a command that takes no arguments, such as QUIT or HELP.
     *
     * @param type the command type; must not be MOVE, MOVES, or INVALID
     * @return the command
     * @throws IllegalArgumentException if {@code type} requires arguments
     */
    public static Command simple(Type type) {
        if (type == Type.MOVE || type == Type.MOVES || type == Type.INVALID) {
            throw new IllegalArgumentException(type + " requires arguments.");
        }
        return new Command(type, null, null, null);
    }

    /**
     * Creates an INVALID command carrying an explanation for the user.
     *
     * @param errorMessage why the input was rejected
     * @return the command
     */
    public static Command invalid(String errorMessage) {
        return new Command(Type.INVALID, null, null, errorMessage);
    }

    /**
     * Returns the command type.
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the source square.
     *
     * @return source square, or {@code null} if not applicable
     */
    public Position getFrom() {
        return from;
    }

    /**
     * Returns the destination square.
     *
     * @return destination square, or {@code null} if not applicable
     */
    public Position getTo() {
        return to;
    }

    /**
     * Returns the error message for an INVALID command.
     *
     * @return error message, or {@code null} for valid commands
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
