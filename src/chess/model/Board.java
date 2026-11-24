package chess.model;

import chess.pieces.King;
import chess.pieces.Pawn;
import chess.pieces.Piece;
import chess.pieces.PieceFactory;
import chess.pieces.PieceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The 8 x 8 grid of squares and the record of captured pieces.
 *
 * <p>The board stores pieces in a {@code Piece[8][8]} array indexed by
 * {@code [row][col]} using the mapping documented in {@link Position}.
 * An empty square holds {@code null}.</p>
 *
 * <p>The board knows <em>where</em> pieces are but not whose turn it is or
 * whether a move is legal; those decisions belong to
 * {@link chess.service.Game}. The board's invariant is that every non-null
 * {@code grid[r][c]} has {@code getPosition()} equal to {@code (r, c)}.</p>
 *
 * <p>Phase 2 adds what the full rules need from the board: the en passant
 * square, attack detection for check, {@link #applyMove(Move)} for the
 * special moves, and {@link #copy()} so a move can be tried on a copy
 * before it is played on the real board (Prototype pattern).</p>
 */
public class Board {

    /** Back-rank piece order from file A to file H, used for both colors. */
    private static final PieceType[] BACK_RANK = {
        PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
        PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
    };

    /** Squares of the board; {@code null} means empty. */
    private final Piece[][] grid;

    /** Pieces removed from the board by captures, in capture order. */
    private final List<Piece> capturedPieces;

    /**
     * The square a pawn skipped over on the previous move by advancing two squares,
     * or {@code null}. An enemy pawn may capture en passant by moving onto it, but
     * only on the very next move, so every move resets this.
     */
    private Position enPassantTarget;

    /**
     * Creates an empty board. Call {@link #initialize()} to set up the starting position.
     */
    public Board() {
        grid = new Piece[Position.BOARD_SIZE][Position.BOARD_SIZE];
        capturedPieces = new ArrayList<>();
    }

    /**
     * Clears the board and places all 32 pieces in the standard starting position.
     * White occupies ranks 1-2 and Black occupies ranks 7-8.
     */
    public void initialize() {
        clear();
        for (PieceColor color : PieceColor.values()) {
            int pawnRow = color.getPawnStartRow();
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                placePiece(new Pawn(color, new Position(pawnRow, col)));
            }
            placeBackRank(color, color.getBackRankRow());
        }
    }

    /**
     * Places the eight back-rank pieces for one color.
     * Order from file A to H: Rook, Knight, Bishop, Queen, King, Bishop, Knight, Rook.
     *
     * @param color the color to place
     * @param row   the array row of that color's back rank
     */
    private void placeBackRank(PieceColor color, int row) {
        for (int col = 0; col < Position.BOARD_SIZE; col++) {
            placePiece(PieceFactory.create(BACK_RANK[col], color, new Position(row, col)));
        }
    }

    /**
     * Removes every piece from the board and empties the captured list.
     */
    public void clear() {
        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                grid[row][col] = null;
            }
        }
        capturedPieces.clear();
        enPassantTarget = null;
    }

    /**
     * Returns a deep copy of this board: every piece is copied, so moves made on the
     * copy never affect this board. Used to test whether a move would leave a king in
     * check, and by the computer opponent to look ahead.
     *
     * @return an independent board in the same position
     */
    public Board copy() {
        Board clone = new Board();
        clone.restoreFrom(this);
        return clone;
    }

    /**
     * Replaces this board's contents with a copy of another board's position,
     * captured pieces, and en passant square.
     *
     * @param source the board to copy from; must not be null
     */
    public void restoreFrom(Board source) {
        if (source == this) {
            return;
        }
        clear();
        for (Piece[] rowSquares : source.grid) {
            for (Piece piece : rowSquares) {
                if (piece != null) {
                    placePiece(piece.copy());
                }
            }
        }
        capturedPieces.addAll(source.capturedPieces);
        enPassantTarget = source.enPassantTarget;
    }

    /**
     * Places a piece on the square recorded in its own position, replacing anything there.
     * Intended for setup and custom test positions, not for making moves.
     *
     * @param piece the piece to place; must not be null
     * @throws IllegalArgumentException if {@code piece} is null
     */
    public void placePiece(Piece piece) {
        if (piece == null) {
            throw new IllegalArgumentException("Cannot place a null piece.");
        }
        Position p = piece.getPosition();
        grid[p.getRow()][p.getCol()] = piece;
    }

    /**
     * Returns the piece on a square.
     *
     * @param position the square to inspect; must not be null
     * @return the piece there, or {@code null} if the square is empty
     */
    public Piece getPiece(Position position) {
        return grid[position.getRow()][position.getCol()];
    }

    /**
     * Checks whether a square is empty.
     *
     * @param position the square to inspect
     * @return {@code true} if no piece occupies the square
     */
    public boolean isEmpty(Position position) {
        return getPiece(position) == null;
    }

    /**
     * Checks whether every square strictly <em>between</em> two positions is empty.
     * The endpoints themselves are not examined.
     *
     * <p>Only meaningful for moves along a rank, file, or diagonal. Adjacent
     * squares have no squares between them, so the path is trivially clear.</p>
     *
     * @param from starting square
     * @param to   ending square
     * @return {@code true} if no piece sits between {@code from} and {@code to};
     *         {@code false} if a piece blocks the path or the squares are not on a common line
     */
    public boolean isPathClear(Position from, Position to) {
        int dRow = to.getRow() - from.getRow();
        int dCol = to.getCol() - from.getCol();
        
        int rowStep = Integer.signum(dRow);
        int colStep = Integer.signum(dCol);
        
        boolean straight = dRow == 0 || dCol == 0;
        boolean diagonal = Math.abs(dRow) == Math.abs(dCol);
        
        if (!straight && !diagonal) {
            return false;
        }
        
        Position current = from.offset(rowStep, colStep);
        while (!current.equals(to)) {
            if (!isEmpty(current)) {
                return false;
            }
            
            current = current.offset(rowStep, colStep);
        }
        
        return true;
    }

    /**
     * Moves a piece, assuming the move has <strong>already been validated</strong> by the caller.
     *
     * <p>Steps, in order: record any captured piece, clear the source square,
     * put the moving piece on the destination square, and update the moving
     * piece's own position so the board invariant still holds.</p>
     *
     * @param from square of the piece being moved; must be occupied
     * @param to   destination square
     * @return the captured piece, or {@code null} if the destination was empty
     * @throws IllegalStateException if {@code from} is empty
     */
    public Piece movePiece(Position from, Position to) {

        Piece mover = getPiece(from);
        if (mover == null) {
            throw new IllegalStateException("No piece on " + from + " to move.");
        }
        
        Piece captured = getPiece(to);
        if (captured != null) {
            capturedPieces.add(captured);
        }
        
        grid[from.getRow()][from.getCol()] = null;
        grid[to.getRow()][to.getCol()] = mover;
        
        mover.move(to);
        
        return captured;
    }

    /**
     * Works out what kind of move {@code move} would be on this board. Call this
     * before {@link #applyMove(Move)}, since applying the move changes the answer.
     *
     * @param move a move whose source square is occupied
     * @return castling, en passant, promotion, or normal
     * @throws IllegalStateException if the source square is empty
     */
    public MoveType classify(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece mover = getPiece(from);
        if (mover == null) {
            throw new IllegalStateException("No piece on " + from + " to move.");
        }
        if (mover instanceof King && ((King) mover).isCastlingShape(to)) {
            return to.getCol() > from.getCol() ? MoveType.CASTLE_KINGSIDE : MoveType.CASTLE_QUEENSIDE;
        }
        if (mover instanceof Pawn) {
            if (from.getCol() != to.getCol() && isEmpty(to) && to.equals(enPassantTarget)) {
                return MoveType.EN_PASSANT;
            }
            if (((Pawn) mover).isPromotionSquare(to)) {
                return MoveType.PROMOTION;
            }
        }
        return MoveType.NORMAL;
    }

    /**
     * Plays a move that has <strong>already been validated</strong>, including the extra
     * work special moves need: castling also moves the rook, en passant removes the pawn
     * beside the destination, and promotion replaces the pawn (with a queen if
     * {@link Move#getPromotion()} is null). Afterwards the en passant square is updated.
     *
     * @param move a legal move for the piece on its source square
     * @return the captured piece, or {@code null} if nothing was captured
     * @throws IllegalStateException if the source square is empty
     */
    public Piece applyMove(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        MoveType type = classify(move);
        Piece mover = getPiece(from);
        Piece captured;

        switch (type) {
            case CASTLE_KINGSIDE:
            case CASTLE_QUEENSIDE:
                captured = movePiece(from, to);
                int rookFromCol = type == MoveType.CASTLE_KINGSIDE ? King.KINGSIDE_ROOK_COL : King.QUEENSIDE_ROOK_COL;
                int rookToCol = (from.getCol() + to.getCol()) / 2;   // the square the king passed over
                movePiece(new Position(from.getRow(), rookFromCol), new Position(from.getRow(), rookToCol));
                break;
            case EN_PASSANT:
                Position victimSquare = new Position(from.getRow(), to.getCol());
                captured = getPiece(victimSquare);
                grid[victimSquare.getRow()][victimSquare.getCol()] = null;
                capturedPieces.add(captured);
                movePiece(from, to);
                break;
            case PROMOTION:
                captured = movePiece(from, to);
                PieceType choice = move.getPromotion() == null ? PieceType.QUEEN : move.getPromotion();
                Piece promoted = PieceFactory.create(choice, mover.getColor(), to);
                promoted.move(to);   // marks it as having moved
                placePiece(promoted);
                break;
            case NORMAL:
            default:
                captured = movePiece(from, to);
                break;
        }

        boolean doubleStep = mover instanceof Pawn && Math.abs(to.getRow() - from.getRow()) == 2;
        enPassantTarget = doubleStep ? from.offset(mover.getColor().getForwardDirection(), 0) : null;
        return captured;
    }

    /**
     * Returns the square an enemy pawn may capture onto en passant this move.
     *
     * @return the skipped square of a pawn that just advanced two squares, or {@code null}
     */
    public Position getEnPassantTarget() {
        return enPassantTarget;
    }

    /**
     * Finds the king of one color.
     *
     * @param color the king's color
     * @return the king's square, or {@code null} if that king is not on the board
     */
    public Position findKing(PieceColor color) {
        for (Piece[] rowSquares : grid) {
            for (Piece piece : rowSquares) {
                if (piece instanceof King && piece.getColor() == color) {
                    return piece.getPosition();
                }
            }
        }
        return null;
    }

    /**
     * Checks whether any piece of {@code attacker}'s color attacks {@code square}.
     * Relies on polymorphism: each piece answers {@link Piece#attacks} its own way.
     *
     * @param square   the square in question
     * @param attacker the color whose pieces might attack it
     * @return {@code true} if at least one such piece attacks the square
     */
    public boolean isSquareAttacked(Position square, PieceColor attacker) {
        for (Piece[] rowSquares : grid) {
            for (Piece piece : rowSquares) {
                if (piece != null && piece.getColor() == attacker && piece.attacks(square, this)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the king of one color is currently attacked.
     *
     * @param color the color whose king to examine
     * @return {@code true} if that king is in check; {@code false} if not, or if it is missing
     */
    public boolean isInCheck(PieceColor color) {
        Position king = findKing(color);
        return king != null && isSquareAttacked(king, color.opposite());
    }

    /**
     * Returns a read-only view of all captured pieces, in capture order.
     *
     * @return unmodifiable list of captured pieces
     */
    public List<Piece> getCapturedPieces() {
        return Collections.unmodifiableList(capturedPieces);
    }

    /**
     * Returns the captured pieces that belonged to one color.
     *
     * @param color the color whose lost pieces are wanted
     * @return new list of that color's captured pieces
     */
    public List<Piece> getCapturedPieces(PieceColor color) {
        List<Piece> result = new ArrayList<>();
        for (Piece piece : capturedPieces) {
            if (piece.getColor() == color) {
                result.add(piece);
            }
        }
        return result;
    }

    /**
     * Returns every piece of one color still on the board.
     *
     * @param color the color to collect
     * @return new list of that color's active pieces, scanned rank 8 to rank 1
     */
    public List<Piece> getActivePieces(PieceColor color) {
        List<Piece> result = new ArrayList<>();
        for (Piece[] rowSquares : grid) {
            for (Piece piece : rowSquares) {
                if (piece != null && piece.getColor() == color) {
                    result.add(piece);
                }
            }
        }
        return result;
    }
}
