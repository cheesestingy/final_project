package final_project;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Ball {
    Circle circle;
    double vx = 0;
    double vy = 0;
    boolean active = false;
    boolean returning = false;
    boolean fireBall = false;

    public Ball(double x, double y, Pane root) {
        circle = new Circle(x, y, GameConfig.BALL_RADIUS, Color.WHITE);
        root.getChildren().add(circle);
    }

    public void setFireBall(boolean fireBall) {
        this.fireBall = fireBall;
        circle.setFill(fireBall ? Color.ORANGERED : Color.WHITE);
    }
}