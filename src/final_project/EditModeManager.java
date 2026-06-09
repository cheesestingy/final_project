package final_project;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;

public class EditModeManager {

    private boolean editMode = false;
    private int ballTypeIndex = 0;

    private final String[] ballTypes = {
            "NORMAL",
            "FIRE",
            "ICE",
            "PIERCE"
    };

    private GameController game;
    private Text editText;

    public EditModeManager(GameController game, Text editText) {
        this.game = game;
        this.editText = editText;
        updateText();
    }

    public void attach(Scene scene) {
        scene.addEventFilter(KeyEvent.KEY_RELEASED, e -> {

            if (e.getCode() == KeyCode.E) {
                editMode = !editMode;

                if (editMode) {
                    applyBallType();
                } else {
                    game.setForceFireRound(false);
                    game.setForceIceRound(false);
                }

                updateText();
                e.consume();
                return;
            }

            if (!editMode) return;

            if (e.getCode() == KeyCode.LEFT) {
                ballTypeIndex--;
                if (ballTypeIndex < 0) {
                    ballTypeIndex = ballTypes.length - 1;
                }
                applyBallType();
                updateText();
                e.consume();
            }

            if (e.getCode() == KeyCode.RIGHT) {
                ballTypeIndex++;
                if (ballTypeIndex >= ballTypes.length) {
                    ballTypeIndex = 0;
                }
                applyBallType();
                updateText();
                e.consume();
            }

            if (e.getCode() == KeyCode.UP) {
                game.moveBlocksUpOneRow();
                e.consume();
            }

            if (e.getCode() == KeyCode.DOWN) {
                game.forceNextWave();
                e.consume();
            }
        });
    }

    private void applyBallType() {
        if (ballTypes[ballTypeIndex].equals("NORMAL")) {
            game.setForceFireRound(false);
            game.setForceIceRound(false);
            game.setForcePierceRound(false);
        } else if (ballTypes[ballTypeIndex].equals("FIRE")) {
            game.setForceFireRound(true);
            game.setForceIceRound(false);
            game.setForcePierceRound(false);
        } else if (ballTypes[ballTypeIndex].equals("ICE")) {
            game.setForceFireRound(false);
            game.setForceIceRound(true);
            game.setForcePierceRound(false);
        } else if (ballTypes[ballTypeIndex].equals("PIERCE")) {
            game.setForceFireRound(false);
            game.setForceIceRound(false);
            game.setForcePierceRound(true);
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
                            "E Exit Edit"
            );
        } else {
            editText.setVisible(false);
        }
    }

}