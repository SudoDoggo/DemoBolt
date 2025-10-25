package com.example.courseprifs.fxControllers;

import com.example.courseprifs.HelloApplication;
import com.example.courseprifs.hibernateControl.CustomHibernate;
import com.example.courseprifs.hibernateControl.GenericHibernate;
import com.example.courseprifs.model.User;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginForm {
    @FXML
    public TextField userField;
    @FXML
    public PasswordField passwordField;
    public Button loginButton;
    public Button registerButton;

    private EntityManagerFactory emf = Persistence.createEntityManagerFactory("wolt");

    public void validateAndLoad() throws IOException {
        CustomHibernate customHibernate = new CustomHibernate(emf);
        User user = customHibernate.getUserByCredentials(userField.getText(), passwordField.getText());
        if(user!=null){
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-form.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = (Stage) userField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }else{
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Warning", "Something went wrong during login", "No such user or wrong credentials");
        }

    }

    public void registerNewUser() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("user-form.fxml"));
        Parent parent = fxmlLoader.load();
        UserForm userForm = fxmlLoader.getController();
        userForm.setData(emf);
        Scene scene = new Scene(parent);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }
}
