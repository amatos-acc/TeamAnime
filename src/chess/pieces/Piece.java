package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for all six chess piece types.
 *
 * <p>Holds the state every piece shares (color, current position, whether it
 * has moved) and the movement helpers several subclasses reuse. Each subclass
 * supplies its own movement pattern by overriding
 * {@link #isValidPattern(Position, Board)}; callers work only with
 * {@code Piece} references and let polymorphism pick the right rule.</p>
 *
 * <p>{@link #canMoveTo(Position, Board)} and {@link #possibleMoves(Board)} are
 * <em>template methods</em>: the superclass fixes the sequence of checks and
 * subclasses fill in the piece-specific step.</p>
 *
 * <p>Simplified prototype: moves are not checked for king safety, so a move
 * that leaves one's own king attacked is still accepted.</p>
 */
public abstract class Piece {

    /** The side this piece belongs to; never changes. */
    private final PieceColor color;

    /** The square this piece currently occupies. */
    private Position position;

    /** {@code true} once this piece has made at least one move. */
    private boolean hasMoved;

    /**
     * Initializes the shared piece state.
     *
     * @param color    side the piece belongs to; must not be null
     * @param position starting square; must not be null
     * @throws NullPointerException if either argument is null
     */
    protected Piece(PieceColor color, Position position) {
        this.color = Objects.requireNonNull(color, "color must not be null");
        this.position = Objects.requireNonNull(position, "position must not be null");
        this.hasMoved = false;
    }

    // ------------------------------------------------------------------
    // Abstract behavior each subclass must provide
    // ------------------------------------------------------------------

    /**
     * Checks whether moving from this piece's current square to {@code target}
     * fits this piece type's movement pattern.
     *
     * <p>Implementations check only the <em>shape</em> of the move (and, for
     * pawns, whether the target is empty or holds an enemy). They do
     * <strong>not</strong> need to check for blocking pieces or friendly
     * destinations; {@link #canMoveTo(Position, Board)} handles those.</p>
     *
     * @param target destination square, never equal to the current position
     * @param board  the board, for pieces whose pattern depends on occupancy
     * @return {@code true} if the move matches this piece's pattern
     */
    public abstract boolean isValidPattern(Position target, Board board);

    /**
     * Returns the one-letter type code used on the board display.
     *
     * @return one of {@code p, R, N, B, Q, K}
     */
    public abstract char getSymbol();

    /**
     * Returns the piece type's name, e.g. {@code "Knight"}.
     *
     * @return the type name
     */
    public abstract String getName();

    // ------------------------------------------------------------------
    // Behavior subclasses may override
    // ------------------------------------------------------------------

    /**
     * Whether this piece needs every intervening square to be empty.
     * Most pieces do; the knight overrides this to jump.
     *
     * @return {@code true} by default
     */
    public boolean requiresClearPath() {
        return true;
    }

    // ------------------------------------------------------------------
    // Template methods shared by all pieces
    // ------------------------------------------------------------------

    /**
     * Checks every rule this prototype enforces for moving this piece to {@code target}:
     * target differs from the current square, target is not occupied by a friendly piece,
     * the move fits the piece's pattern, and (if required) the path is clear.
     *
     * @param target destination square
     * @param board  current board
     * @return {@code true} if the move is allowed
     */
    public boolean canMoveTo(Position target, Board board) {
        if (target.equals(position)) {
            return false;
        }
        Piece occupant = board.getPiece(target);
        if (occupant != null && occupant.getColor() == color) {
            return false;
        }
        if (!isValidPattern(target, board)) {
            return false;
        }
        return !requiresClearPath() || board.isPathClear(position, target);
    }

    /**
     * Lists every square this piece could move to under the prototype's rules.
     *
     * @param board current board
     * @return new list of candidate destinations (may be empty)
     */
    public List<Position> possibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();
        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                Position target = new Position(row, col);
                if (canMoveTo(target, board)) {
                    moves.add(target);
                }
            }
        }
        return moves;
    }

    /**
     * Updates this piece's position after the board has accepted a move.
     * Should only be called by {@link Board#movePiece(Position, Position)}.
     *
     * @param newPosition the square the piece now occupies; must not be null
     */
    public void move(Position newPosition) {
        this.position = Objects.requireNonNull(newPosition, "newPosition must not be null");
        this.hasMoved = true;
    }

    // ------------------------------------------------------------------
    // Geometry helpers reused by subclasses
    // ------------------------------------------------------------------

    /**
     * Returns the signed number of rows from the current square to {@code target}.
     *
     * @param target destination square
     * @return {@code target.row - current.row}
     */
    protected int rowDelta(Position target) {
        return target.getRow() - position.getRow();
    }

    /**
     * Returns the signed number of columns from the current square to {@code target}.
     *
     * @param target destination square
     * @return {@code target.col - current.col}
     */
    protected int colDelta(Position target) {
        return target.getCol() - position.getCol();
    }

    /**
     * Checks whether {@code target} is on the same rank or file as this piece.
     *
     * @param target destination square (assumed different from the current one)
     * @return {@code true} for a horizontal or vertical move
     */
    protected boolean isStraightLine(Position target) {
        return rowDelta(target) == 0 || colDelta(target) == 0;
    }

    /**
     * Checks whether {@code target} is on one of this piece's diagonals.
     *
     * @param target destination square (assumed different from the current one)
     * @return {@code true} for a diagonal move
     */
    protected boolean isDiagonal(Position target) {
        // TODO(5): A move is diagonal when the absolute row change equals the absolute column change.
        //   Use Math.abs(rowDelta(target)) and Math.abs(colDelta(target)).
        //   Look at isStraightLine() above for the style to follow.
        return false;
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------

    /**
     * Returns this piece's color.
     *
     * @return the piece's color
     */
    public PieceColor getColor() {
        return color;
    }

    /**
     * Returns the square this piece occupies.
     *
     * @return current position
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Returns whether this piece has moved since the game started.
     *
     * @return {@code true} after the first move
     */
    public boolean hasMoved() {
        return hasMoved;
    }

    /**
     * Returns the two-character board symbol, e.g. {@code "wK"} or {@code "bp"}.
     *
     * @return color prefix followed by type code
     */
    public String getDisplaySymbol() {
        return "" + color.getPrefix() + getSymbol();
    }

    /**
     * Returns a description such as {@code "White Knight at G1"}.
     *
     * @return color, type, and square
     */
    @Override
    public String toString() {
        return color.displayName() + " " + getName() + " at " + position;
    }
}
