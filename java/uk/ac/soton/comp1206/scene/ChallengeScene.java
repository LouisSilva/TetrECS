package uk.ac.soton.comp1206.scene;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);
    private Game game;

    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private final IntegerProperty level = new SimpleIntegerProperty(0);
    private final IntegerProperty lives = new SimpleIntegerProperty(3);
    private final IntegerProperty multiplier = new SimpleIntegerProperty(1);

    private PieceBoard currentPieceBoard;
    private PieceBoard followingPieceBoard;


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
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        BorderPane mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        // Build footer
        StackPane footer = new StackPane();
        ProgressBar progressBar = new ProgressBar();
        footer.getChildren().addAll(progressBar);

        // Build game board
        var board = new GameBoard(game.getGrid(), (double) gameWindow.getWidth() / 2, (double) gameWindow.getWidth() / 2);
        board.getStyleClass().add("gameBox");
        board.setOnBlockClick(this::blockClicked);

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
        scoreBox.getChildren().addAll(scoreHeader, scoreLabel);

        // Challenge Mode title label
        Label titleLabel = new Label("Challenge Mode");
        titleLabel.getStyleClass().add("title");

        // Lives label
        VBox livesBox = new VBox();
        Label livesHeader = new Label();
        Label livesLabel = new Label();
        livesHeader.textProperty().set("Lives");
        livesLabel.textProperty().bind(lives.asString("%d"));
        livesBox.getStyleClass().add("stat-container");
        livesHeader.getStyleClass().add("heading");
        livesLabel.getStyleClass().add("lives");
        livesBox.setAlignment(Pos.TOP_RIGHT);
        livesBox.getChildren().addAll(livesHeader, livesLabel);

        header.getChildren().addAll(scoreBox, titleLabel, livesBox);

        // Build sidebar
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");

        // Highscore label
        VBox highScoreBox = new VBox();
        Label highScoreHeader = new Label();
        Label highScoreLabel = new Label();
        highScoreHeader.textProperty().set("High Score");
        highScoreLabel.textProperty().bind(score.asString("%d"));
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
                (double) gameWindow.getWidth() / 8);

        followingPieceBoard = new PieceBoard(
                3, 3,
                (double) gameWindow.getWidth() / 10,
                (double) gameWindow.getWidth() / 10);

        currentPieceBoard.getStyleClass().add("gameBox");
        followingPieceBoard.getStyleClass().add("gameBox");

        sidebar.getChildren().addAll(highScoreBox, levelBox, incomingLabel, currentPieceBoard, followingPieceBoard);

        mainPane.setBottom(footer);
        mainPane.setCenter(board);
        mainPane.setTop(header);
        mainPane.setRight(sidebar);
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);

        // Add binds
        this.bindScore(game.score);
        this.bindLevel(game.level);
        this.bindLives(game.lives);
        this.bindMultiplier(game.multiplier);

        game.setNextPieceListener(this::nextPiece);
        game.setRotatePieceListener(this::rotatePiece);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        game.start();

        getScene().setOnKeyPressed(this::handleKeyPressed);
    }

    private void nextPiece(GamePiece currentGamePiece, GamePiece followingGamePiece) {
        this.currentPieceBoard.displayPiece(currentGamePiece);
        this.followingPieceBoard.displayPiece(followingGamePiece);
    }

    private void rotatePiece(GamePiece currentGamePiece) {
        this.currentPieceBoard.displayPiece(currentGamePiece);
    }

    private void handleKeyPressed(KeyEvent keyEvent) {
        KeyCode keyCode = keyEvent.getCode();

        switch (keyCode) {
            case ESCAPE -> {
                game.exitGame();
                gameWindow.loadScene(new MenuScene(gameWindow));
            }

            case Q, Z, OPEN_BRACKET -> {
                game.rotateCurrentPiece(3);
            }

            case E, C, CLOSE_BRACKET -> {
                game.rotateCurrentPiece();
            }

            case SPACE, R -> {
                game.swapCurrentPiece();
            }

            case ENTER, X -> {
                game.dropCurrentPiece();
            }
        }
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
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
}
