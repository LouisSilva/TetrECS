package uk.ac.soton.comp1206.game;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.*;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    /**
     * The logger used for debugging
     */
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
    protected GamePiece currentPiece;

    /**
     * The next piece
     */
    protected GamePiece followingPiece;

    /**
     * The score
     */
    public final IntegerProperty score;

    /**
     * The level
     */
    public final IntegerProperty level;

    /**
     * The lives the player has left in the game
     */
    public final IntegerProperty lives;

    /**
     * The score multiplier
     */
    public final IntegerProperty multiplier;

    /**
     * The next piece listener for the ui GameBoards
     */
    protected NextPieceListener nextPieceListener;

    /**
     * The rotate piece listener for the ui GameBoards
     */
    private RotatePieceListener rotatePieceListener;

    /**
     * The game loop listener for the ui timer
     */
    private GameLoopListener gameLoopListener;

    /**
     * The line cleared listener for the ui game board
     */
    private LineClearedListener lineClearedListener;

    /**
     * The end game listener for the challenge scene
     */
    private EndGameListener endGameListener;

    /**
     * A listener for playing audio in the scene
     */
    private PlayAudioListener playAudioListener;

    /**
     * The scheduler which is used as a timer for the game loop
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * The game loop future which is used for resetting and shutting down the timer properly
     */
    private Future<?> gameLoopFuture;

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
        this.grid = new Grid(cols, rows);
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

        this.score.set(0);
        this.level.set(0);
        this.lives.set(3);
        this.multiplier.set(1);
        this.grid.clearGrid();

        this.currentPiece = this.spawnPiece();
        this.followingPiece = this.spawnPiece();
        this.nextPieceListener.nextPiece(this.currentPiece, this.followingPiece);

        this.startGameLoop();
    }

    /**
     * Starts the game loop
     */
    private void startGameLoop() {
        if (gameLoopFuture != null && !gameLoopFuture.isDone()) {
            gameLoopFuture.cancel(true);
        }

        // Start game loop
        Runnable gameLoopTask = () -> {
            if (Thread.currentThread().isInterrupted()) return;
            gameLoop();
        };

        long timerDelay = getTimerDelay();
        gameLoopFuture = scheduler.scheduleAtFixedRate(gameLoopTask, timerDelay, timerDelay, TimeUnit.MILLISECONDS);

        // Notify listener
        if (this.gameLoopListener != null)
            gameLoopListener.onGameLoop();
    }

    /**
     * Handles what happens when the game loop timer goes to zero
     */
    private void gameLoop() {
        // Handle the game loop stuff in the fx thread
        Platform.runLater(() -> {
            logger.info("Game Loop!");
            this.loseLife();

            // End game if all lives are gone
            if (lives.get() < 0) {
                this.endGame();
            }

            else {
                this.nextPiece();
                this.multiplier.set(1);
                this.resetTimer();
            }
        });
    }

    /**
     * Resets the game loop timer
     */
    private void resetTimer() {
        if (gameLoopFuture != null) {
            gameLoopFuture.cancel(true);
        }

        this.startGameLoop();
    }

    /**
     * Shuts down the game loop properly
     */
    private void shutdownGameLoop() {
        if (gameLoopFuture != null) {
            gameLoopFuture.cancel(true);
        }

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    /**
     * Ends the game properly
     * Its protected so it can be overridden
     */
    protected void endGame() {
        this.shutdownGameLoop();
        if (this.endGameListener != null) this.endGameListener.endGame(this);
        else this.start();
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
            this.handlePlayPiece();
        }
        else if (this.playAudioListener != null){
            this.playAudioListener.playAudio("fail.wav");
        }
    }

    /**
     * Handle what should happen when a piece was successfully played.
     * This is in a separate method so it can be overridden
     */
    protected void handlePlayPiece() {
        if (this.playAudioListener != null) this.playAudioListener.playAudio("place.wav");
        this.nextPiece();
        this.resetTimer();
        this.gameLoopListener.onGameLoop();
        this.afterPiecePlayed();
    }

    /**
     * Calculates whether line/s should be cleared, and the resulting score and multiplier after
     */
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
        }

        // Update score, level and call line cleared listener
        if (numOfLinesCleared >= 1 && !blocksToClear.isEmpty()) {
            this.calculateNewScore(numOfLinesCleared, blocksToClear.size());
            this.calculateNewLevel();
            lineClearedListener.onLineCleared(blocksToClear);
        }

        // Update multiplier
        this.calculateNewMultiplier(numOfLinesCleared >= 1);
    }

    /**
     * Calculates the new score after a line is cleared
     * @param numOfLines the number of lines cleared
     * @param numOfBlocks the number of blocks cleared
     */
    protected void calculateNewScore(int numOfLines, int numOfBlocks) {
        int addedScore = numOfLines * numOfBlocks * 10 * this.multiplier.get();
        this.score.set(this.score.get() + addedScore);
    }

    /**
     * Calculates the new multiplier after a game piece is played
     * @param increase whether to increase the multiplier
     */
    private void calculateNewMultiplier(boolean increase) {
        if (increase) {
            this.multiplier.set(this.multiplier.get() + 1);
        } else {
            this.multiplier.set(1);
        }
    }

    /**
     * Calculates the level after a game piece is played
     */
    private void calculateNewLevel() {
        int oldLevel = this.level.get();
        this.level.set(((this.score.get() / 1000) * 1000) / 1000);
        if (oldLevel < this.level.get() && this.playAudioListener != null) {
            this.playAudioListener.playAudio("level.wav");
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
    protected void nextPiece() {
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

    /**
     * Performs the given number of rotations on the current piece
     * @param rotations number of rotations to complete
     */
    public void rotateCurrentPiece(int rotations) {
        for (int i=0; i < rotations; i++) {
            this.rotateCurrentPiece();
        }
    }

    /**
     * Swaps the current piece with the following piece
     */
    public void swapCurrentPiece() {
        GamePiece tempPiece = this.currentPiece;
        this.currentPiece = this.followingPiece;
        this.followingPiece = tempPiece;

        if (this.nextPieceListener != null) this.nextPieceListener.nextPiece(this.currentPiece, this.followingPiece);
    }

    /**
     * Decrements the lives by one
     * Protected method so it can be overridden
     */
    protected void loseLife() {
        lives.set(lives.get() - 1);
        if (this.playAudioListener != null) this.playAudioListener.playAudio("lifelose.wav");
    }

    /**
     * A public method for accessing the game's nextPiece method
     */
    public void dropCurrentPiece() {
        this.nextPiece();
    }

    /**
     * Sets the given NextPieceListener
     * @param listener the NextPieceListener instance
     */
    public void setNextPieceListener(NextPieceListener listener) {
        this.nextPieceListener = listener;
    }

    /**
     * Sets the given RotatePieceListener
     * @param listener the RotatePieceListener instance
     */
    public void setRotatePieceListener(RotatePieceListener listener) {
        this.rotatePieceListener = listener;
    }

    /**
     * Sets the given GameLoopListener
     * @param listener the GameLoopListener instance
     */
    public void setGameLoopListener(GameLoopListener listener) {
        this.gameLoopListener = listener;
    }

    /**
     * Sets the given LineClearedListener
     * @param listener the LineClearedListener instance
     */
    public void setLineClearedListener(LineClearedListener listener) {
        this.lineClearedListener = listener;
    }

    /**
     * Sets the given EndGameListener
     * @param listener the EndGameListener instance
     */
    public void setEndGameListener(EndGameListener listener) {
        this.endGameListener = listener;
    }

    /**
     * Sets the given PlayAudioListener
     * @param listener the PlayAudioListener instance
     */
    public void setPlayAudioListener(PlayAudioListener listener) {
        this.playAudioListener = listener;
    }
    /**
     * Calculates the timer delay based on the current level of the game
     * @return the timer delay in milliseconds
     */
    public long getTimerDelay() {
        //return 1000;
        return Math.max(2500, 12000 - 500 * this.level.get());
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

    /**
     * Public method that a scene can call to end the game.
     *
     */
    public void remoteEndGame() {
        this.shutdownGameLoop();
        if (this.endGameListener != null) this.endGameListener.endGame(this);
    }
}
