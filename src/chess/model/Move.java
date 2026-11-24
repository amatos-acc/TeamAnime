package chess.model;

import chess.pieces.PieceType;

import java.util.Objects;

/**
 * A requested move: a source square, a destination square, and (for a pawn
 * reaching the last rank) the piece type to promote to.
 *
 * <p>Instances are immutable, like {@link Position}, so the same object can
 * be passed between the GUI, the game, and the computer opponent without
 * anyone changing it. A {@code Move} says nothing about whether it is legal;
 * {@link chess.service.MoveValidator} decides that.</p>
 */
public final class Move {

    /** Square the piece moves from. */
    private final Position from;

    /** Square the piece moves to. */
    private final Position to;

    /** Promotion choice, or {@code null} for a normal move (a queen is assumed if needed). */
    private final PieceType promotion;

    /**
     * Creates a move with no promotion choice.
     *
     * @param from source square; must not be null
     * @param to   destination square; must not be null
     */
    public Move(Position from, Position to) {
        this(from, to, null);
    }

    /**
     * Creates a move with an optional promotion choice.
     *
     * @param from      source square; must not be null
     * @param to        destination square; must not be null
     * @param promotion queen, rook, bishop, or knight; or {@code null} for none
     * @throws NullPointerException     if {@code from} or {@code to} is null
     * @throws IllegalArgumentException if {@code promotion} is a pawn or king
     */
    public Move(Position from, Position to, PieceType promotion) {
        this.from = Objects.requireNonNull(from, "from must not be null");
        this.to = Objects.requireNonNull(to, "to must not be null");
        if (promotion != null && !promotion.isPromotionChoice()) {
            throw new IllegalArgumentException("A pawn cannot be promoted to a " + promotion.getDisplayName() + ".");
        }
        this.promotion = promotion;
    }

    /**
     * Returns the source square.
     *
     * @return where the piece starts
     */
    public Position getFrom() {
        return from;
    }

    /**
     * Returns the destination square.
     *
     * @return where the piece ends up
     */
    public Position getTo() {
        return to;
    }

    /**
     * Returns the promotion choice.
     *
     * @return the chosen piece type, or {@code null} if none was given
     */
    public PieceType getPromotion() {
        return promotion;
    }

    /**
     * Returns a copy of this move with a different promotion choice.
     *
     * @param newPromotion the promotion choice for the copy
     * @return a new move with the same squares
     */
    public Move withPromotion(PieceType newPromotion) {
        return new Move(from, to, newPromotion);
    }

    /**
     * Two moves are equal when they have the same squares and promotion choice.
     *
     * @param other the object to compare with
     * @return {@code true} if {@code other} is an equal move
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Move)) {
            return false;
        }
        Move that = (Move) other;
        return from.equals(that.from) && to.equals(that.to) && promotion == that.promotion;
    }

    /**
     * Returns a hash code consistent with {@link #equals(Object)}.
     *
     * @return hash of the squares and promotion choice
     */
    @Override
    public int hashCode() {
        return Objects.hash(from, to, promotion);
    }

    /**
     * Returns the move in the same form the console accepts, e.g. {@code "E2 E4"} or {@code "E7 E8 Q"}.
     *
     * @return text form of the move
     */
    @Override
    public String toString() {
        return from + " " + to + (promotion == null ? "" : " " + promotion.getSymbol());
    }
}
