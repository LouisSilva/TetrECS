package uk.ac.soton.comp1206.scene;

import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class InstructionsScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

  public InstructionsScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Instructions Scene");
  }

  @Override
  public void initialise() {
    logger.info("Initialising Instructions");

    getScene().setOnKeyPressed( event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        gameWindow.loadScene(new MenuScene(gameWindow));
      }
    });
  }

  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

    var instructionPane = new StackPane();
    instructionPane.setMaxWidth(gameWindow.getWidth());
    instructionPane.setMaxHeight(gameWindow.getHeight());
    instructionPane.getStyleClass().add("menu-background");
    root.getChildren().add(instructionPane);
  }
}
