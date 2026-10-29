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
 * <p>Simplified prototype: en passant is not supported. See
 * {@link chess.service.Game} for what happens on the last rank.</p>
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
            return !board.isEmpty(target) && getColor() != board.getPiece(target).getColor();
        }
        return false;
    }

    /**
     * Checks whether this pawn has reached the far rank (rank 8 for White, rank 1 for Black).
     *
     * @return {@code true} if the pawn stands on the opponent's back rank
     */
    public boolean isOnLastRank() {
        return getPosition().getRow() == getColor().opposite().getBackRankRow();
    }

    /**
     * Returns the pawn's board code.
     *
     * @return {@code 'p'} (lower case, as specified in the assignment)
     */
    @Override
    public char getSymbol() {
        return 'p';
    }

    /**
     * Returns the type name.
     *
     * @return {@code "Pawn"}
     */
    @Override
    public String getName() {
        return "Pawn";
    }
}
