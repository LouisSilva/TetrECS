package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

public interface SwapPieceListener {
    void swapPiece(GamePiece currentGamePiece, GamePiece followingGamePiece);
}
