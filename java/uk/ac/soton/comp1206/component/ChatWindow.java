package uk.ac.soton.comp1206.component;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;

public class ChatWindow extends BorderPane {
    /**
     * Logger for debugging
     */
    private static final Logger logger = LogManager.getLogger(ChatWindow.class);

    private final Communicator communicator;

    private final TextFlow recievedMessagesTextFlow;

    private final TextField messageToSendTextField;

    private final ScrollPane messageScrollPane;

    private boolean scrollToBottom = false;

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
        this.setBottom(sendMessageHBox);

        // Create received messages container
        this.recievedMessagesTextFlow = new TextFlow();
        this.recievedMessagesTextFlow.getStyleClass().add("chat-window-text-flow");

        // Add the scroller
        this.messageScrollPane = new ScrollPane();
        this.messageScrollPane.getStylesheets().clear();
        this.messageScrollPane.getStyleClass().add("chat-window-scroll-pane");
        this.messageScrollPane.setContent(this.recievedMessagesTextFlow);
        this.messageScrollPane.setFitToWidth(true);
        this.setCenter(this.messageScrollPane);
    }

    /**
     * Handles an incoming server message
     * @param msg the server message
     */
    private void handleServerMessage(String msg) {
        // Split message into key and value
        String[] msgSplit = msg.split(" ");
        if (msgSplit.length <= 1) return;
        String keyword = msgSplit[0];
        msg = msg.substring(keyword.length() + 1);

        // Handles when a message is received
        if (keyword.equals("MSG")) {
            msgSplit = msg.split(":");
            if (msgSplit.length <= 1) {
                logger.error("Message received from server only had {} parts", msgSplit.length);
                return;
            }

            String username = msgSplit[0];
            msg = msg.substring(username.length() + 1);

            String finalMsg = msg;
            Platform.runLater(() -> {
                Text receivedMessage = new Text(username + ": " + finalMsg + "\n");
                receivedMessage.getStyleClass().add("chat-window-message");
                this.recievedMessagesTextFlow.getChildren().add(receivedMessage);
                if (this.messageScrollPane.getVvalue() == 0.0f || this.messageScrollPane.getVvalue() > 0.9f) {
                    this.scrollToBottom = true;
                }
            });
        }
    }

    private void sendMessage(String msg) {
        // Check if the user is trying to change their nickname first
        if (msg.startsWith("/nickname ") || msg.startsWith("/NICKNAME ") || msg.startsWith("/Nickname ") || msg.startsWith("/nick")) {
            this.communicator.send("NICK " + msg.substring("/nickname ".length()));
            messageToSendTextField.clear();
        }
        else {
            this.communicator.send("MSG " + msg.strip());
            messageToSendTextField.clear();
        }
    }

    public Runnable jumpScrollerToBottom() {
        if (!this.scrollToBottom) return null;
        this.messageScrollPane.setVvalue(1.0f);
        this.scrollToBottom = false;
        return null;
    }
}
