package uk.ac.soton.comp1206.component;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.event.BlockClickedListener;
import uk.ac.soton.comp1206.game.Grid;

import java.util.Set;

/**
 * A GameBoard is a visual component to represent the visual GameBoard.
 * It extends a GridPane to hold a grid of GameBlocks.
 * <p>
 * The GameBoard can hold an internal grid of it's own, for example, for displaying an upcoming block. It also be
 * linked to an external grid, for the main game board.
 * <p>
 * The GameBoard is only a visual representation and should not contain game logic or model logic in it, which should
 * take place in the Grid.
 */
public class GameBoard extends GridPane {

    private static final Logger logger = LogManager.getLogger(GameBoard.class);

    /**
     * Number of columns in the board
     */
    private final int cols;

    /**
     * Number of rows in the board
     */
    private final int rows;

    /**
     * The visual width of the board - has to be specified due to being a Canvas
     */
    private final double width;

    /**
     * The visual height of the board - has to be specified due to being a Canvas
     */
    private final double height;

    /**
     * The grid this GameBoard represents
     */
    protected final Grid grid;

    /**
     * The blocks inside the grid
     */
     protected GameBlock[][] blocks;

    /**
     * The listener to call when a specific block is clicked
     */
    private BlockClickedListener blockClickedListener;


    /**
     * Create a new GameBoard, based off a given grid, with a visual width and height.
     * @param grid linked grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(Grid grid, double width, double height) {
        this.cols = grid.getCols();
        this.rows = grid.getRows();
        this.width = width;
        this.height = height;
        this.grid = grid;

        // Build the GameBoard
        build();
    }

    /**
     * Create a new GameBoard with it's own internal grid, specifying the number of columns and rows, along with the
     * visual width and height.
     *
     * @param cols number of columns for internal grid
     * @param rows number of rows for internal grid
     * @param width the visual width
     * @param height the visual height
     */
    public GameBoard(int cols, int rows, double width, double height) {
        this.cols = cols;
        this.rows = rows;
        this.width = width;
        this.height = height;
        this.grid = new Grid(cols, rows);

        // Build the GameBoard
        build();
    }

    /**
     * Get a specific block from the GameBoard, specified by it's row and column
     * @param x column
     * @param y row
     * @return game block at the given column and row
     */
    public GameBlock getBlock(int x, int y) {
        return blocks[x][y];
    }

    /**
     * Build the GameBoard by creating a block at every x and y column and row
     */
    protected void build() {
        // logger.info("Building grid: {} x {}", this.getCols(), this.getRows());

        setMaxWidth(width);
        setMaxHeight(height);

        setGridLinesVisible(true);

        blocks = new GameBlock[cols][rows];

        for(var y = 0; y < this.getRows(); y++) {
            for (var x = 0; x < this.getCols(); x++) {
                createBlock(x, y);
            }
        }
    }

    /**
     * Create a block at the given x and y position in the GameBoard
     *
     * @param x column
     * @param y row
     */
    protected void createBlock(int x, int y) {
        var blockWidth = width / this.getCols();
        var blockHeight = height / this.getRows();

        // Create a new GameBlock UI component
        GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

        // Add to the GridPane
        this.add(block, x, y);

        // Add to our block directory
        blocks[x][y] = block;

        // Link the GameBlock component to the corresponding value in the Grid
        block.bind(grid.getGridProperty(x,y));

        // Add a mouse click handler to the block to trigger GameBoard blockClicked method
        block.setOnMouseClicked((e) -> blockClicked(e, block));

    }

    /**
     * Set the listener to handle an event when a block is clicked
     * @param listener listener to add
     */
    public void setOnBlockClick(BlockClickedListener listener) {
        this.blockClickedListener = listener;
    }

    /**
     * Triggered when a block is clicked. Call the attached listener.
     * @param event the mouse event
     * @param block block clicked on
     */
    private void blockClicked(MouseEvent event, GameBlock block) {
        MouseButton button = event.getButton();

        switch (button) {
            case PRIMARY -> {
                if (blockClickedListener != null) {
                    blockClickedListener.blockClicked(block);
                }
            }
        }


    }

    /**
     * Fades out the blocks in the given game block coordinates
     * @param blocks the set of blocks to fade out
     */
    public void fadeOut(Set<GameBlockCoordinate> blocks) {
        for (GameBlockCoordinate coordinate : blocks) {
            GameBlock block = getBlock(coordinate.getX(), coordinate.getY());
            if (block != null) block.fadeOut();
        }
    }

    protected int getCols() {
        return this.cols;
    }

    protected int getRows() {
        return this.rows;
    }

    /**
     * Transforms the board into a string for debugging
     * @return the board string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");

        for (int y=0; y < getRows(); y++) {
            for (int x=0; x < getCols(); x++) {
                GameBlock currentBlock = this.getBlock(x, y);
                sb.append(String.format("%" + 3 + "d", currentBlock.getValue()));
            }

            sb.append("\n");
        }

        return sb.toString();
    }

}
