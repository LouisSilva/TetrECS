package uk.ac.soton.comp1206.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

import java.text.MessageFormat;

public class PieceBoard extends GameBoard {

    private static final Logger logger = LogManager.getLogger(PieceBoard.class);

    public PieceBoard(Grid grid, double width, double height) {
        super(grid, width, height);
    }

    public PieceBoard(int cols, int rows, double width, double height) {
        super(cols, rows, width, height);
    }

    public void displayPiece(GamePiece pieceToDisplay) {
        this.grid.clearGrid();
        GameBlockCoordinate gridCentre = this.grid.getGridCentre();
        this.grid.playPiece(pieceToDisplay, gridCentre.getX(), gridCentre.getY());
        logger.debug(this.toString());
        logger.debug(this.grid.toString());
    }
}
