module com.example.courseprifs {
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;

    opens com.example.courseprifs to javafx.fxml;
    exports com.example.courseprifs;
    opens com.example.courseprifs.fxController to javafx.fxml;
    exports com.example.courseprifs.fxController;
}