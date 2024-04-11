package uk.ac.soton.comp1206.component;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A javafx UI component that acts as a list of buttons for containing the available channels in the lobby
 */
public class ChannelsList extends VBox {
    /**
     * Logger for debugging
     */
    private static final Logger logger = LogManager.getLogger(ChannelsList.class);

    /**
     * The communicator object for communicating with the server
     */
    private final Communicator communicator;

    /**
     * The list of channels
     */
    private final SimpleListProperty<String> channels;

    /**
     * The scheduler which is used as a timer for calling a function that updates the list of channels
     */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * The update channels future which is used for resetting and shutting down the timer properly
     */
    private Future<?> updateChannelsFuture;

    /**
     * The constructor for this component
     */
    public ChannelsList(Communicator communicator) {
        this.communicator = communicator;
        this.communicator.addListener(this::handleServerMessage);
        this.updateChannels();

        this.channels = new SimpleListProperty<>(FXCollections.observableArrayList());
        this.channels.addListener((observable, oldValue, newValue) -> this.updateChannelsDisplay());

        this.startUpdateChannelsTimer();
    }

    /**
     * Starts the update channels timer
     */
    private void startUpdateChannelsTimer() {
        if (this.updateChannelsFuture != null && !this.updateChannelsFuture.isDone()) {
            this.updateChannelsFuture.cancel(true);
        }

        // Start the timer
        Runnable updateChannelsTask = () -> {
            if (Thread.currentThread().isInterrupted()) return;
            this.updateChannels();
        };

        long timerDelay = 3000;
        this.updateChannelsFuture = scheduler.scheduleAtFixedRate(updateChannelsTask, timerDelay, timerDelay, TimeUnit.MILLISECONDS);
    }

    /**
     * Sends a message to the communicator to send over a list of the available game channels to join, and then resets the timer
     */
    private void updateChannels() {
        communicator.send("LIST");
        this.resetUpdateChannelsTimer();
    }

    /**
     * Resets the update channels timer
     */
    private void resetUpdateChannelsTimer() {
        if (updateChannelsFuture != null) {
            updateChannelsFuture.cancel(true);
        }

        this.startUpdateChannelsTimer();
    }

    /**
     * Shuts down the update channels timer properly
     */
    public void shutdownUpdateChannelsTimer() {
        if (updateChannelsFuture != null) {
            updateChannelsFuture.cancel(true);
        }

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    /**
     * Adds a new button for each channel in the list
     */
    private void updateChannelsDisplay() {
        Platform.runLater(() -> {
            this.getChildren().clear();
            this.getStyleClass().add("channel-list-vbox");
            for (int i=0; i < this.getChannels().toArray().length; i++) {
                String channel = this.getChannels().get(i);
                Button channelButton = new Button();
                channelButton.setOnAction((event) -> this.joinChannel(channel));
                channelButton.setText(channel.trim().strip());
                channelButton.getStyleClass().add("host-game-button");
                this.getChildren().add(channelButton);
            }
        });
    }

    /**
     * Joins a given channel
     * @param channel the name of the channel to join
     */
    private void joinChannel(String channel) {
        this.communicator.send("JOIN " + channel);
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

        // Handles when it receives a list of all teh channels
        if (keyword.equals("CHANNELS")) {
            msgSplit = msg.split("\n");

            // Create a new channels list
            List<String> newChannels = new ArrayList<>(Arrays.asList(msgSplit));

            // Update the component
            Platform.runLater(() -> {
                ObservableList<String> currentChannels = getChannels();
                currentChannels.clear();
                currentChannels.addAll(newChannels);
            });
        }
    }

    /**
     * A getter for the list of channels
     * @return the list of channels
     */
    public SimpleListProperty<String> getChannels() {
        return this.channels;
    }
}
