package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.Game;

/**
 * The listener for when a game has ended
 */
public interface EndGameListener {
    /**
     * Triggers the end game event
     * @param finalGameObject the game object which game was ended
     */
    void endGame(Game finalGameObject);
}
