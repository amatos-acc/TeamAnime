package chess.gui;

import chess.model.PieceColor;
import chess.pieces.PieceType;

/**
 * What the {@link GameController} needs from the window.
 *
 * <p>The controller talks to the window only through this interface, so it
 * does not depend on any particular Swing layout, and a test could supply a
 * fake view. {@link ChessFrame} is the real implementation.</p>
 */
public interface GameView {

    /**
     * Redraws the board, turn indicator, status, captured pieces, and move list
     * from the current game state.
     */
    void refresh();

    /**
     * Shows a one-line message in the message bar.
     *
     * @param text    the message
     * @param isError {@code true} to show it as an error (for example, a rejected move)
     */
    void showMessage(String text, boolean isError);

    /**
     * Asks the player which piece a pawn should be promoted to.
     *
     * @param color the promoting pawn's color (for drawing the choices)
     * @return the chosen type, or {@code null} if the player cancelled
     */
    PieceType askPromotion(PieceColor color);

    /**
     * Announces the end of the game (checkmate or stalemate) prominently.
     *
     * @param outcome a description such as {@code "Checkmate. Black wins"}
     */
    void announceGameOver(String outcome);
}
