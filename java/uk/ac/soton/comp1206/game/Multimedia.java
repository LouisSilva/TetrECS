package uk.ac.soton.comp1206.game;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.Objects;

public class Multimedia {

    private static Multimedia instance;

    private MediaPlayer audioPlayer;
    private MediaPlayer musicPlayer;

    private Multimedia() {}

    public static Multimedia getInstance() {
        if (instance == null) {
            instance = new Multimedia();
        }
        return instance;
    }

    public void playAudioFile(String audioFileName) {
        if (this.audioPlayer != null) this.audioPlayer.stop();
        Media sound = createMedia("/sounds/" + audioFileName);
        this.audioPlayer = new MediaPlayer(sound);
        this.audioPlayer.play();
    }

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
