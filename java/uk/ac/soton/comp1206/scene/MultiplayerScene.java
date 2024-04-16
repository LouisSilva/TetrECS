package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.Leaderboard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MultiplayerScene extends ChallengeScene {
    /**
     * Logger for debugging
     */
    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

    private MultiplayerGame game;

    private final int maxLeaderboardEntries = 5;

    private SimpleListProperty<Leaderboard.LeaderboardEntry> leaderboardEntries;

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow) {
        super(gameWindow);
    }

    @Override
    public void build() {
        logger.info("Building {}", this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("challenge-background");
        root.getChildren().add(challengePane);

        BorderPane mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        // Build game board
        gameBoard = new GameBoard(this.getGame().getGrid(), (double) gameWindow.getWidth() / 2, (double) gameWindow.getWidth() / 2);
        gameBoard.getStyleClass().add("gameBox");
        gameBoard.setOnBlockClick(this::blockClicked);

        // Build header
        HBox header = new HBox();
        header.setSpacing(20);
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("challenge-header");

        // Score label
        VBox scoreBox = new VBox();
        Label scoreHeader = new Label();
        Label scoreLabel = new Label();
        scoreHeader.textProperty().set("Score");
        scoreLabel.textProperty().bind(score.asString("%d"));
        scoreBox.getStyleClass().add("stat-container");
        scoreHeader.getStyleClass().add("heading");
        scoreLabel.getStyleClass().add("score");
        scoreLabel.setMinWidth(Label.USE_PREF_SIZE);
        scoreBox.setMinSize(VBox.USE_PREF_SIZE, VBox.USE_PREF_SIZE);
        scoreBox.getChildren().addAll(scoreHeader, scoreLabel);

        // Challenge Mode title label
        Label titleLabel = new Label("Challenge Mode");
        titleLabel.getStyleClass().add("title");
        titleLabel.setMinWidth(Label.USE_PREF_SIZE);

        // Lives label
        VBox livesBox = new VBox();
        Label livesHeader = new Label();
        Label livesLabel = new Label();
        livesHeader.textProperty().set("Lives");
        livesLabel.textProperty().bind(lives.asString("%d"));
        livesBox.getStyleClass().add("stat-container");
        livesHeader.getStyleClass().add("heading");
        livesLabel.getStyleClass().add("lives");
        livesLabel.setMinWidth(Label.USE_PREF_SIZE);
        livesBox.setAlignment(Pos.TOP_RIGHT);
        livesBox.setMinSize(VBox.USE_PREF_SIZE, VBox.USE_PREF_SIZE);
        livesBox.getChildren().addAll(livesHeader, livesLabel);

        header.getChildren().addAll(scoreBox, titleLabel, livesBox);

        // Build sidebar
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        // Incoming label
        Label incomingLabel = new Label();
        incomingLabel.textProperty().set("Incoming");
        incomingLabel.getStyleClass().add("heading");

        // Piece boards
        currentPieceBoard = new PieceBoard(
                3, 3,
                (double) gameWindow.getWidth() / 8,
                (double) gameWindow.getWidth() / 8,
                true);

        followingPieceBoard = new PieceBoard(
                3, 3,
                (double) gameWindow.getWidth() / 10,
                (double) gameWindow.getWidth() / 10);

        currentPieceBoard.getStyleClass().add("gameBox");
        currentPieceBoard.setOnBlockClick(this::currentPieceBoardClicked);
        followingPieceBoard.getStyleClass().add("gameBox");

        // Verses Label
        Label versesLabel = new Label();
        versesLabel.textProperty().set("Verses");
        versesLabel.getStyleClass().add("heading");

        // Leaderboard
        List<Leaderboard.LeaderboardEntry> leaderboardEntryList = new ArrayList<>();
        ObservableList<Leaderboard.LeaderboardEntry> observableLeaderboardEntryList = FXCollections.observableArrayList(leaderboardEntryList);
        this.leaderboardEntries = new SimpleListProperty<>(observableLeaderboardEntryList);
        Leaderboard leaderboard = new Leaderboard();
        leaderboard.getLeaderboardEntries().bind(this.leaderboardEntries);
        this.gameWindow.getCommunicator().addListener(this::handleServerMessage);

        sidebar.getChildren().addAll(versesLabel, leaderboard, incomingLabel, currentPieceBoard, followingPieceBoard);

        // Build footer with timer
        StackPane footer = new StackPane();
        this.timerBar = new Rectangle(0, 20);
        this.timerBar.setFill(Color.GREEN);
        footer.getChildren().add(timerBar);

        mainPane.setBottom(footer);
        mainPane.setCenter(gameBoard);
        mainPane.setTop(header);
        mainPane.setRight(sidebar);

        this.resetTimerBar();
    }

    /**
     * Initialise the scene
     */
    @Override
    public void initialise() {
        super.initialise();
        this.gameWindow.getCommunicator().send("SCORES ");
    }

    /**
     * Handles an incoming server message
     * @param msg the server message
     */
    private void handleServerMessage(String msg) {
        if (msg.startsWith("SCORES")) {

            // Parse and load the scores
            Platform.runLater(() -> {
                List<Leaderboard.LeaderboardEntry> scores = parseStringScores(msg);
                scores.sort(Comparator.comparingInt(Leaderboard.LeaderboardEntry::getScore).reversed()); // Sort list of scores

                this.game.allScores = scores.stream().map(leaderboardEntry -> new ScoresScene.Score(leaderboardEntry.getName(), leaderboardEntry.getScore())).collect(Collectors.toList());
                List<Leaderboard.LeaderboardEntry> finalScores = scores.subList(0, Math.min(maxLeaderboardEntries, scores.size()));
                leaderboardEntries.set(FXCollections.observableArrayList(finalScores));
            });
        }
    }

    /**
     * Converts a given string of scores into a list of leaderboard entry objects
     * @param scores the string of scores to parse
     * @return the list of leaderboard entry objects
     */
    private List<Leaderboard.LeaderboardEntry> parseStringScores(String scores) {
        String[] scoresStr = scores.substring("SCORES ".length()).split("\n");
        List<Leaderboard.LeaderboardEntry> scoresArr = new ArrayList<>();

        // Loop over each score and add to array
        for (String scoreStr : scoresStr) {
            String[] parts = scoreStr.split(":");
            if (parts.length == 3) {

                // Catch the error if one of the entries has a value of null
                try {
                    int lives;
                    boolean isAlive;

                    if (parts[2].equals("-1")) {
                        lives = -1;
                        isAlive = false;
                    } else {
                        lives = Integer.parseInt(parts[2]);
                        isAlive = true;
                    }

                    scoresArr.add(new Leaderboard.LeaderboardEntry(parts[0], Integer.parseInt(parts[1]), lives, isAlive));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return scoresArr;
    }

    /**
     * A method which can be overridden that creates a new game instance
     */
    @Override
    protected void createGameInstance() {
        this.game = new MultiplayerGame(5, 5, this.gameWindow.getCommunicator());
    }

    /**
     * Returns the game object
     * @return the game object
     */
    @Override
    protected Game getGame() {
        return this.game;
    }

    /**
     * Overrides the original getInitialHighScore method to do nothing, because the multiplayer version doesnt have a highscore label
     */
    protected void getInitialHighScore() {

    }
}
