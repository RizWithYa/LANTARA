module com.example.lantara {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.lantara.controller to javafx.fxml;
    opens com.example.lantara.view to javafx.fxml;

    opens com.example.lantara.model to javafx.base;

    exports com.example.lantara;
}
