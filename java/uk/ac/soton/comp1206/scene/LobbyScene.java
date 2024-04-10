package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ChatWindow;
import uk.ac.soton.comp1206.component.HorizontalSpacer;
import uk.ac.soton.comp1206.component.VerticalSpacer;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class LobbyScene extends BaseScene {
    /**
     * Logger for debugging
     */
    private static final Logger logger = LogManager.getLogger(LobbyScene.class);

    private BorderPane mainBorderPane;

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public LobbyScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating lobby scene");
    }

    /**
     * Initialise this scene. Called after creation
     */
    @Override
    public void initialise() {
        logger.info("Initializing lobby scene");

        getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                this.gameWindow.getCommunicator().send("PART");
                this.gameWindow.startMenu();
            }
        });
    }

    /**
     * Build the layout of the scene
     */
    @Override
    public void build() {
        logger.info("Building lobby scene");

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
        StackPane mainStackPane = new StackPane();
        mainStackPane.setMaxWidth(gameWindow.getWidth());
        mainStackPane.setMaxHeight(gameWindow.getHeight());
        mainStackPane.getStyleClass().add("menu-background");
        root.getChildren().add(mainStackPane);

        this.mainBorderPane = new BorderPane();
        mainStackPane.getChildren().add(mainBorderPane);

        // Set title
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("challenge-header");
        Label title = new Label("Multiplayer");
        title.getStyleClass().add("title");
        title.setMinWidth(Label.USE_PREF_SIZE);
        header.getChildren().add(title);
        mainBorderPane.setTop(header);

        // Create left side which holds the current games
        VBox sidebar = new VBox();
        sidebar.setAlignment(Pos.TOP_LEFT);

        // Create host new game button and create game text field
        VerticalSpacer spacer1 = new VerticalSpacer(10);
        TextField hostNewGameTextField = new TextField();
        hostNewGameTextField.setVisible(false);
        Button hostNewGameButton = new Button("Host New Game");
        hostNewGameButton.getStyleClass().add("host-game-button");
        hostNewGameButton.setOnAction(event -> handleHostNewGameButtonPressed(hostNewGameTextField));
        sidebar.getChildren().addAll(spacer1, hostNewGameButton, hostNewGameTextField);

        // Create current games list
        VerticalSpacer spacer2 = new VerticalSpacer(40);
        Label currentGamesTitle = new Label("Current Games");
        currentGamesTitle.getStyleClass().add("heading");
        VBox currentGamesContainer = new VBox();
        currentGamesContainer.setAlignment(Pos.TOP_LEFT);
        sidebar.getChildren().addAll(spacer2, currentGamesTitle, currentGamesContainer);
        mainBorderPane.setLeft(sidebar);

        this.gameWindow.getCommunicator().addListener(this::handleServerMessage);
    }

    private void handleHostNewGameButtonPressed(TextField hostNewGameTextField) {
        if (hostNewGameTextField.getText().isEmpty()) {
            hostNewGameTextField.setVisible(true);
            hostNewGameTextField.requestFocus();
        }
        else {
            // Host new game
            this.gameWindow.getCommunicator().send("CREATE " + hostNewGameTextField.getText());
        }
    }

    private void handleServerMessage(String msg) {
        // Split message into key and value
        String[] msgSplit = msg.split(" ");
        if (msgSplit.length <= 1) return;
        String keyword = msgSplit[0];
        msg = msg.substring(keyword.length() + 1);

        if (keyword.equals("JOIN")) {
            // Create chat window
            String finalMsg = msg;
            Platform.runLater(() -> {
                HBox centreBox = new HBox();
                centreBox.setAlignment(Pos.TOP_LEFT);
                VBox chatWindowContainer = new VBox();
                chatWindowContainer.setAlignment(Pos.TOP_CENTER);

                // Setup channel name header
                Label channelNameLabel = new Label(finalMsg);
                channelNameLabel.getStyleClass().add("small-heading");

                VerticalSpacer spacer1 = new VerticalSpacer(10);
                VerticalSpacer spacer2 = new VerticalSpacer(40);
                HorizontalSpacer spacer3 = new HorizontalSpacer(40);
                HorizontalSpacer spacer4 = new HorizontalSpacer(5);
                ChatWindow chatWindow = new ChatWindow(this.gameWindow.getCommunicator());
                chatWindow.getStyleClass().add("chat-window");

                centreBox.getChildren().addAll(spacer3, chatWindowContainer, spacer4);
                chatWindowContainer.getChildren().addAll(spacer1, channelNameLabel, new VerticalSpacer(10), chatWindow, spacer2);

                mainBorderPane.setCenter(centreBox);
            });
        }
    }
}
