package chess.ai;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Position;
import chess.pieces.Piece;
import chess.pieces.PieceType;

/**
 * Scores a position for the minimax search: positive is good for the side asked about.
 *
 * <p>The score is material (each piece's {@link PieceType#getValue()}) plus a
 * small positional bonus: pawns are worth more as they advance, and knights
 * and bishops are worth more near the center. Scores are in centipawns, so
 * 100 means "about one pawn better".</p>
 */
public class BoardEvaluator {

    /** Bonus per rank a pawn has advanced from its starting rank. */
    private static final int PAWN_ADVANCE_BONUS = 8;

    /** Bonus per step a knight or bishop is closer to the center than the edge. */
    private static final int CENTER_BONUS = 6;

    /**
     * Creates an evaluator. The evaluator has no state.
     */
    public BoardEvaluator() {
    }

    /**
     * Scores the position from {@code side}'s point of view.
     *
     * @param board the position to score
     * @param side  the color the score is for
     * @return {@code side}'s total minus the opponent's total, in centipawns
     */
    public int evaluate(Board board, PieceColor side) {
        return total(board, side) - total(board, side.opposite());
    }

    /**
     * Adds up material and positional bonuses for one color.
     *
     * @param board the position
     * @param color the color to total
     * @return that color's score
     */
    private int total(Board board, PieceColor color) {
        int score = 0;
        for (Piece piece : board.getActivePieces(color)) {
            score += piece.getType().getValue() + positionalBonus(piece);
        }
        return score;
    }

    /**
     * Returns the positional bonus for one piece.
     *
     * @param piece a piece on the board
     * @return a small bonus in centipawns (zero for rooks, queens, and kings)
     */
    private int positionalBonus(Piece piece) {
        Position at = piece.getPosition();
        switch (piece.getType()) {
            case PAWN:
                int ranksAdvanced = Math.abs(at.getRow() - piece.getColor().getPawnStartRow());
                return ranksAdvanced * PAWN_ADVANCE_BONUS;
            case KNIGHT:
            case BISHOP:
                // 0 on the edge, 3 on the four center squares.
                int rowCentrality = Math.min(at.getRow(), Position.BOARD_SIZE - 1 - at.getRow());
                int colCentrality = Math.min(at.getCol(), Position.BOARD_SIZE - 1 - at.getCol());
                return Math.min(rowCentrality, colCentrality) * CENTER_BONUS;
            default:
                return 0;
        }
    }
}
