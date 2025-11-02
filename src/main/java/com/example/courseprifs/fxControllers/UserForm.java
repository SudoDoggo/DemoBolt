package com.example.courseprifs.fxControllers;

import com.example.courseprifs.hibernateControl.GenericHibernate;
import com.example.courseprifs.model.Cuisine;
import com.example.courseprifs.model.Restaurant;
import com.example.courseprifs.model.User;
import com.example.courseprifs.model.VehicleType;
import jakarta.persistence.EntityManagerFactory;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.ResourceBundle;

public class UserForm implements Initializable {
    public RadioButton userRadio;
    public RadioButton restaurantRadio;
    public RadioButton clientRadio;
    public RadioButton driverRadio;
    public TextField loginField;
    public PasswordField passwordField;
    public PasswordField rePasswordField;
    public TextField nameField;
    public TextField surnameField;
    public TextField phoneNumberField;
    public Pane userField;
    public Pane clientPane;
    public TextField ClientAddress;
    public ToggleGroup userType;
    public TextField resName;
    public TextField resDesc;
    public TextField resAddress;
    public TextField resPhone;
    public TextField resEmail;
    public ChoiceBox<Cuisine> resCuisineType;
    public TextField resDeliFee;
    public TextField resEstDeliTime;
    public TextField resOpeningTime;
    public TextField resClosingTime;
    public TextField driverAddress;
    public Pane driverPane;
    public Pane restaurantPane;
    public TextField driverLicense;
    public DatePicker driverBirthDate;
    public ComboBox<VehicleType> driverVehicleType;
    public TextField driverVehicleInfo;
    public Pane resPane;

    private EntityManagerFactory emf;
    private GenericHibernate genericHibernate;

    public void setData(EntityManagerFactory emf) {
        this.emf = emf;
        this.genericHibernate = new GenericHibernate(emf);
    }
    public void createNewUser(){
        if(userRadio.isSelected()){
            User user = new User(loginField.getText(),
                    passwordField.getText(),
                    nameField.getText(),
                    surnameField.getText(),
                    phoneNumberField.getText(),
                    true);
            genericHibernate.create(user);
        }
        else if(restaurantRadio.isSelected()){
            Restaurant restaurant = new Restaurant();
        }
    }


    public void disableFields(){
        boolean isUser = userRadio.isSelected();
        boolean isRestaurant = restaurantRadio.isSelected();
        boolean isClient = clientRadio.isSelected();
        boolean isDriver = driverRadio.isSelected();


        if(isUser){;
            resPane.setDisable(true);
            resPane.setVisible(false);
            driverPane.setDisable(true);
            driverPane.setVisible(false);
            clientPane.setDisable(true);
            clientPane.setVisible(false);
        }
        else if(isRestaurant){
            resPane.setDisable(false);
            resPane.setVisible(true);
            driverPane.setDisable(true);
            driverPane.setVisible(false);
            clientPane.setDisable(true);
            clientPane.setVisible(false);

        } else if (isClient) {
            resPane.setDisable(true);
            resPane.setVisible(false);
            driverPane.setDisable(true);
            driverPane.setVisible(false);
            clientPane.setDisable(false);
            clientPane.setVisible(true);
        }
        else if(isDriver){
            resPane.setDisable(true);
            resPane.setVisible(false);
            driverPane.setDisable(false);
            driverPane.setVisible(true);
            clientPane.setDisable(true);
            clientPane.setVisible(false);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        disableFields();
    }
}
