package com.example.timer;

import com.example.timer.data.Data;
import com.example.timer.data.Event;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class Client extends Application {

    private static ObjectOutputStream outputStream;
    private static ObjectInputStream inputStream;

    private final Data data = new Data();

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 340, 390);

        Map<String, Object> controllerNamespace = fxmlLoader.getNamespace();

        VBox eventBox = (VBox) controllerNamespace.get("eventBox");
        Label timeLabel = (Label) controllerNamespace.get("timeLabel");
        Label eventLabel = (Label) controllerNamespace.get("eventLabel");

        stage.setScene(scene);
        stage.show();
        stage.setTitle("Таймер");

        connect();
        updateInterface(eventBox, timeLabel, eventLabel);

        TextField nameField = (TextField) controllerNamespace.get("nameField");
        TextField minutesField = (TextField) controllerNamespace.get("minutesField");
        TextField secondsField = (TextField) controllerNamespace.get("secondsField");

        nameField.setOnAction(e -> {
            String name = nameField.getText();
            int minutes = Integer.parseInt(minutesField.getText());
            int seconds = Integer.parseInt(secondsField.getText());

            synchronized (outputStream) {
                try {
                    outputStream.writeObject(new Event(name, minutes, seconds));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        minutesField.setOnAction(e -> {
            String name = nameField.getText();
            int minutes = Integer.parseInt(minutesField.getText());
            int seconds = Integer.parseInt(secondsField.getText());

            synchronized (outputStream) {
                try {
                    outputStream.writeObject(new Event(name, minutes, seconds));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        secondsField.setOnAction(e -> {
            String name = nameField.getText();
            int minutes = Integer.parseInt(minutesField.getText());
            int seconds = Integer.parseInt(secondsField.getText());

            synchronized (outputStream) {
                try {
                    outputStream.writeObject(new Event(name, minutes, seconds));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void updateInterface(VBox eventBox, Label timeLabel, Label eventLabel) {
        Thread interfaceThread = new Thread(() -> {
            Runnable updater = () -> {
                timeLabel.setText(data.getTime());

                if (data.getEventList() != null) {
                    eventBox.getChildren().clear();
                    for (Event event : data.getEventList()) {
                        String eventsString = event.getName() + "\n" +
                                event.getMin() + ":" +
                                event.getSec() + " " +
                                "\n";
                        Label label = new Label();
                        label.setText(eventsString);
                        eventBox.getChildren().add(label);
                    }
                }

                if (data.getCurrentEvent() != null) {
                    eventLabel.setText(data.getCurrentEvent().getName()
                            + " " + data.getCurrentEvent().getMin()
                            + ":" + data.getCurrentEvent().getSec());
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
    }

    private void connect() {
        Thread clientThread = new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(InetAddress.getLocalHost(), 8888);
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    try {
                        int code = (int) inputStream.readObject();

                        if (code == 1) {
                            data.setTime((String) inputStream.readObject());
                        }
                        if (code == 2) {
                            data.setEventList((List<Event>) inputStream.readObject());
                        }
                        if (code == 3) {
                            data.setCurrentEvent((Event) inputStream.readObject());
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
    }

    public static void main(String[] args) {
        launch();
    }
}