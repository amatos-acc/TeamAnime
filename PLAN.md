# Console Chess — Phase 1 Implementation Plan

This is your roadmap. The scaffold compiles and runs right now. Every place you need to write code is marked `// TODO(n)`, and the numbers give a sensible order. Run `grep -rn "TODO(" src` to list them all.

---

## 1. Architecture at a glance

```
chess.console   ChessApp (main) ─► ConsoleUI ─► InputParser ─► Command
                DemoDriver (labeled test scenarios)
                     │  calls only public methods of Game
                     ▼
chess.service   Game ─► MoveResult            (no System.out here, so it's reusable by the Phase 2 GUI)
                     │
                     ▼
chess.model     Board (Piece[8][8] + captured list), Position, Player, PieceColor
                     │
                     ▼
chess.pieces    Piece (abstract) ◄── Pawn, Rook, Knight, Bishop, Queen, King
```

### Design decisions you should be able to explain (the grader may ask)

| Decision | Why |
|---|---|
| `Piece` is **abstract** with `abstract boolean isValidPattern(Position, Board)` | Each subclass supplies only what makes it different. `Game` calls `mover.isValidPattern(...)` through a `Piece` reference. That's the polymorphism requirement. |
| `canMoveTo()` / `possibleMoves()` live in `Piece` (**Template Method** pattern) | The *order* of checks is shared (not same square → not friendly → pattern → path). Subclasses only fill in the pattern step. |
| `requiresClearPath()` is overridden by `Knight` | Avoids `if (piece instanceof Knight)` in `Game` (open/closed principle: a new piece type needs no change to `Game`). |
| Geometry helpers (`isStraightLine`, `isDiagonal`, `rowDelta`) are `protected` in `Piece` | Shared behavior lives in the superclass. `Queen` reuses both helpers, since Java can't extend both `Rook` and `Bishop`. |
| `Position` is **immutable** and validates in its constructor | An invalid square can't exist, so no other class needs bounds checks. |
| `PieceColor` stores forward direction / start rows | Pawn logic has no `if (WHITE)` branches. |
| `Game.tryMove` returns a `MoveResult` instead of printing | Keeps core logic separate from console I/O (rubric: 3 pts). The GUI can show the same message in a dialog. |
| Validate **everything** before calling `Board.movePiece` | "If an operation fails, the application must preserve its valid state." |
| `Game.start()` is `final` | The constructor calls it. An overridable method called from a constructor is a known pitfall (javac warns `this-escape`). |
| Capturing the king ends the game | Check isn't implemented, so this is the simplest honest win rule. It's documented as a simplification. |

**Row/col mapping** (documented in `Position`): row 0 = rank 8, col 0 = file A → `E2` = row 6, col 4.

---

## 2. Implementation order (your TODOs)

Run `./scripts/demo.sh` after each step. The FAIL count should drop as you go.

| # | File | What | Unlocks |
|---|---|---|---|
| 1 | `model/Position.java` `fromAlgebraic` | Parse `"E2"` → `Position(6,4)`. Inverse of the given `toAlgebraic()`. | Every move and test |
| 2 | `model/Board.java` `placeBackRank` | R N B Q K B N R on the back row | Starting position |
| 15 | `console/InputParser.java` `parseMove` | `"E2 E4"` → `Command.move(...)`. Copy the shape of `parseMovesHint`. | Typing moves |
| 16 | `console/ConsoleUI.java` `displayBoard` | Print the 8×8 grid with `##` for empty | Seeing anything 🙂 |
| 4 | `model/Board.java` `movePiece` | capture → clear source → fill dest → `mover.move(to)` | Board actually changes |
| 5 | `pieces/Piece.java` `isDiagonal` | `|dr| == |dc|` | Bishop / Queen |
| 6–10 | `Bishop`, `Queen`, `Knight` (×2), `King` | One-liners modeled on `Rook` | Five piece types |
| 11 | `pieces/Pawn.java` `isValidPattern` | Single, double (from start rank), diagonal capture | The trickiest piece |
| 3 | `model/Board.java` `isPathClear` | Walk with `Integer.signum` steps | Blocking rules |
| 12, 13 | `service/Game.java` `tryMove` | Friendly-destination check, blocked-path check | Correct error messages |
| 14 | `service/Game.java` `handlePromotion` | **Optional.** Auto-queen. | Nicer last-rank behavior |

Done when `./scripts/demo.sh` prints **`0 failed`**.

---

## 3. Phase 1 requirement → where it's satisfied

| Requirement | Where |
|---|---|
| Working core features | Piece rules, `Board.movePiece`, `Game.tryMove` (your TODOs) |
| Runnable demo: repeated ops + clean exit | `ConsoleUI.run()` loop; `quit`/`exit`; EOF also exits cleanly. Plus `DemoDriver` for labeled scenarios. |
| Input & operation validation, state preserved | `InputParser` never throws; `Game.tryMove` rejects before mutating; DemoDriver `checkStateUnchanged()` proves it |
| Multiple classes, one public class per file, thin `main` | 18 files; `ChessApp.main` is 3 lines |
| Encapsulation | All fields `private`; `Position`/`MoveResult`/`Command` immutable; unmodifiable list getters |
| Inheritance (meaningful) | `Piece` holds color/position/hasMoved + shared helpers; six subclasses |
| Polymorphism through superclass refs | `Game.tryMove` → `mover.isValidPattern`, `mover.requiresClearPath`; `Piece.possibleMoves` → `canMoveTo`; `getSymbol()` in display |
| Abstract class / interface | `abstract class Piece` |
| ≥ 2 packages, logic separate from I/O | 4 packages; only `chess.console` touches `System.in/out` |
| ≥ 500 code lines | `./scripts/count_loc.sh` → ~1,110 now (about 820 without `DemoDriver`); grows as you fill TODOs |
| Javadoc on classes/methods/attributes + HTML | Already written on all scaffold code. **Keep it accurate as you implement.** `./scripts/javadoc.sh` → `docs/api/index.html` |
| GitHub history | See §5 |

---

## 4. Testing and screenshots for the report (§3 "Demonstration and testing")

`./scripts/demo.sh` covers every scenario in the instructions' table plus per-piece boundary cases. For the report, take screenshots of:

1. **Interactive startup**: the starting board and "White to move".
2. **E2 E4, E7 E5**: both accepted, board updates, White to move.
3. **E2 E4, D7 D5, E4 D5**: capture, then `captured` and `history` commands.
4. **A1 A3** rejected (blocked) / **G1 F3** accepted (knight jump).
5. **Invalid ops**: `E2 E5`, `E7 E5`, `E2 E1`, `E3 E4`, `E2 E2`, each with its message and "board is unchanged".
6. **Invalid input**: `Z9 E4`, `E2`, `hello`, then `quit` exits cleanly.
7. **`moves G1`** hint output.
8. **DemoDriver run**: the PASS lines and the `0 failed` summary.

For each one, write a sentence of **expected vs. observed** next to the screenshot. The rubric awards 4 of the 10 points for invalid/boundary evidence.

---

## 5. Suggested commit plan (rubric: "multiple meaningful commits")

```bash
git init && git add . && git commit -m "Add Phase 1 scaffold: packages, Piece hierarchy, Game, console UI"
```
Then commit after each milestone, for example:
1. `Implement Position.fromAlgebraic and move parsing`
2. `Set up back rank and render board in ConsoleUI`
3. `Implement Board.movePiece with capture tracking`
4. `Add Bishop, Queen, Knight, King movement patterns`
5. `Implement pawn single/double step and diagonal capture`
6. `Add path-blocking and friendly-destination validation`
7. `Generate Javadoc and complete README`
8. `Add UML diagram and test evidence`

Push to a GitHub repo and make sure the instructor can open it (public, or add them as a collaborator).

---

## 6. Submission checklist

- [ ] All `TODO(n)` done (`grep -rn "TODO(" src` shows only the optional #14, or nothing)
- [ ] `./scripts/demo.sh` → 0 failed; also tested by hand interactively
- [ ] `./scripts/count_loc.sh` ≥ 500
- [ ] `./scripts/javadoc.sh` → `docs/api/index.html` opens; committed or included in the submission
- [ ] README filled in (purpose, Java version, entry point, run steps, features, limitations, sample input)
- [ ] UML diagram exported from `docs/uml.md` (classes, inheritance, associations, packages)
- [ ] Report (PDF/Word): 1) summary with title/name/purpose/users/features, 2) UML, 3) screenshots + expected/observed, 4) AI disclosure ending with the required statement
- [ ] GitHub link in the report; repo accessible with history visible
- [ ] You can explain: `tryMove`'s check order, why `possibleMoves` is a template method, how `E2` maps to `[6][4]`, why the knight overrides `requiresClearPath`

### AI disclosure note
This scaffold was AI-generated (architecture, Javadoc, scripts, `DemoDriver`, worked examples like `Rook`). The `TODO` bodies are yours. Say so in the report's AI-disclosure section, with a summary of the prompt and how you verified the result.
