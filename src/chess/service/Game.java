package chess.service;

import chess.model.Board;
import chess.model.PieceColor;
import chess.model.Player;
import chess.model.Position;
import chess.pieces.King;
import chess.pieces.Pawn;
import chess.pieces.Piece;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Coordinates a two-player game: owns the board, the players, and the turn,
 * and decides whether a requested move is allowed.
 *
 * <p>This class contains no console input or output, so the Phase 2 GUI can
 * call exactly the same methods from its event handlers.</p>
 *
 * <h2>Simplified rules in this prototype</h2>
 * <ul>
 *   <li>Check, checkmate, stalemate, castling, and en passant are not implemented.</li>
 *   <li>Because check is not enforced, a king can be captured; capturing the
 *       opposing king ends the game and that player wins.</li>
 *   <li>Pawn promotion: see {@link #handlePromotion(Position)}.</li>
 * </ul>
 */
public class Game {

    /** The board this game is played on. */
    private final Board board;

    /** The player controlling the white pieces. */
    private final Player whitePlayer;

    /** The player controlling the black pieces. */
    private final Player blackPlayer;

    /** The player whose turn it is. */
    private Player currentPlayer;

    /** Accepted moves in order, e.g. {@code "1. White: E2 E4"}. */
    private final List<String> moveHistory;

    /** {@code true} once the game has ended (king captured or {@link #end()} called). */
    private boolean gameOver;

    /** The winner, or {@code null} if the game is ongoing or ended without a winner. */
    private Player winner;

    /**
     * Creates a game with default player names ("White" and "Black").
     * Call {@link #start()} before making moves.
     */
    public Game() {
        this(null, null);
    }

    /**
     * Creates a game with the given player names.
     * Call {@link #start()} before making moves.
     *
     * @param whiteName name for the white player, or null/blank for the default
     * @param blackName name for the black player, or null/blank for the default
     */
    public Game(String whiteName, String blackName) {
        this.board = new Board();
        this.whitePlayer = new Player(PieceColor.WHITE, whiteName);
        this.blackPlayer = new Player(PieceColor.BLACK, blackName);
        this.moveHistory = new ArrayList<>();
        start();
    }

    /**
     * Starts (or restarts) the game: sets up the board, clears history, and gives White the first move.
     * Declared {@code final} because the constructor calls it; an overridable method called from a
     * constructor could run in a subclass before that subclass is initialized.
     */
    public final void start() {
        board.initialize();
        moveHistory.clear();
        currentPlayer = whitePlayer;
        gameOver = false;
        winner = null;
    }

    /**
     * Ends the game without declaring a winner (for example, when a player quits).
     */
    public void end() {
        gameOver = true;
    }

    /**
     * Attempts to move the current player's piece from one square to another.
     *
     * <p>Checks are applied in this order, and the first failure is reported:
     * game over, same square, empty source, wrong owner, friendly destination,
     * movement pattern, blocked path. If any check fails, the board and the
     * turn are left exactly as they were.</p>
     *
     * @param from source square; must not be null
     * @param to   destination square; must not be null
     * @return a {@link MoveResult} describing success or the reason for failure
     */
    public MoveResult tryMove(Position from, Position to) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");

        if (gameOver) {
            return MoveResult.failure("The game is over. Type 'reset' to play again.");
        }
        if (from.equals(to)) {
            return MoveResult.failure("Source and destination are the same square (" + from + ").");
        }

        Piece mover = board.getPiece(from);
        if (mover == null) {
            return MoveResult.failure("There is no piece on " + from + ".");
        }
        if (mover.getColor() != currentPlayer.getColor()) {
            return MoveResult.failure("The " + mover.getName() + " on " + from + " belongs to "
                    + mover.getColor().displayName() + ". It is "
                    + currentPlayer.getColor().displayName() + "'s turn.");
        }

        Piece target = board.getPiece(to);
        if (target != null && target.getColor() == mover.getColor()) {
            return MoveResult.failure(to + " is occupied by your own " + target.getName() + ".");
        }

        // Polymorphic call: the actual subclass (Pawn, Rook, ...) decides what "valid" means.
        if (!mover.isValidPattern(to, board)) {
            return MoveResult.failure("A " + mover.getName() + " cannot move from "
                    + from + " to " + to + ".");
        }

        // Rejects a move whose path is blocked.
        // Only applies when mover.requiresClearPath() is true (another polymorphic call - Knight says false).
     
        if (mover.requiresClearPath()) {
            if (!board.isPathClear(from, to)) {
                return MoveResult.failure("A " + mover.getName() + " is blocked from "
                    + from + " to " + to + ".");
            }
        } 

        Piece captured = board.movePiece(from, to);
        String description = mover.getName() + " " + from + " -> " + to;
        if (captured != null) {
            description += ", capturing " + captured.getColor().displayName() + " " + captured.getName();
        }
        moveHistory.add((moveHistory.size() + 1) + ". " + currentPlayer.getColor().displayName()
                + ": " + from + " " + to + (captured != null ? " (x" + captured.getDisplaySymbol() + ")" : ""));

        String promotionNote = handlePromotion(to);
        if (promotionNote != null) {
            description += ". " + promotionNote;
        }

        if (captured instanceof King) {
            gameOver = true;
            winner = currentPlayer;
            return MoveResult.success(description + ". " + currentPlayer + " captured the king and wins!",
                    captured);
        }

        switchTurn();
        return MoveResult.success(description + ".", captured);
    }

    /**
     * Applies this prototype's last-rank rule for a pawn that just moved to {@code square}.
     *
     * <p>Default Phase 1 behavior: the pawn simply stays a pawn. Because pawns
     * only move forward, it can no longer move. This limitation is documented
     * in the README.</p>
     *
     * @param square the destination square of the move just made
     * @return a note to append to the move description, or {@code null} if nothing happened
     */
    private String handlePromotion(Position square) {
        Piece piece = board.getPiece(square);
        if (!(piece instanceof Pawn) || !((Pawn) piece).isOnLastRank()) {
            return null;
        }
        // TODO(14, OPTIONAL): Auto-promote to a queen instead of leaving a stuck pawn:
        //   board.placePiece(new Queen(piece.getColor(), square));   // (import chess.pieces.Queen)
        //   return "Pawn promoted to Queen";
        //   If you do this, update the README "Known limitations" section and the class javadoc above.
        return "Pawn reached the last rank (promotion is not implemented in this prototype)";
    }

    /**
     * Gives the turn to the other player.
     */
    private void switchTurn() {
        currentPlayer = (currentPlayer == whitePlayer) ? blackPlayer : whitePlayer;
    }

    /**
     * Lists the squares the piece on {@code from} may move to, for the "moves" hint command.
     *
     * @param from the square to inspect
     * @return destinations allowed by the prototype rules; empty if the square is empty
     */
    public List<Position> getLegalMoves(Position from) {
        Piece piece = board.getPiece(from);
        if (piece == null) {
            return Collections.emptyList();
        }
        return piece.possibleMoves(board);
    }

    /**
     * Returns the board, for display purposes.
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
     * Returns whether the game has ended.
     *
     * @return {@code true} after a king capture or {@link #end()}
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Returns the winner, if any.
     *
     * @return the winning player, or {@code null}
     */
    public Player getWinner() {
        return winner;
    }
}
