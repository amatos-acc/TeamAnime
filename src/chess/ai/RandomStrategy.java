package chess.ai;

import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;
import chess.service.MoveValidator;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * The easiest opponent: plays a uniformly random legal move.
 *
 * <p>Also used as the safety net when another strategy fails (see
 * {@code chess.gui.GameController}), because it can only fail when there is
 * no legal move at all.</p>
 */
public class RandomStrategy implements MoveStrategy {

    /** Source of randomness; injectable so tests can use a fixed seed. */
    private final Random random;

    /** Used to list the legal moves. */
    private final MoveValidator validator = new MoveValidator();

    /**
     * Creates a random strategy with an unpredictable seed.
     */
    public RandomStrategy() {
        this(new Random());
    }

    /**
     * Creates a random strategy using the given random-number generator.
     *
     * @param random the generator to use; must not be null
     */
    public RandomStrategy(Random random) {
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    /**
     * Picks one of {@code side}'s legal moves at random.
     *
     * @param board a copy of the current position
     * @param side  the color to move
     * @return a random legal move, or {@code null} if there is none
     */
    @Override
    public Move chooseMove(Board board, PieceColor side) {
        List<Move> moves = validator.legalMoves(board, side);
        if (moves.isEmpty()) {
            return null;
        }
        return moves.get(random.nextInt(moves.size()));
    }

    /**
     * Returns the strategy's name.
     *
     * @return {@code "Random"}
     */
    @Override
    public String getName() {
        return "Random";
    }
}
