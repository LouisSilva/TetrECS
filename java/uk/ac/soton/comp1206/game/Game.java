package uk.ac.soton.comp1206.game;

import java.util.HashSet;
import java.util.Random;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.event.RotatePieceListener;
import uk.ac.soton.comp1206.event.SwapPieceListener;

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
     * The next piece
     */
    private GamePiece followingPiece;

    public final IntegerProperty score;
    public final IntegerProperty level;
    public final IntegerProperty lives;
    public final IntegerProperty multiplier;

    private NextPieceListener nextPieceListener;
    private RotatePieceListener rotatePieceListener;
    private SwapPieceListener swapPieceListener;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        this.score = new SimpleIntegerProperty(this, "score", 0);
        this.level = new SimpleIntegerProperty(this, "level", 0);
        this.lives = new SimpleIntegerProperty(this, "lives", 3);
        this.multiplier = new SimpleIntegerProperty(this, "multiplier", 1);

        // Create a new grid model to represent the game state
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
        this.followingPiece = this.spawnPiece();
    }

    public void exitGame() {
        logger.info("Exiting game");
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
            this.nextPiece();
            logger.debug("Played piece");
            this.afterPiecePlayed();
        }

        logger.debug(grid.toString());
        logger.debug("Current piece: " + this.getCurrentPiece().toGridString());
    }

    private void afterPiecePlayed() {
        HashSet<GameBlockCoordinate> blocksToClear = new HashSet<>();
        int numOfLinesCleared = 0;

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
                numOfLinesCleared++;
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
                numOfLinesCleared++;
                for (int row=0; row < getRows(); row++) {
                    blocksToClear.add(new GameBlockCoordinate(col, row));
                }
            }
        }

        for (GameBlockCoordinate block : blocksToClear) {
            grid.updateGridValue(block.getX(), block.getY(), 0);
            logger.debug("Cleared block with coords (x,y): (" + block.getX() + "," + block.getY() + ")");
        }

        // Update score and level
        if (numOfLinesCleared >= 1 && !blocksToClear.isEmpty()) {
            this.calculateNewScore(numOfLinesCleared, blocksToClear.size());
            this.calculateNewLevel();
        }

        // Update multiplier
        this.calculateNewMultiplier(numOfLinesCleared >= 1);
    }

    /**
     * Calculates the new score after a line is cleared
     * @param numOfLines the number of lines cleared
     * @param numOfBlocks the number of blocks cleared
     */
    private void calculateNewScore(int numOfLines, int numOfBlocks) {
        score.add(numOfLines * numOfBlocks * 10 * this.multiplier.get());
    }

    /**
     * Calculates the new multiplier after a game piece is played
     * @param increase whether to increase the multiplier
     */
    private void calculateNewMultiplier(boolean increase) {
        if (increase) {
            multiplier.add(1);
        } else {
            multiplier.set(1);
        }
    }

    /**
     * Calculates the level after a game piece is played
     */
    private void calculateNewLevel() {
        if (this.score.get() <= 999) {
            this.level.set(1);
        }
        else {
            this.level.set(((this.score.get() / 1000) * 1000) / 1000);
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
     * Changes the current piece and following piece variables to the next piece
     */
    private void nextPiece() {
        this.currentPiece = this.followingPiece;
        this.followingPiece = this.spawnPiece();

        this.nextPieceListener.nextPiece(this.currentPiece, this.followingPiece);
    }

    /**
     * Rotates the current piece
     */
    public void rotateCurrentPiece() {
        this.getCurrentPiece().rotate();
        if (this.rotatePieceListener != null) {
            rotatePieceListener.rotatePiece(this.currentPiece);
        }
    }

    public void rotateCurrentPiece(int rotations) {
        for (int i=0; i < rotations; i++) {
            this.rotateCurrentPiece();
        }
    }

    public void swapCurrentPiece() {
        GamePiece tempPiece = this.currentPiece;
        this.currentPiece = this.followingPiece;
        this.followingPiece = tempPiece;

        this.swapPieceListener.swapPiece(this.currentPiece, this.followingPiece);
    }

    public void dropCurrentPiece() {
        this.nextPiece();
    }

    public void setNextPieceListener(NextPieceListener listener) {
        this.nextPieceListener = listener;
    }

    public void setRotatePieceListener(RotatePieceListener listener) {
        this.rotatePieceListener = listener;
    }

    public void setSwapPieceListener(SwapPieceListener listener) {
        this.swapPieceListener = listener;
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
