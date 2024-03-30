package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

public interface RotatePieceListener {
    void rotatePiece(GamePiece currentGamePiece);
}
