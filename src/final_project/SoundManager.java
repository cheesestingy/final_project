package final_project;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class SoundManager {

    private final AudioClip hitSound;
    private final AudioClip destroySound;
    private final AudioClip powerupSound;
    private final AudioClip shootSound;
    private final AudioClip gameOverSound;
    private MediaPlayer backgroundPlayer;

    public SoundManager() {
        hitSound = loadSound("sound/hit.wav");
        destroySound = loadSound("sound/destory.wav");
        powerupSound = loadSound("sound/powerup.wav");
        shootSound = loadSound("sound/shoot.wav");
        gameOverSound = loadSound("sound/gameover.wav");
        setupBackgroundMusic();
    }

    private AudioClip loadSound(String path) {
        return new AudioClip(getClass().getResource(path).toExternalForm());
    }

    public void playHit() {
        hitSound.play();
    }

    public void playDestroy() {
        destroySound.play();
    }

    public void playPowerup() {
        powerupSound.play();
    }

    public void playShoot() {
        shootSound.play();
    }

    public void playGameOver() {
        gameOverSound.play();
    }
    private void setupBackgroundMusic() {
        Media media = new Media(
                getClass().getResource("sound/background_music.wav").toExternalForm()
        );

        backgroundPlayer = new MediaPlayer(media);
        backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        backgroundPlayer.setVolume(0.25);
    }
    public void playBackgroundMusic() {
        if (backgroundPlayer != null) {
            backgroundPlayer.play();
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundPlayer != null) {
            backgroundPlayer.stop();
        }
    }
}