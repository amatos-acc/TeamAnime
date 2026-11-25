package chess.console;

import static chess.TestBoards.sq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import chess.pieces.PieceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests parsing of console commands and moves.
 */
class InputParserTest {

    private final InputParser parser = new InputParser();

    @Test
    void parsesAMove() {
        Command command = parser.parse("E2 E4");
        assertEquals(Command.Type.MOVE, command.getType());
        assertEquals(sq("E2"), command.getFrom());
        assertEquals(sq("E4"), command.getTo());
        assertNull(command.getPromotion());
    }

    @Test
    void movesIgnoreCaseAndExtraSpaces() {
        Command command = parser.parse("   e2     e4  ");
        assertEquals(Command.Type.MOVE, command.getType());
        assertEquals(sq("E4"), command.getTo());
    }

    @Test
    void parsesAPromotionChoice() {
        Command command = parser.parse("E7 E8 N");
        assertEquals(Command.Type.MOVE, command.getType());
        assertEquals(PieceType.KNIGHT, command.getPromotion());
    }

    @Test
    void parsesKeywordCommands() {
        assertEquals(Command.Type.QUIT, parser.parse("quit").getType());
        assertEquals(Command.Type.QUIT, parser.parse("EXIT").getType());
        assertEquals(Command.Type.HELP, parser.parse("help").getType());
        assertEquals(Command.Type.RESET, parser.parse("reset").getType());
        assertEquals(Command.Type.HISTORY, parser.parse("history").getType());
        assertEquals(Command.Type.CAPTURED, parser.parse("captured").getType());
        assertEquals(Command.Type.BOARD, parser.parse("board").getType());
    }

    @Test
    void parsesAMovesHint() {
        Command command = parser.parse("moves G1");
        assertEquals(Command.Type.MOVES, command.getType());
        assertEquals(sq("G1"), command.getFrom());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "Z9 E4", "E2", "E2 E4 E5", "E2 E9", "E7 E8 K", "moves", "quit now"})
    void malformedInputIsInvalidWithAMessage(String line) {
        Command command = parser.parse(line);
        assertEquals(Command.Type.INVALID, command.getType());
        assertNotNull(command.getErrorMessage());
    }
}
