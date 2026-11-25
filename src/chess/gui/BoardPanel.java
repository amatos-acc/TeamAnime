package chess.gui;

import chess.model.Board;
import chess.model.Move;
import chess.model.PieceColor;
import chess.model.Position;
import chess.pieces.Piece;
import chess.service.Game;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

/**
 * Draws the 8 x 8 board and turns mouse actions into calls on the {@link GameController}.
 *
 * <p>The panel holds no game state of its own: every repaint reads the current
 * position from the controller's {@link Game}, so the picture can never drift
 * from the real state. It shows file letters and rank numbers around the edge,
 * highlights the last move, the selected piece, its legal targets, and a king
 * in check. Players can click a piece and then a target, or drag the piece and
 * drop it.</p>
 */
@SuppressWarnings("serial")   // Swing components are Serializable; this one is never serialized.
public final class BoardPanel extends JComponent {

    /** Width of the coordinate margin around the board, in pixels. */
    private static final int MARGIN = 26;

    /** Preferred size of one square, in pixels. */
    private static final int PREFERRED_SQUARE = 72;

    /** Light square color. */
    private static final Color LIGHT = new Color(240, 217, 181);

    /** Dark square color. */
    private static final Color DARK = new Color(181, 136, 99);

    /** Tint for the two squares of the last move. */
    private static final Color LAST_MOVE = new Color(255, 235, 59, 110);

    /** Tint for the selected piece's square. */
    private static final Color SELECTED = new Color(76, 175, 80, 150);

    /** Marker color for legal targets. */
    private static final Color TARGET = new Color(30, 30, 30, 90);

    /** Tint for a king in check. */
    private static final Color CHECK = new Color(229, 57, 53, 170);

    /** The controller that receives clicks and supplies the game to draw. */
    private final GameController controller;

    /** Square where a drag started, or {@code null} when not dragging. */
    private Position dragFrom;

    /** Current mouse location during a drag. */
    private Point dragPoint;

    /** Square where the mouse button was pressed. */
    private Position pressedOn;

    /**
     * Creates the board panel and installs its mouse handlers.
     *
     * @param controller the controller to notify about clicks and drags; must not be null
     */
    public BoardPanel(GameController controller) {
        this.controller = Objects.requireNonNull(controller, "controller must not be null");
        int side = 2 * MARGIN + Position.BOARD_SIZE * PREFERRED_SQUARE;
        setPreferredSize(new Dimension(side, side));
        setMinimumSize(new Dimension(2 * MARGIN + 8 * 40, 2 * MARGIN + 8 * 40));
        MouseHandler handler = new MouseHandler();
        addMouseListener(handler);
        addMouseMotionListener(handler);
    }

    /**
     * Returns the current size of one square, which grows and shrinks with the window.
     *
     * @return square size in pixels
     */
    private int squareSize() {
        int usable = Math.min(getWidth(), getHeight()) - 2 * MARGIN;
        return Math.max(usable / Position.BOARD_SIZE, 1);
    }

    /**
     * Converts a pixel location to a board square (White is always at the bottom).
     *
     * @param point a location in this panel
     * @return the square under the point, or {@code null} if the point is off the board
     */
    private Position toSquare(Point point) {
        int size = squareSize();
        int col = Math.floorDiv(point.x - MARGIN, size);
        int row = Math.floorDiv(point.y - MARGIN, size);
        return Position.isInBounds(row, col) ? new Position(row, col) : null;
    }

    /**
     * Paints the board, highlights, coordinates, and pieces.
     *
     * @param g the graphics context supplied by Swing
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(getParent() == null ? Color.WHITE : getParent().getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());
            Game game = controller.getGame();
            if (game == null) {
                return;
            }
            int size = squareSize();
            paintSquares(g2, size);
            paintHighlights(g2, game, size);
            paintCoordinates(g2, size);
            paintPieces(g2, game.getBoard(), size);
        } finally {
            g2.dispose();
        }
    }

    /**
     * Paints the alternating light and dark squares (A1 is dark).
     *
     * @param g2   graphics context
     * @param size square size in pixels
     */
    private void paintSquares(Graphics2D g2, int size) {
        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                g2.setColor((row + col) % 2 == 0 ? LIGHT : DARK);
                g2.fillRect(MARGIN + col * size, MARGIN + row * size, size, size);
            }
        }
    }

    /**
     * Paints the last-move, check, selection, and legal-target highlights.
     *
     * @param g2   graphics context
     * @param game the game being drawn
     * @param size square size in pixels
     */
    private void paintHighlights(Graphics2D g2, Game game, int size) {
        Move last = game.getLastMove();
        if (last != null) {
            fillSquare(g2, last.getFrom(), size, LAST_MOVE);
            fillSquare(g2, last.getTo(), size, LAST_MOVE);
        }
        for (PieceColor color : PieceColor.values()) {
            if (game.isInCheck(color)) {
                fillSquare(g2, game.getBoard().findKing(color), size, CHECK);
            }
        }
        Position selected = controller.getSelected();
        if (selected != null) {
            fillSquare(g2, selected, size, SELECTED);
            g2.setColor(TARGET);
            for (Position target : controller.getSelectedTargets()) {
                int x = MARGIN + target.getCol() * size;
                int y = MARGIN + target.getRow() * size;
                if (game.getBoard().isEmpty(target)) {
                    int dot = size / 4;
                    g2.fillOval(x + (size - dot) / 2, y + (size - dot) / 2, dot, dot);
                } else {
                    g2.setStroke(new BasicStroke(Math.max(3f, size / 14f)));
                    g2.drawOval(x + 3, y + 3, size - 6, size - 6);
                }
            }
        }
    }

    /**
     * Fills one square with a translucent color.
     *
     * @param g2     graphics context
     * @param square the square, ignored if {@code null}
     * @param size   square size in pixels
     * @param color  the tint
     */
    private void fillSquare(Graphics2D g2, Position square, int size, Color color) {
        if (square == null) {
            return;
        }
        g2.setColor(color);
        g2.fillRect(MARGIN + square.getCol() * size, MARGIN + square.getRow() * size, size, size);
    }

    /**
     * Paints the file letters A-H above and below the board and rank numbers 1-8 on both sides.
     *
     * @param g2   graphics context
     * @param size square size in pixels
     */
    private void paintCoordinates(Graphics2D g2, int size) {
        g2.setColor(new Color(60, 60, 60));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        int boardEnd = MARGIN + Position.BOARD_SIZE * size;
        for (int i = 0; i < Position.BOARD_SIZE; i++) {
            String file = String.valueOf((char) ('A' + i));
            int fx = MARGIN + i * size + (size - fm.stringWidth(file)) / 2;
            g2.drawString(file, fx, MARGIN - 8);
            g2.drawString(file, fx, boardEnd + fm.getAscent() + 4);

            String rank = String.valueOf(Position.BOARD_SIZE - i);
            int ry = MARGIN + i * size + (size + fm.getAscent()) / 2 - 2;
            g2.drawString(rank, (MARGIN - fm.stringWidth(rank)) / 2, ry);
            g2.drawString(rank, boardEnd + (MARGIN - fm.stringWidth(rank)) / 2, ry);
        }
    }

    /**
     * Paints every piece, drawing a dragged piece under the mouse instead of on its square.
     *
     * @param g2    graphics context
     * @param board the position
     * @param size  square size in pixels
     */
    private void paintPieces(Graphics2D g2, Board board, int size) {
        Font font = new Font(Font.SERIF, Font.PLAIN, (int) (size * 0.8));
        Piece dragged = null;
        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            for (int col = 0; col < Position.BOARD_SIZE; col++) {
                Position square = new Position(row, col);
                Piece piece = board.getPiece(square);
                if (piece == null) {
                    continue;
                }
                if (square.equals(dragFrom) && dragPoint != null) {
                    dragged = piece;
                    continue;
                }
                drawPiece(g2, piece, font, MARGIN + col * size + size / 2.0, MARGIN + row * size + size / 2.0);
            }
        }
        if (dragged != null) {
            drawPiece(g2, dragged, font, dragPoint.x, dragPoint.y);
        }
    }

    /**
     * Draws one piece centered on a point: the solid chess symbol filled white or black,
     * with a contrasting outline so both colors stand out on both square colors.
     *
     * @param g2      graphics context
     * @param piece   the piece to draw
     * @param font    the font for the symbol
     * @param centerX x coordinate of the center
     * @param centerY y coordinate of the center
     */
    private void drawPiece(Graphics2D g2, Piece piece, Font font, double centerX, double centerY) {
        GlyphVector glyph = font.createGlyphVector(g2.getFontRenderContext(), PieceGlyphs.solid(piece.getType()));
        Rectangle2D bounds = glyph.getVisualBounds();
        double x = centerX - bounds.getCenterX();
        double y = centerY - bounds.getCenterY();
        Shape outline = AffineTransform.getTranslateInstance(x, y).createTransformedShape(glyph.getOutline());
        boolean white = piece.getColor() == PieceColor.WHITE;
        g2.setColor(white ? Color.WHITE : new Color(25, 25, 25));
        g2.fill(outline);
        g2.setStroke(new BasicStroke(1.6f));
        g2.setColor(white ? new Color(25, 25, 25) : new Color(200, 200, 200));
        g2.draw(outline);
    }

    /**
     * Turns mouse presses, drags, and releases into controller calls.
     */
    private final class MouseHandler extends MouseAdapter {

        /** Creates the handler; it reads and updates the enclosing panel's drag state. */
        MouseHandler() {
        }

        /**
         * Remembers where the press happened and whether a drag may start there.
         *
         * @param e the mouse event
         */
        @Override
        public void mousePressed(MouseEvent e) {
            pressedOn = toSquare(e.getPoint());
            dragFrom = controller.canPickUp(pressedOn) ? pressedOn : null;
            dragPoint = null;
        }

        /**
         * Moves the dragged piece with the mouse.
         *
         * @param e the mouse event
         */
        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragFrom != null) {
                dragPoint = e.getPoint();
                repaint();
            }
        }

        /**
         * Finishes a click or a drag-and-drop.
         *
         * @param e the mouse event
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            Position releasedOn = toSquare(e.getPoint());
            boolean wasDrag = dragFrom != null && dragPoint != null;
            Position from = dragFrom;
            dragFrom = null;
            dragPoint = null;
            if (wasDrag && !Objects.equals(from, releasedOn)) {
                controller.pieceDropped(from, releasedOn);
            } else if (Objects.equals(pressedOn, releasedOn)) {
                controller.squareClicked(releasedOn);
            }
            pressedOn = null;
            repaint();
        }
    }
}
