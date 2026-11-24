package chess.service;

import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;
import chess.model.Position;
import chess.pieces.King;
import chess.pieces.MoveProblem;
import chess.pieces.Pawn;
import chess.pieces.Piece;
import chess.pieces.PieceType;

import java.util.ArrayList;
import java.util.List;

/**
 * Decides whether a move is legal and, if not, explains why.
 *
 * <p>This class is the Phase 2 answer to duplicated logic. In Phase 1,
 * {@code Game.tryMove} repeated the checks in {@code Piece.canMoveTo} so it
 * could produce error messages, and the two copies could drift apart. Now
 * the piece-level sequence lives only in {@link Piece#checkMove}, and this
 * class is the only place that adds the rules needing the whole position:
 * whose piece it is, castling out of or through check, and king safety.
 * {@link Game#tryMove}, the legal-move hints, the checkmate test, and the
 * computer opponent all call the same {@link #findProblem} method, so they
 * always agree.</p>
 *
 * <p>The class has no state; it reads the board it is given.</p>
 */
public class MoveValidator {

    /**
     * Creates a validator.
     */
    public MoveValidator() {
    }

    /**
     * Explains why {@code move} is illegal for {@code side}, or returns {@code null} if it is legal.
     *
     * <p>Checks, in order: the source square is occupied, the piece belongs to {@code side},
     * the piece-level checks ({@link Piece#checkMove}), castling-specific attack rules, the
     * promotion choice, and finally that the move does not leave {@code side}'s king in check.
     * The last check plays the move on a {@linkplain Board#copy() copy} of the board, so the
     * real board is never touched.</p>
     *
     * @param board the current position
     * @param side  the color that is moving
     * @param move  the move to check
     * @return a message for the player, or {@code null} if the move is legal
     */
    public String findProblem(Board board, PieceColor side, Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();

        Piece mover = board.getPiece(from);
        if (mover == null) {
            return "There is no piece on " + from + ".";
        }
        if (mover.getColor() != side) {
            return "The " + mover.getName() + " on " + from + " belongs to "
                    + mover.getColor().displayName() + ". It is " + side.displayName() + "'s turn.";
        }

        String pieceProblem = describe(mover.checkMove(to, board), mover, from, to, board);
        if (pieceProblem != null) {
            return pieceProblem;
        }

        if (mover instanceof King && ((King) mover).isCastlingShape(to)) {
            if (board.isInCheck(side)) {
                return "You cannot castle while your king is in check.";
            }
            Position passedOver = from.offset(0, Integer.signum(to.getCol() - from.getCol()));
            if (board.isSquareAttacked(passedOver, side.opposite())) {
                return "You cannot castle through " + passedOver + " because it is attacked.";
            }
        }

        if (move.getPromotion() != null
                && !(mover instanceof Pawn && ((Pawn) mover).isPromotionSquare(to))) {
            return "Only a pawn reaching the last rank can be promoted.";
        }

        Board trial = board.copy();
        trial.applyMove(move);
        if (trial.isInCheck(side)) {
            return board.isInCheck(side)
                    ? "Your king is in check, and that move does not get it out of check."
                    : "That move would put your own king in check.";
        }
        return null;
    }

    /**
     * Checks whether a move is legal for {@code side}.
     *
     * @param board the current position
     * @param side  the color that is moving
     * @param move  the move to check
     * @return {@code true} if {@link #findProblem} finds nothing wrong
     */
    public boolean isLegal(Board board, PieceColor side, Move move) {
        return findProblem(board, side, move) == null;
    }

    /**
     * Lists the legal moves of the piece on {@code from}, scanning destinations from rank 8
     * to rank 1. A move onto the last rank is listed once, promoting to a queen.
     *
     * @param board the current position
     * @param from  the square of the piece to examine
     * @return new list of legal moves (empty if the square is empty or the piece is stuck)
     */
    public List<Move> legalMovesFrom(Board board, Position from) {
        List<Move> moves = new ArrayList<>();
        Piece piece = board.getPiece(from);
        if (piece == null) {
            return moves;
        }
        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                Position to = new Position(row, col);
                if (!piece.canMoveTo(to, board)) {
                    continue;   // cheap test first; skips the board copy for most squares
                }
                Move move = new Move(from, to, promotionFor(piece, to));
                if (isLegal(board, piece.getColor(), move)) {
                    moves.add(move);
                }
            }
        }
        return moves;
    }

    /**
     * Lists every legal move for one color.
     *
     * @param board the current position
     * @param side  the color to move
     * @return new list of legal moves; empty means checkmate or stalemate
     */
    public List<Move> legalMoves(Board board, PieceColor side) {
        List<Move> moves = new ArrayList<>();
        for (Piece piece : board.getActivePieces(side)) {
            moves.addAll(legalMovesFrom(board, piece.getPosition()));
        }
        return moves;
    }

    /**
     * Checks whether one color has at least one legal move. Stops at the first one found,
     * which makes it faster than {@code !legalMoves(...).isEmpty()}.
     *
     * @param board the current position
     * @param side  the color to move
     * @return {@code false} exactly when {@code side} is checkmated or stalemated
     */
    public boolean hasAnyLegalMove(Board board, PieceColor side) {
        for (Piece piece : board.getActivePieces(side)) {
            if (!legalMovesFrom(board, piece.getPosition()).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the default promotion choice for a move, so generated moves are complete.
     *
     * @param piece the moving piece
     * @param to    its destination
     * @return {@link PieceType#QUEEN} for a pawn reaching the last rank, otherwise {@code null}
     */
    private static PieceType promotionFor(Piece piece, Position to) {
        boolean promotes = piece instanceof Pawn && ((Pawn) piece).isPromotionSquare(to);
        return promotes ? PieceType.QUEEN : null;
    }

    /**
     * Turns a piece-level {@link MoveProblem} into a message for the player.
     *
     * @param problem the problem reported by {@link Piece#checkMove}
     * @param mover   the moving piece
     * @param from    its square
     * @param to      the requested destination
     * @param board   the current position
     * @return the message, or {@code null} for {@link MoveProblem#NONE}
     */
    private static String describe(MoveProblem problem, Piece mover, Position from, Position to, Board board) {
        switch (problem) {
            case NONE:
                return null;
            case SAME_SQUARE:
                return "Source and destination are the same square (" + from + ").";
            case OWN_PIECE:
                return to + " is occupied by your own " + board.getPiece(to).getName() + ".";
            case BLOCKED_PATH:
                return "A " + mover.getName() + " is blocked from " + from + " to " + to + ".";
            case ILLEGAL_PATTERN:
            default:
                if (mover instanceof King && ((King) mover).isCastlingShape(to)) {
                    return "You cannot castle that way: the king or that rook has already moved, "
                            + "or a piece stands between them.";
                }
                return "A " + mover.getName() + " cannot move from " + from + " to " + to + ".";
        }
    }
}
