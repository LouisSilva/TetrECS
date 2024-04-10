package uk.ac.soton.comp1206.component;

import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class HorizontalSpacer extends HBox {
    public HorizontalSpacer(double spaceInPixels) {
        super();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.NEVER);
        spacer.setMinWidth(spaceInPixels);
        spacer.setPrefWidth(spaceInPixels);
        this.getChildren().add(spacer);
    }
}
