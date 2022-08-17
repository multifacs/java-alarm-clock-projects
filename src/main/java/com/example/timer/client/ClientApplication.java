package com.example.timer.client;

import com.example.timer.models.Event;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Map;

public class ClientApplication extends javafx.application.Application {

    private final Client client = new Client();
    private Map<String, Object> controllerNamespace;
    private final int updateRate = 50;
    private final int showTime = 30;

    private void startUIThread() {
        Thread UIThread = new Thread(() -> {
            Runnable updater = () -> {
                updateTime();
                updateEvents();
                updateEvent();
            };

            while (true) {
                try {
                    Thread.sleep(updateRate);
                } catch (InterruptedException ignored) {
                }

                Platform.runLater(updater);
            }
        }
        );
        UIThread.setDaemon(true);
        UIThread.start();
    }
    private void startListeners() {
        Button sendBtn = (Button) controllerNamespace.get("sendBtn");
        TextField hoursField = (TextField) controllerNamespace.get("hoursField");
        TextField minutesField = (TextField) controllerNamespace.get("minutesField");
        TextField secondsField = (TextField) controllerNamespace.get("secondsField");
        TextField eventName = (TextField) controllerNamespace.get("eventName");

        sendBtn.setOnAction(e -> {
            sendEvent();
        });
        hoursField.setOnAction(e -> {
            sendEvent();
        });
        minutesField.setOnAction(e -> {
            sendEvent();
        });
        secondsField.setOnAction(e -> {
            sendEvent();
        });
        eventName.setOnAction(e -> {
            sendEvent();
        });
    }

    private void updateTime() {

        Label timeLabel = (Label) controllerNamespace.get("timeLabel");

        int hours = client.time.getTime().getHour();
        int minutes = client.time.getTime().getMinute();
        int seconds = client.time.getTime().getSecond();
        client.currentTime = LocalTime.of(hours, minutes, seconds);
        timeLabel.setText(client.currentTime.format(client.formatter));
    }
    private void updateEvents() {

        TextArea eventsArea = (TextArea) controllerNamespace.get("eventsArea");

        StringBuilder eventsString = new StringBuilder();
        for (Event event : client.events.getEvents()) {
            eventsString.append(event.toString()).append("\n");
        }
        eventsArea.setText(eventsString.toString());
    }
    private void updateEvent() {

        Label eventLabel = (Label) controllerNamespace.get("eventLabel");

        if (client.timeout == 1) {
            LocalTime time = LocalTime.of(
                    client.event.getEvent().hour,
                    client.event.getEvent().minute,
                    client.event.getEvent().second);

            String eventString = "Событие: " +
                    client.event.getEvent().name +
                    " " +
                    time.format(client.formatter);
            eventLabel.setText(eventString);
        }
        if (client.timeout >= 1) {
            client.timeout = client.timeout + 1;
        }
        if (client.timeout >= showTime) {
            eventLabel.setText("");
            client.timeout = 0;
        }
    }
    private void sendEvent(){
        TextField hoursField = (TextField) controllerNamespace.get("hoursField");
        TextField minutesField = (TextField) controllerNamespace.get("minutesField");
        TextField secondsField = (TextField) controllerNamespace.get("secondsField");
        TextField eventName = (TextField) controllerNamespace.get("eventName");

        client.sendEvent(
                hoursField.getText(),
                minutesField.getText(),
                secondsField.getText(),
                eventName.getText()
        );
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 360);
        controllerNamespace = fxmlLoader.getNamespace();

        stage.setTitle("Timer");
        stage.setScene(scene);
        stage.show();

        int PORT = 8888;
        client.start(PORT);
        startUIThread();
        startListeners();
    }

    public static void main(String[] args) {
        launch();
    }
}