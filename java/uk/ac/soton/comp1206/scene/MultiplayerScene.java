package uk.ac.soton.comp1206.scene;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.ui.GameWindow;

public class MultiplayerScene extends ChallengeScene {
    /**
     * Logger for debugging
     */
    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

    private MultiplayerGame game;

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow) {
        super(gameWindow);
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
}
