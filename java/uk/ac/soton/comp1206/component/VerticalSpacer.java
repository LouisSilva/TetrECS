package uk.ac.soton.comp1206.component;

import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * A custom component that creates a vertical spacer with a specific size
 */
public class VerticalSpacer extends VBox {

    /**
     * The constructor for the vertical spacer
     * @param spaceInPixels the length in pixels of the spacer
     */
    public VerticalSpacer(double spaceInPixels) {
        super();

        // Setup the spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.NEVER);
        spacer.setMinHeight(spaceInPixels);
        spacer.setPrefHeight(spaceInPixels);
        this.getChildren().add(spacer);
    }
}
