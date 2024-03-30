package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

public interface NextPieceListener {
    /**
     *
     * @param nextGamePiece the next game piece
     */
    void nextPiece(GamePiece nextGamePiece, GamePiece followingGamePiece);
}
