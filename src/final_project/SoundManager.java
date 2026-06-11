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
    private final AudioClip buySound;
    private final AudioClip burnSound;
    private final AudioClip freezeSound;
    private MediaPlayer backgroundPlayer;
    private double currentVolume = 0.25;
    private double sfxVolume = 1.0;

    public SoundManager() {
        hitSound = loadSound("sound/hit.wav");
        destroySound = loadSound("sound/destory.wav");
        powerupSound = loadSound("sound/powerup.wav");
        shootSound = loadSound("sound/shoot.wav");
        buySound = loadSound("sound/buy.wav");
        burnSound = loadSound("sound/burn.wav");
        freezeSound = loadSound("sound/freeze.wav");
        gameOverSound = loadSound("sound/gameover.wav");
        setupBackgroundMusic();
        setSfxVolume(1.0);
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
    public void playBuy() {
        buySound.play();
    }
    private void setupBackgroundMusic() {
        Media media = new Media(
                getClass().getResource("sound/background_music.wav").toExternalForm()
        );

        backgroundPlayer = new MediaPlayer(media);
        backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        backgroundPlayer.setVolume(currentVolume);
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

    public void setVolume(double volume) {
        this.currentVolume = volume;
        if (backgroundPlayer != null) {
            backgroundPlayer.setVolume(volume);
        }
    }

    public double getVolume() {
        return currentVolume;
    }

    public void setSfxVolume(double volume) {
        this.sfxVolume = volume;

        // Update all individual clips at once
        hitSound.setVolume(volume);
        destroySound.setVolume(volume);
        powerupSound.setVolume(volume);
        shootSound.setVolume(volume);
        buySound.setVolume(volume);
        burnSound.setVolume(volume);
        freezeSound.setVolume(volume);
        gameOverSound.setVolume(volume);
    }

    public double getSfxVolume() {
        return sfxVolume;
    }

    public void playBurn() {
        burnSound.play();
    }

    public void playFreeze() {
        freezeSound.play();
    }
}