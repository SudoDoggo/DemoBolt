package com.example.courseprifs.fxController;

import com.example.courseprifs.HelloApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginForm {

 public TextField loginField;
 public PasswordField passwordField;

 public void validateAndLoad() throws IOException {
     FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-form.fxml"));
     Scene scene = new Scene(fxmlLoader.load());
     Stage stage = (Stage)passwordField.getScene().getWindow();
     stage.setTitle("Hello!");
     stage.setScene(scene);
     stage.show();
 }
 public void registerNewUser() throws IOException {
     FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("user-form.fxml"));
     Scene scene = new Scene(fxmlLoader.load());
     Stage stage = new Stage();
     stage.setTitle("Hello!");
     stage.setScene(scene);
     stage.show();
 }
}
