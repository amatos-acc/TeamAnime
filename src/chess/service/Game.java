package chess.service;

import chess.model.Board;
import chess.model.Move;
import chess.model.MoveType;
import chess.model.PieceColor;
import chess.model.Player;
import chess.model.Position;
import chess.pieces.Pawn;
import chess.pieces.Piece;
import chess.pieces.PieceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Coordinates a two-player game: owns the board, the players, and the turn,
 * decides whether a requested move is allowed, and tracks check, checkmate,
 * and stalemate.
 *
 * <p>This class contains no console or Swing code. The console version, the
 * Swing GUI, the computer opponent, and the unit tests all call the same
 * public methods. Interfaces that need to react to changes register a
 * {@link GameListener} (Observer pattern).</p>
 *
 * <h2>Rules</h2>
 * <ul>
 *   <li>All standard piece moves, including castling, en passant, and promotion
 *       to a queen, rook, bishop, or knight.</li>
 *   <li>A move that leaves the mover's own king in check is rejected.</li>
 *   <li>The game ends in checkmate (the side that delivered it wins) or
 *       stalemate (a draw). After that, every move is rejected until
 *       {@link #start()} is called.</li>
 *   <li>Not implemented: draws by threefold repetition, the 50-move rule,
 *       insufficient material, or agreement.</li>
 * </ul>
 */
public class Game {

    /** Logger for game events; output goes wherever {@link chess.util.LogConfig} sends it. */
    private static final Logger LOG = Logger.getLogger(Game.class.getName());

    /** The board this game is played on. */
    private final Board board;

    /** The player controlling the white pieces. */
    private final Player whitePlayer;

    /** The player controlling the black pieces. */
    private final Player blackPlayer;

    /** Accepted moves in order, e.g. {@code "1. White: E2 E4"}. */
    private final List<String> moveHistory;

    /** Objects notified after the game starts or a move is accepted. */
    private final List<GameListener> listeners;

    /** Shared rule checker; also used for legal-move hints and checkmate detection. */
    private final MoveValidator validator;

    /** The player whose turn it is. */
    private Player currentPlayer;

    /** The current status from the point of view of {@link #currentPlayer}. */
    private GameStatus status;

    /** The winner after checkmate, or {@code null}. */
    private Player winner;

    /** The most recent accepted move, or {@code null} at the start. */
    private Move lastMove;

    /**
     * Creates a game with default player names ("White" and "Black") in the starting position.
     */
    public Game() {
        this(null, null);
    }

    /**
     * Creates a game with the given player names in the starting position.
     *
     * @param whiteName name for the white player, or null/blank for the default
     * @param blackName name for the black player, or null/blank for the default
     */
    public Game(String whiteName, String blackName) {
        this.board = new Board();
        this.whitePlayer = new Player(PieceColor.WHITE, whiteName);
        this.blackPlayer = new Player(PieceColor.BLACK, blackName);
        this.moveHistory = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.validator = new MoveValidator();
        resetState();
    }

    /**
     * Starts (or restarts) the game: sets up the board, clears history, gives White the first
     * move, and notifies listeners. Declared {@code final} so a subclass cannot change what a
     * restart means.
     */
    public final void start() {
        resetState();
        fireGameStarted();
    }

    /**
     * Puts every field back to the starting position without notifying anyone. The constructor
     * calls this instead of {@link #start()}: notifying listeners from a constructor would hand
     * out {@code this} before the object is fully built (javac's {@code this-escape} warning),
     * and no listener could be registered yet anyway.
     */
    private void resetState() {
        board.initialize();
        moveHistory.clear();
        currentPlayer = whitePlayer;
        status = GameStatus.IN_PROGRESS;
        winner = null;
        lastMove = null;
        LOG.info("New game: " + whitePlayer + " vs " + blackPlayer);
    }

    /**
     * Replaces the position with a custom one, for tests and demonstrations
     * (for example, a stalemate position that would take many moves to reach).
     * The move history is cleared and the status is recomputed for {@code toMove}.
     *
     * @param setup  the position to copy; the game keeps its own copy
     * @param toMove the color whose turn it is
     */
    public void loadPosition(Board setup, PieceColor toMove) {
        Objects.requireNonNull(setup, "setup must not be null");
        board.restoreFrom(setup);
        moveHistory.clear();
        currentPlayer = getPlayer(toMove);
        lastMove = null;
        status = computeStatus();
        winner = status == GameStatus.CHECKMATE ? getPlayer(toMove.opposite()) : null;
        LOG.info("Loaded custom position, " + toMove.displayName() + " to move, status " + status);
        fireGameStarted();
    }

    /**
     * Attempts to move the current player's piece from one square to another.
     * A pawn reaching the last rank becomes a queen; use {@link #tryMove(Move)} to choose.
     *
     * @param from source square; must not be null
     * @param to   destination square; must not be null
     * @return a {@link MoveResult} describing success or the reason for failure
     */
    public MoveResult tryMove(Position from, Position to) {
        return tryMove(new Move(from, to));
    }

    /**
     * Attempts a move for the current player.
     *
     * <p>If the game is over or {@link MoveValidator#findProblem} finds a problem, the
     * board, turn, and history are left exactly as they were and a failure is returned.
     * Otherwise the move is applied, the turn passes, the status is recomputed (check,
     * checkmate, stalemate), and every {@link GameListener} is notified.</p>
     *
     * @param move the move to play; must not be null
     * @return a {@link MoveResult} describing success or the reason for failure
     */
    public MoveResult tryMove(Move move) {
        Objects.requireNonNull(move, "move must not be null");
        PieceColor side = currentPlayer.getColor();

        if (status.isGameOver()) {
            return MoveResult.failure("The game is over (" + describeOutcome() + "). Start a new game to play again.");
        }
        String problem = validator.findProblem(board, side, move);
        if (problem != null) {
            LOG.fine("Rejected " + side.displayName() + " move " + move + ": " + problem);
            return MoveResult.failure(problem);
        }

        Piece mover = board.getPiece(move.getFrom());
        MoveType type = board.classify(move);
        Move played = (type == MoveType.PROMOTION && move.getPromotion() == null)
                ? move.withPromotion(PieceType.QUEEN) : move;
        Piece captured = board.applyMove(played);
        lastMove = played;

        switchTurn();
        status = computeStatus();
        if (status == GameStatus.CHECKMATE) {
            winner = getPlayer(side);
        }

        String description = describeMove(mover, played, type, captured) + statusSentence();
        moveHistory.add(historyEntry(side, played, type, captured));
        LOG.info(side.displayName() + " played " + played + " -> " + status);

        MoveResult result = MoveResult.success(description, played, type, captured, status);
        fireMoveMade(result);
        return result;
    }

    /**
     * Works out the status for the player who is about to move.
     *
     * @return checkmate or stalemate if that player has no legal move, otherwise check or in-progress
     */
    private GameStatus computeStatus() {
        PieceColor side = currentPlayer.getColor();
        boolean inCheck = board.isInCheck(side);
        if (!validator.hasAnyLegalMove(board, side)) {
            return inCheck ? GameStatus.CHECKMATE : GameStatus.STALEMATE;
        }
        return inCheck ? GameStatus.CHECK : GameStatus.IN_PROGRESS;
    }

    /**
     * Builds the sentence describing an accepted move, e.g.
     * {@code "Pawn E5 -> D6, capturing Black Pawn en passant"}.
     *
     * @param mover    the piece that moved (before any promotion)
     * @param move     the move played
     * @param type     what kind of move it was
     * @param captured the captured piece, or {@code null}
     * @return the description, without a final period
     */
    private static String describeMove(Piece mover, Move move, MoveType type, Piece captured) {
        StringBuilder text = new StringBuilder(mover.getName() + " " + move.getFrom() + " -> " + move.getTo());
        if (type == MoveType.CASTLE_KINGSIDE) {
            text.append(" (castles kingside)");
        } else if (type == MoveType.CASTLE_QUEENSIDE) {
            text.append(" (castles queenside)");
        }
        if (captured != null) {
            text.append(", capturing ").append(captured.getColor().displayName()).append(' ').append(captured.getName());
            if (type == MoveType.EN_PASSANT) {
                text.append(" en passant");
            }
        }
        if (type == MoveType.PROMOTION) {
            text.append(". Pawn promoted to ").append(move.getPromotion().getDisplayName());
        }
        return text.toString();
    }

    /**
     * Returns the sentence about the new status to append to a move description.
     *
     * @return for example {@code ". Black is in check!"}
     */
    private String statusSentence() {
        switch (status) {
            case CHECK:
                return ". " + currentPlayer.getColor().displayName() + " is in check!";
            case CHECKMATE:
            case STALEMATE:
                return ". " + describeOutcome() + ".";
            case IN_PROGRESS:
            default:
                return ".";
        }
    }

    /**
     * Builds a numbered history line such as {@code "5. White: E7 E8=Q (xbR)+"}.
     *
     * @param side     the color that moved
     * @param move     the move played
     * @param type     what kind of move it was
     * @param captured the captured piece, or {@code null}
     * @return the history entry
     */
    private String historyEntry(PieceColor side, Move move, MoveType type, Piece captured) {
        StringBuilder entry = new StringBuilder();
        entry.append(moveHistory.size() + 1).append(". ").append(side.displayName()).append(": ")
                .append(move.getFrom()).append(' ').append(move.getTo());
        if (type == MoveType.PROMOTION) {
            entry.append('=').append(move.getPromotion().getSymbol());
        }
        if (type == MoveType.CASTLE_KINGSIDE) {
            entry.append(" (O-O)");
        } else if (type == MoveType.CASTLE_QUEENSIDE) {
            entry.append(" (O-O-O)");
        }
        if (captured != null) {
            entry.append(" (x").append(captured.getDisplaySymbol()).append(type == MoveType.EN_PASSANT ? " e.p." : "")
                    .append(')');
        }
        if (status == GameStatus.CHECKMATE) {
            entry.append('#');
        } else if (status == GameStatus.CHECK) {
            entry.append('+');
        }
        return entry.toString();
    }

    /**
     * Describes how the game ended.
     *
     * @return for example {@code "Checkmate. Black wins"} or {@code "Stalemate. The game is a draw"}
     */
    public String describeOutcome() {
        switch (status) {
            case CHECKMATE:
                return "Checkmate. " + winner + " wins";
            case STALEMATE:
                return "Stalemate. The game is a draw";
            default:
                return "The game is still in progress";
        }
    }

    /**
     * Gives the turn to the other player.
     */
    private void switchTurn() {
        currentPlayer = (currentPlayer == whitePlayer) ? blackPlayer : whitePlayer;
    }

    /**
     * Lists the squares the piece on {@code from} may legally move to, for move hints
     * and for highlighting targets in the GUI. Works for either color's pieces.
     *
     * @param from the square to inspect
     * @return legal destinations, scanned from rank 8 to rank 1; empty if the square is empty
     */
    public List<Position> getLegalMoves(Position from) {
        List<Position> targets = new ArrayList<>();
        for (Move move : validator.legalMovesFrom(board, from)) {
            targets.add(move.getTo());
        }
        return targets;
    }

    /**
     * Lists every legal move for the player whose turn it is.
     *
     * @return new list of moves; empty when the game is over
     */
    public List<Move> getAllLegalMoves() {
        if (status.isGameOver()) {
            return new ArrayList<>();
        }
        return validator.legalMoves(board, currentPlayer.getColor());
    }

    /**
     * Checks whether moving from {@code from} to {@code to} is a legal pawn promotion for the
     * current player, so a user interface knows to ask which piece to promote to
     * <em>before</em> calling {@link #tryMove(Move)}.
     *
     * @param from source square
     * @param to   destination square
     * @return {@code true} if the move is legal and promotes a pawn
     */
    public boolean isPromotionMove(Position from, Position to) {
        Piece piece = board.getPiece(from);
        if (!(piece instanceof Pawn) || !((Pawn) piece).isPromotionSquare(to) || status.isGameOver()) {
            return false;
        }
        return validator.isLegal(board, currentPlayer.getColor(), new Move(from, to, PieceType.QUEEN));
    }

    /**
     * Checks whether the king of one color is in check.
     *
     * @param color the color to examine
     * @return {@code true} if that king is attacked
     */
    public boolean isInCheck(PieceColor color) {
        return board.isInCheck(color);
    }

    /**
     * Registers a listener to be told about new games and accepted moves.
     *
     * @param listener the listener to add; must not be null
     */
    public void addListener(GameListener listener) {
        listeners.add(Objects.requireNonNull(listener, "listener must not be null"));
    }

    /**
     * Unregisters a listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(GameListener listener) {
        listeners.remove(listener);
    }

    /**
     * Tells every listener that the game started. Iterates over a copy so a listener
     * may add or remove listeners while being notified.
     */
    private void fireGameStarted() {
        for (GameListener listener : new ArrayList<>(listeners)) {
            listener.onGameStarted(this);
        }
    }

    /**
     * Tells every listener that a move was accepted.
     *
     * @param result the accepted move's result
     */
    private void fireMoveMade(MoveResult result) {
        for (GameListener listener : new ArrayList<>(listeners)) {
            listener.onMoveMade(this, result);
        }
    }

    /**
     * Returns the board, for display purposes. Callers should not change it directly;
     * moves go through {@link #tryMove(Move)}.
     *
     * @return the game board
     */
    public Board getBoard() {
        return board;
    }

    /**
     * Returns the player whose turn it is.
     *
     * @return the current player
     */
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Returns the player for a color.
     *
     * @param color the color wanted
     * @return the white or black player
     */
    public Player getPlayer(PieceColor color) {
        return color == PieceColor.WHITE ? whitePlayer : blackPlayer;
    }

    /**
     * Returns a read-only view of the accepted moves.
     *
     * @return unmodifiable move history
     */
    public List<String> getMoveHistory() {
        return Collections.unmodifiableList(moveHistory);
    }

    /**
     * Returns the current status for the player to move.
     *
     * @return in progress, check, checkmate, or stalemate
     */
    public GameStatus getStatus() {
        return status;
    }

    /**
     * Returns whether the game has ended.
     *
     * @return {@code true} after checkmate or stalemate
     */
    public boolean isGameOver() {
        return status.isGameOver();
    }

    /**
     * Returns the winner, if any.
     *
     * @return the player who delivered checkmate, or {@code null}
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * Returns the most recent accepted move, for highlighting in the GUI.
     *
     * @return the last move, or {@code null} if no move has been played
     */
    public Move getLastMove() {
        return lastMove;
    }
}
