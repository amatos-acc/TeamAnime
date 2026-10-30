# TeamAnime — Console Chess (Simplified Prototype)

Class: CS 3354
Team Members: Alexander Matos

## Purpose
A two-player chess game played in the terminal. Players type moves like `E2 E4`; the program shows the board, enforces each piece's basic movement and capture rules, rejects invalid input with a clear message, and alternates turns. The core logic (`chess.model`, `chess.pieces`, `chess.service`) has no console code, so a Phase 2 GUI can reuse it.

Intended users: Anyone who knows how to play chess or anyone who is learning. Can be played for one to two players through the terminal window. Good for learning the rules of the game because it rejects invalid moves and explains why they are rejected.

## Requirements
- Java **17 or newer** (JDK 26)
- macOS/Linux shell for the scripts, or the plain `javac`/`java` commands below

## Entry points
| Class | Purpose |
|---|---|
| `chess.console.ChessApp` | Interactive game (main entry point) |
| `chess.console.DemoDriver` | Non-interactive driver that runs labeled test scenarios and prints PASS/FAIL |

## Build and run
```bash
./scripts/build.sh                 # compile to out/
./scripts/run.sh                   # play  (optional: ./scripts/run.sh Alice Bob)
./scripts/demo.sh                  # run the labeled test scenarios
./scripts/javadoc.sh               # generate docs/api/index.html
./scripts/count_loc.sh             # count nonblank, non-comment lines
```
Without the scripts:
```bash
javac -d out $(find src -name '*.java')
java -cp out chess.console.ChessApp
```

## Commands
| Input | Effect |
|---|---|
| `E2 E4` | Move the piece on E2 to E4 (captures detected automatically) |
| `moves E2` | List squares the piece on E2 can move to |
| `captured` / `history` / `board` | Show captured pieces / move list / board |
| `reset` | Start a new game |
| `help` | List commands |
| `quit` or `exit` | Exit |

## Sample session
```
White to move > E2 E4 
Pawn E2 -> E4.
Black to move > D7 D4
Illegal move: A Pawn cannot move from D7 to D4.
Black to move > D7 D6
Pawn D7 -> D6.
White to move > G8 F6
Illegal move: The Knight on G8 belongs to Black. It is White's turn.
```

## Completed features
### Gameplay
- The board is printed following the layout of the assignment. Empty squares appear as two empty spaces instead of `##` because the board looks less busy.
- Movement rules for all six pieces:
  - Pawn: one step, two from its starting square, and diagonal capture.
  - Rook, Bishop and Queen: they slide and are blocked by pieces in the way.
  - Knight: L-shape, jumping over pieces.
  - King: one square.
- Captures are detected automatically when you move onto an enemy piece.
- White and Black alternate turns, starting with White.
- Capturing the opposing king wins the game.
- A pawn reaching the last rank is promoted to a queen automatically.

### Input and validation
- Moves are typed as E2 E4. Input is case-insensitive and extra spaces are ignored.
- Invalid moves are rejected with a specific reason and the board is left unchanged. Reasons include an empty square, the wrong player's piece, your own piece on the destination, a pattern the piece can't make, or a blocked path.
- Malformed input is rejected with a usage hint. Examples: `Z9`, `E2`, `E2 E4 E5`.

### Commands
- `moves <square>`: lists where a piece can go.
- `captured`: shows the pieces each side has lost.
- `history`: shows the numbered move list.
- `board`: prints the board again.
- `reset`: starts a new game.
- `help`: shows the commands.
- `quit / exit`: leaves the game.

### Testing
- ./scripts/demo.sh runs 117 automated scenario checks.

## Board coordinates
Row 0 = rank 8, column 0 = file A. Example: `E2` → `[6][4]`. See `Position` Javadoc.

## Known limitations (simplified prototype)
- No check, checkmate, or stalemate detection; moves that expose your own king are allowed.
- Since check isn't enforced, **capturing the opposing king ends the game**.
- No castling or en passant.
- **Pawn promotion:** a pawn reaching the last rank is automatically promoted to a queen; the player cannot choose a rook, bishop, or knight instead.
- No draw detection (50-move rule, repetition, insufficient material).
- **Known issue:** after the game ends, a rejected move still prints "It is still White's turn." (or Black's). The move is correctly refused; only the message is misleading. The line comes from `ConsoleUI.handleMove`, which prints it after every rejected move without checking whether the game is over.

## Project structure
```
src/chess/pieces   Piece (abstract), Pawn, Rook, Knight, Bishop, Queen, King
src/chess/model    Board, Position, Player, PieceColor
src/chess/service  Game, MoveResult
src/chess/console  ChessApp, ConsoleUI, InputParser, Command, DemoDriver
docs/api           generated Javadoc (index.html)
docs/uml.md        UML class diagram source
```

## Author
Alexander Matos. Repository: https://github.com/amatos-acc/TeamAnime
