package uk.ac.soton.comp1206.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

public class PieceBoard extends GameBoard {

    private static final Logger logger = LogManager.getLogger(PieceBoard.class);

    public PieceBoard(Grid grid, double width, double height) {
        super(grid, width, height);
    }

    public void displayPiece(GamePiece pieceToDisplay) {

    }

    /**
     * Create a block at the given x and y position in the GameBoard
     * @param x column
     * @param y row
     * */
    @Override
    protected GameBlock createBlock(int x, int y) {
        var blockWidth = this.getWidth() / this.getCols();
        var blockHeight = this.getHeight() / this.getRows();

        GameBlock block = new GameBlock(this, x, y, blockWidth, blockHeight);

        this.add(block, x, y);
        this.blocks[x][y] = block;
        block.bind(grid.getGridProperty(x, y));

        return block;
    }
}
