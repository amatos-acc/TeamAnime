# UML Class Diagram (source)

Paste the block below into https://mermaid.live, export a PNG/SVG, and put it in your report.
Keep it in sync with the code. If you add or rename methods, update this file.

```mermaid
classDiagram
    direction TB

    namespace chess_pieces {
        class Piece {
            <<abstract>>
            -PieceColor color
            -Position position
            -boolean hasMoved
            #Piece(PieceColor, Position)
            +isValidPattern(Position, Board)* boolean
            +getSymbol()* char
            +getName()* String
            +requiresClearPath() boolean
            +canMoveTo(Position, Board) boolean
            +possibleMoves(Board) List~Position~
            +move(Position) void
            #rowDelta(Position) int
            #colDelta(Position) int
            #isStraightLine(Position) boolean
            #isDiagonal(Position) boolean
            +getDisplaySymbol() String
        }
        class Pawn {
            +isValidPattern(Position, Board) boolean
            +isOnLastRank() boolean
        }
        class Rook
        class Knight {
            +requiresClearPath() boolean
        }
        class Bishop
        class Queen
        class King
    }

    namespace chess_model {
        class Board {
            -Piece[][] grid
            -List~Piece~ capturedPieces
            +initialize() void
            +clear() void
            +placePiece(Piece) void
            +getPiece(Position) Piece
            +isEmpty(Position) boolean
            +isPathClear(Position, Position) boolean
            +movePiece(Position, Position) Piece
            +getCapturedPieces() List~Piece~
            +getActivePieces(PieceColor) List~Piece~
        }
        class Position {
            <<immutable>>
            -int row
            -int col
            +fromAlgebraic(String)$ Position
            +isInBounds(int, int)$ boolean
            +toAlgebraic() String
            +offset(int, int) Position
        }
        class Player {
            -PieceColor color
            -String name
        }
        class PieceColor {
            <<enumeration>>
            WHITE
            BLACK
            +getForwardDirection() int
            +getPawnStartRow() int
            +getBackRankRow() int
            +opposite() PieceColor
        }
    }

    namespace chess_service {
        class Game {
            -Board board
            -Player whitePlayer
            -Player blackPlayer
            -Player currentPlayer
            -List~String~ moveHistory
            -boolean gameOver
            +start() void
            +tryMove(Position, Position) MoveResult
            +end() void
            +getLegalMoves(Position) List~Position~
            -switchTurn() void
            -handlePromotion(Position) String
        }
        class MoveResult {
            <<immutable>>
            -boolean success
            -String message
            -Piece capturedPiece
            +success(String, Piece)$ MoveResult
            +failure(String)$ MoveResult
        }
    }

    namespace chess_console {
        class ChessApp {
            +main(String[])$ void
        }
        class ConsoleUI {
            -Game game
            -InputParser parser
            +run() void
            +displayBoard() void
        }
        class InputParser {
            +parse(String) Command
        }
        class Command {
            -Type type
            -Position from
            -Position to
        }
        class DemoDriver {
            +main(String[])$ void
        }
    }

    Piece <|-- Pawn
    Piece <|-- Rook
    Piece <|-- Knight
    Piece <|-- Bishop
    Piece <|-- Queen
    Piece <|-- King

    Piece --> PieceColor
    Piece --> Position
    Board "1" o-- "0..32" Piece : grid / captured
    Game "1" *-- "1" Board
    Game "1" *-- "2" Player
    Player --> PieceColor
    Game ..> MoveResult : creates
    ChessApp ..> ConsoleUI : creates
    ConsoleUI --> Game
    ConsoleUI --> InputParser
    InputParser ..> Command : creates
    DemoDriver --> Game
    DemoDriver --> InputParser
```
