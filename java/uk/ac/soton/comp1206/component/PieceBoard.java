package uk.ac.soton.comp1206.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * A component that extends the game board
 * Its purpose is to only display pieces, you cannot click on it to add pieces like a normal game board
 */
public class PieceBoard extends GameBoard {

    /**
     * The logger for this class
     */
    private static final Logger logger = LogManager.getLogger(PieceBoard.class);

    /**
     * Create a new PieceBoard, based off a given grid, with a visual width and height.
     * @param grid linked grid
     * @param width the visual width
     * @param height the visual height
     */
    public PieceBoard(Grid grid, double width, double height) {
        super(grid, width, height);
    }

    /**
     * Create a new PieceBoard with it's own internal grid, specifying the number of columns and rows, along with the
     * visual width and height.
     *
     * @param cols number of columns for internal grid
     * @param rows number of rows for internal grid
     * @param width the visual width
     * @param height the visual height
     */
    public PieceBoard(int cols, int rows, double width, double height) {
        super(cols, rows, width, height);
    }

    /**
     * Create a new PieceBoard with it's own internal grid, specifying the number of columns and rows, along with the
     * visual width and height.
     * <p></p>
     * It also has a boolean parameter to control whether to show a white circle indicator on the centre of a displayed piece
     * @param cols number of columns for internal grid
     * @param rows number of rows for internal grid
     * @param width the visual width
     * @param height the visual height
     * @param displayIndicator whether to show the display indicator on a displayed piece
     */
    public PieceBoard(int cols, int rows, double width, double height, boolean displayIndicator) {
        super(cols, rows, width, height);

        if (displayIndicator) {
            this.getCenterBlock().displayIndicator = true;
        }
    }

    /**
     * Displays a given piece
     * @param pieceToDisplay the piece to display
     */
    public void displayPiece(GamePiece pieceToDisplay) {
        this.grid.clearGrid();
        GameBlockCoordinate gridCentre = this.grid.getGridCentre();
        this.grid.playPiece(pieceToDisplay, gridCentre.getX(), gridCentre.getY());
    }
}
