package uk.ac.soton.comp1206.game;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.Objects;

/**
 * A class for playing sound effects and music
 */
public class Multimedia {

    /**
     * The instance of this class.
     * This is so you can treat the class as a static class, without it actually being one.
     * I've done this so I can use it my components AND scenes easily
     */
    private static Multimedia instance;

    /**
     * The media player for the sound effects
     */
    private MediaPlayer audioPlayer;

    /**
     * The media player for the music
     */
    private MediaPlayer musicPlayer;

    /**
     * The constructor for this class
     */
    private Multimedia() {}

    /**
     * Gets the current instance of this object.
     * If there isn't an instance, it will create one.
     * @return the Multimedia object
     */
    public static Multimedia getInstance() {
        if (instance == null) {
            instance = new Multimedia();
        }
        return instance;
    }

    /**
     * Plays a short sound effect
     * @param audioFileName the name of the sound effect file
     */
    public void playAudioFile(String audioFileName) {
        if (this.audioPlayer != null) this.audioPlayer.stop();
        Media sound = createMedia("/sounds/" + audioFileName);
        this.audioPlayer = new MediaPlayer(sound);
        this.audioPlayer.play();
    }

    /**
     * Plays background music
     * @param musicFileName the name of the music file
     */
    public void playBackgroundMusic(String musicFileName) {
        if (this.musicPlayer != null) this.musicPlayer.stop();
        Media music = createMedia("/music/" + musicFileName);
        this.musicPlayer = new MediaPlayer(music);
        this.musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.play();
    }

    /**
     * Creates a media object from a given resource path
     * This is so we can load the music from the maven/resources
     * @param resourcePath the filepath of the resource
     * @return the new media object
     */
    private Media createMedia(String resourcePath) {
        URL resource = getClass().getResource(resourcePath);
        Objects.requireNonNull(resource, "Resource not found: " + resourcePath);
        return new Media(resource.toString());
    }
}
