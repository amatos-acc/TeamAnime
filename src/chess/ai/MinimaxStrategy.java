package chess.ai;

import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;
import chess.pieces.Piece;
import chess.service.MoveValidator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * A computer opponent that looks a fixed number of moves ahead using
 * minimax search with alpha-beta pruning.
 *
 * <p><b>Minimax</b>: to score one of my moves, assume the opponent replies
 * with whichever move is worst for me, and so on down to {@code depth}
 * half-moves; then score the leaf positions with {@link BoardEvaluator}.
 * This is written in the compact <em>negamax</em> form: a position's score
 * for one side is the negative of its score for the other side, so one
 * method handles both players.</p>
 *
 * <p><b>Alpha-beta pruning</b> skips replies that cannot change the
 * decision: once a reply is found that already makes a move worse than a
 * move examined earlier, the rest of that move's replies are skipped. Trying
 * captures first makes those cut-offs happen sooner.</p>
 *
 * <p>Checkmate is scored as a huge value minus the number of half-moves needed,
 * so the search prefers the fastest mate and the slowest loss.</p>
 */
public class MinimaxStrategy implements MoveStrategy {

    /** Score for delivering checkmate (before subtracting the distance to it). */
    static final int MATE_SCORE = 1_000_000;

    /** How many half-moves to look ahead (at least 1). */
    private final int depth;

    /** Lists legal moves and detects checkmate and stalemate. */
    private final MoveValidator validator = new MoveValidator();

    /** Scores positions at the end of the search. */
    private final BoardEvaluator evaluator = new BoardEvaluator();

    /**
     * Creates a minimax opponent.
     *
     * @param depth half-moves to look ahead; 2 is quick, 3 is noticeably stronger
     * @throws IllegalArgumentException if {@code depth} is less than 1
     */
    public MinimaxStrategy(int depth) {
        if (depth < 1) {
            throw new IllegalArgumentException("Search depth must be at least 1, got " + depth + ".");
        }
        this.depth = depth;
    }

    /**
     * Searches every legal move and returns the one with the best minimax score.
     * Ties keep the first move found, so the result is deterministic.
     *
     * @param board a copy of the current position (not modified)
     * @param side  the color to move
     * @return the best move found, or {@code null} if there is no legal move
     */
    @Override
    public Move chooseMove(Board board, PieceColor side) {
        List<Move> moves = orderedMoves(board, side);
        Move best = null;
        int alpha = -Integer.MAX_VALUE;
        for (Move move : moves) {
            Board child = board.copy();
            child.applyMove(move);
            int score = -negamax(child, side.opposite(), depth - 1, -Integer.MAX_VALUE, -alpha, 1);
            if (best == null || score > alpha) {
                best = move;
                alpha = score;
            }
        }
        return best;
    }

    /**
     * Scores a position for {@code side} by searching {@code remaining} more half-moves.
     *
     * @param board     the position (a private copy that may be modified)
     * @param side      the color to move in this position
     * @param remaining half-moves left to search
     * @param alpha     the best score {@code side} is already guaranteed elsewhere
     * @param beta      the score above which the opponent will avoid this position
     * @param ply       half-moves from the root, used to prefer faster mates
     * @return the position's score for {@code side}
     */
    private int negamax(Board board, PieceColor side, int remaining, int alpha, int beta, int ply) {
        List<Move> moves = orderedMoves(board, side);
        if (moves.isEmpty()) {
            return board.isInCheck(side) ? -(MATE_SCORE - ply) : 0;   // checkmated, or stalemate
        }
        if (remaining == 0) {
            return evaluator.evaluate(board, side);
        }
        for (Move move : moves) {
            Board child = board.copy();
            child.applyMove(move);
            int score = -negamax(child, side.opposite(), remaining - 1, -beta, -alpha, ply + 1);
            if (score >= beta) {
                return score;   // the opponent will not allow this line; stop searching it
            }
            alpha = Math.max(alpha, score);
        }
        return alpha;
    }

    /**
     * Lists the legal moves with captures of the most valuable pieces first, which helps
     * alpha-beta pruning cut off bad lines sooner.
     *
     * @param board the position
     * @param side  the color to move
     * @return the legal moves, best-guess first
     */
    private List<Move> orderedMoves(Board board, PieceColor side) {
        List<Move> moves = new ArrayList<>(validator.legalMoves(board, side));
        moves.sort(Comparator.comparingInt((Move m) -> -captureValue(board, m)));
        return moves;
    }

    /**
     * Returns the value of the piece a move captures.
     *
     * @param board the position
     * @param move  the move
     * @return the captured piece's value, or 0 for a quiet move
     */
    private static int captureValue(Board board, Move move) {
        Piece victim = board.getPiece(move.getTo());
        return victim == null ? 0 : victim.getType().getValue();
    }

    /**
     * Returns the strategy's name.
     *
     * @return for example {@code "Minimax (depth 2)"}
     */
    @Override
    public String getName() {
        return "Minimax (depth " + depth + ")";
    }
}
