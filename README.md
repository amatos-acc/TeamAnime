# TeamAnime — Console Chess (Simplified Prototype)

Class: CS 3354
Team Members: Alexander Matos

> TODO(README): Replace every _italic placeholder_ once your implementation is finished. The README must describe the version you actually submit.

## Purpose
A two-player chess game played in the terminal. Players type moves like `E2 E4`; the program shows the board, enforces each piece's basic movement and capture rules, rejects invalid input with a clear message, and alternates turns. The core logic (`chess.model`, `chess.pieces`, `chess.service`) has no console code, so a Phase 2 GUI can reuse it.

_Intended users: who is this for?_

## Requirements
- Java **17 or newer** (developed with _your JDK version_)
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
White to move > E7 E5        (rejected: Black's piece; still White's turn)
White to move > E2 E4
Black to move > Z9 E4        (rejected: not on the board)
Black to move > E7 E5
White to move > G1 F3
Black to move > quit
```
_Paste a short real transcript here once displayBoard is implemented._

## Completed features
- _List what you finished, e.g. board display, all six piece movement rules, captures, alternating turns, input validation, move hints, history, captured list, reset, king-capture win._

## Board coordinates
Row 0 = rank 8, column 0 = file A. Example: `E2` → `[6][4]`. See `Position` Javadoc.

## Known limitations (simplified prototype)
- No check, checkmate, or stalemate detection; moves that expose your own king are allowed.
- Since check isn't enforced, **capturing the opposing king ends the game**.
- No castling or en passant.
- **Pawn promotion:** a pawn reaching the last rank is automatically promoted to a queen; the player cannot choose a rook, bishop, or knight instead.
- No draw detection (50-move rule, repetition, insufficient material).
- _Anything else you know is missing or buggy._

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
