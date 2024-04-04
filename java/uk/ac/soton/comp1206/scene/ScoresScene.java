package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScoresScene extends BaseScene {

    /**
     * Logger for debugging
     */
    private static final Logger logger = LogManager.getLogger(ScoresScene.class);

    private final Game finishedGame;

    private String username = null;

    private static final int maxScoreArraySize = 10;

    public record Score(String name, Integer score) {

        public String getName() {
            return name;
        }

        public int getScore() {
            return score;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Name: ''{0}'', score: {1}", this.name(), this.getScore());
        }
    }

    private SimpleListProperty<Score> localScores;

    private SimpleListProperty<Score> remoteScores;

    private ScoresList localScoresListComponent;
    private ScoresList onlineScoresListComponent;

    private CompletableFuture<String> usernameFuture;

    private final AtomicBoolean localHighScoreAchieved = new AtomicBoolean(false);

    private final AtomicBoolean onlineHighScoreAchieved = new AtomicBoolean(false);

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     *
     * @param gameWindow the game window
     */
    public ScoresScene(GameWindow gameWindow, Game finishedGame) {
        super(gameWindow);
        this.finishedGame = finishedGame;
    }

    @Override
    public void initialise() {
        logger.info("Initializing Scores Scene");

        getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                this.gameWindow.startMenu();
            }
        });

        CompletableFuture<Void> onlineScoresFuture = CompletableFuture.runAsync(this::loadOnlineScores);
        CompletableFuture<Void> localScoresFuture = CompletableFuture.runAsync(() -> this.loadLocalScores(null));

        // Combine futures to proceed after both online and local scores are processed
        CompletableFuture.allOf(onlineScoresFuture, localScoresFuture).thenRun(() -> {
            if (localHighScoreAchieved.get() || onlineHighScoreAchieved.get()) {
                Platform.runLater(() -> promptForUsername().thenAccept(username -> {
                    this.username = username;
                    revealScores();
                }));

            }
            else {
                revealScores();
            }
        });
    }

    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        StackPane mainStackPane = new StackPane();
        mainStackPane.setMaxWidth(gameWindow.getWidth());
        mainStackPane.setMaxHeight(gameWindow.getHeight());
        mainStackPane.getStyleClass().add("menu-background");
        root.getChildren().add(mainStackPane);

        BorderPane mainBorderPane = new BorderPane();
        mainStackPane.getChildren().add(mainBorderPane);

        // Set title image
        HBox header = new HBox();
        VBox headerVBox = new VBox();
        header.getStyleClass().add("stat-container");
        headerVBox.getStyleClass().add("scores-header-vbox");
        Image titleImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/TetrECS.png")));
        ImageView titleImageView = new ImageView(titleImage);
        titleImageView.setPreserveRatio(true);
        titleImageView.setRotate(0);
        titleImageView.setFitWidth(600);
        headerVBox.getChildren().add(titleImageView);
        mainBorderPane.setTop(header);

        // Set the game over label and high scores title/header label
        Label gameOverLabel = new Label("Game Over");
        Label highScoresLabel = new Label("High Scores");
        gameOverLabel.getStyleClass().add("bigtitle");
        highScoresLabel.getStyleClass().add("title");
        headerVBox.getChildren().addAll(gameOverLabel, highScoresLabel);
        header.getChildren().add(headerVBox);

        VBox centreBox = new VBox();
        GridPane centreGrid = new GridPane();
        centreBox.getStyleClass().add("scores-centre-vbox");
        centreBox.setPadding(new Insets(20, 0 ,0 ,0));
        centreGrid.getStyleClass().add("scores-grid");
        centreBox.getChildren().add(centreGrid);
        mainBorderPane.setCenter(centreBox);

        // Setup local scores list
        List<Score> scoreArrayList = new ArrayList<>();
        ObservableList<Score> observableScoreList = FXCollections.observableArrayList(scoreArrayList);
        this.localScores = new SimpleListProperty<>(observableScoreList);
        this.localScoresListComponent = new ScoresList();
        this.localScoresListComponent.getScores().bind(this.localScores);
        centreGrid.add(this.localScoresListComponent, 1, 2);

        // Setup online scores list
        List<Score> onlineScoreArrayList = new ArrayList<>();
        ObservableList<Score> observableOnlineScoreList = FXCollections.observableArrayList(onlineScoreArrayList);
        this.remoteScores = new SimpleListProperty<>(observableOnlineScoreList);
        this.onlineScoresListComponent = new ScoresList();
        this.onlineScoresListComponent.getScores().bind(this.remoteScores);
        this.gameWindow.getCommunicator().addListener(this::handleServerMessage);
        centreGrid.add(this.onlineScoresListComponent, 2, 2);

        // Setup headers for the score lists
        Label localScoresLabel = new Label("Local Scores");
        Label onlineScoresLabel = new Label("Online Scores");
        localScoresLabel.getStyleClass().add("heading");
        onlineScoresLabel.getStyleClass().add("heading");
        centreGrid.add(localScoresLabel, 1, 1);
        centreGrid.add(onlineScoresLabel, 2, 1);
    }

    /**
     * Prompts the user for their username asynchronously (needed because it opens up another window,
     * Not making it asynchronous would make the other windows go onto "not responding" (don't know the technical term for that)
     * @return the username. Returns null if it was unable to get the username
     */
    private CompletableFuture<String> promptForUsername() {
        CompletableFuture<String> usernamePromptFuture = new CompletableFuture<>();

        Platform.runLater(() -> {
            // Prompt user
            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setTitle("Enter your username");
            nameDialog.setHeaderText("You have a highscore! Please enter your username for the scoreboard");
            nameDialog.setContentText("Name: ");

            Optional<String> result = nameDialog.showAndWait();
            if (result.isPresent()) {
                usernamePromptFuture.complete(result.get());
            } else {
                usernamePromptFuture.complete(null);
            }
        });

        return usernamePromptFuture;
    }

    /**
     * Sends a message to the server asking for their high scores
     */
    private void loadOnlineScores() {
        this.gameWindow.getCommunicator().send("HISCORES UNIQUE");
    }

    /**
     * Handles an incoming server message
     * @param msg the server message
     */
    private void handleServerMessage(String msg) {
        if (msg.startsWith("HISCORES")) {
            // Parse and load the scores
            logger.debug("In handleservermessage");
            List<Score> scores = parseScores(msg);
            Platform.runLater(() -> remoteScores.set(FXCollections.observableList(scores)));

            if (seeIfNewHighScore(scores)) {
                onlineHighScoreAchieved.set(true);
                if (!usernameFuture.isDone()) {
                    promptForUsername().thenAccept(username -> {
                        scores.add(new Score(username, finishedGame.score.getValue()));
                        sortAndTrimScoresList(scores);
                        Platform.runLater(() -> remoteScores.set(FXCollections.observableList(scores)));
                    });
                }
            } else {
                sortAndTrimScoresList(scores);
                Platform.runLater(() -> remoteScores.set(FXCollections.observableList(scores)));
            }
        }
    }

    /**
     * Loads the scores from a local scores.txt file
     * @param filePath the scores text file path, if it exists
     */
    private void loadLocalScores(String filePath) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        logger.debug("in loadlocalscores");
        File scoresFile;
        List<Score> scores = new ArrayList<>();

        // Get file path
        if (filePath == null) {
            String userDir = System.getProperty("user.dir");
            scoresFile = Paths.get(userDir, "data", "scores.txt").toFile();
        } else {
            scoresFile = new File(filePath);
        }

        // Check if file exists, if not then create some dummy scores
        if (!scoresFile.exists()) {
            logger.info("Could find scores file: " + filePath + ", creating a new file");

            scores.add(new Score("Jeff", 1));
            scores.add(new Score("Bob", 2));
            writeLocalScores(scoresFile.getPath(), scores);
        }

        BufferedReader reader = null;
        try {
            // Read file
            reader = new BufferedReader(new FileReader(scoresFile));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    scores.add(new Score(parts[0], Integer.parseInt(parts[1])));
                }
            }

        } catch (IOException e) {
            logger.info("Could not open scores file: " + filePath);
            logger.debug(e);

        // Close reader
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                logger.debug("Error closing reader", e);
            }
        }

        if (this.seeIfNewHighScore(scores)) {
            localHighScoreAchieved.set(true);
            if (!usernameFuture.isDone()) {
                promptForUsername().thenAccept(username -> {
                    scores.add(new Score(username, finishedGame.score.getValue()));
                    sortAndTrimScoresList(scores);
                    this.writeLocalScores(scoresFile.getPath(), scores);
                    Platform.runLater(() -> localScores.set(FXCollections.observableList(scores)));
                });
            }
        } else {
            sortAndTrimScoresList(scores);
            Platform.runLater(() -> localScores.set(FXCollections.observableList(scores)));
        }

        writeLocalScores(scoresFile.getPath(), scores); // Write the new scores
        logger.debug("Local scores have loaded: " + localScores.toString());
    }

    /**
     * Writes the given list of scores to the given file
     * @param filePath the scores text file path
     * @param scores the list of scores to write
     */
    private void writeLocalScores(String filePath, List<Score> scores) {
        File file = new File(filePath);
        try {
            // Create file if it doesn't already exist
            if (!file.exists() && !file.createNewFile()) {
                logger.info("Failed to create scores file: " + filePath);
                return;
            }

            // Write the scores
            // Using the try block like this automatically closes the buffered writer
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
                scores.sort(Comparator.comparingInt(Score::getScore).reversed());
                for (Score score : scores) {
                    writer.write(score.getName() + ":" + score.getScore());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            logger.info("Could not open scores file: " + filePath, e);
        }
    }

    /**
     * Sends a message to the server to submit the given new high-score
     * @param newHighScore the new high score to write to the server
     */
    private void writeOnlineScore(Score newHighScore) {
        this.gameWindow.getCommunicator().send("HISCORE " + newHighScore.getName() + ":" + newHighScore.getScore());
    }

    /**
     * Sees if the score in the finished game object beats any of the high scores
     * @param scores the list of scores loaded in from the file
     * @return a boolean determining whether there is a new high score
     */
    private boolean seeIfNewHighScore(List<Score> scores) {
        int scoreThreshold = Collections.min(scores, Comparator.comparingInt(Score::getScore)).getScore();
        return this.finishedGame.score.getValue() > scoreThreshold;
    }

    /**
     * Sorts the given list of scores by descending, and limits the list to the top 10 highest scores
     * @param scores the list to sort and trim
     */
    private void sortAndTrimScoresList(List<Score> scores) {
        // Sort list of scores
        scores.sort(Comparator.comparingInt(Score::getScore).reversed());

        // If the list of scores is bigger than the maxScoreArraySize, then remove all the scores below that
        if (scores.size() > maxScoreArraySize)
            scores.subList(maxScoreArraySize, scores.size()).clear();
    }

    /**
     * Plays the reveal animation on the online and local score list components
     */
    private void revealScores() {
        Platform.runLater(() -> {
            logger.debug("Revealing scores!");
            this.localScoresListComponent.revealScores();
            this.onlineScoresListComponent.revealScores();
        });
    }

    private List<Score> parseScores(String scores) {
        String[] scoresStr = scores.substring("HISCORES ".length()).split("\n");
        List<Score> scoresArr = new ArrayList<>();

        // Loop over each score and add to array
        for (String scoreStr : scoresStr) {
            String[] parts = scoreStr.split(":");
            if (parts.length == 2) {

                // Catch the error if one of the scores has a value of "null"
                try {
                    scoresArr.add(new Score(parts[0], Integer.parseInt(parts[1])));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return scoresArr;
    }

    private static CompletableFuture<Void> waitAsync(long time, TimeUnit unit) {
        return CompletableFuture.runAsync(() -> {
            try {
                unit.sleep(time);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        });
    }
}
