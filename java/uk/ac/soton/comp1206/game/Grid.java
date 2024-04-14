package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 * <p>
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 * <p>
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 * <p>
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    private static final Logger logger = LogManager.getLogger(Grid.class);

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        // Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        // Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void updateGridValue(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int getGridValue(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
    }

    /**
     * Get whether you can play a specific piece at the given coordinates
     * @param gamePiece gamePiece
     * @param x column
     * @param y row
     * @return whether you can place the piece at the given coordinates or not
     */
    public boolean canPlayPiece(GamePiece gamePiece, int x, int y) {
        int[][] blocks = gamePiece.getBlocks();

        // Iterate through each block of the piece
        for (int xPiece=0; xPiece < blocks.length; xPiece++) {
            for (int yPiece=0; yPiece < blocks[xPiece].length; yPiece++) {

                // Check if there is something already there
                if (blocks[xPiece][yPiece] != 0) {

                    // Calculate the game grid position
                    int xGrid = x + xPiece;
                    int yGrid = y + yPiece;

                    // Check if the position is outside of the game grid bounds
                    if (xGrid < 0 ||xGrid >= cols || yGrid < 0 || yGrid >= rows) return false;

                    // Check if the grid position is not empty
                    if (getGridValue(xGrid, yGrid) != 0) return false;
                }
            }
        }

        return true;
    }

    /**
     * Places a piece at the given coordinates, if possible
     * @param gamePiece gamePiece to be placed
     * @param x column
     * @param y row
     * @return whether the operation was successful
     */
    public boolean playPiece(GamePiece gamePiece, int x, int y) {
        // Get centre offsets and apply them
        x -= gamePiece.getBlocks().length / 2;
        y -= gamePiece.getBlocks()[0].length / 2;

        // Check if the piece can be played
        if (canPlayPiece(gamePiece, x, y)) {
            int[][] blocks = gamePiece.getBlocks();

            for (int xPiece = 0; xPiece < blocks.length; xPiece++) {
                for (int yPiece = 0; yPiece < blocks[xPiece].length; yPiece++) {

                    // Checks if the current block is part of the piece to be played, and if so, update the grid to fill the block in
                    if (blocks[xPiece][yPiece] != 0) {
                        int xGrid = x + xPiece;
                        int yGrid = y + yPiece;

                        this.updateGridValue(xGrid, yGrid, gamePiece.getValue());
                    }
                }
            }
            return true; // Piece was played successfully

        } else {
            return false; // Piece cannot be played
        }
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
     * Transforms the grid into a string for debugging
     * @return the grid string
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");

        for (int y=0; y < getRows(); y++) {
            for (int x=0; x < getCols(); x++) {
                sb.append(String.format("%" + 3 + "d", getGridProperty(x, y).get()));
            }

            sb.append("\n");
        }

        return sb.toString();
    }

    public GameBlockCoordinate getGridCentre() {
        int centreRow = (this.getRows() - 1) / 2;
        int centreCol = (this.getCols() - 1) / 2;

        return new GameBlockCoordinate(centreRow, centreCol);
    }

    public void clearGrid() {
        for(var y = 0; y < this.getRows(); y++) {
            for(var x = 0; x < this.getCols(); x++) {
                this.updateGridValue(x, y, 0);
            }
        }
    }

}
