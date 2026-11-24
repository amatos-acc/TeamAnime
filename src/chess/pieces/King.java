package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

/**
 * A king: moves exactly one square in any direction, or castles.
 *
 * <p>Castling is written as the king moving two squares toward a rook
 * (E1 to G1 or E1 to C1 for White). This class checks the parts of castling
 * that depend only on the pieces involved: neither the king nor that rook has
 * moved, and every square between them is empty. The rules that depend on
 * attacks (not castling out of, through, or into check) need the whole
 * position, so {@link chess.service.MoveValidator} checks them.</p>
 */
public class King extends Piece {

    /** Column a king must stand on to castle (file E). */
    public static final int START_COL = 4;

    /** Column of the rook a king castles with on the kingside (file H). */
    public static final int KINGSIDE_ROOK_COL = 7;

    /** Column of the rook a king castles with on the queenside (file A). */
    public static final int QUEENSIDE_ROOK_COL = 0;

    /**
     * Creates a king.
     *
     * @param color    side the king belongs to
     * @param position starting square
     */
    public King(PieceColor color, Position position) {
        super(color, position);
    }

    /**
     * A king's move is valid when it changes each coordinate by at most one,
     * or when it is a castling move whose piece-level conditions hold.
     *
     * @param target destination square (different from the current one)
     * @param board  the board, used to check the castling rook and the squares in between
     * @return {@code true} for one-square moves in any direction or an allowed castle
     */
    @Override
    public boolean isValidPattern(Position target, Board board) {
        if (isOneSquareMove(target)) {
            return true;
        }
        return isCastlingShape(target) && canCastleToward(target, board);
    }

    /**
     * A king attacks only the eight squares around it. Castling is never a
     * capture, so it is deliberately left out here.
     *
     * @param target the square in question
     * @param board  unused
     * @return {@code true} if {@code target} is adjacent to the king
     */
    @Override
    public boolean attacks(Position target, Board board) {
        return !target.equals(getPosition()) && isOneSquareMove(target);
    }

    /**
     * Checks whether a move has the shape of castling: two squares sideways along the rank.
     *
     * @param target destination square
     * @return {@code true} if the king would move exactly two files and no ranks
     */
    public boolean isCastlingShape(Position target) {
        return rowDelta(target) == 0 && Math.abs(colDelta(target)) == 2;
    }

    /**
     * Checks whether {@code target} is at most one square away in each direction.
     *
     * @param target destination square
     * @return {@code true} for a one-square king step
     */
    private boolean isOneSquareMove(Position target) {
        return Math.abs(rowDelta(target)) <= 1 && Math.abs(colDelta(target)) <= 1;
    }

    /**
     * Checks the piece-level castling conditions for castling toward {@code target}:
     * the king has not moved and stands on its back rank, the matching corner holds an
     * unmoved rook of the same color, and every square between king and rook is empty.
     *
     * @param target the king's destination (two files left or right)
     * @param board  current board
     * @return {@code true} if those conditions hold
     */
    private boolean canCastleToward(Position target, Board board) {
        Position from = getPosition();
        if (hasMoved() || from.getRow() != getColor().getBackRankRow() || from.getCol() != START_COL) {
            return false;
        }
        int direction = Integer.signum(colDelta(target));
        int rookCol = direction > 0 ? KINGSIDE_ROOK_COL : QUEENSIDE_ROOK_COL;
        Piece rook = board.getPiece(new Position(from.getRow(), rookCol));
        if (!(rook instanceof Rook) || rook.getColor() != getColor() || rook.hasMoved()) {
            return false;
        }
        for (int col = from.getCol() + direction; col != rookCol; col += direction) {
            if (!board.isEmpty(new Position(from.getRow(), col))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Identifies this piece as a king; the symbol and name come from {@link PieceType#KING}.
     *
     * @return {@link PieceType#KING}
     */
    @Override
    public PieceType getType() {
        return PieceType.KING;
    }
}
