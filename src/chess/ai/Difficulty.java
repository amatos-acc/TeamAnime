package chess.ai;

/**
 * The computer opponent's difficulty levels, each mapped to a {@link MoveStrategy}.
 */
public enum Difficulty {

    /** Random legal moves. */
    EASY("Easy (random moves)"),

    /** Minimax, two half-moves ahead: takes free pieces and avoids losing its own. */
    MEDIUM("Medium (looks 2 moves ahead)"),

    /** Minimax, three half-moves ahead: slower but sees simple tactics. */
    HARD("Hard (looks 3 moves ahead)");

    /** Text shown in the new-game dialog. */
    private final String label;

    /**
     * Creates a difficulty constant.
     *
     * @param label text shown in the new-game dialog
     */
    Difficulty(String label) {
        this.label = label;
    }

    /**
     * Creates a new strategy object for this difficulty.
     *
     * @return the strategy the computer should use
     */
    public MoveStrategy createStrategy() {
        switch (this) {
            case EASY:
                return new RandomStrategy();
            case HARD:
                return new MinimaxStrategy(3);
            case MEDIUM:
            default:
                return new MinimaxStrategy(2);
        }
    }

    /**
     * Returns the label, so a combo box can display the constant directly.
     *
     * @return the label
     */
    @Override
    public String toString() {
        return label;
    }
}
