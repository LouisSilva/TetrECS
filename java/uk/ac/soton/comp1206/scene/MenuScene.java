package uk.ac.soton.comp1206.scene;

import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Objects;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());
        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        // Set title image
        HBox header = new HBox();
        header.getStyleClass().add("menu-header");
        Image titleImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/TetrECS.png")));
        ImageView titleImageView = new ImageView(titleImage);
        titleImageView.setPreserveRatio(true);
        titleImageView.setRotate(0);
        titleImageView.setFitWidth(600);
        header.getChildren().add(titleImageView);
        mainPane.setTop(header);

        VBox buttonContainer = new VBox(10);
        buttonContainer.getStyleClass().add("menu-button-container");

        // Make buttons
        var playButton = new Button("Play");
        playButton.getStyleClass().add("menu-button");
        var multiplayerButton = new Button("Multiplayer");
        multiplayerButton.getStyleClass().add("menu-button");
        var instructionsButton = new Button("How to Play");
        instructionsButton.getStyleClass().add("menu-button");
        var exitButton = new Button("Exit");
        exitButton.getStyleClass().add("menu-button");

        buttonContainer.getChildren().addAll(playButton, multiplayerButton, instructionsButton, exitButton);
        mainPane.setBottom(buttonContainer);
        BorderPane.setAlignment(buttonContainer, Pos.CENTER);

        //Bind the playButton action to the startGame method in the menu
        playButton.setOnAction(this::startGame);
        instructionsButton.setOnAction(this::switchToInstructionsMenu);
        exitButton.setOnAction(this::exitGame);
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {

    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }

    private void switchToInstructionsMenu(ActionEvent event) {
        gameWindow.loadScene(new InstructionsScene(this.gameWindow));
    }

    private void exitGame(ActionEvent event) {
        Platform.exit();
        System.exit(0);
    }

}
