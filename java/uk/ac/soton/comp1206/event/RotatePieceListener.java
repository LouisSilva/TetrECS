package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * A listener for when a game piece is rotated
 */
public interface RotatePieceListener {
    /**
     * Rotates the given game piece
     * @param currentGamePiece the game piece to rotate
     */
    void rotatePiece(GamePiece currentGamePiece);
}
