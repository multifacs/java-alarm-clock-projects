package com.example.timerclient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.Map;
import java.util.Scanner;
import java.io.IOException;

public class ClientApp extends Application {

    private static String URL = "ws://localhost:8080/chat";
    private TimeData msg = new TimeData("00:00");
    private EventContainer eventContainer = new EventContainer();
    private OccurredData occurredData = new OccurredData();

    private SessionHandler sessionHandler = null;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApp.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);

        Map<String, Object> controllerNamespace = fxmlLoader.getNamespace();

        Label timeLabel = (Label) controllerNamespace.get("timeLabel");
        TextArea eventArea = (TextArea) controllerNamespace.get("eventArea");
        Button sendBtn = (Button) controllerNamespace.get("sendBtn");
        Label eventLabel = (Label) controllerNamespace.get("eventLabel");
        TextField timeInput = (TextField) controllerNamespace.get("timeInput");

        Thread wsThread = new Thread(() -> {
                WebSocketClient client = new StandardWebSocketClient();

                WebSocketStompClient stompClient = new WebSocketStompClient(client);
                stompClient.setMessageConverter(new StringMessageConverter());

                sessionHandler = new SessionHandler(msg, eventContainer, occurredData);
                stompClient.connect(URL, sessionHandler);

                new Scanner(System.in).nextLine(); // Don't close immediately.
                System.out.println("closed");
            }
        );
        wsThread.start();

        // longrunning operation runs on different thread
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Runnable updater = new Runnable() {

                    @Override
                    public void run() {
                        timeLabel.setText(msg.getMsg());
                        eventLabel.setText(occurredData.getMsg());
                        eventArea.setText(eventContainer.getMsg());
                    }
                };

                while (true) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                    }

                    // UI update is run on the Application thread
                    Platform.runLater(updater);
                }
            }

        });
        // don't let thread prevent JVM shutdown
        thread.setDaemon(true);
        thread.start();

        sendBtn.setOnAction(e -> {
            sessionHandler.sendMsg(timeInput.getText());
        });

        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}