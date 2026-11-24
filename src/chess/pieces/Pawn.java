package chess.pieces;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;

/**
 * A pawn: moves forward one square into an empty square, or two squares from
 * its starting rank if both squares are empty, and captures one square
 * diagonally forward. White advances toward rank 8; Black toward rank 1.
 *
 * <p>The pawn is the only piece whose pattern depends on what occupies the
 * destination, which is why {@link Piece#isValidPattern} receives the board.</p>
 *
 * <p>En passant: right after an enemy pawn advances two squares and lands
 * beside this pawn, this pawn may capture it by moving diagonally onto the
 * square it skipped ({@link Board#getEnPassantTarget()}). A pawn reaching the
 * last rank is promoted by {@link Board#applyMove(chess.model.Move)}.</p>
 */
public class Pawn extends Piece {

    /**
     * Creates a pawn.
     *
     * @param color    side the pawn belongs to
     * @param position starting square
     */
    public Pawn(PieceColor color, Position position) {
        super(color, position);
    }

    /**
     * Checks the pawn's forward-move and diagonal-capture patterns.
     *
     * @param target destination square
     * @param board  the board, used to check whether squares are empty or hold an enemy
     * @return {@code true} if the move is a legal pawn advance or capture
     */
    @Override
    public boolean isValidPattern(Position target, Board board) {
        int forward = getColor().getForwardDirection();  
        int dr = rowDelta(target);
        int dc = colDelta(target);
        
        if (dc == 0 && dr == forward) {
            return board.isEmpty(target);
        } 
        
        if (dc == 0 && dr == (2 * forward) && getPosition().getRow() == getColor().getPawnStartRow()) {
            Position middleSquare = getPosition().offset(forward, 0);
            return board.isEmpty(target) && board.isEmpty(middleSquare);
        }
        
        if (Math.abs(dc) == 1 && dr == forward) {
            if (board.isEmpty(target)) {
                // En passant: the only diagonal pawn move onto an empty square. The board
                // remembers the square an enemy pawn skipped over on the previous move.
                return target.equals(board.getEnPassantTarget());
            }
            return getColor() != board.getPiece(target).getColor();
        }
        return false;
    }

    /**
     * A pawn attacks the two squares diagonally in front of it, whether or not
     * anything stands there. (It moves straight but captures diagonally, so the
     * inherited {@link Piece#attacks} would be wrong.)
     *
     * @param target the square in question
     * @param board  unused; pawn attacks do not depend on occupancy
     * @return {@code true} if {@code target} is one square diagonally forward
     */
    @Override
    public boolean attacks(Position target, Board board) {
        return Math.abs(colDelta(target)) == 1 && rowDelta(target) == getColor().getForwardDirection();
    }

    /**
     * Checks whether this pawn has reached the far rank (rank 8 for White, rank 1 for Black).
     *
     * @return {@code true} if the pawn stands on the opponent's back rank
     */
    public boolean isOnLastRank() {
        return isPromotionSquare(getPosition());
    }

    /**
     * Checks whether a pawn of this color arriving on {@code square} must be promoted.
     *
     * @param square a destination square
     * @return {@code true} if {@code square} is on the opponent's back rank
     */
    public boolean isPromotionSquare(Position square) {
        return square.getRow() == getColor().opposite().getBackRankRow();
    }

    /**
     * Identifies this piece as a pawn; the symbol and name come from {@link PieceType#PAWN}.
     *
     * @return {@link PieceType#PAWN}
     */
    @Override
    public PieceType getType() {
        return PieceType.PAWN;
    }
}
