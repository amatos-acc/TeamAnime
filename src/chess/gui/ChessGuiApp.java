package chess.gui;

import chess.util.LogConfig;

import javax.swing.SwingUtilities;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point for the Swing chess application.
 *
 * <p>Usage: {@code java -cp out chess.gui.ChessGuiApp}</p>
 */
public final class ChessGuiApp {

    /** Logger for start-up and unexpected errors. */
    private static final Logger LOG = Logger.getLogger(ChessGuiApp.class.getName());

    /** Not instantiable; this class only holds {@code main}. */
    private ChessGuiApp() {
    }

    /**
     * Sets up logging, then builds and shows the window on the Swing event dispatch thread
     * (Swing components must only be created and changed on that thread).
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        LogConfig.init();
        Thread.setDefaultUncaughtExceptionHandler((thread, error) ->
                LOG.log(Level.SEVERE, "Unexpected error on thread " + thread.getName(), error));
        LOG.info("Starting the Swing chess application on Java " + System.getProperty("java.version"));
        SwingUtilities.invokeLater(() -> new ChessFrame(GameSettings.twoPlayer(null, null)).show());
    }
}
