package com.example.courseprifs.fxControllers;

import com.example.courseprifs.HelloApplication;
import com.example.courseprifs.hibernateControl.CustomHibernate;
import com.example.courseprifs.model.Restaurant;
import com.example.courseprifs.model.User;
import com.example.courseprifs.utils.FxUtils;
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

    private EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("wolt");

    public void validateAndLoad() throws IOException {
        CustomHibernate customHibernate = new CustomHibernate(entityManagerFactory);
        User user = customHibernate.getUserByCredentials(userField.getText(), passwordField.getText());
        if (user != null && (user.isAdmin() || user instanceof Restaurant)) {

            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-form.fxml"));
            Parent parent = fxmlLoader.load();

            MainForm mainForm = fxmlLoader.getController();
            mainForm.setData(entityManagerFactory, user);

            Scene scene = new Scene(parent);
            Stage stage = (Stage) userField.getScene().getWindow();
            stage.setTitle("Main form");
            stage.setScene(scene);
            stage.show();
        } else {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Warning", "Something went wrong during login", "No such user or wrong credentials");
        }
    }

    public void registerNewUser() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("user-form.fxml"));
        Parent parent = fxmlLoader.load();

        UserForm userForm = fxmlLoader.getController();
        userForm.setData(entityManagerFactory, null, false);
        userForm.setLoginFormReference(this);

        Scene scene = new Scene(parent);
        Stage stage = (Stage) userField.getScene().getWindow();
        stage.setTitle("Register form");
        stage.setScene(scene);
        stage.show();
    }

    public void switchBackToLogin(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-form.fxml"));
        Parent parent = fxmlLoader.load();

        Scene scene = new Scene(parent);
        if (stage == null) {
            stage = (Stage) userField.getScene().getWindow();
        }
        stage.setTitle("Login form");
        stage.setScene(scene);
        stage.show();
    }

    public void switchBackToLogin() throws IOException {
        Stage stage = (Stage) userField.getScene().getWindow();
        switchBackToLogin(stage);
    }


}
