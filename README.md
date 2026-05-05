#  Flood-It Pro

> A feature-rich Java Swing implementation of the classic Flood-It puzzle game — enhanced with BFS flood fill, Divide & Conquer color analysis, and a Greedy bot opponent.

---

## What is Flood-It?

Starting from the **top-left corner**, you select colors to "flood" the board outward. Each turn, your region expands to absorb all adjacent cells of the chosen color. The goal: make the **entire grid a single color** in as few moves as possible.



---

## Features

- **Configurable board** — grid size from 4×4 to 14×14, and 2–6 colors
- **Player vs Bot mode** — compete against a greedy AI that alternates turns with you
- **Hint system** — suggests the best color move using the same algorithm as the bot
- **Undo / Redo** — full move history with unlimited steps
- **Progress tracking** — live move count and percentage of the board flooded
- **Win detection** — alerts when the board is fully flooded

---

## Getting Started

**Requirements:** Java 8 or later · No external dependencies

```bash
# Compile
javac FloodItGame.java

# Run
java FloodItGame

# Or do both at once
javac FloodItGame.java && java FloodItGame
```

---

## Controls

| Action | How |
|--------|-----|
| Select a color | Click any cell of that color on the board |
| Undo | Click **Undo** |
| Redo | Click **Redo** |
| Get a hint | Click **Hint** — suggestion appears in the status bar |
| New game | Click **New Game** |
| Change settings | Adjust the **Mode**, **Size**, or **Colors** dropdowns before starting |

---

## Configuration

All settings are adjustable in the toolbar at runtime:

| Setting | Range | Default |
|---------|-------|---------|
| Grid size | 4×4 – 14×14 | 10×10 |
| Number of colors | 2–6 | 6 |
| Game mode | Single Player / Player vs Bot | Single Player |

> **Tip:** Cell size is fixed at 40×40 px. To change it, update the `CELL` constant in the source.

---

## Algorithms

| Component | Algorithm | How it's used |
|-----------|-----------|---------------|
| Flood fill | BFS (Breadth-First Search) | Expands the player's color region each turn |
| Color frequency | Divide & Conquer | Recursively splits the grid to count color distribution |
| Bot move selection | Greedy | Picks the neighbor color with the highest global frequency |

### How the bot works

1. **BFS** identifies all cells currently in the player's region.
2. **Divide & Conquer** counts the frequency of every color across the full grid.
3. The bot finds which colors border the current region.
4. It **greedily picks** the neighboring color with the largest global count — maximizing board coverage per move.

---

## Project Structure

```
FloodItGame.java
│
├── FloodItGame            Main JFrame — game logic, toolbar, state management
│   ├── newGame()          Initializes or resets the grid
│   ├── floodFill()        BFS flood from (0,0) to the selected color
│   ├── getBestColor()     Greedy hint / bot move selection
│   ├── countGlobalDC()    Divide & Conquer color frequency counter
│   ├── markRegionBFS()    BFS to identify the current flooded region
│   └── undo / redo        Stack-based move history
│
└── Board                  Inner JPanel — renders the grid, handles mouse clicks
```

---

## License

MIT — free to use, modify, and distribute.
