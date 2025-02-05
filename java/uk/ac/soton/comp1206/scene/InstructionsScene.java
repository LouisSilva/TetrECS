package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;

/**
 * The Instructions scene which displays the instructions on how to play the game
 */
public class InstructionsScene extends BaseScene {

    /**
     * The logger used for debugging
     */
    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     * @param gameWindow the game window
     */
    public InstructionsScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Instructions Scene");
    }

    /**
     * Initializes the scene and sets the on key pressed event listener
     */
    @Override
    public void initialise() {
        logger.info("Initialising Instructions");

        getScene().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                gameWindow.loadScene(new MenuScene(gameWindow));
            }
        });
    }

    /**
     * Build the Instructions window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());
        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var instructionPane = new StackPane();
        instructionPane.setMaxWidth(gameWindow.getWidth());
        instructionPane.setMaxHeight(gameWindow.getHeight());
        instructionPane.getStyleClass().add("menu-background");
        root.getChildren().add(instructionPane);

        BorderPane mainPane = new BorderPane();
        instructionPane.getChildren().add(mainPane);

        // Build header
        HBox header = new HBox();
        VBox textBox = new VBox();
        Label titleLabel = new Label();
        Label descriptionLabel = new Label();
        titleLabel.textProperty().set("Instructions");
        descriptionLabel.textProperty().set(instructionsDescriptionText);
        titleLabel.getStyleClass().add("heading");
        descriptionLabel.getStyleClass().add("small-text");
        textBox.getStyleClass().add("stat-container");
        textBox.getChildren().addAll(titleLabel, descriptionLabel);
        header.getChildren().add(textBox);
        mainPane.setTop(header);

        // Add instructions image
        VBox centreBox = new VBox();
        centreBox.getStyleClass().add("stat-container");
        Image instructionsImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Instructions.png")));
        ImageView instructionsImageView = new ImageView(instructionsImage);
        instructionsImageView.setPreserveRatio(true);
        instructionsImageView.setFitWidth(500);

        // Add game pieces title
        Label gamePiecesLabel = new Label();
        gamePiecesLabel.textProperty().set("Game Pieces");
        gamePiecesLabel.getStyleClass().add("heading");

        // Add dynamic piece boards
        VBox pieceBoardContainer = new VBox();
        pieceBoardContainer.setAlignment(Pos.CENTER);

        double pieceBoardWidth = (double) this.gameWindow.getWidth() / 10;
        double pieceBoardHeight = (double) this.gameWindow.getHeight() / 10;

        Random random = new Random();
        int[] pieceBoardsPerRow = {8, 7};

        // Get a HashSet of all the game pieces to display
        HashSet<Integer> gamePiecesSet = new HashSet<>();
        for (int i=0; i < GamePiece.PIECES; i++) {
            gamePiecesSet.add(i);
        }

        // Loop over all the rows and columns to add the piece boards
        for (int numPieceBoards : pieceBoardsPerRow) {
            HBox rowContainer = new HBox();
            rowContainer.setAlignment(Pos.CENTER);

            for (int col=0; col < numPieceBoards; col++) {
                PieceBoard pieceBoard = new PieceBoard(3, 3, pieceBoardWidth, pieceBoardHeight);
                pieceBoard.getStyleClass().add("gameBox");

                // Picks the random piece to display
                if (!gamePiecesSet.isEmpty()) {
                    int index = random.nextInt(gamePiecesSet.size());
                    Iterator<Integer> iterator = gamePiecesSet.iterator();

                    // Choose random game piece to display
                    for (int i=0; i < index; i++) {
                        iterator.next();
                    }

                    int gamePieceIndex = iterator.next();
                    gamePiecesSet.remove(gamePieceIndex);

                    GamePiece gamePieceToDisplay = GamePiece.createPiece(gamePieceIndex);
                    pieceBoard.displayPiece(gamePieceToDisplay);
                    rowContainer.getChildren().add(pieceBoard);
                }
            }

            pieceBoardContainer.getChildren().add(rowContainer);
        }

        centreBox.getChildren().addAll(instructionsImageView, gamePiecesLabel, pieceBoardContainer);
        mainPane.setCenter(centreBox);
    }

    /**
     * The game description displayed in the window
     */
    private static final String instructionsDescriptionText = "TetrECS is a fast-paced gravity-free block placement game, where you must survive by clearing rows through careful placement of the upcoming blocks before the time runs out. Lose all 3 lives and you're destroyed!";
}
