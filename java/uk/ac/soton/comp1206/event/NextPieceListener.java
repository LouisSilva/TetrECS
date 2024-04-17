package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The listener for when a new next piece is made
 */
public interface NextPieceListener {
    /**
     * Triggers the next piece event with the new current piece and the new next piece
     * @param nextGamePiece the next game piece that replaces the current piece
     * @param followingGamePiece the following game piece that replaces the next piece
     */
    void nextPiece(GamePiece nextGamePiece, GamePiece followingGamePiece);
}
