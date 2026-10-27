package com.example.courseprifs.fxControllers;

import com.example.courseprifs.HelloApplication;
import com.example.courseprifs.hibernateControl.GenericHibernate;
import com.example.courseprifs.model.*;
import com.example.courseprifs.utils.FxUtils;
import com.example.courseprifs.utils.PasswordUtils;
import jakarta.persistence.EntityManagerFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class UserForm implements Initializable {
    //<editor-fold desc="RadioButtons">
    public RadioButton userRadio;
    public RadioButton restaurantRadio;
    public RadioButton clientRadio;
    public RadioButton driverRadio;
    //</editor-fold>
    //<editor-fold desc="UserPane">
    public TextField loginField;
    public PasswordField passwordField;
    public TextField nameField;
    public TextField surnameField;
    public TextField phoneNumberField;
    //</editor-fold>
    public Pane clientPane;
    public ToggleGroup userType;
    public TextField resName;
    public TextField resDesc;
    public TextField resAddress;
    public TextField resPhone;
    public TextField resEmail;
    public ComboBox<CuisineType> resCuisineType;
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
    public TextField clientAddress;

    private EntityManagerFactory emf;
    private GenericHibernate genericHibernate;
    private User userForUpdate;
    private boolean isForUpdate;
    private LoginForm loginFormReference;
    private User currentUser;

    private static final Pattern STRONG_PWD =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9])\\S{8,64}$");
    private boolean isStrongPassword(String pwd) {
        return pwd != null && STRONG_PWD.matcher(pwd).matches();
    }


    public void setData(EntityManagerFactory entityManagerFactory, User user, boolean isForUpdate) {
        this.emf = entityManagerFactory;
        this.genericHibernate = new GenericHibernate(entityManagerFactory);
        this.userForUpdate = user;
        this.isForUpdate = isForUpdate;
        this.currentUser = null; // Not logged in (registration)
        setRadioButtonVisibility();
        fillUserDataForUpdate();
    }

    public void setData(EntityManagerFactory entityManagerFactory, User user, boolean isForUpdate, User currentUser) {
        this.emf = entityManagerFactory;
        this.genericHibernate = new GenericHibernate(entityManagerFactory);
        this.userForUpdate = user;
        this.isForUpdate = isForUpdate;
        this.currentUser = currentUser;
        setRadioButtonVisibility();
        fillUserDataForUpdate();
    }

    public void setLoginFormReference(LoginForm loginForm) {
        this.loginFormReference = loginForm;
    }

    public void createNewUser(){
        if (loginField.getText().isEmpty() || passwordField.getText().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Missing Data", "Login or Password Missing", "Please fill in all login and password fields.");
            return;
        }
        if (!isStrongPassword(passwordField.getText())) {
            FxUtils.generateAlert(Alert.AlertType.ERROR, "Weak password",
                    "Password does not meet requirements",
                    "Use 8–64 characters with at least 1 uppercase, 1 lowercase, 1 digit, and 1 symbol. No spaces.");
            return;
        }
        if (nameField.getText().isEmpty() || surnameField.getText().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Missing Data", "Name or Surname Missing", "Please fill in all required personal information.");
            return;
        }
        // Hash the password before saving
        String hashedPassword = PasswordUtils.hashPassword(passwordField.getText());
        
        if(userRadio.isSelected()){
            User user = new User(loginField.getText(),
                    hashedPassword,
                    nameField.getText(),
                    surnameField.getText(),
                    phoneNumberField.getText(),
                    LocalDateTime.now());
            genericHibernate.create(user);
        } else if(restaurantRadio.isSelected()){
            if (resName.getText().isEmpty() || resDesc.getText().isEmpty() || resAddress.getText().isEmpty() ||
                    resPhone.getText().isEmpty() || resEmail.getText().isEmpty() || resCuisineType.getValue() == null ||
                    resDeliFee.getText().isEmpty() || resOpeningTime.getText().isEmpty() || resClosingTime.getText().isEmpty()) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Missing Data", "Incomplete Restaurant Info", "Please fill in all restaurant fields.");
                return;
            }
            Restaurant restaurant = new Restaurant(loginField.getText(),
                    hashedPassword,
                    nameField.getText(),
                    surnameField.getText(),
                    resPhone.getText(),
                    LocalDateTime.now(),
                    resAddress.getText(),
                    resName.getText(),
                    resDesc.getText(),
                    resEmail.getText(),
                    resCuisineType.getValue(),
                    LocalTime.parse(resOpeningTime.getText()),
                    LocalTime.parse(resClosingTime.getText()),
                    Double.parseDouble(resDeliFee.getText()));
            genericHibernate.create(restaurant);
        } else if(clientRadio.isSelected()){
            BasicUser basicUser = new BasicUser(loginField.getText(),
                    hashedPassword,
                    nameField.getText(),
                    surnameField.getText(),
                    phoneNumberField.getText(),
                    clientAddress.getText());
            genericHibernate.create(basicUser);
        }else if(driverRadio.isSelected()){
            Driver driver = new Driver(loginField.getText(),
                    hashedPassword,
                    nameField.getText(),
                    surnameField.getText(),
                    phoneNumberField.getText(),
                    LocalDateTime.now(),
                    driverAddress.getText(),
                    driverLicense.getText(),
                    driverBirthDate.getValue(),
                    driverVehicleType.getValue(),
                    driverVehicleInfo.getText());
            genericHibernate.create(driver);
        }
        
        // Show success message and switch back to login form
        FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Registration Successful", 
                "User has been created successfully. You can now login.");
        
        // Switch back to login form if we came from login form
        if (!isForUpdate) {
            try {
                switchBackToLogin();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void switchBackToLogin() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-form.fxml"));
        Parent parent = fxmlLoader.load();

        Scene scene = new Scene(parent);
        Stage stage = (Stage) loginField.getScene().getWindow();
        stage.setTitle("Login form");
        stage.setScene(scene);
        stage.show();
    }


    private void fillUserDataForUpdate() {
        if(userForUpdate != null && isForUpdate){
            if(userForUpdate instanceof User){
                loginField.setText(userForUpdate.getLogin());
                // Don't display the hashed password, leave it empty for security
                passwordField.clear();
                //likusius reiktu pabaigti
            }
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
            phoneNumberField.setDisable(true);
            phoneNumberField.setVisible(false);

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

    public void updateUser(ActionEvent actionEvent) {
    }

    private void setRadioButtonVisibility() {
        // If not logged in (registration) or logged in as non-admin: show only Restaurant
        // If logged in as admin: show all options
        if (currentUser == null || !currentUser.isAdmin()) {
            // Not logged in or regular user: only show Restaurant radio button
            userRadio.setVisible(false);
            userRadio.setManaged(false);
            clientRadio.setVisible(false);
            clientRadio.setManaged(false);
            driverRadio.setVisible(false);
            driverRadio.setManaged(false);
            restaurantRadio.setVisible(true);
            restaurantRadio.setManaged(true);
            restaurantRadio.setSelected(true);
            disableFields(); // This will show the restaurant pane
        } else {
            // Admin: show all radio buttons
            userRadio.setVisible(true);
            userRadio.setManaged(true);
            restaurantRadio.setVisible(true);
            restaurantRadio.setManaged(true);
            clientRadio.setVisible(true);
            clientRadio.setManaged(true);
            driverRadio.setVisible(true);
            driverRadio.setManaged(true);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        disableFields();
        driverVehicleType.getItems().addAll(VehicleType.values());
        resCuisineType.getItems().addAll(CuisineType.values());
    }
}
