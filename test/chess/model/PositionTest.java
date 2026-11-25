package chess.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests coordinate conversion between algebraic names and array indices.
 */
class PositionTest {

    @Test
    void e2MapsToRow6Column4() {
        Position e2 = Position.fromAlgebraic("E2");
        assertEquals(6, e2.getRow());
        assertEquals(4, e2.getCol());
    }

    @Test
    void cornersMapToArrayCorners() {
        assertEquals(new Position(0, 0), Position.fromAlgebraic("A8"));
        assertEquals(new Position(0, 7), Position.fromAlgebraic("H8"));
        assertEquals(new Position(7, 0), Position.fromAlgebraic("A1"));
        assertEquals(new Position(7, 7), Position.fromAlgebraic("H1"));
    }

    @Test
    void parsingIgnoresCaseAndSurroundingSpaces() {
        assertEquals(Position.fromAlgebraic("E2"), Position.fromAlgebraic("  e2 "));
    }

    @Test
    void everySquareRoundTrips() {
        for (char file = 'A'; file <= 'H'; file++) {
            for (char rank = '1'; rank <= '8'; rank++) {
                String name = "" + file + rank;
                assertEquals(name, Position.fromAlgebraic(name).toAlgebraic());
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"Z9", "I1", "A0", "A9", "", "E", "E22", "22", "EE"})
    void offBoardOrMalformedSquaresAreRejected(String text) {
        assertThrows(IllegalArgumentException.class, () -> Position.fromAlgebraic(text));
    }

    @Test
    void nullIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> Position.fromAlgebraic(null));
    }

    @Test
    void boundsCheckCoversTheEdges() {
        assertTrue(Position.isInBounds(0, 0));
        assertTrue(Position.isInBounds(7, 7));
        assertFalse(Position.isInBounds(-1, 0));
        assertFalse(Position.isInBounds(0, 8));
    }

    @Test
    void equalPositionsHaveEqualHashCodes() {
        Position a = new Position(3, 4);
        Position b = new Position(3, 4);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, new Position(4, 3));
    }

    @Test
    void offsetMovesByTheGivenDeltas() {
        assertEquals(Position.fromAlgebraic("E4"), Position.fromAlgebraic("E2").offset(-2, 0));
    }
}
