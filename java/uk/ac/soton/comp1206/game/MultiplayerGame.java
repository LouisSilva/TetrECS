package uk.ac.soton.comp1206.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.LinkedList;
import java.util.Queue;

public class MultiplayerGame extends Game {
    /**
     * The logger used for debugging
     */
    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

    private final Communicator communicator;

    private final Queue<GamePiece> receivedGamePieces = new LinkedList<>();

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(int cols, int rows, Communicator communicator) {
        super(cols, rows);

        this.communicator = communicator;
        this.communicator.addListener(this::handleServerMessage);

        this.requestNextGamePiece(5);
    }

    private void handleServerMessage(String msg) {
        if (msg.startsWith("PIECE")) {
            String receivedGamePieceType = msg.substring("PIECE ".length());
            logger.debug("Received game piece: {}", receivedGamePieceType);
            receivedGamePieces.add(GamePiece.GamePieceType.createGamePiece(Integer.parseInt(receivedGamePieceType) + 1));
        }
    }

    private void requestNextGamePiece() {
        this.communicator.send("PIECE");
    }

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
     * Public method that a scene can call to end the game.
     *
     */
    @Override
    public void remoteEndGame() {
        super.remoteEndGame();
        this.communicator.send("DIE");
    }
}
