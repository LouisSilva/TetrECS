package uk.ac.soton.comp1206.event;

/**
 * The listener for playing audio
 */
public interface PlayAudioListener {
    /**
     * Plays the given audio file
     * @param audioFile the audio file to play
     */
    void playAudio(String audioFile);
}
