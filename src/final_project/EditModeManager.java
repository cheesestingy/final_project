package final_project;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditModeManager {

    private boolean editMode = false;
    private int ballTypeIndex = 0;

    private final String[] ballTypes = {
            "NORMAL", "FIRE", "ICE", "PIERCE", "SHRINK"
    };

    private GameController game;
    private Text editText;
    private Pane root;

    private MediaView mediaView;
    private MediaPlayer mediaPlayer;

    private final List<KeyCode> KONAMI_CODE = Arrays.asList(
            KeyCode.UP, KeyCode.UP, KeyCode.DOWN, KeyCode.DOWN,
            KeyCode.LEFT, KeyCode.RIGHT, KeyCode.LEFT, KeyCode.RIGHT,
            KeyCode.A, KeyCode.B
    );
    private List<KeyCode> inputQueue = new ArrayList<>();

    public EditModeManager(GameController game, Text editText, Pane root) {
        this.game = game;
        this.editText = editText;
        this.root = root;
        updateText();
    }

    public void attach(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> {

            if (!editMode) {
                inputQueue.add(e.getCode());
                if (inputQueue.size() > KONAMI_CODE.size()) {
                    inputQueue.remove(0);
                }

                if (inputQueue.equals(KONAMI_CODE)) {
                    enableEditMode();
                    inputQueue.clear();
                }
                return;
            }

            // --- IF WE ARE ALREADY IN EDIT MODE ---
            if (e.getCode() == KeyCode.E) {
                disableEditMode();
                e.consume();
                return;
            }

            if (e.getCode() == KeyCode.LEFT) {
                ballTypeIndex--;
                if (ballTypeIndex < 0) {
                    ballTypeIndex = ballTypes.length - 1;
                }
                applyBallType();
                updateText();
                e.consume();
            } else if (e.getCode() == KeyCode.RIGHT) {
                ballTypeIndex++;
                if (ballTypeIndex >= ballTypes.length) {
                    ballTypeIndex = 0;
                }
                applyBallType();
                updateText();
                e.consume();
            } else if (e.getCode() == KeyCode.UP) {
                game.moveBlocksUpOneRow();
                e.consume();
            } else if (e.getCode() == KeyCode.DOWN) {
                game.forceNextWave();
                e.consume();
            }
        });
    }

    private void enableEditMode() {
        editMode = true;
        applyBallType();
        updateText();
        playSecretVideo();
    }

    private void disableEditMode() {
        editMode = false;

        game.setForceFireRound(false);
        game.setForceIceRound(false);
        game.setForcePierceRound(false);
        game.setForceShrinkRound(false);

        editText.setVisible(false);
    }

    private void playSecretVideo() {
        try {
            String videoUrl = getClass().getResource("sound/SECRET.mp4").toExternalForm();
            Media media = new Media(videoUrl);
            mediaPlayer = new MediaPlayer(media);
            mediaView = new MediaView(mediaPlayer);
            mediaView.setFitWidth(GameConfig.WINDOW_WIDTH); // dwayne the rock johnson
            mediaView.setFitHeight(GameConfig.WINDOW_HEIGHT);
            mediaView.setPreserveRatio(false);
            root.getChildren().add(mediaView);
            mediaView.toFront();
            editText.toFront();
            mediaPlayer.setCycleCount(1);
            mediaPlayer.setOnEndOfMedia(this::stopSecretVideo);
            mediaPlayer.play();

        } catch (Exception ex) {
            System.out.println("DAMN");
        }
    }

    private void stopSecretVideo() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            root.getChildren().remove(mediaView);
            mediaPlayer = null;
            mediaView = null;
        }
    }

    private void applyBallType() {
        if (ballTypes[ballTypeIndex].equals("NORMAL")) {
            game.setForceFireRound(false);
            game.setForceIceRound(false);
            game.setForcePierceRound(false);
            game.setForceShrinkRound(false);
        } else if (ballTypes[ballTypeIndex].equals("FIRE")) {
            game.setForceFireRound(true);
            game.setForceIceRound(false);
            game.setForcePierceRound(false);
            game.setForceShrinkRound(false);
        } else if (ballTypes[ballTypeIndex].equals("ICE")) {
            game.setForceFireRound(false);
            game.setForceIceRound(true);
            game.setForcePierceRound(false);
            game.setForceShrinkRound(false);
        } else if (ballTypes[ballTypeIndex].equals("PIERCE")) {
            game.setForceFireRound(false);
            game.setForceIceRound(false);
            game.setForcePierceRound(true);
            game.setForceShrinkRound(false);
        } else if (ballTypes[ballTypeIndex].equals("SHRINK")) {
            game.setForceFireRound(false);
            game.setForceIceRound(false);
            game.setForcePierceRound(false);
            game.setForceShrinkRound(true);
        }
    }

    private void updateText() {
        if (editText == null) return;

        if (editMode) {
            editText.setVisible(true);
            editText.setText(
                    "EDIT MODE ON\n" +
                            "Ball: " + ballTypes[ballTypeIndex] + "\n" +
                            "← → Change Ball\n" +
                            "↑ Move Blocks Up\n" +
                            "↓ Next Wave\n" +
                            "Press 'E' to Exit"
            );
        }
    }
}