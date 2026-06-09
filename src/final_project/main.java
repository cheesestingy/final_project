package final_project;

import javafx.application.Application;
import javafx.stage.Stage;

public class main extends Application {
    @Override
    public void start(Stage primaryStage) {
        GameController game = new GameController();
        game.start(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}