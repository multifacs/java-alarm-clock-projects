package com.example.client;

import com.github.simplenet.Client;
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
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ClientApp extends Application {

    private static Instant time = Instant.now();
    private static String events = "";
    private static String event = "";

    private static Client client = new Client();

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApp.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);

        Map<String, Object> controllerNamespace = fxmlLoader.getNamespace();

        VBox eventsVBox = (VBox) controllerNamespace.get("eventsVBox");
        Label timeLabel = (Label) controllerNamespace.get("timeLabel");
        Label eventLabel = (Label) controllerNamespace.get("eventLabel");
        Button sendButton = (Button) controllerNamespace.get("sendButton");
        TextField minutesInput = (TextField) controllerNamespace.get("minutesInput");
        TextField secondsInput = (TextField) controllerNamespace.get("secondsInput");

        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

        Thread clientThread = new Thread(() -> {
            // This callback is invoked when this client connects to a server.
            client.onConnect(() -> {
                // Repeatedly read a single byte.
                client.readByteAlways(opcode -> {
                    switch (opcode) {
                        case 1: // Get time
                            client.readString(msg -> {
                                time = Instant.parse(msg);
                            });
                            break;
                        case 2: // Get events
                            client.readString(msg -> {
                                events = msg;
                            });
                            break;
                        case 3: // Event happened
                            client.readString(msg -> {
                                event = msg;
                            });
                            break;
                    }
                });
            });

            // Attempt to connect to a server AFTER registering listeners.
            client.connect("localhost", 43594);
        });
        clientThread.setDaemon(true);
        clientThread.start();

        // longrunning operation runs on different thread
        Thread uiThread = new Thread(() -> {
                Runnable updater = new Runnable() {

                    @Override
                    public void run() {

                        int minutes = time.atZone(ZoneId.systemDefault()).getMinute();
                        int seconds = time.atZone(ZoneId.systemDefault()).getSecond();
                        timeLabel.setText(minutes + ":" + seconds);

                        if (!events.isEmpty()) {
                            List<String> items = Arrays.asList(events.split("\\s* \\s*"));
                            eventsVBox.getChildren().clear();
                            for(String item : items) {
                                Label event = new Label();
                                event.setText(item);
                                eventsVBox.getChildren().add(event);
                            }
                        }

                        eventLabel.setText(event);
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
        uiThread.setDaemon(true);
        uiThread.start();

        sendButton.setOnAction(e -> {
            String minutes = minutesInput.getText();
            String seconds = secondsInput.getText();

            if (minutes.length() == 2 && seconds.length() == 2) {
                Packet.builder().putString(minutes + ":" + seconds).queueAndFlush(client);
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}