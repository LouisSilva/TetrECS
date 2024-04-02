package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
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

    private enum EnterOrExit {
        ENTER,
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

        // A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        // Do an initial paint
        paint();

        // When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);

        // Set hover effect
        this.setOnMouseEntered(e -> this.onHover(EnterOrExit.ENTER));
        this.setOnMouseExited(e -> this.onHover(EnterOrExit.EXIT));
    }

    /**
     * Handles what happens when the mouse hovers over a block
     * @param enterOrExit whether the mouse entered the block or exited
     */
    private void onHover(EnterOrExit enterOrExit) {

        switch (enterOrExit) {
            case ENTER -> {
                this.paintHover();
            }
            case EXIT -> {
                this.paint();
            }
        }
    }

    /**
     * Plays the fade out animation for the block
     */
    public void fadeOut() {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), this);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0);
        fadeTransition.setOnFinished(e -> {
            this.value.set(0);
            this.paint();
        });
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
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
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

}
