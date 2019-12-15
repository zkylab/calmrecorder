import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jdk.nashorn.internal.objects.Global;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

import java.awt.*;
import java.awt.event.InputEvent;
import java.lang.annotation.Native;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;


public class CalmRecorder extends Application {

    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) {
        ArrayList<Point> clickedLocations = new ArrayList<>();
        Queue<Object> eventsQueue = new LinkedList<>();
        Label label= new Label("Calm Recorder v1.0 - by TOTOM");
        label.setStyle("-fx-background-color: #ff4c7b; -fx-text-fill: white; -fx-text-alignment: center; -fx-padding: 15px");
        ListView listView = new ListView();

        Button buttonRecord = new Button("Start Record!");
        buttonRecord.setStyle("-fx-background-color: green; -fx-text-fill: white");
        Button playButton = new Button("Play!");
        playButton.setDisable(true);
        playButton.setStyle("-fx-background-color: #7bff3e;");
        AtomicBoolean recording = new AtomicBoolean(false);
        Label typingLbl = new Label("Typing.. =>  ");
        HBox buttons = new HBox(5,buttonRecord,playButton,typingLbl);
        VBox root = new VBox(5, label, listView, buttons);
        root.setStyle("-fx-background-color: #ffaca5; -fx-padding: 15px");
        Robot robot = null;
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        Robot finalRobot = robot;
        playButton.setOnMouseClicked(mouseEvent -> {
            eventsQueue.forEach(event ->{
                if(event instanceof Point){
                    finalRobot.mouseMove(((Point) event).x, ((Point) event).y);
                    finalRobot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                    finalRobot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                    finalRobot.delay(550);
                }else if (event instanceof String){
                    String word = (String) event;
                    word.chars().forEach(w ->{
                        finalRobot.keyPress((int)w);
                    });
                }
            });

        });

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
        StringBuilder stringBuilder = new StringBuilder();
        NativeKeyListener nativeKeyListener = new NativeKeyListener() {
            @Override
            public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {
            }

            @Override
            public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
                stringBuilder.append((char)nativeKeyEvent.getRawCode());
                Platform.runLater(() -> typingLbl.setText("Typing.. => " + stringBuilder.toString()));
            }

            @Override
            public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
            }
        };

        NativeMouseListener nativeMouseListener = new NativeMouseListener() {
            @Override
            public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
                Point clicked = MouseInfo.getPointerInfo().getLocation();
                clickedLocations.add(clicked);
                eventsQueue.add(clicked);
                if(stringBuilder.length() != 0){
                    listView.getItems().add(stringBuilder.toString());
                    eventsQueue.add(stringBuilder.toString());
                    stringBuilder.delete(0,stringBuilder.length());
                }
                listView.getItems().add("x: " + clicked.getX() + " - " + "y: " + clicked.getY());
            }

            @Override
            public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
            }

            @Override
            public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {
            }
        };

        buttonRecord.setOnMouseClicked(actionEvent -> {
            recording.set(!recording.get());
            if(recording.get()){
                playButton.setDisable(false);
                buttonRecord.setText("Start Record!");
                buttonRecord.setStyle("-fx-background-color: green; -fx-text-fill: white");
                clickedLocations.remove(clickedLocations.size()-1);
                eventsQueue.remove(eventsQueue.size()-1);
                stop(nativeMouseListener,nativeKeyListener);
            }else{
                playButton.setDisable(true);
                listView.getItems().clear();
                buttonRecord.setText("Stop Record");
                buttonRecord.setStyle("-fx-background-color: red; -fx-text-fill: white");
                if(!clickedLocations.isEmpty())
                    clickedLocations.clear();
                if(eventsQueue.size() != 0){
                    eventsQueue.clear();
                }
                record(nativeMouseListener,nativeKeyListener);
            }
        });

        Scene scene = new Scene(root,400,400);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        root.setAlignment(Pos.CENTER);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent windowEvent) {
                Platform.exit();
                System.exit(0);
            }
        });
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setTitle("Calm Recorder");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public void record(NativeMouseListener listener, NativeKeyListener nativeKeyListener){
        GlobalScreen.addNativeMouseListener(listener);
        GlobalScreen.addNativeKeyListener(nativeKeyListener);
    }
    public void stop(NativeMouseListener listener, NativeKeyListener nativeKeyListener){
        GlobalScreen.removeNativeMouseListener(listener);
        GlobalScreen.removeNativeKeyListener(nativeKeyListener);
    }
}
