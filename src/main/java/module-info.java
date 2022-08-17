module com.example.timer {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;


    opens com.example.timer to javafx.fxml;
    exports com.example.timer;
}