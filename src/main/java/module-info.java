module com.example.courseprifs {
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;
    requires org.hibernate.orm.core;
    requires java.sql;
    requires java.naming;
    requires mysql.connector.j;
    requires jakarta.persistence;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;
    requires javafx.base;

    opens com.example.courseprifs to javafx.fxml;
    exports com.example.courseprifs;
    opens com.example.courseprifs.fxControllers to javafx.fxml;
    exports com.example.courseprifs.fxControllers;
    opens com.example.courseprifs.model to org.hibernate.orm.core;
    exports com.example.courseprifs.model;
}