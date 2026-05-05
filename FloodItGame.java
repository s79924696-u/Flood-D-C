import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
public class FloodItGame extends JFrame {
    private final Color[] ALL_COLORS = {
            Color.RED, Color.GREEN, Color.BLUE,
            Color.YELLOW, Color.MAGENTA, Color.ORANGE
    };
    private final String[] COLOR_NAMES = {
            "Red", "Green", "Blue", "Yellow", "Magenta", "Orange"
    };
    private int SIZE = 10;
    private int COLOR_COUNT = 6;
    private final int CELL = 40;
    private int[][] grid;
    private int moves = 0;
    private boolean vsBot = false;
    private boolean playerTurn = true;
    private boolean gameOver = false;
    private Stack<int[][]> undo = new Stack<>();
    private Stack<int[][]> redo = new Stack<>();
    private Board board;
    private JLabel movesLabel;
    private JLabel hintLabel;
    private JComboBox<String> modeBox;
    private JComboBox<Integer> sizeBox;
    private JComboBox<Integer> colorBox;
    private JButton undoBtn, redoBtn;

    public FloodItGame() {
        setTitle("Flood-It Pro (BFS + D&C + Greedy)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        createToolbar();
        createBoard();
        createStatusPanel();
        newGame();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    private void createToolbar() {
        JPanel toolbar = new JPanel();
        modeBox = new JComboBox<>(new String[]{"Single Player", "Player vs Bot"});
        sizeBox = new JComboBox<>();
        colorBox = new JComboBox<>();
        for (int i = 4; i <= 14; i++) sizeBox.addItem(i);
        for (int i = 2; i <= 6; i++) colorBox.addItem(i);
        JButton newBtn = new JButton("New Game");
        undoBtn = new JButton("Undo");
        redoBtn = new JButton("Redo");
        JButton hintBtn = new JButton("Hint");
        newBtn.addActionListener(e -> newGame());
        undoBtn.addActionListener(e -> undoMove());
        redoBtn.addActionListener(e -> redoMove());
        hintBtn.addActionListener(e -> showHint());
        toolbar.add(new JLabel("Mode:"));
        toolbar.add(modeBox);
        toolbar.add(new JLabel("Size:"));
        toolbar.add(sizeBox);
        toolbar.add(new JLabel("Colors:"));
        toolbar.add(colorBox);
        toolbar.add(newBtn);
        toolbar.add(undoBtn);
        toolbar.add(redoBtn);
        toolbar.add(hintBtn);
        add(toolbar, BorderLayout.NORTH);
    }
    private void createBoard() {
        board = new Board();
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.add(board);
        add(wrapper, BorderLayout.CENTER);
    }
    private void createStatusPanel() {
        JPanel status = new JPanel(new BorderLayout());
        movesLabel = new JLabel("", JLabel.CENTER);
        movesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        hintLabel = new JLabel(" ", JLabel.CENTER);

        status.add(movesLabel, BorderLayout.CENTER);
        status.add(hintLabel, BorderLayout.SOUTH);
        add(status, BorderLayout.SOUTH);
    }
    private void newGame() {
        SIZE = (Integer) sizeBox.getSelectedItem();
        COLOR_COUNT = (Integer) colorBox.getSelectedItem();
        vsBot = modeBox.getSelectedIndex() == 1;
        grid = new int[SIZE][SIZE];
        Random r = new Random();

        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                grid[i][j] = r.nextInt(COLOR_COUNT);
        moves = 0;
        gameOver = false;
        playerTurn = true;
        undo.clear();
        redo.clear();
        undoBtn.setEnabled(false);
        redoBtn.setEnabled(false);

        board.setPreferredSize(new Dimension(SIZE * CELL, SIZE * CELL));
        pack();
        updateStatus();
        board.repaint();
    }
    private void showHint() {
        if (gameOver) return;
        int best = getBestColor();
        if (best != -1)
            hintLabel.setText("Hint: Try " + COLOR_NAMES[best]);
        else
            hintLabel.setText("No good move found.");
    }
    private void botMove() {
        if (!vsBot || gameOver) return;

        int best = getBestColor();
        if (best != -1) {
            floodFill(best);
            board.repaint();
        }
        playerTurn = true;
    }
    private int getBestColor() {
        int oldColor = grid[0][0];
        boolean[][] region = new boolean[SIZE][SIZE];
        markRegionBFS(0, 0, oldColor, region);

        int[] globalCounts = countGlobalDC(0, SIZE - 1, 0, SIZE - 1);
        boolean[] neighbors = getNeighborColors(region);

        return pickBestColor(globalCounts, neighbors, oldColor);
    }
    private int[] countGlobalDC(int r1, int r2, int c1, int c2) {
        int[] result = new int[COLOR_COUNT];
        if (r1 > r2 || c1 > c2) return result;

        if (r1 == r2 && c1 == c2) {
            result[grid[r1][c1]]++;
            return result;
        }
        int midR = (r1 + r2) / 2;
        int midC = (c1 + c2) / 2;
        int[] q1 = countGlobalDC(r1, midR, c1, midC);
        int[] q2 = countGlobalDC(r1, midR, midC + 1, c2);
        int[] q3 = countGlobalDC(midR + 1, r2, c1, midC);
        int[] q4 = countGlobalDC(midR + 1, r2, midC + 1, c2);
        for (int i = 0; i < COLOR_COUNT; i++)
            result[i] = q1[i] + q2[i] + q3[i] + q4[i];
        return result;
    }
    private void markRegionBFS(int i, int j, int color, boolean[][] region) {
        Queue<Point> q = new LinkedList<>();
        q.add(new Point(i, j));
        region[i][j] = true;

        int[][] dir = {{1,0},{-1,0},{0,1},{0,-1}};
        while (!q.isEmpty()) {
            Point p = q.poll();
            for (int[] d : dir) {
                int ni = p.x + d[0];
                int nj = p.y + d[1];
                if (ni >= 0 && nj >= 0 && ni < SIZE && nj < SIZE
                        && !region[ni][nj]
                        && grid[ni][nj] == color) {

                    region[ni][nj] = true;
                    q.add(new Point(ni, nj));
                }
            }
        }
    }
    private boolean[] getNeighborColors(boolean[][] region) {
        boolean[] neighbor = new boolean[COLOR_COUNT];
        int[][] dir = {{1,0},{-1,0},{0,1},{0,-1}};

        for (int i = 0; i < SIZE; i++)
            for (int j = 0; j < SIZE; j++)
                if (region[i][j])
                    for (int[] d : dir) {
                        int ni = i + d[0];
                        int nj = j + d[1];
                        if (ni >= 0 && nj >= 0 && ni < SIZE && nj < SIZE
                                && !region[ni][nj])
                            neighbor[grid[ni][nj]] = true;
                    }
        return neighbor;
    }

    private int pickBestColor(int[] counts, boolean[] neighbors, int currentColor) {
        int best = -1, max = -1;

        for (int i = 0; i < COLOR_COUNT; i++)
            if (i != currentColor && neighbors[i] && counts[i] > max) {
                max = counts[i];
                best = i;
            }
        if (best != -1) return best;

        for (int i = 0; i < COLOR_COUNT; i++)
            if (i != currentColor && counts[i] > max) {
                max = counts[i];
                best = i;
            }
        return best;
    }
    private void floodFill(int color) {
        int old = grid[0][0];
        if (old == color || gameOver) return;

        saveState();
        boolean[][] visited = new boolean[SIZE][SIZE];
        Queue<Point> q = new LinkedList<>();
        q.add(new Point(0, 0));
        visited[0][0] = true;

        int[][] dir = {{1,0},{-1,0},{0,1},{0,-1}};

        while (!q.isEmpty()) {
            Point p = q.poll();
            grid[p.x][p.y] = color;

            for (int[] d : dir) {
                int ni = p.x + d[0];
                int nj = p.y + d[1];

                if (ni >= 0 && nj >= 0 && ni < SIZE && nj < SIZE
                        && !visited[ni][nj]
                        && grid[ni][nj] == old) {

                    visited[ni][nj] = true;
                    q.add(new Point(ni, nj));
                }
            }
        }
        moves++;
        updateStatus();
        checkEnd();
    }
    private void checkEnd() {
        int c = grid[0][0];
        for (int[] row : grid)
            for (int x : row)
                if (x != c) return;

        gameOver = true;
        JOptionPane.showMessageDialog(this,
                "Game finished in " + moves + " moves!");
    }

    private void updateStatus() {
        movesLabel.setText("Moves: " + moves +
                " | Flooded: " + calculateFlooded() + "%");
    }

    private int calculateFlooded() {
        int count = 0;
        int color = grid[0][0];

        for (int[] row : grid)
            for (int x : row)
                if (x == color) count++;

        return count * 100 / (SIZE * SIZE);
    }

    private void saveState() {
        undo.push(copyGrid());
        redo.clear();
        undoBtn.setEnabled(true);
        redoBtn.setEnabled(false);
    }
    private void undoMove() {
        if (!undo.isEmpty() && !gameOver) {
            redo.push(copyGrid());
            grid = undo.pop();
            moves--;
            updateStatus();
            board.repaint();
            undoBtn.setEnabled(!undo.isEmpty());
            redoBtn.setEnabled(true);
        }
    }

    private void redoMove() {
        if (!redo.isEmpty() && !gameOver) {
            undo.push(copyGrid());
            grid = redo.pop();
            moves++;
            updateStatus();
            board.repaint();
            redoBtn.setEnabled(!redo.isEmpty());
            undoBtn.setEnabled(true);
        }
    }

    private int[][] copyGrid() {
        int[][] copy = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++)
            copy[i] = Arrays.copyOf(grid[i], SIZE);
        return copy;
    }

    private class Board extends JPanel {
        Board() {
            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (gameOver) return;
                    if (vsBot && !playerTurn) return;

                    int row = e.getY() / CELL;
                    int col = e.getX() / CELL;

                    if (row >= SIZE || col >= SIZE) return;

                    int clickedColor = grid[row][col];

                    if (clickedColor != grid[0][0]) {
                        floodFill(clickedColor);
                        repaint();

                        if (vsBot && !gameOver) {
                            playerTurn = false;
                            botMove();
                        }
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int i = 0; i < SIZE; i++)
                for (int j = 0; j < SIZE; j++) {
                    g.setColor(ALL_COLORS[grid[i][j]]);
                    g.fillRect(j * CELL, i * CELL, CELL, CELL);
                    g.setColor(Color.BLACK);
                    g.drawRect(j * CELL, i * CELL, CELL, CELL);
                }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FloodItGame::new);
    }

}

