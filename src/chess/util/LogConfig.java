package chess.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Sets up {@code java.util.logging} for the application.
 *
 * <p>Every class logs through a logger named after itself (for example
 * {@code chess.service.Game}). Those loggers are children of the
 * {@code "chess"} logger, so configuring that one logger here controls all of
 * them. {@link #init()} sends their output to {@code logs/chess.log} instead
 * of the console, so log lines never mix with the console game's board.</p>
 */
public final class LogConfig {

    /** Directory the log file is written to, relative to the working directory. */
    public static final String LOG_DIR = "logs";

    /** Name of the log file inside {@link #LOG_DIR}. */
    public static final String LOG_FILE = "chess.log";

    /**
     * The parent logger of every application logger. Kept in a static field because
     * {@code Logger.getLogger} holds loggers only weakly; without this reference the
     * configured logger could be garbage-collected and its settings lost.
     */
    private static final Logger ROOT = Logger.getLogger("chess");

    /** Whether {@link #init(Path)} has already run. */
    private static boolean initialized;

    /** Not instantiable; all methods are static. */
    private LogConfig() {
    }

    /**
     * Sends application log messages (level FINE and above) to {@code logs/chess.log},
     * appending to any existing file. Safe to call more than once.
     */
    public static void init() {
        init(Paths.get(LOG_DIR));
    }

    /**
     * Sends application log messages (level FINE and above) to {@code chess.log} in the given
     * directory. If the file cannot be opened, logging falls back to the console and a
     * warning is printed; the game still runs.
     *
     * @param directory where to write the log file
     */
    public static synchronized void init(Path directory) {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            Files.createDirectories(directory);
            FileHandler file = new FileHandler(directory.resolve(LOG_FILE).toString(), true);
            file.setFormatter(new LineFormatter());
            file.setLevel(Level.FINE);
            ROOT.addHandler(file);
            ROOT.setLevel(Level.FINE);
            ROOT.setUseParentHandlers(false);
        } catch (IOException | SecurityException e) {
            System.err.println("Could not open the log file (" + e.getMessage() + "); logging to the console.");
        }
    }

    /**
     * Turns application logging off entirely, for runs whose console output must stay clean
     * (the scenario driver). Has no effect after {@link #init(Path)} has run.
     */
    public static synchronized void silence() {
        if (!initialized) {
            ROOT.setUseParentHandlers(false);
        }
    }

    /**
     * Formats each record on one line: {@code 2026-11-24 17:55:01 INFO    [Game] message}.
     */
    private static final class LineFormatter extends Formatter {

        /** Creates the formatter. */
        LineFormatter() {
        }

        /** Timestamp format used at the start of each line. */
        private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        /**
         * Formats one log record.
         *
         * @param record the record to format
         * @return one line of text, plus a stack trace if the record has an exception
         */
        @Override
        public String format(LogRecord record) {
            String logger = record.getLoggerName();
            String shortName = logger == null ? "?" : logger.substring(logger.lastIndexOf('.') + 1);
            StringBuilder line = new StringBuilder();
            line.append(LocalDateTime.now().format(TIME)).append(' ')
                    .append(String.format("%-7s", record.getLevel().getName()))
                    .append(" [").append(shortName).append("] ")
                    .append(formatMessage(record)).append(System.lineSeparator());
            if (record.getThrown() != null) {
                line.append("    ").append(record.getThrown()).append(System.lineSeparator());
                for (StackTraceElement frame : record.getThrown().getStackTrace()) {
                    line.append("        at ").append(frame).append(System.lineSeparator());
                }
            }
            return line.toString();
        }
    }
}
