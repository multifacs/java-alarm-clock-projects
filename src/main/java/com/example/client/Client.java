package com.example.client;

import com.github.simplenet.packet.Packet;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Client extends Application {

    private static LocalTime time = LocalTime.of(0, 0, 0);
    private static String events = "";
    private static String event = "";

    private static int timeout = 0;

    private static com.github.simplenet.Client client = new com.github.simplenet.Client();

    // create formatter Object for ISO_LOCAL_TIME
    private static DateTimeFormatter formatter
            = DateTimeFormatter.ISO_LOCAL_TIME;

    private static void updateTime(Label timeLabel) {
        timeLabel.setText(time.format(formatter));
    }

    private static boolean checkTime(String hours, String minutes, String seconds) {
        return hours.length() == 2 && minutes.length() == 2 && seconds.length() == 2;
    }

    private static void sendEvent(String hours, String minutes, String seconds) {
        if (checkTime(hours, minutes, seconds)) {
            Packet.builder().putString(hours + ":" + minutes + ":" + seconds).queueAndFlush(client);
        }
    }

    private static void receiveMessage(byte opcode) {
        switch (opcode) {
            case 1: // Получить время
                client.readString(msg -> {
                    time = LocalTime.parse(msg);
                });
                break;
            case 2: // Получить все события
                client.readString(msg -> {
                    events = msg;
                });
                break;
            case 3: // Событие произошло
                client.readString(msg -> {
                    event = msg;
                    timeout = 1;
                });
                break;
        }
    }

    private static void updateEvents(VBox eventsVBox) {
        if (!events.isEmpty()) {
            String[] items = events.split("\\s* \\s*");
            eventsVBox.getChildren().clear();
            for(String item : items) {
                Label event = new Label();
                event.setText(item);
                eventsVBox.getChildren().add(event);
            }
        } else {
            eventsVBox.getChildren().clear();
        }
    }

    private static void updateEvent(Label eventLabel) {
        if (timeout == 1) {
            eventLabel.setText("Событие: " + event);
        }
        if (timeout >= 1) {
            timeout += 1;
        }
        if (timeout >= 30) {
            eventLabel.setText("");
            timeout = 0;
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 280, 480);

        Map<String, Object> controllerNamespace = fxmlLoader.getNamespace();

        VBox eventsVBox = (VBox) controllerNamespace.get("eventsVBox");
        Label timeLabel = (Label) controllerNamespace.get("timeLabel");
        Label eventLabel = (Label) controllerNamespace.get("eventLabel");
        Button sendButton = (Button) controllerNamespace.get("sendButton");
        TextField hoursInput = (TextField) controllerNamespace.get("hoursInput");
        TextField minutesInput = (TextField) controllerNamespace.get("minutesInput");
        TextField secondsInput = (TextField) controllerNamespace.get("secondsInput");

        stage.setTitle("Timer");
        stage.setScene(scene);
        stage.show();

        Thread clientThread = new Thread(() -> {
            client.onConnect(() -> {
                client.readByteAlways(opcode -> {
                    receiveMessage(opcode);
                });
            });

            client.connect("localhost", 43594);
        });
        clientThread.setDaemon(true);
        clientThread.start();

        Thread uiThread = new Thread(() -> {
                Runnable updater = new Runnable() {

                    @Override
                    public void run() {
                        updateTime(timeLabel);
                        updateEvents(eventsVBox);
                        updateEvent(eventLabel);
                    }
                };

                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ignored) {
                    }
                    Platform.runLater(updater);
                }
            }
        );
        uiThread.setDaemon(true);
        uiThread.start();

        sendButton.setOnAction(e -> {
            String hours = hoursInput.getText();
            String minutes = minutesInput.getText();
            String seconds = secondsInput.getText();

            sendEvent(hours, minutes, seconds);
        });
    }

    @Override
    public void stop() {
        client.close();
    }

    public static void main(String[] args) {
        launch();
    }
}