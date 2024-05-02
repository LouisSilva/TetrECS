package uk.ac.soton.comp1206.scene;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.*;

/**
 * The scene for showing an opponents game board in multiplayer mode
 */
public class OpponentBoardScene extends BaseScene {

    /**
     * The logger for this class
     */
    private static final Logger logger = LogManager.getLogger(OpponentBoardScene.class);

    /**
     * The root Stack Pane
     */
    protected StackPane root;

    /**
     * The map of opponent's usernames and their game board
     */
    public LinkedHashMap<String, PieceBoard> opponentBoards = new LinkedHashMap<>();

    /**
     * A container for displaying the piece boards
     */
    private VBox currentOpponentBoardContainer;

    /**
     * An iterator for going through the map of opponent boards in order
     */
    public ListIterator<Map.Entry<String, PieceBoard>> pieceBoardIterator;

    /**
     * A list of the entries from the opponent boards map
     */
    public ArrayList<Map.Entry<String, PieceBoard>> pieceBoardMapEntries;

    /**
     * The label containing the opponent's name
     */
    private Label opponentNameLabel;

    /**
     * An enum for clearly specifying whether to show the next opponent, or the previous one
     */
    private enum PreviousOrNext {
        /**
         * The previous opponent
         */
        PREVIOUS,

        /**
         * The next opponent
         */
        NEXT
    }

    /**
     * Create a new scene, passing in the GameWindow the scene will be displayed in
     * @param gameWindow the game window
     */
    public OpponentBoardScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating OpponentBoard Scene");
    }

    /**
     * Initialise this scene. Called after creation
     */
    @Override
    public void initialise() {
        this.pieceBoardMapEntries = new ArrayList<>(opponentBoards.entrySet());
        this.pieceBoardIterator = this.pieceBoardMapEntries.listIterator();
    }

    /**
     * Build the layout of the scene
     */
    @Override
    public void build() {
        logger.info("Building {}", this.getClass().getName());

        root = new StackPane();
        StackPane mainStackPane = new StackPane();
        mainStackPane.setMaxWidth((double) gameWindow.getWidth() / 2);
        mainStackPane.setMaxHeight(10 + (double) gameWindow.getHeight() / 2);
        mainStackPane.getStyleClass().add("opponent-board-displayer-background");
        root.getChildren().add(mainStackPane);

        BorderPane mainBorderPane = new BorderPane();
        mainStackPane.getChildren().add(mainBorderPane);

        // Build header
        HBox header = new HBox();
        header.setSpacing(10);
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("opponent-board-displayer-header");

        // Build opponent name labels
        Label opponentNameTitleLabel = new Label("Opponent: ");
        opponentNameLabel = new Label("...");
        opponentNameTitleLabel.getStyleClass().add("heading");
        opponentNameLabel.getStyleClass().add("heading");
        header.getChildren().addAll(opponentNameTitleLabel, opponentNameLabel);

        // Build the board displayer
        currentOpponentBoardContainer = new VBox();
        PieceBoard currentOpponentBoard = new PieceBoard(
                5, 5,
                (double) gameWindow.getWidth() / 4,
                (double) gameWindow.getWidth() / 4
        );
        currentOpponentBoard.getStyleClass().add("gameBox");
        currentOpponentBoardContainer.getChildren().add(currentOpponentBoard);

        // Build footer
        HBox footer = new HBox();
        footer.setSpacing(5);
        footer.setAlignment(Pos.CENTER);
        header.getStyleClass().add("opponent-board-displayer-header");

        // Build buttons to select an opponent to view
        Button previousPlayerButton = new Button("<");
        Button nextPlayerButton = new Button(">");
        previousPlayerButton.setOnAction(event -> this.changeCurrentPieceBoard(PreviousOrNext.PREVIOUS));
        nextPlayerButton.setOnAction(event -> this.changeCurrentPieceBoard(PreviousOrNext.NEXT));
        footer.getChildren().addAll(previousPlayerButton, nextPlayerButton);

        mainBorderPane.setTop(header);
        mainBorderPane.setCenter(currentOpponentBoardContainer);
        mainBorderPane.setBottom(footer);
    }

    /**
     * Handles what happens when the user presses one of the arrow buttons to display another opponent's piece board
     * @param previousOrNext whether to show the previous opponent or the next
     */
    private void changeCurrentPieceBoard(PreviousOrNext previousOrNext) {
        try {
            PieceBoard newPieceboard = switch (previousOrNext) {
                case PREVIOUS -> {
                    if (!pieceBoardIterator.hasPrevious()) {
                        this.refreshOpponentBoardMap();
                        pieceBoardIterator.previous();
                    }

                    var curEntry = pieceBoardIterator.previous();
                    opponentNameLabel.textProperty().set(curEntry.getKey());
                    yield curEntry.getValue();
                }

                default -> { // changed it to default because the IDE was getting angry that the newPieceBoard might not be initialized
                    if (!pieceBoardIterator.hasNext()) {
                        this.pieceBoardIterator.next();
                        this.refreshOpponentBoardMap();
                    }

                    var curEntry = pieceBoardIterator.next();
                    opponentNameLabel.textProperty().set(curEntry.getKey());
                    yield curEntry.getValue();
                }
            };

            currentOpponentBoardContainer.getChildren().clear();
            currentOpponentBoardContainer.getChildren().add(newPieceboard);
        } catch (NoSuchElementException ignored) {
            // Ignore this error because all it means is that there are no opponent boards in the map, and the buttons shouldn't do anything
        }

    }

    /**
     * Re-initializes the iterator for going through opponents
     */
    public void refreshOpponentBoardMap() {
        this.pieceBoardMapEntries = new ArrayList<>(opponentBoards.entrySet());
        this.pieceBoardIterator = this.pieceBoardMapEntries.listIterator();
    }
}
