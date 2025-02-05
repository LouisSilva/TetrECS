package uk.ac.soton.comp1206.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Instances of GamePiece Represents the model of a specific Game Piece with it's block makeup.
 * <p>
 * The GamePiece class also contains a factory for producing a GamePiece of a particular shape, as specified by it's
 * number.
 */
public class GamePiece {

    /**
     * The logger for this class
     */
    private static final Logger logger = LogManager.getLogger(GamePiece.class);

    /**
     * The total number of pieces in this game
     */
    public static final int PIECES = 15;

    /**
     * The 2D grid representation of the shape of this piece
     */
    private int[][] blocks;

    /**
     * The value of this piece
     */
    private final int value;

    /**
     * The name of this piece
     */
    private final String name;

    /**
     * Enum for better storage of all the game piece types
     */
    public enum GamePieceType {
        /**
         * The line piece
         */
        LINE(new int[][]{{0, 0, 0}, {1, 1, 1}, {0, 0, 0}}, "Line", 1),

        /**
         * The C shaped piece
         */
        C(new int[][]{{0, 0, 0}, {1, 1, 1}, {1, 0, 1}}, "C", 2),

        /**
         * The plus symbol shaped piece
         */
        PLUS(new int[][]{{0, 1, 0}, {1, 1, 1}, {0, 1, 0}}, "Plus", 3),

        /**
         * The dot piece
         */
        DOT(new int[][]{{0, 0, 0}, {0, 1, 0}, {0, 0, 0}}, "Dot", 4),

        /**
         * The square piece
         */
        SQUARE(new int[][]{{1, 1, 0}, {1, 1, 0}, {0, 0, 0}}, "Square", 5),

        /**
         * The L shaped piece
         */
        L(new int[][]{{0, 0, 0}, {1, 1, 1}, {0, 0, 1}}, "L", 6),

        /**
         * The J shaped piece
         */
        J(new int[][]{{0, 0, 1}, {1, 1, 1}, {0, 0, 0}}, "J", 7),

        /**
         * The S shaped piece
         */
        S(new int[][]{{0, 0, 0}, {0, 1, 1}, {1, 1, 0}}, "S", 8),

        /**
         * The Z shaped piece
         */
        Z(new int[][]{{1, 1, 0}, {0, 1, 1}, {0, 0, 0}}, "Z", 9),

        /**
         * The T shaped piece
         */
        T(new int[][]{{1, 0, 0}, {1, 1, 0}, {1, 0, 0}}, "T", 10),

        /**
         * The X shaped piece
         */
        X(new int[][]{{1, 0, 1}, {0, 1, 0}, {1, 0, 1}}, "X", 11),

        /**
         * The corner piece
         */
        CORNER(new int[][]{{0, 0, 0}, {1, 1, 0}, {1, 0, 0}}, "Corner", 12),

        /**
         * The inverse corner piece
         */
        INVERSE_CORNER(new int[][]{{1, 0, 0}, {1, 1, 0}, {0, 0, 0}}, "Inverse Corner", 13),

        /**
         * The diagonal piece
         */
        DIAGONAL(new int[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}}, "Diagonal", 14),

        /**
         * Thed double piece
         */
        DOUBLE(new int[][]{{0, 1, 0}, {0, 1, 0}, {0, 0, 0}}, "Double", 15);

        /**
         * The 2d array this game piece represents
         */
        private final int[][] blocks;

        /**
         * The name of the game piece
         */
        private final String name;

        /**
         * The value of the game piece, which represents the colour of the piece
         */
        private final int value;

        /**
         * The constructor for this enum
         * @param blocks the two dimensional array representing the game piece
         * @param name the name of the game piece
         * @param value the value (colour) that represents the game piece
         */
        GamePieceType(int[][] blocks, String name, int value) {
            this.blocks = blocks;
            this.name = name;
            this.value = value;
        }

        /**
         * Creates a new game piece object
         * @return the new object
         */
        public GamePiece createPiece() {
            return new GamePiece(name, blocks, value);
        }

        /**
         * Creates a new game piece object from a given game piece number
         * @param pieceNumber the number of the game piece
         * @return the new object
         */
        public static GamePiece createGamePiece(int pieceNumber) {
            for (GamePieceType gamePieceType : GamePieceType.values()) {
                if (gamePieceType.value == pieceNumber) {
                    return gamePieceType.createPiece();
                }
            }

            logger.error("Invalid game piece type: {}", pieceNumber);
            return null;
        }
    }

    /**
     * Create a new GamePiece of the specified piece number
     * @param piece piece number
     * @return the created GamePiece
     */
    public static GamePiece createPiece(int piece) {
        if (piece < 0 || piece >= GamePieceType.values().length)
            throw new IndexOutOfBoundsException("No such piece: " + piece);

        return GamePieceType.values()[piece].createPiece();
    }

    /**
     * Create a new GamePiece of the specified piece number and rotation
     * @param piece piece number
     * @param rotation number of times to rotate
     * @return the created GamePiece
     */
    public static GamePiece createPiece(int piece, int rotation) {
        var newPiece = createPiece(piece);

        newPiece.rotate(rotation);
        return newPiece;
    }

    /**
     * Create a new GamePiece with the given name, block makeup and value. Should not be called directly, only via the
     * factory.
     * @param name name of the piece
     * @param blocks block makeup of the piece
     * @param value the value of this piece
     */
    private GamePiece(String name, int[][] blocks, int value) {
        this.name = name;
        this.blocks = blocks;
        this.value = value;

        //Use the shape of the block to create a grid with either 0 (empty) or the value of this shape for each block.
        for(int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                if(blocks[x][y] == 0) continue;
                blocks[x][y] = value;
            }
        }
    }

    /**
     * Get the value of this piece
     * @return piece value
     */
    public int getValue() {
        return value;
    }

    /**
     * Get the block makeup of this piece
     * @return 2D grid of the blocks representing the piece shape
     */
    public int[][] getBlocks() {
        return blocks;
    }

    /**
     * Get the value at a specific coordinate
     * @param x the x coordinate
     * @param y the y coordinate
     * @return The value at the given coordinates
     */
    public int getValueAtCoordinate(int x, int y) {
        return this.blocks[x][y];
    }

    /**
     * Rotate this piece the given number of rotations
     * @param rotations number of rotations
     */
    public void rotate(int rotations) {
        for(int rotated = 0; rotated < rotations; rotated ++) {
            rotate();
        }
    }

    /**
     * Rotate this piece exactly once by rotating it's 3x3 grid
     */
    public void rotate() {
        int[][] rotated = new int[blocks.length][blocks[0].length];
        rotated[2][0] = blocks[0][0];
        rotated[1][0] = blocks[0][1];
        rotated[0][0] = blocks[0][2];

        rotated[2][1] = blocks[1][0];
        rotated[1][1] = blocks[1][1];
        rotated[0][1] = blocks[1][2];

        rotated[2][2] = blocks[2][0];
        rotated[1][2] = blocks[2][1];
        rotated[0][2] = blocks[2][2];

        blocks = rotated;
    }

    /**
     * Return the string representation of this piece
     * @return the name of this piece
     */
    public String toString() {
        return this.name;
    }

    /**
     * Transforms the grid of the game piece into a string for debugging
     * @return the grid string of the game piece
     */
    public String toGridString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");

        for (int y=0; y < 3; y++) {
            for (int x=0; x < 3; x++) {
                sb.append(String.format("%" + 3 + "d", getValueAtCoordinate(x,y)));
            }

            sb.append("\n");
        }

        return sb.toString();
    }
}
