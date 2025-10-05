module com.example.courseprifs {
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.courseprifs to javafx.fxml;
    exports com.example.courseprifs;
}