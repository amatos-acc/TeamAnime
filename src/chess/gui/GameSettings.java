package chess.gui;

import chess.ai.Difficulty;
import chess.model.PieceColor;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The choices made in the new-game dialog: player names, and whether one side
 * is played by the computer (and how well). Immutable.
 */
public final class GameSettings {

    /** Longest player name accepted. */
    public static final int MAX_NAME_LENGTH = 20;

    /** Characters allowed in a player name. */
    private static final Pattern NAME_CHARS = Pattern.compile("[A-Za-z0-9 ._'-]+");

    /** White player's name, or {@code null} for the default. */
    private final String whiteName;

    /** Black player's name, or {@code null} for the default. */
    private final String blackName;

    /** The color the computer plays, or {@code null} for a two-player game. */
    private final PieceColor computerColor;

    /** The computer's difficulty; ignored in a two-player game. */
    private final Difficulty difficulty;

    /**
     * Creates settings. Prefer the {@link #twoPlayer} and {@link #vsComputer} factories.
     *
     * @param whiteName     white player's name, or null/blank for the default
     * @param blackName     black player's name, or null/blank for the default
     * @param computerColor color the computer plays, or {@code null} for none
     * @param difficulty    computer difficulty; must not be null
     * @throws IllegalArgumentException if a name fails {@link #checkName(String)}
     */
    private GameSettings(String whiteName, String blackName, PieceColor computerColor, Difficulty difficulty) {
        requireValidName(whiteName);
        requireValidName(blackName);
        this.whiteName = normalize(whiteName);
        this.blackName = normalize(blackName);
        this.computerColor = computerColor;
        this.difficulty = Objects.requireNonNull(difficulty, "difficulty must not be null");
    }

    /**
     * Settings for two people sharing the mouse.
     *
     * @param whiteName white player's name, or null/blank for "White"
     * @param blackName black player's name, or null/blank for "Black"
     * @return the settings
     * @throws IllegalArgumentException if a name is invalid
     */
    public static GameSettings twoPlayer(String whiteName, String blackName) {
        return new GameSettings(whiteName, blackName, null, Difficulty.MEDIUM);
    }

    /**
     * Settings for one person against the computer.
     *
     * @param humanName     the person's name, or null/blank for the color's default
     * @param computerColor the color the computer plays; must not be null
     * @param difficulty    how strong the computer is; must not be null
     * @return the settings
     * @throws IllegalArgumentException if the name is invalid
     */
    public static GameSettings vsComputer(String humanName, PieceColor computerColor, Difficulty difficulty) {
        Objects.requireNonNull(computerColor, "computerColor must not be null");
        String computerName = "Computer";
        return computerColor == PieceColor.WHITE
                ? new GameSettings(computerName, humanName, computerColor, difficulty)
                : new GameSettings(humanName, computerName, computerColor, difficulty);
    }

    /**
     * Validates a player name typed by the user.
     *
     * @param raw the text from the name field (may be null or blank, meaning "use the default")
     * @return an error message to show the user, or {@code null} if the name is acceptable
     */
    public static String checkName(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String name = raw.trim();
        if (name.length() > MAX_NAME_LENGTH) {
            return "Names must be " + MAX_NAME_LENGTH + " characters or fewer (\"" + name + "\" has "
                    + name.length() + ").";
        }
        if (!NAME_CHARS.matcher(name).matches()) {
            return "Names may contain only letters, digits, spaces, and . _ ' - (\"" + name + "\").";
        }
        return null;
    }

    /**
     * Throws if a name fails {@link #checkName(String)}.
     *
     * @param raw the name to check
     * @throws IllegalArgumentException with the validation message
     */
    private static void requireValidName(String raw) {
        String problem = checkName(raw);
        if (problem != null) {
            throw new IllegalArgumentException(problem);
        }
    }

    /**
     * Trims a name, turning blank into {@code null} so {@link chess.model.Player} uses its default.
     *
     * @param raw the name
     * @return the trimmed name, or {@code null}
     */
    private static String normalize(String raw) {
        return (raw == null || raw.isBlank()) ? null : raw.trim();
    }

    /**
     * Returns the white player's name.
     *
     * @return the name, or {@code null} for the default
     */
    public String getWhiteName() {
        return whiteName;
    }

    /**
     * Returns the black player's name.
     *
     * @return the name, or {@code null} for the default
     */
    public String getBlackName() {
        return blackName;
    }

    /**
     * Returns the color the computer plays.
     *
     * @return the computer's color, or {@code null} in a two-player game
     */
    public PieceColor getComputerColor() {
        return computerColor;
    }

    /**
     * Returns the computer's difficulty.
     *
     * @return the difficulty (meaningful only when {@link #isVsComputer()} is true)
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Checks whether one side is played by the computer.
     *
     * @return {@code true} for a game against the computer
     */
    public boolean isVsComputer() {
        return computerColor != null;
    }
}
