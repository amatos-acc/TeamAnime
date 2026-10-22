package chess.console;

import chess.service.Game;

/**
 * Entry point for the interactive console chess application.
 *
 * <p>{@code main} only wires objects together; all game logic is in
 * {@link Game} and all console I/O is in {@link ConsoleUI}.</p>
 *
 * <p>Usage: {@code java -cp out chess.console.ChessApp [whiteName] [blackName]}</p>
 */
public final class ChessApp {

    /** Not instantiable; this class only holds {@code main}. */
    private ChessApp() {
    }

    /**
     * Starts the game.
     *
     * @param args optional player names: {@code args[0]} for White, {@code args[1]} for Black
     */
    public static void main(String[] args) {
        String whiteName = args.length > 0 ? args[0] : null;
        String blackName = args.length > 1 ? args[1] : null;
        new ConsoleUI(new Game(whiteName, blackName), System.in, System.out).run();
    }
}
