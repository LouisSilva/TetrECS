package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    /**
     * The logger for this class
     */
    private static final Logger logger = LogManager.getLogger(ChallengeScene.class);

    /**
     * The game object that this scene represents visually
     */
    private Game game;

    /**
     * The UI copy of the game's score
     */
    protected final IntegerProperty score = new SimpleIntegerProperty(0);

    /**
     * The UI copy of the game level
     */
    private final IntegerProperty level = new SimpleIntegerProperty(0);

    /**
     * The UI copy of the game lives
     */
    protected final IntegerProperty lives = new SimpleIntegerProperty(3);

    /**
     * The UI copy of the game's score multiplier
     */
    private final IntegerProperty multiplier = new SimpleIntegerProperty(1);

    /**
     * The UI copy of the game's local highscore
     */
    private final IntegerProperty highscore = new SimpleIntegerProperty(0);

    /**
     * The main GameBoard component which the game is played on
     */
    protected GameBoard gameBoard;

    /**
     * The PieceBoard component that holds the current piece
     */
    protected PieceBoard currentPieceBoard;

    /**
     * The PieceBoard component that holds the following piece
     */
    protected PieceBoard followingPieceBoard;

    /**
     * The highscore label, needed so I can rebind the value at a later date
     */
    private Label highScoreLabel;

    /**
     * The rectangle representing the timer
     */
    protected Rectangle timerBar;

    /**
     * The Timeline of the timer which handles all the keyframes for smoothly changing the timerBar scale and colour
     */
    private Timeline timeline;


    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
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

        // Highscore label
        VBox highScoreBox = new VBox();
        Label highScoreHeader = new Label();
        highScoreLabel = new Label();
        highScoreHeader.textProperty().set("High Score");
        highScoreLabel.textProperty().bind(score.asString("%d")); // Bind it to score initially
        highScoreBox.getStyleClass().add("stat-container");
        highScoreHeader.getStyleClass().add("heading");
        highScoreLabel.getStyleClass().add("hiscore");
        highScoreBox.getChildren().addAll(highScoreHeader, highScoreLabel);

        // Level label
        VBox levelBox = new VBox();
        Label levelHeader = new Label();
        Label levelLabel = new Label();
        levelHeader.textProperty().set("Level");
        levelLabel.textProperty().bind(level.asString("%d"));
        levelBox.getStyleClass().add("stat-container");
        levelHeader.getStyleClass().add("heading");
        levelLabel.getStyleClass().add("level");
        levelBox.getChildren().addAll(levelHeader, levelLabel);

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

        sidebar.getChildren().addAll(highScoreBox, levelBox, incomingLabel, currentPieceBoard, followingPieceBoard);

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
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        // Start new game
        this.createGameInstance();

        // Add binds
        this.bindScore(this.getGame().score);
        this.bindLevel(this.getGame().level);
        this.bindLives(this.getGame().lives);
        this.bindMultiplier(this.getGame().multiplier);

        this.getGame().setNextPieceListener(this::nextPiece);
        this.getGame().setRotatePieceListener(this::rotatePiece);
        this.getGame().setGameLoopListener(this::resetTimerBar);
        this.getGame().setLineClearedListener(this::fadeOut);
        this.getGame().setEndGameListener(this::handleEndGame);
        this.getGame().setPlayAudioListener(this::handlePlayAudio);
    }

    /**
     * A method which can be overridden that creates a new game instance
     */
    protected void createGameInstance() {
        this.game = new Game(5, 5);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        this.getGame().start();

        getScene().setOnKeyPressed(this::handleKeyPressed);
        getScene().setOnMouseClicked(this::handleRightClick);

        getInitialHighScore();

        // Play background music
        Multimedia.getInstance().playBackgroundMusic("game_start.wav");
        waitAsync(46, TimeUnit.SECONDS).thenRun(() -> Multimedia.getInstance().playBackgroundMusic("game.wav"));
    }

    /**
     * Gets the initial highscore and displays it
     */
    protected void getInitialHighScore() {
        String userDir = System.getProperty("user.dir");
        File scoresFile = Paths.get(userDir, "data", "scores.txt").toFile();

        // Handle if the scores file does not exist
        if (!scoresFile.exists()) {
            logger.warn("Scores file does not exist. Filepath tried: {}", scoresFile.getPath());
        }

        // Load the high score
        BufferedReader reader = null;
        try {
            // Read file
            reader = new BufferedReader(new FileReader(scoresFile));
            List<ScoresScene.Score> scores = new ArrayList<>();
            String line;

            // Loop over all the scores
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    scores.add(new ScoresScene.Score(parts[0], Integer.parseInt(parts[1])));
                }
            }

            // Get the one with the highest score
            highscore.set(Collections.max(scores, Comparator.comparingInt(ScoresScene.Score::getScore)).getScore());

            // Unbind the highscore label from the score, and bind it to the high score
            highScoreLabel.textProperty().unbind();
            highScoreLabel.textProperty().bind(highscore.asString("%d"));

        } catch (IOException e) {
            logger.error("Could not open scores file: {}", scoresFile.getPath());
            logger.debug(e);
            return;

            // Close reader
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                logger.debug("Error closing reader", e);
            }
        }
    }

    /**
     * Resets the timer bar
     */
    protected void resetTimerBar() {
        // Reset any existing timeline
        if (timeline != null) {
            timeline.stop();
        }

        timerBar.setWidth(gameWindow.getWidth());
        timerBar.setFill(Color.GREEN);

        // Create timeline with keyframes
        timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(timerBar.widthProperty(), gameWindow.getWidth())),
                new KeyFrame(Duration.seconds((this.getGame().getTimerDelay() / 1000.0) * 0.7), e -> timerBar.setFill(Color.RED)),
                new KeyFrame(Duration.seconds(this.getGame().getTimerDelay() / 1000.0), new KeyValue(timerBar.widthProperty(), 0))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    /**
     * Updates the current and following piece boards with the new pieces
     * @param currentGamePiece the new current piece (usually the old following piece)
     * @param followingGamePiece the new following piece
     */
    private void nextPiece(GamePiece currentGamePiece, GamePiece followingGamePiece) {
        this.currentPieceBoard.displayPiece(currentGamePiece);
        this.followingPieceBoard.displayPiece(followingGamePiece);
    }

    /**
     * Rotates the current piece
     * @param currentGamePiece the new piece that has already been rotated by the game object
     */
    private void rotatePiece(GamePiece currentGamePiece) {
        this.currentPieceBoard.displayPiece(currentGamePiece);
        Multimedia.getInstance().playAudioFile("rotate.wav");
    }

    /**
     * Handles what to do when there is a right click
     * @param mouseEvent the mouse event generated by the click
     */
    private void handleRightClick(MouseEvent mouseEvent) {
        MouseButton button = mouseEvent.getButton();

        switch (button) {
            case SECONDARY -> this.getGame().rotateCurrentPiece();
        }
    }

    /**
     * Handles what to do when a key is pressed
     * @param keyEvent the key event generated by the pressed key
     */
    private void handleKeyPressed(KeyEvent keyEvent) {
        KeyCode keyCode = keyEvent.getCode();

        switch (keyCode) {
            case ESCAPE -> {
                this.getGame().remoteEndGame();
                gameWindow.loadScene(new MenuScene(gameWindow));
            }

            case Q, Z, OPEN_BRACKET -> this.getGame().rotateCurrentPiece(3);

            case E, C, CLOSE_BRACKET -> this.getGame().rotateCurrentPiece();

            case SPACE, R -> this.getGame().swapCurrentPiece();

            case ENTER, X -> this.getGame().dropCurrentPiece();

            case O -> this.getGame().remoteEndGame(); // only used for testing

            case LEFT, RIGHT, UP, DOWN, W, A, S, D -> this.handleBlockSelectionByKeyboard(keyCode);
        }
    }

    /**
     * Selects a new game block when the keyboard is used as the controls
     * @param keyCode the key pressed
     */
    private void handleBlockSelectionByKeyboard(KeyCode keyCode) {
        GameBlockCoordinate currentlySelectedGameBlockCoordinate = this.gameBoard.gameBlockCurrentlySelected.getCoordinates();
        GameBlockCoordinate newCoordinate;

        switch (keyCode) {
            case UP, W -> newCoordinate = currentlySelectedGameBlockCoordinate.add(0, -1);
            case DOWN, S -> newCoordinate = currentlySelectedGameBlockCoordinate.add(0, 1);
            case LEFT, A -> newCoordinate = currentlySelectedGameBlockCoordinate.add(-1, 0);
            case RIGHT, D -> newCoordinate = currentlySelectedGameBlockCoordinate.add(1, 0);
            default -> {
                logger.warn("The handleBlockSelectionByKeyboard method failed. This should not happen.");
                newCoordinate = currentlySelectedGameBlockCoordinate.add(0, 0);
            }
        }

        this.gameBoard.gameBlockCurrentlySelected = this.gameBoard.getBlock(newCoordinate.getX(), newCoordinate.getY());

        // Clear any hover effects first
        for (int y=0; y < this.gameBoard.getRows(); y++) {
            for (int x=0; x < this.gameBoard.getCols(); x++) {
                GameBlock currentBlock = this.gameBoard.getBlock(x, y);
                currentBlock.onHover(GameBlock.EnterOrExit.EXIT);
            }
        }

        // Apply the hover effect
        this.gameBoard.gameBlockCurrentlySelected.onHover(GameBlock.EnterOrExit.ENTER);
    }

    /**
     * Tells the game object to rotate the current piece when a block in the piece is clicked
     * @param gameBlock the game block that was clicked
     */
    protected void currentPieceBoardClicked(GameBlock gameBlock) {
        this.getGame().rotateCurrentPiece();
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clicked
     */
    protected void blockClicked(GameBlock gameBlock) {
        this.getGame().blockClicked(gameBlock);
    }

    /**
     * Handles what happens when the game has ended
     * @param finalGameObject the game object
     */
    private void handleEndGame(Game finalGameObject) {
        this.gameWindow.loadScene(new ScoresScene(this.gameWindow, finalGameObject));
    }

    /**
     * Fades out the the game blocks corresponding to the given game block coordinates
     * After it will update the highscore value if needed
     * @param gameBlockCoordinates the coordinates of the game blocks to fade out
     */
    private void fadeOut(Set<GameBlockCoordinate> gameBlockCoordinates) {
        gameBoard.fadeOut(gameBlockCoordinates);
        Multimedia.getInstance().playAudioFile("clear.wav");

        // Check if the highscore has been broken, if so then update it
        if (this.getGame().score.getValue() > highscore.getValue() && this.highScoreLabel != null) {
            highScoreLabel.textProperty().unbind();
            highScoreLabel.textProperty().bind(score.asString("%d"));
        }
    }

    /**
     * Plays an audio file given by the listener
     * @param audioFile the audio file to play
     */
    private void handlePlayAudio(String audioFile) {
        Multimedia.getInstance().playAudioFile(audioFile);
    }

    /**
     * Bind the score of the game to the UI copy
     * @param input property to bind the value to
     */
    private void bindScore(ObservableValue<? extends Number> input) {
        this.score.bind(input);
    }

    /**
     * Bind the level of the game to the UI copy
     * @param input property to bind the value to
     */
    private void bindLevel(ObservableValue<? extends Number> input) {
        this.level.bind(input);
    }

    /**
     * Bind the lives in the game to the UI copy
     * @param input property to bind the value to
     */
    private void bindLives(ObservableValue<? extends Number> input) {
        this.lives.bind(input);
    }

    /**
     * Bind the multiplier of the game to the UI copy
     * @param input property to bind the value to
     */
    private void bindMultiplier(ObservableValue<? extends Number> input) {
        this.multiplier.bind(input);
    }

    /**
     * Returns the game object
     * @return the game object
     */
    protected Game getGame() {
        return this.game;
    }

    /**
     * Waits on the thread that it was called in for a specified time
     * @param time the amount of time to wait for
     * @param unit the unit of time to be used on the time parameter
     * @return the completable future
     */
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
