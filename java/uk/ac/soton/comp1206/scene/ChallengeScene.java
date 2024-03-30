package uk.ac.soton.comp1206.scene;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.event.RotatePieceListener;
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
    protected Game game;

    private final IntegerProperty score = new SimpleIntegerProperty(0);
    private final IntegerProperty level = new SimpleIntegerProperty(0);
    private final IntegerProperty lives = new SimpleIntegerProperty(3);
    private final IntegerProperty multiplier = new SimpleIntegerProperty(1);

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

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        var board = new GameBoard(game.getGrid(), (double) gameWindow.getWidth() /2, (double) gameWindow.getWidth() /2);
        board.getStyleClass().add("gameBox");

        // Build header
        HBox header = new HBox();
        header.setSpacing(20);
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("challenge-header");

        VBox scoreBox = new VBox();
        Label scoreHeader = new Label();
        Label scoreLabel = new Label();
        scoreHeader.textProperty().set("Score");
        scoreLabel.textProperty().bind(score.asString("%d"));
        scoreBox.getStyleClass().add("stat-container");
        scoreHeader.getStyleClass().add("heading");
        scoreLabel.getStyleClass().add("score");

        Label titleLabel = new Label("Challenge Mode");
        titleLabel.getStyleClass().add("title");

        VBox livesBox = new VBox();
        Label livesHeader = new Label();
        Label livesLabel = new Label();
        livesHeader.textProperty().set("Lives");
        livesLabel.textProperty().bind(lives.asString("%d"));
        livesBox.getStyleClass().add("stat-container");
        livesHeader.getStyleClass().add("heading");
        livesLabel.getStyleClass().add("lives");

        scoreBox.getChildren().addAll(scoreHeader, scoreLabel);
        livesBox.getChildren().addAll(livesHeader, livesLabel);
        header.getChildren().addAll(scoreBox, titleLabel);

        // Build sidebar
        VBox sidebar = new VBox();
        sidebar.setSpacing(10);
        sidebar.setAlignment(Pos.CENTER);

        VBox highScoreBox = new VBox();
        Label highScoreHeader = new Label();
        Label highScoreLabel = new Label();
        highScoreHeader.textProperty().set("Highscore");
        highScoreLabel.textProperty().bind(score.asString("%d"));
        highScoreBox.getStyleClass().add("stat-container");
        highScoreHeader.getStyleClass().add("heading");
        highScoreLabel.getStyleClass().add("hiscore");

        VBox levelBox = new VBox();
        Label levelHeader = new Label();
        Label levelLabel = new Label();
        levelHeader.textProperty().set("Level");
        levelLabel.textProperty().bind(level.asString("%d"));
        levelBox.getStyleClass().add("stat-container");
        levelHeader.getStyleClass().add("heading");
        levelLabel.getStyleClass().add("level");

        PieceBoard currentPieceBoard = new PieceBoard(
                new Grid(3, 3),
                (double) gameWindow.getWidth() / 10,
                (double) gameWindow.getHeight() / 10);

        PieceBoard followingPieceBoard = new PieceBoard(
                new Grid(3, 3),
                (double) gameWindow.getWidth() / 12,
                (double) gameWindow.getHeight() / 12);

        currentPieceBoard.getStyleClass().add("gameBox");
        followingPieceBoard.getStyleClass().add("gameBox");

        highScoreBox.getChildren().addAll(highScoreHeader, highScoreLabel);
        levelBox.getChildren().addAll(levelHeader, levelLabel);
        sidebar.getChildren().addAll(livesBox, highScoreBox, levelBox, currentPieceBoard, followingPieceBoard);

        // Build footer
        StackPane footer = new StackPane();

        mainPane.setCenter(board);
        mainPane.setTop(header);
        mainPane.setRight(sidebar);
        mainPane.setBottom(footer);

        // Handle block on game-board grid being clicked
        board.setOnBlockClick(this::blockClicked);
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
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
        game.setSwapPieceListener(this::swapPiece);
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

    private void nextPiece(GamePiece nextGamePiece, GamePiece followingGamePiece) {
        logger.debug("next piece yeeeeeeee");
    }

    private void rotatePiece(GamePiece currentGamePiece) {

    }

    private void swapPiece(GamePiece currentGamePiece, GamePiece followingGamePiece) {

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
