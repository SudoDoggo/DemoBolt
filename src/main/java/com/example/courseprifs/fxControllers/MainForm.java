package com.example.courseprifs.fxControllers;

import com.example.courseprifs.hibernateControl.CustomHibernate;
import com.example.courseprifs.model.*;
import com.mysql.cj.xdevapi.RemoveStatement;
import jakarta.persistence.EntityManagerFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainForm implements Initializable {
    public ListView<FoodOrder> orderList;
    public ComboBox<BasicUser> clientsBox;
    public Button newButton;
    public Button updateButton;
    public Button deleteButton;
    public TextField titleField;
    public TextField priceField;
    public ComboBox<Restaurant> restaurantBox;
    @FXML
    public TableView<UserTableParameters> userTable;
    @FXML
    public TableColumn<UserTableParameters, Integer> idCol;
    @FXML
    public TableColumn<UserTableParameters, String> userTypeCol;
    @FXML
    public TableColumn<UserTableParameters, String> loginCol;
    @FXML
    public TableColumn<UserTableParameters, String> passwordCol;
    @FXML
    public TableColumn<UserTableParameters, String> nameCol;
    @FXML
    public TableColumn<UserTableParameters, String> surnameCol;
    @FXML
    public TableColumn<UserTableParameters, String> addrCol;
    @FXML
    public TableColumn<UserTableParameters, Void> dummyCol;
    public Tab userTab;

    private EntityManagerFactory entityManagerFactory;
    private CustomHibernate customHibernate;
    private User currentUser;
    private ObservableList<UserTableParameters> data = FXCollections.observableArrayList();


    public static boolean isNumeric(String str) {
        try{
            Double.parseDouble(str);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }
    private void clearAllFields() {
        orderList.getItems().clear();
        clientsBox.getItems().clear();
        restaurantBox.getItems().clear();
        titleField.clear();
        priceField.clear();
    }

    public void createOrder() {
        if(isNumeric(priceField.getText())){
            FoodOrder foodOrder = new FoodOrder(titleField.getText(),Double.parseDouble(priceField.getText()), clientsBox.getValue(),restaurantBox.getValue());
        }

    }

    public void updateOrder(ActionEvent actionEvent) {
    }

    public void deleteOrder(ActionEvent actionEvent) {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userTable.setEditable(true);
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        userTypeCol.setCellValueFactory(new PropertyValueFactory<>("userType"));
        loginCol.setCellValueFactory(new PropertyValueFactory<>("login"));
        passwordCol.setCellValueFactory(new PropertyValueFactory<>("password"));
        passwordCol.setCellFactory(TextFieldTableCell.forTableColumn());
        passwordCol.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setPassword(event.getNewValue());
            User user = customHibernate.getEntityById(User.class, event.getTableView().getItems().get(event.getTablePosition().getRow()).getId());
            user.setPassword(event.getNewValue());
            customHibernate.update(user);
        });

        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setName(event.getNewValue());
            User user = customHibernate.getEntityById(User.class, event.getTableView().getItems().get(event.getTablePosition().getRow()).getId());
            user.setName(event.getNewValue());
            customHibernate.update(user);
        });
    }
    public void setData(EntityManagerFactory entityManagerFactory, User user) {
        this.entityManagerFactory = entityManagerFactory;
        this.currentUser = user;
        this.customHibernate = new CustomHibernate(entityManagerFactory);
        setUserFormVisibility();
    }
    private void setUserFormVisibility() {
        if (currentUser instanceof User) {
            //turbut nieko nedarom, gal kazka custom
        }
    }
    public void reloadTableData() {
        if (userTab.isSelected()) {
            List<User> users = customHibernate.getAllRecords(User.class);
            for (User u : users) {
                UserTableParameters userTableParameters = new UserTableParameters();
                userTableParameters.setId(u.getId());
                userTableParameters.setUserType(u.getClass().getSimpleName());
                userTableParameters.setLogin(u.getLogin());
                userTableParameters.setPassword(u.getPassword());
                //baigti bendrus laukus
                if (u instanceof BasicUser) {
                    userTableParameters.setAddress(((BasicUser) u).getAddress());
                }
                if (u instanceof Restaurant) {

                }
                if (u instanceof Driver) {

                }
                data.add(userTableParameters);
            }
            userTable.getItems().addAll(data);
        } /*else if (managementTab.isSelected()) {
            List<FoodOrder> foodOrders = getFoodOrders();
        } else if (altTab.isSelected()) {
            List<User> userList = customHibernate.getAllRecords(User.class);
            userListField.getItems().addAll(userList);
        }*/
        //pabaigt
    }
}
