package uk.ac.soton.comp1206.component;

import javafx.beans.property.SimpleListProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A javafx UI component that displays a live list of scores in a multiplayer game in order
 */
public class Leaderboard extends VBox {

    /**
     * The list of leaderboard entries
     */
    private final SimpleListProperty<LeaderboardEntry> leaderboardEntries;

    /**
     * A record that contains a user's name, score, lives and whether they are dead
     * @param name the name of the user
     * @param score the score the user has
     * @param lives the lives the user has
     * @param isAlive whether the user is alive
     */
    public record LeaderboardEntry(String name, Integer score, Integer lives, boolean isAlive) {

        /**
         * Gets the name of the user
         * @return the name of the user
         */
        public String getName() {
            return name;
        }

        /**
         * Gets the score of the user
         * @return the score of the user
         */
        public int getScore() {
            return score;
        }

        /**
         * Gets the lives of the user
         * @return the lives of the user
         */
        public int getLives() {
            return lives;
        }

        /**
         * Gets whether the user is alive
         * @return whether the user is alive
         */
        public boolean getIsAlive() {
            return isAlive;
        }

        /**
         * Returns a nicely formatted string representing this record
         * @return the string representing this record
         */
        @Override
        public String toString() {
            return MessageFormat.format("Name: {0}, score: {1}, lives: {2}, dead/left the game: {3}", this.getName(), this.getScore(), this.getLives(), this.getIsAlive());
        }
    }

    /**
     * The list of leaderboard entry labels
     */
    private final List<Label> leaderboardEntryLabels = new ArrayList<>();

    /**
     * The constructor for this component
     */
    public Leaderboard() {
        this.leaderboardEntries = new SimpleListProperty<>();
        this.leaderboardEntries.addListener((observable, oldValue, newValue) -> this.updateLeaderboardDisplay());
    }

    /**
     * Adds a new label for each leaderboard entry in the leaderboard
     */
    private void updateLeaderboardDisplay() {
        this.getChildren().clear();
        this.getStyleClass().add("scores-list-vbox");
        for (int i=0; i < this.getLeaderboardEntries().toArray().length; i++) {
            LeaderboardEntry entry = this.getLeaderboardEntries().get(i);
            StringBuilder sb = new StringBuilder(entry.getName() + ": " + entry.getScore() + ", ");

            // Customize the label name depending on whether the player is alive or dead
            if (entry.getIsAlive()) sb.append("lives: ").append(entry.getLives());
            else sb.append("DEAD");

            // Create the label
            Label entryLabel = new Label(sb.toString());
            entryLabel.getStyleClass().add("score-label");
            leaderboardEntryLabels.add(entryLabel);

            // Apply colour depending on where it is in the list
            Color colour = ScoresList.LabelColours.values()[i % ScoresList.LabelColours.values().length].getColour();
            entryLabel.setTextFill(colour);
            this.getChildren().addAll(entryLabel);
        }
    }

    /**
     * A getter for the list of leaderboard scores
     * @return the list of leaderboard scores
     */
    public SimpleListProperty<LeaderboardEntry> getLeaderboardEntries() {
        return leaderboardEntries;
    }
}
