package chess.ai;

import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;

/**
 * A way of choosing a move for the computer opponent (Strategy pattern).
 *
 * <p>The GUI's controller holds a {@code MoveStrategy} reference and calls
 * {@link #chooseMove}; it does not know or care whether the strategy picks a
 * random move or searches ahead. Adding a stronger opponent means writing a
 * new class that implements this interface, with no change to the GUI or the
 * game rules (open/closed principle).</p>
 *
 * <p>Implementations must not change the board they are given. The caller
 * passes a {@linkplain Board#copy() copy}, so a strategy can safely run on a
 * background thread while the real game stays on the Swing thread.</p>
 */
public interface MoveStrategy {

    /**
     * Chooses a move for {@code side}.
     *
     * @param board a copy of the current position
     * @param side  the color the computer is playing
     * @return a legal move, or {@code null} if {@code side} has no legal move
     */
    Move chooseMove(Board board, PieceColor side);

    /**
     * Returns a short name for menus and logs.
     *
     * @return for example {@code "Minimax (depth 2)"}
     */
    String getName();
}
