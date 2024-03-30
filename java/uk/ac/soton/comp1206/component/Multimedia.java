package uk.ac.soton.comp1206.component;

import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Multimedia {

  private static final Logger logger = LogManager.getLogger(Multimedia.class);

  private MediaPlayer audioPlayer;
  private MediaPlayer musicPlayer;

  public Multimedia() {

  }

  public void playAudioFile() {

  }

  public void playBackgroundMusic() {
    this.musicPlayer.play();
  }
}
