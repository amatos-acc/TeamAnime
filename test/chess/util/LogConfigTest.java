package chess.util;

import static chess.TestBoards.sq;
import static org.junit.jupiter.api.Assertions.assertTrue;

import chess.service.Game;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Handler;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests that game events and rejected moves are written to the log file.
 */
class LogConfigTest {

    @TempDir
    Path logDir;

    @Test
    void gameEventsAreWrittenToTheLogFile() throws IOException {
        LogConfig.init(logDir);

        Game game = new Game("Alice", "Bob");
        game.tryMove(sq("E2"), sq("E4"));
        game.tryMove(sq("E2"), sq("E4"));
        for (Handler handler : Logger.getLogger("chess").getHandlers()) {
            handler.flush();
        }

        String log = Files.readString(logDir.resolve(LogConfig.LOG_FILE));
        assertTrue(log.contains("New game: Alice (White) vs Bob (Black)"), log);
        assertTrue(log.contains("White played"), log);
        assertTrue(log.contains("Rejected Black move"), log);
    }
}
