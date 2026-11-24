package chess.pieces;

/**
 * The six kinds of chess piece, with the data that does not depend on color.
 *
 * <p>Phase 1 repeated the board code and name in every subclass
 * ({@code getSymbol()} and {@code getName()} overrides). Phase 2 keeps that
 * data in one place here, and each subclass only reports its type through
 * {@link Piece#getType()}. The enum is also what {@link PieceFactory} uses to
 * decide which subclass to build, which is how promotion and board setup
 * create pieces.</p>
 */
public enum PieceType {

    /** A pawn, worth 100 points to the computer opponent. */
    PAWN('p', "Pawn", 100),

    /** A knight, worth 320 points to the computer opponent. */
    KNIGHT('N', "Knight", 320),

    /** A bishop, worth 330 points to the computer opponent. */
    BISHOP('B', "Bishop", 330),

    /** A rook, worth 500 points to the computer opponent. */
    ROOK('R', "Rook", 500),

    /** A queen, worth 900 points to the computer opponent. */
    QUEEN('Q', "Queen", 900),

    /** A king. Its value only needs to be larger than everything else combined. */
    KING('K', "King", 20000);

    /** One-letter board code ({@code p} is lower case, as in the Phase 1 assignment). */
    private final char symbol;

    /** Name shown to the user, e.g. {@code "Knight"}. */
    private final String displayName;

    /** Material value used by the computer opponent's evaluation. */
    private final int value;

    /**
     * Creates a piece type constant.
     *
     * @param symbol      one-letter board code
     * @param displayName name shown to the user
     * @param value       material value in centipawns
     */
    PieceType(char symbol, String displayName, int value) {
        this.symbol = symbol;
        this.displayName = displayName;
        this.value = value;
    }

    /**
     * Returns the one-letter board code.
     *
     * @return one of {@code p, N, B, R, Q, K}
     */
    public char getSymbol() {
        return symbol;
    }

    /**
     * Returns the name shown to the user.
     *
     * @return for example {@code "Queen"}
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the material value used by the computer opponent.
     *
     * @return value in centipawns (a pawn is 100)
     */
    public int getValue() {
        return value;
    }

    /**
     * Checks whether a pawn may be promoted to this type.
     *
     * @return {@code true} for queen, rook, bishop, and knight
     */
    public boolean isPromotionChoice() {
        return this == QUEEN || this == ROOK || this == BISHOP || this == KNIGHT;
    }

    /**
     * Looks up a type from a letter such as {@code 'Q'} or {@code 'n'} (case-insensitive).
     *
     * @param letter the board code to look up
     * @return the matching type
     * @throws IllegalArgumentException if no type uses that letter
     */
    public static PieceType fromLetter(char letter) {
        for (PieceType type : values()) {
            if (Character.toUpperCase(type.symbol) == Character.toUpperCase(letter)) {
                return type;
            }
        }
        throw new IllegalArgumentException("'" + letter + "' is not a piece letter (use Q, R, B, or N).");
    }
}
