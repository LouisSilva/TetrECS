package uk.ac.soton.comp1206.component;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleListProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import uk.ac.soton.comp1206.scene.ScoresScene;

import java.util.ArrayList;
import java.util.List;

/**
 * A javafx UI component that acts as a list of labels for containing high-scores
 */
public class ScoresList extends VBox {
    /**
     * The list of scores
     */
    private final SimpleListProperty<ScoresScene.Score> scores;

    /**
     * An enum used to store all the colours that a label is set to in order
     */
    private enum LabelColours {
        PINK(Color.HOTPINK),
        RED(Color.RED),
        ORANGE(Color.ORANGE),
        YELLOW(Color.YELLOW),
        LIME(Color.LIME),
        GREEN(Color.GREEN),
        DARK_GREEN(Color.DARKGREEN),
        LIGHT_BLUE(Color.LIGHTBLUE),
        BLUE(Color.BLUE);

        /**
         * The colour that this enum represents
         */
        private final Color colour;

        /**
         * The constructor for this enum
         * @param colour the colour to represent
         */
        LabelColours(Color colour) {
            this.colour = colour;
        }

        /**
         * Returns the stored colour
         * @return the stored colour
         */
        public Color getColour() {
            return colour;
        }
    }

    /**
     * The list of the score labels
     */
    private final List<Label> scoreLabels = new ArrayList<>();

    /**
     * The constructor for this component
     */
    public ScoresList() {
        this.scores = new SimpleListProperty<>();
        this.scores.addListener((observable, oldValue, newValue) -> this.updateScoresDisplay());
    }

    /**
     * Adds a new label for each score in the scores list
     */
    private void updateScoresDisplay() {
        this.getChildren().clear();
        this.getStyleClass().add("scores-list-vbox");
        for (int i=0; i < this.getScores().toArray().length; i++) {
            ScoresScene.Score score = this.getScores().get(i);
            Label scoreLabel = new Label(score.getName() + ": " + score.getScore());
            scoreLabel.getStyleClass().add("score-label");
            scoreLabel.setOpacity(0);
            scoreLabels.add(scoreLabel);

            // Apply colour depending on where it is in the list
            Color colour = LabelColours.values()[i % LabelColours.values().length].getColour();
            scoreLabel.setTextFill(colour);
            this.getChildren().addAll(scoreLabel);
        }
    }

    /**
     * Plays an animation for revealing all the scores in the list
     */
    public void revealScores() {
        double delay = 0.25; // The delay between each score being animated
        double duration = 1.5; // The animation duration

        for (int i=0; i < this.scoreLabels.size(); i++) {
            Label curLabel = this.scoreLabels.get(i);
            curLabel.setOpacity(0);

            // Do animation
            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(duration), curLabel);
            fadeTransition.setFromValue(0);
            fadeTransition.setToValue(1);
            fadeTransition.setDelay(Duration.seconds(delay * i));
            fadeTransition.play();
        }
    }

    /**
     * A getter for the list of scores
     * @return the list of scores
     */
    public SimpleListProperty<ScoresScene.Score> getScores() {
        return scores;
    }
}
