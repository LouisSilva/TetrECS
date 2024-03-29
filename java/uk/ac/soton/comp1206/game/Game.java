package uk.ac.soton.comp1206.game;

import java.util.HashSet;
import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /**
     * The current piece
     */
    private GamePiece currentPiece;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");

        this.currentPiece = this.spawnPiece();
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        // Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        // Play piece if possible
        if (grid.playPiece(this.getCurrentPiece(), x, y)) {
            this.nextPiece(this.spawnPiece());
            logger.debug("Played piece");
            this.afterPiece();
        }

        logger.debug(grid.toString());
        logger.debug("Current piece: " + this.getCurrentPiece().toGridString());
    }

    private void afterPiece() {
        HashSet<GameBlockCoordinate> blocksToClear = new HashSet<>();

        // Check for full rows
        for (int row=0; row < getRows(); row++) {
            boolean fullRow = true;
            for (int col=0; col < getCols(); col++) {
                if (grid.getGridProperty(col, row).get() == 0) {
                    fullRow = false;
                    break;
                }
            }

            if (fullRow) {
                for (int col=0; col < getCols(); col++) {
                    blocksToClear.add(new GameBlockCoordinate(col, row));
                }
            }
        }

        // Check for full columns
        for (int col=0; col < getCols(); col++) {
            boolean fullCol = true;
            for (int row=0; row < getRows(); row++) {
                if (grid.getGridProperty(col, row).get() == 0) {
                    fullCol = false;
                    break;
                }
            }

            if (fullCol) {
                for (int row=0; row < getRows(); row++) {
                    blocksToClear.add(new GameBlockCoordinate(col, row));
                }
            }
        }

        for (GameBlockCoordinate block : blocksToClear) {
            grid.updateGridValue(block.getX(), block.getY(), 0);
            logger.debug("Cleared block with coords (x,y): (" + block.getX() + "," + block.getY() + ")");
        }
    }

    /**
     * Returns a random new game piece object
     * @return new game piece
     */
    private GamePiece spawnPiece() {
        Random random = new Random();
        return GamePiece.createPiece(random.nextInt(0, GamePiece.PIECES));
    }

    /**
     * Changes the current piece variable to the specified piece
     * @param piece the piece to switch to
     */
    private void nextPiece(GamePiece piece) {
        this.currentPiece = piece;
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Get the current game piece
     * @return the current game piece
     */
    public GamePiece getCurrentPiece() {
        return this.currentPiece;
    }
}
