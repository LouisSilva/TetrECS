package uk.ac.soton.comp1206.component;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.Arrays;

/**
 * A custom component for making a chat window
 */
public class ChatWindow extends BorderPane {
    /**
     * Logger for debugging
     */
    private static final Logger logger = LogManager.getLogger(ChatWindow.class);

    /**
     * The communicator class needed to handle messages and nicknames
     */
    private final Communicator communicator;

    /**
     * The box which holds all of the text messages
     */
    private final TextFlow recievedMessagesTextFlow;

    /**
     * The field where a user can enter a text message
     */
    private final TextField messageToSendTextField;

    /**
     * The button that starts the game if the user is the host
     * It only shows if the user is the host, otherwise it is disabled and invisible
     */
    private final Button startGameButton;

    /**
     * The constructor for this component
     * @param communicator The communicator object needed to handle messages and nicknames
     */
    public ChatWindow(Communicator communicator) {
        super();

        this.communicator = communicator;
        this.communicator.addListener(this::handleServerMessage);

        // Create the message input text field and send message button
        this.messageToSendTextField = new TextField("");
        this.messageToSendTextField.setOnKeyPressed(event -> {if (event.getCode() == KeyCode.ENTER) sendMessage(this.messageToSendTextField.getText());});
        Button sendMessageButton = new Button("Send");
        sendMessageButton.setOnAction(event -> this.sendMessage(this.messageToSendTextField.getText()));
        HBox sendMessageHBox = new HBox();
        HBox.setHgrow(this.messageToSendTextField, Priority.ALWAYS);
        sendMessageHBox.getChildren().addAll(this.messageToSendTextField, sendMessageButton);

        // Create start and leave game buttons
        HBox leaveStartGameHBox = new HBox();
        leaveStartGameHBox.setPadding(new Insets(10f, 5f, 5f, 5f));
        Button leaveGameButton = new Button("Leave");
        leaveGameButton.setOnAction(event -> this.leaveGame());
        this.startGameButton = new Button("Start");
        this.startGameButton.setOnAction(event -> this.startGame());
        this.startGameButton.setOpacity(0);
        this.startGameButton.setDisable(true);
        leaveStartGameHBox.getChildren().addAll(leaveGameButton, startGameButton);

        // Add the text field and button H-boxes to a Vbox
        VBox chatButtonsVBox = new VBox();
        chatButtonsVBox.getChildren().addAll(sendMessageHBox, leaveStartGameHBox);
        this.setBottom(chatButtonsVBox);

        // Create received messages container
        this.recievedMessagesTextFlow = new TextFlow();
        this.recievedMessagesTextFlow.getStyleClass().add("chat-window-text-flow");

        // Add the scroller
        ScrollPane messageScrollPane = new ScrollPane();
        messageScrollPane.getStylesheets().clear();
        messageScrollPane.getStyleClass().add("chat-window-scroll-pane");
        messageScrollPane.setContent(this.recievedMessagesTextFlow);
        messageScrollPane.setFitToWidth(true);
        this.setCenter(messageScrollPane);
    }

    /**
     * Leaves the current lobby
     */
    private void leaveGame() {
        this.communicator.send("PART");
    }

    /**
     * Starts the game in the current lobby
     */
    private void startGame() {
        this.communicator.send("START");
    }

    /**
     * Is called from the lobby scene to make sure the start game button appears if the user is the host
     */
    public void setAsHost() {
        if (this.startGameButton == null) return;
        Platform.runLater(() -> {
            this.startGameButton.setDisable(false);
            this.startGameButton.setOpacity(1);
        });
    }

    /**
     * Handles an incoming server message
     * @param msg the server message
     */
    private void handleServerMessage(String msg) {
        // Handles when the server says the user is the host
        if (msg.startsWith("HOST")) {
            if (this.startGameButton != null) {
                Platform.runLater(() -> {
                    this.startGameButton.setDisable(false);
                    this.startGameButton.setOpacity(1);
                });
            }
        }

        // Handles when a message is received
        else if (msg.startsWith("MSG")) {
            msg = msg.substring("MSG ".length());
            String[] msgSplit = msg.split(":");

            if (msgSplit.length <= 1) {
                logger.error("Message received from server only had {} parts", msgSplit.length);
                return;
            }

            Platform.runLater(() -> {
                Text receivedMessage = new Text(msgSplit[0] + ": " + msgSplit[1] + "\n");
                receivedMessage.getStyleClass().add("chat-window-message");
                this.recievedMessagesTextFlow.getChildren().add(receivedMessage);
                Multimedia.getInstance().playAudioFile("message.wav");
            });

        }
    }

    /**
     * Sends a message to the server
     * Makes sure to handle if a user types a command like "/nick"
     * @param msg the message to send
     */
    private void sendMessage(String msg) {
        // Check if the user is trying to change their nickname first
        logger.debug(msg);
        if (msg.startsWith("/nickname ") || msg.startsWith("/NICKNAME ") || msg.startsWith("/Nickname ")) {
            this.communicator.send("NICK " + msg.substring("/nickname ".length()));
            messageToSendTextField.clear();
        }
        else if (msg.startsWith("/nick")) {
            this.communicator.send("NICK " + msg.substring("/nick ".length()));
            messageToSendTextField.clear();
        }
        else {
            this.communicator.send("MSG " + msg.strip());
            messageToSendTextField.clear();
        }
    }
}
