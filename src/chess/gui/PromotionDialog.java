package chess.gui;

import chess.model.PieceColor;
import chess.pieces.PieceType;

import javax.swing.JOptionPane;
import java.awt.Component;

/**
 * Asks which piece a pawn reaching the last rank should become.
 */
public final class PromotionDialog {

    /** The choices, in the order they are shown. */
    private static final PieceType[] CHOICES = {PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT};

    /** Not instantiable; use {@link #ask}. */
    private PromotionDialog() {
    }

    /**
     * Shows a modal dialog with one button per promotion choice.
     *
     * @param parent component to center the dialog over
     * @param color  the promoting pawn's color
     * @return the chosen type, or {@code null} if the dialog was closed without choosing
     */
    public static PieceType ask(Component parent, PieceColor color) {
        String[] labels = new String[CHOICES.length];
        for (int i = 0; i < CHOICES.length; i++) {
            labels[i] = PieceGlyphs.text(CHOICES[i], color) + "  " + CHOICES[i].getDisplayName();
        }
        int choice = JOptionPane.showOptionDialog(parent,
                "Your pawn reached the last rank. Promote it to:",
                "Pawn promotion",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, labels, labels[0]);
        return choice >= 0 ? CHOICES[choice] : null;
    }
}
