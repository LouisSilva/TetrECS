package uk.ac.soton.comp1206.component;

import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class VerticalSpacer extends VBox {
    public VerticalSpacer(double spaceInPixels) {
        super();

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.NEVER);
        spacer.setMinHeight(spaceInPixels);
        spacer.setPrefHeight(spaceInPixels);
        this.getChildren().add(spacer);
    }
}
