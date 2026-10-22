package chess.model;

import java.util.Objects;

/**
 * One of the two people playing the game.
 *
 * <p>A player is identified by a {@link PieceColor} and an optional display
 * name. The player's active pieces are not stored here; they are looked up
 * from the {@link Board} so there is a single source of truth.</p>
 */
public class Player {

    /** The color this player controls. */
    private final PieceColor color;

    /** Display name shown in prompts; never null or blank. */
    private final String name;

    /**
     * Creates a player.
     *
     * @param color the side this player controls; must not be null
     * @param name  display name; if null or blank, the color name is used instead
     * @throws NullPointerException if {@code color} is null
     */
    public Player(PieceColor color, String name) {
        this.color = Objects.requireNonNull(color, "color must not be null");
        this.name = (name == null || name.isBlank()) ? color.displayName() : name.trim();
    }

    /**
     * Returns the color this player controls.
     *
     * @return the player's color
     */
    public PieceColor getColor() {
        return color;
    }

    /**
     * Returns the player's display name.
     *
     * @return the name, never blank
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a label such as {@code "Alice (White)"}.
     *
     * @return name followed by color
     */
    @Override
    public String toString() {
        if (name.equals(color.displayName())) {
            return name;
        }
        return name + " (" + color.displayName() + ")";
    }
}
