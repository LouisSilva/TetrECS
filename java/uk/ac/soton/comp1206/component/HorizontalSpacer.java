package uk.ac.soton.comp1206.component;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * A custom component that creates a horizontal spacer with a specified width
 */
public class HorizontalSpacer extends HBox {

    /**
     * The constructor for the horizontal spacer
     * @param spaceInPixels the width in pixels of the spacer
     */
    public HorizontalSpacer(double spaceInPixels) {
        super();

        // Setup the spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.NEVER);
        spacer.setMinWidth(spaceInPixels);
        spacer.setPrefWidth(spaceInPixels);
        this.getChildren().add(spacer);
    }
}
