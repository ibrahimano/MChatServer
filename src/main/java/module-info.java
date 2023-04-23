module com.example.mchatserver {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires models;


    opens com.example.mchatserver to javafx.fxml;
    exports com.example.mchatserver;
}