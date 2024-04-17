package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlockCoordinate;

import java.util.Set;

/**
 * The listener for when a line gets cleared
 */
public interface LineClearedListener {
    /**
     * Triggers the on line cleared event with the set of GameBlockCoordinates that need to be cleared
     * @param clearedBlocks the blocks to be cleared
     */
    void onLineCleared(Set<GameBlockCoordinate> clearedBlocks);
}
