package com.example.timer;

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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class Client extends Application {

    private static ObjectOutputStream output;
    private static ObjectInputStream input;

    private static final int TIME = 1;
    private static final int EVENTS = 2;
    private static final int NOTIFICATION = 3;

    private static LocalTime time = LocalTime.now();
    private static List<Event> events;
    private static Event currentEvent;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 300, 450);

        Map<String, Object> controllerNamespace = fxmlLoader.getNamespace();

        stage.setScene(scene);
        stage.show();

        Thread clientThread = new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(InetAddress.getLocalHost(), 8888);
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    try {
                        int num = (int) input.readObject();
                        switch (num) {
                            case TIME -> time = LocalTime.parse((String) input.readObject());
                            case EVENTS -> events = (List<Event>) input.readObject();
                            case NOTIFICATION -> currentEvent = (Event) input.readObject();
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        Platform.exit();
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                Platform.exit();
                e.printStackTrace();
            }
        });
        clientThread.setDaemon(true);
        clientThread.start();

        Thread interfaceThread = new Thread(() -> {

            VBox eventsVBox = (VBox) controllerNamespace.get("eventsVBox");
            Label timeLabel = (Label) controllerNamespace.get("timeLabel");
            Label eventLabel = (Label) controllerNamespace.get("eventLabel");

            Runnable updater = () -> {
                timeLabel.setText(time.toString().substring(0, 8));

                if (events != null) {
                    eventsVBox.getChildren().clear();
                    for (Event event : events) {
                        Label newEvent = new Label();
                        newEvent.setText(event.getTime().toString());
                        eventsVBox.getChildren().add(newEvent);
                    }
                }

                if (currentEvent != null) {
                    eventLabel.setText(currentEvent.getTime().toString());
                }
            };

            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }

                Platform.runLater(updater);
            }
        }
        );
        interfaceThread.setDaemon(true);
        interfaceThread.start();

        Button sendButton = (Button) controllerNamespace.get("sendBtn");
        TextField hoursField = (TextField) controllerNamespace.get("hoursField");
        TextField minutesField = (TextField) controllerNamespace.get("minutesField");
        TextField secondsField = (TextField) controllerNamespace.get("secondsField");

        sendButton.setOnAction(e -> {
            int hours = Integer.parseInt(hoursField.getText());
            int minutes = Integer.parseInt(minutesField.getText());
            int seconds = Integer.parseInt(secondsField.getText());
            LocalTime time = LocalTime.of(hours, minutes, seconds);
            synchronized (output) {
                try {
                    output.writeObject(new Event(time));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}