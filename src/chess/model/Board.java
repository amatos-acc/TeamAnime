package chess.model;

import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Piece;
import chess.pieces.Queen;
import chess.pieces.Rook;

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
 */
public class Board {

    /** Squares of the board; {@code null} means empty. */
    private final Piece[][] grid;

    /** Pieces removed from the board by captures, in capture order. */
    private final List<Piece> capturedPieces;

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
        // Place the eight back-rank pieces on `row` using placePiece(...).
        //   Columns 0-7 should get: Rook, Knight, Bishop, Queen, King, Bishop, Knight, Rook.
        //   Example for column 0:  placePiece(new Rook(color, new Position(row, 0)));
        //   Tip: the queen goes on column 3 (file D) and the king on column 4 (file E) for BOTH colors.
        //   (The imports for every piece type are already at the top of this file.)
        placePiece(new Rook(color, new Position(row, 0)));
        placePiece(new Knight(color, new Position(row, 1)));
        placePiece(new Bishop(color, new Position(row, 2)));
        placePiece(new Queen(color, new Position(row, 3)));
        placePiece(new King(color, new Position(row, 4)));
        placePiece(new Bishop(color, new Position(row, 5)));
        placePiece(new Knight(color, new Position(row, 6)));
        placePiece(new Rook(color, new Position(row, 7)));
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
