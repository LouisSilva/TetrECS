package uk.ac.soton.comp1206.component;

import javafx.animation.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.*;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 * <p>
 * Extends Canvas and is responsible for drawing itself.
 * <p>
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 * <p>
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    /**
     * The logger for this class
     */
    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    /**
     * The physical width of the block
     */
    private final double width;

    /**
     * The physical height of the block
     */
    private final double height;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Keeps track of whether the block is in the fade out animation, so other paint functions that are called dont run
     */
    private boolean inFadeOutAnimation = false;

    /**
     * Keeps track of whether the block is part of a game board, because if not then the hover effect should not happen
     */
    public boolean isPartOfGameBoard = false;

    /**
     * Whether to display the circle indicator on this block.
     * Used by PieceBoards
     */
    public boolean displayIndicator = false;

    /**
     * The animated colour used for the fade out
     */
    private final ObjectProperty<Color> fadeAnimationColour = new SimpleObjectProperty<>(Color.WHITE);

    /**
     * The animated opacity used for the fade out
     */
    private final ObjectProperty<Double> fadeAnimationOpacity = new SimpleObjectProperty<>(1.0);

    /**
     * The game board that the block belongs to
     */
    private final GameBoard parentGameBoard;

    /**
     * An enum used to determine whether a mouse hover event is for the mouse exiting or entering the block
     */
    public enum EnterOrExit {
        /**
         * The enter value
         */
        ENTER,

        /**
         * The exit value
         */
        EXIT
    }

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.parentGameBoard = gameBoard;

        // A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        // Do an initial paint
        paint();

        // When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
        fadeAnimationColour.addListener((observable, oldValue, newValue) -> this.paintColorBasic(newValue));
        fadeAnimationOpacity.addListener((observable, oldValue, newValue) -> this.setOpacity(newValue));

        // Set hover effect
        this.setOnMouseEntered(e -> this.onHover(EnterOrExit.ENTER));
        this.setOnMouseExited(e -> this.onHover(EnterOrExit.EXIT));

        this.isPartOfGameBoard = !(gameBoard instanceof PieceBoard);
    }

    /**
     * Handles what happens when the mouse hovers over a block
     * @param enterOrExit whether the mouse entered the block or exited
     */
    public void onHover(EnterOrExit enterOrExit) {
        if (this.inFadeOutAnimation) return;
        if (!this.isPartOfGameBoard) return;

        switch (enterOrExit) {
            case ENTER -> {
                // Make sure to get rid of the hover effect on the previously hovered block
                if (this.parentGameBoard.gameBlockCurrentlySelected != null) this.parentGameBoard.gameBlockCurrentlySelected.onHover(EnterOrExit.EXIT);

                this.paintHover();
                this.parentGameBoard.gameBlockCurrentlySelected = this;
            }
            case EXIT -> this.paint();
        }
    }

    /**
     * Plays the fade out animation for the block
     */
    public void fadeOut() {
        if (inFadeOutAnimation) return;

        this.inFadeOutAnimation = true;
        this.paintEmpty();
        this.paintHover();

        Timeline timeline = new Timeline();
        double toGreenDuration = 0.2;
        double toWhiteDuration = 0.6;

        // Create key frames
        KeyFrame toGreen = new KeyFrame(Duration.seconds(toGreenDuration),
                new KeyValue(this.fadeAnimationColour, Color.GREEN),
                new KeyValue(this.fadeAnimationOpacity, 0.7));
        KeyFrame toWhite = new KeyFrame(Duration.seconds(toWhiteDuration),
                new KeyValue(this.fadeAnimationColour, Color.WHITE, Interpolator.EASE_OUT),
                new KeyValue(this.fadeAnimationOpacity, 0.0, Interpolator.EASE_IN));

        timeline.getKeyFrames().addAll(toGreen, toWhite);

        timeline.setOnFinished(e -> {
            this.inFadeOutAnimation = false;
            paint();
        });

        timeline.play();
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        if (this.inFadeOutAnimation) return;
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        this.setOpacity(1);

        // If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();

        } else {
            // If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);

            GraphicsContext gc = getGraphicsContext2D();
            if (displayIndicator) {
                double radius = Math.min(width, height) / 4;
                gc.setFill(Color.WHITE.deriveColor(0, 1, 1, 0.5));
                gc.fillOval((width / 2) - radius, (height / 2) - radius, radius * 2, radius * 2);
            }
        }
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        // Clear
        gc.clearRect(0, 0, width, height);

        // Top right
        gc.setFill(Color.BLACK.deriveColor(0, 1, 1, 0.05));
        gc.fillPolygon(new double[]{width, width, 0}, new double[]{0, height, 0}, 3);

        // Bottom left
        gc.setFill(Color.WHITE.deriveColor(0, 1, 0.2, 0.05));
        gc.fillPolygon(new double[]{0, width, 0}, new double[]{height, height, 0}, 3);

        // Border
        gc.setStroke(Color.WHITE.deriveColor(0, 1, 0.6, 0.8));
        gc.strokeRect(0, 0, width, height);
    }

    /**
     * Paints the canvas with a specific colour and opacity for when it is being hovered over
     */
    private void paintHover() {
        if (this.getValue() == 0) {
            var gc = this.getGraphicsContext2D();

            // Clear
            gc.clearRect(0, 0, width, height);

            // Top right
            gc.setFill(Color.WHITE.deriveColor(0, 1, 1, 0.2));
            gc.fillPolygon(new double[]{width, width, 0}, new double[]{0, height, 0}, 3);

            // Bottom left
            gc.setFill(Color.WHITE.deriveColor(0, 1, 1, 0.2));
            gc.fillPolygon(new double[]{0, width, 0}, new double[]{height, height, 0}, 3);

            // Border
            gc.setStroke(Color.WHITE.deriveColor(0, 1, 0.6, 0.8));
            gc.strokeRect(0, 0, width, height);
        }
        else {
            this.setOpacity(0.5);
        }
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Color colour) {
        var gc = getGraphicsContext2D();

        // Clear
        gc.clearRect(0, 0, width, height);

        // Top right
        gc.setFill(colour.deriveColor(0, 1, 0.8, 1));
        gc.fillPolygon(new double[]{width, width, 0}, new double[]{0, height, 0}, 3);

        // Bottom left
        gc.setFill(colour.deriveColor(0, 1, 1.2, 1));
        gc.fillPolygon(new double[]{0, width, 0}, new double[]{height, height, 0}, 3);

        // Border
        gc.setStroke(Color.WHITE.deriveColor(0, 1, 0.6, 0.8));
        gc.strokeRect(0, 0, width, height);
    }

    /**
     * Paint this canvas with the given colour, but with no nice looking tiling
     * @param colour the colour to paint
     */
    private void paintColorBasic(Color colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0,0, width, height);

        // Border
        gc.setStroke(Color.WHITE.deriveColor(0, 1, 0.6, 0.8));
        gc.strokeRect(0, 0, width, height);
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Returns the game block coordinate for this block
     * @return the game block coordinate for this block
     */
    public GameBlockCoordinate getCoordinates() {
        return new GameBlockCoordinate(this.getX(), this.getY());
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

}
