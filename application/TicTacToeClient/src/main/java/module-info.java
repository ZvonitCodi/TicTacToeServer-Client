module com.example.tictactoeclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;


    opens com.example.tictactoeclient to javafx.fxml;
    exports com.example.tictactoeclient;
}