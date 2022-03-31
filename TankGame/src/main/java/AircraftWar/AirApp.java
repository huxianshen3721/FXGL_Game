package AircraftWar;

import Tank.GameType;
import Tank.TankApp;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.dsl.EntityBuilder;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
import com.almasb.fxgl.dsl.components.OffscreenCleanComponent;
import com.almasb.fxgl.dsl.components.ProjectileComponent;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.texture.Texture;
import com.almasb.fxgl.time.LocalTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Map;

import static javafx.scene.paint.Color.RED;

/**
 * @author HuXianshen
 * @creat 2022-03-09:06
 */
public class AirApp extends GameApplication {
    private Entity airEntity;

    private boolean isMoving;

    private LocalTimer shootTimer;

    private final Duration shootDelay = Duration.seconds(0.25);

    private int r = 5;

    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setWidth(600);
        gameSettings.setHeight(800);
        gameSettings.setTitle("Aircraft War");
        gameSettings.setVersion("7.7");
        gameSettings.setAppIcon("logo64.png");
    }

    @Override
    protected void onPreInit() {
        FXGL.getSettings().setGlobalMusicVolume(0.5);
        FXGL.getSettings().setGlobalSoundVolume(0.8);
        FXGL.loopBGM("bg.mp3");
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("score", 0);
        vars.put("x", 0.0);
        vars.put("list", new ArrayList<>());
        vars.put("name", "");
    }

    @Override
    protected void initGame() {
        FXGL.getip("score").addListener((ob, ov, nv) ->{
            if(nv.intValue() == 100) {
                FXGL.getNotificationService().pushNotification("你已经获得100分了！");
            }
        });

        airEntity = FXGL.entityBuilder()
                .viewWithBBox("logo64.png")
                .at(268, 720)
                .build();
        airEntity.setRotationOrigin(new Point2D(32, 32));
        FXGL.getGameWorld().addEntity(airEntity);

        createEnmy();
    }

    private void createEnmy() {
        FXGL.entityBuilder()
                .type(GameType.ENEMY)
                .at(FXGLMath.random(0, 536), FXGLMath.random(0, 600))
                .viewWithBBox("tank.png")
                .collidable()
                .buildAndAttach();
        r += 0.8;
    }

    @Override
    protected void initInput() {
        FXGL.getInput().addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {
                if(isMoving) {
                    return;
                }
                isMoving = true;
                airEntity.translateY(-5);
            }
        }, KeyCode.UP);
        FXGL.getInput().addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {
                if(isMoving) {
                    return;
                }
                isMoving = true;
                airEntity.translateY(5);
            }
        }, KeyCode.DOWN);
        FXGL.getInput().addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                if(isMoving) {
                    return;
                }
                isMoving = true;
                airEntity.translateX(-5);
            }
        }, KeyCode.LEFT);
        FXGL.getInput().addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                if(isMoving) {
                    return;
                }
                isMoving = true;
                airEntity.translateX(5);
            }
        }, KeyCode.RIGHT);

        FXGL.getInput().addAction(new UserAction("shoot") {
            @Override
            protected void onAction() {
                FXGL.play("shoot.wav");
                Entity bullet = FXGL.entityBuilder()
                        .type(GameType.BULLET)
                        .at(airEntity.getCenter().getX()-5, airEntity.getCenter().getY()-5)
                        .viewWithBBox(new Circle(r, RED))
                        .with(new ProjectileComponent(new Point2D(0, -1), 600))
                        .with(new OffscreenCleanComponent())
                        .collidable()
                        .buildAndAttach();
            }
        }, KeyCode.SPACE);
    }

    @Override
    protected void initPhysics() {
        FXGL.getPhysicsWorld().addCollisionHandler(new CollisionHandler(GameType.BULLET, GameType.ENEMY) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity enemy) {
                FXGL.play("bomb.wav");
                FXGL.inc("score", 10);
                bullet.removeFromWorld();
                Point2D center = enemy.getCenter();
                enemy.removeFromWorld();

                Circle circle = new Circle(10, RED);
                Duration duration = Duration.seconds(.35);
                Entity boom = FXGL.entityBuilder()
                        .at(center)
                        .with(new ExpireCleanComponent(duration))
                        .view(circle)
                        .buildAndAttach();
                ScaleTransition st = new ScaleTransition(duration, circle);
                st.setToX(10);
                st.setToY(10);

                FadeTransition ft = new FadeTransition(duration, circle);
                ft.setToValue(0);

                ParallelTransition pt = new ParallelTransition(st, ft);
                pt.play();

                createEnmy();
            }
        });
    }

    @Override
    protected void initUI() {
        Text text = FXGL.getUIFactoryService()
                .newText(FXGL.getip("score").asString("score:%d"));
        text.setLayoutX(0);
        text.setLayoutY(795);
        text.setFill(Color.BLUE);
        FXGL.addUINode(text);
    }

    @Override
    protected void onUpdate(double tpf) {
        isMoving = false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
