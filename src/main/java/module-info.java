module com.example.timer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports com.example.timer.client;
    opens com.example.timer.client to javafx.fxml;
}