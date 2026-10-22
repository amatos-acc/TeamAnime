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
        // TODO(11): This is the most involved piece. Suggested structure:
        //   int forward = getColor().getForwardDirection();   // -1 for White, +1 for Black
        //   int dr = rowDelta(target);
        //   int dc = colDelta(target);
        //
        //   a) Single step:  dc == 0 && dr == forward            -> valid only if board.isEmpty(target)
        //   b) Double step:  dc == 0 && dr == 2 * forward
        //                    && getPosition().getRow() == getColor().getPawnStartRow()
        //                    -> valid only if BOTH the middle square and target are empty
        //                       (middle square = getPosition().offset(forward, 0))
        //   c) Capture:      |dc| == 1 && dr == forward          -> valid only if target holds an ENEMY piece
        //                    (canMoveTo already rejects friendly pieces, but checking != null is still required:
        //                     a pawn may NOT move diagonally into an empty square)
        //   d) Anything else -> false (including any backward move)
        //
        //   Test cases: E2 E3, E2 E4 valid at start; E2 E5 invalid; E4 E3 (backward) invalid;
        //   E2 E4 when E3 is occupied invalid; E4 D5 valid only if D5 has an enemy.
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
