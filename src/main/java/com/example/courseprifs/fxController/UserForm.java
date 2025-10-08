package com.example.courseprifs.fxController;

import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

public class UserForm {
    public RadioButton userRadio;
    public RadioButton restaurantRadio;
    public RadioButton clientRadio;
    public RadioButton driverRadio;
    public TextField loginField;
    public PasswordField passwordField;
    public PasswordField rePasswordField;


    public void disableFields(){
        if(userRadio.isSelected()){
        addressField.setDisable(true);
        }
        else if(restaurantRadio.isSelected()){

        } else if (clientRadio.isSelected()) {

        }
        else if(driverRadio.isSelected()){

        }
    }
}
