package uk.ac.soton.comp1206.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.scene.ScoresScene;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * An extension of the Game object for a multiplayer game
 */
public class MultiplayerGame extends Game {

    /**
     * The logger for this class
     */
    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

    /**
     * The communicator class for sending and receiving game information
     */
    private final Communicator communicator;

    /**
     * A queue of pieces that all players use
     */
    private final Queue<GamePiece> receivedGamePieces = new LinkedList<>();

    /**
     * A list of scores for the in-game leaderboard
     */
    public List<ScoresScene.Score> allScores = new ArrayList<>();

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     * @param communicator the communicator object used to get info to the server
     */
    public MultiplayerGame(int cols, int rows, Communicator communicator) {
        super(cols, rows);

        this.communicator = communicator;
        this.communicator.addListener(this::handleServerMessage);

        this.requestNextGamePiece(5);
    }

    /**
     * Handles what happens when a message is received from the server
     * @param msg the incoming server message
     */
    private void handleServerMessage(String msg) {
        if (msg.startsWith("PIECE")) {
            String receivedGamePieceType = msg.substring("PIECE ".length());
            logger.debug("Received game piece: {}", receivedGamePieceType);
            receivedGamePieces.add(GamePiece.GamePieceType.createGamePiece(Integer.parseInt(receivedGamePieceType) + 1));
        }
    }

    /**
     * Sends a message to the server asking for the next piece
     */
    private void requestNextGamePiece() {
        this.communicator.send("PIECE");
    }

    /**
     * Requests a given amount of next pieces from the server
     * @param pieces the amount of pieces to request
     */
    private void requestNextGamePiece(int pieces) {
        for (int i=0; i < pieces; i++) {
            requestNextGamePiece();
        }
    }

    /**
     * Sends a message to the server of the user's current game board
     */
    private void sendUpdatedBoardToServer() {
        StringBuilder sb = new StringBuilder("BOARD");
        for (int y=0; y < this.getRows(); y++) {
            for (int x=0; x < this.getCols(); x++) {
                sb.append(" ").append(this.getGrid().getGridValue(x, y));
            }
        }

        this.communicator.send(sb.toString());
    }

    /**
     * Sends a message to the server saying that the user has left the game and channel
     */
    @Override
    protected void endGame() {
        super.endGame();
        this.communicator.send("DIE");
    }

    /**
     * Sends a message to the server with the new amount of lives the player has
     */
    @Override
    protected void loseLife() {
        super.loseLife();
        this.communicator.send("LIVES " + this.lives.get());
    }

    /**
     * Handle what should happen when a piece was successfully played.
     * This overridden method sends a message to the server updating its board values
     */
    @Override
    protected void handlePlayPiece() {
        super.handlePlayPiece();
        this.sendUpdatedBoardToServer();
    }

    /**
     * Changes the current piece and following piece variables to the next piece.
     * This method overrides the original nextPiece method. It now gets the next piece from the server
     */
    @Override
    protected void nextPiece() {
        this.currentPiece = this.followingPiece;
        this.followingPiece = this.receivedGamePieces.remove();

        this.nextPieceListener.nextPiece(this.currentPiece, this.followingPiece);
        this.requestNextGamePiece(2); // Asks for 2 more pieces each time as a buffer
    }

    /**
     * Calculates the new score after a line is cleared
     * @param numOfLines the number of lines cleared
     * @param numOfBlocks the number of blocks cleared
     */
    protected void calculateNewScore(int numOfLines, int numOfBlocks) {
        super.calculateNewScore(numOfLines, numOfBlocks);
        this.communicator.send("SCORE " + this.score.get());
    }

    /**
     * Public method that a scene can call to end the game.
     */
    @Override
    public void remoteEndGame() {
        super.remoteEndGame();
        this.communicator.send("DIE");
    }
}
