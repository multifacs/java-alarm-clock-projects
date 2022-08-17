package com.example.timer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Client extends Application {

    private static ObjectOutputStream outputStream;
    private static ObjectInputStream inputStream;

    private final InputData inputData = new InputData();

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Client.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 250, 430);

        Map<String, Object> controllerNamespace = fxmlLoader.getNamespace();

        ListView<String> alarmList = (ListView<String>) controllerNamespace.get("alarmList");
        Label timeLabel = (Label) controllerNamespace.get("timeLabel");
        Label alarmLabel = (Label) controllerNamespace.get("alarmLabel");

        Font font = Font.loadFont(Client.class.getResourceAsStream("Orbitron-VariableFont_wght.ttf"), 45);
        timeLabel.setFont(font);

        stage.setScene(scene);
        stage.show();
        stage.setTitle("Таймер");

        Thread clientThread = new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(InetAddress.getLocalHost(), 8888);
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                inputStream = new ObjectInputStream(socket.getInputStream());

                while (true) {
                    try {
                        int num = (int) inputStream.readObject();

                        if (num == 1) {
                            inputData.setTime((String) inputStream.readObject());
                        }
                        if (num == 2) {
                            inputData.setAlarmList((List<Alarm>) inputStream.readObject());
                        }
                        if (num == 3) {
                            inputData.setAlarm((Alarm) inputStream.readObject());
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
            Runnable updater = () -> {
                timeLabel.setText(inputData.getTime());

                if (inputData.getAlarmList() != null) {
                    alarmList.getItems().clear();
                    for (Alarm alarm : inputData.getAlarmList()) {
                        String alarmString = alarm.getDeciSec() / 10 / 60 + ":" +
                                alarm.getDeciSec() / 10 + ":" +
                                alarm.getDeciSec() % 10;
                        alarmList.getItems().add(alarmString);
                    }
                }

                if (inputData.getAlarm() != null) {
                    alarmLabel.setText(inputData.getAlarm().getDeciSec() / 10 / 60 + ":" +
                            inputData.getAlarm().getDeciSec() / 10 + ":" +
                            inputData.getAlarm().getDeciSec() % 10);
                }
            };

            while (true) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                }

                Platform.runLater(updater);
            }
        }
        );
        interfaceThread.setDaemon(true);
        interfaceThread.start();

        TextField secInput = (TextField) controllerNamespace.get("secInput");
        Button addButton = (Button) controllerNamespace.get("addButton");

        addButton.setOnAction(e -> {
            int decisecs = Integer.parseInt(secInput.getText()) * 10;

            if (decisecs < 0) {
                return;
            }

            synchronized (outputStream) {
                try {
                    outputStream.writeObject(new Alarm(decisecs));
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

class InputData {
    private String time;
    private List<Alarm> alarmList;
    private Alarm alarm;

    public InputData() {
        time = "";
        alarmList = new ArrayList<>();
        alarm = null;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<Alarm> getAlarmList() {
        return alarmList;
    }

    public void setAlarmList(List<Alarm> alarmList) {
        this.alarmList = alarmList;
    }

    public Alarm getAlarm() {
        return alarm;
    }

    public void setAlarm(Alarm currentAlarm) {
        this.alarm = currentAlarm;
    }
}