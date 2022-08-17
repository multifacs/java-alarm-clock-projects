module com.votyakova.timerclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires spring.websocket;
    requires spring.messaging;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jdk8;

    opens com.example.timerclient to javafx.fxml;
    exports com.example.timerclient;
}