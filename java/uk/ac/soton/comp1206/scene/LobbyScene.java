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
import uk.ac.soton.comp1206.component.ChannelsList;
import uk.ac.soton.comp1206.component.ChatWindow;
import uk.ac.soton.comp1206.component.HorizontalSpacer;
import uk.ac.soton.comp1206.component.VerticalSpacer;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The scene for displaying and creating multiplayer lobbies
 */
public class LobbyScene extends BaseScene {
    /**
     * Logger for debugging
     */
    private static final Logger logger = LogManager.getLogger(LobbyScene.class);

    /**
     * The border pane which holds all of the components
     */
    private BorderPane mainBorderPane;

    /**
     * The ChannelsList component which lists all the available channels to join
     */
    private ChannelsList channelsList;

    /**
     * The ChatWindow component which is a chat window...
     */
    private ChatWindow chatWindow;

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
                this.channelsList.shutdownUpdateChannelsTimer();
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
        this.channelsList = new ChannelsList(this.gameWindow.getCommunicator());
        sidebar.getChildren().addAll(spacer2, currentGamesTitle, this.channelsList);
        this.mainBorderPane.setLeft(sidebar);

        this.gameWindow.getCommunicator().addListener(this::handleServerMessage);
    }

    /**
     * Makes sure to either create a new lobby with the given channel name or make the text field visible
     * @param hostNewGameTextField the text field where the user can input the channel name
     */
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

    /**
     * Handles the server messages
     * @param msg the message from the server
     */
    private void handleServerMessage(String msg) {
        // Handles when the server says the user is the host
        if (msg.startsWith("HOST")) {
            Platform.runLater(() -> {
                this.chatWindow.setAsHost();
            });
            return;
        }

        // Handles when we disconnect from a channel
        if (msg.equals("PARTED")) {
            Platform.runLater(() -> this.mainBorderPane.setCenter(null));
            return;
        }

        // Split message into key and value
        String[] msgSplit = msg.split(" ");
        String keyword;
        if (msgSplit.length == 0) keyword = msg;
        else {
            keyword = msgSplit[0];
            msg = msg.substring(keyword.length() + 1);
        }

        // Handles when a join message is received
        if (keyword.equals("JOIN")) {
            // Create chat window
            String finalMsg = msg.trim().strip();
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
                this.chatWindow = new ChatWindow(this.gameWindow.getCommunicator());
                this.chatWindow.getStyleClass().add("chat-window");

                centreBox.getChildren().addAll(spacer3, chatWindowContainer, spacer4);
                chatWindowContainer.getChildren().addAll(spacer1, channelNameLabel, new VerticalSpacer(10), this.chatWindow, spacer2);

                this.mainBorderPane.setCenter(centreBox);
            });
        }
    }
}
