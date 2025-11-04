package com.example.courseprifs.fxControllers;

import com.example.courseprifs.HelloApplication;
import com.example.courseprifs.hibernateControl.CustomHibernate;
import com.example.courseprifs.model.*;
import jakarta.persistence.EntityManagerFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

public class MainForm implements Initializable {
    @FXML
    public Tab userTab;
    @FXML
    public Tab managementTab;
    @FXML
    public Tab foodTab;
    @FXML
    public ListView<User> userListField;
    @FXML //Laikinas, galutineje versijoje jo nebus
    public Tab altTab;
    @FXML
    public TabPane tabsPane;
    //<editor-fold desc="User Tab Elements">
    @FXML
    public TableView<UserTableParameters> userTable;
    @FXML
    public TableColumn<UserTableParameters, Integer> idCol;
    @FXML
    public TableColumn<UserTableParameters, String> userTypeCol;
    @FXML
    public TableColumn<UserTableParameters, String> loginCol;
    @FXML
    public TableColumn<UserTableParameters, String> passCol;
    @FXML
    public TableColumn<UserTableParameters, String> nameCol;
    @FXML
    public TableColumn<UserTableParameters, String> surnameCol;
    @FXML
    public TableColumn<UserTableParameters, String> addrCol;
    @FXML
    public TableColumn<UserTableParameters, String> phoneNrCol;
    @FXML
    public TableColumn<UserTableParameters, String> dateCreatedCol;
    @FXML
    public TableColumn<UserTableParameters, String> dateUpdateCol;

    private ObservableList<UserTableParameters> data = FXCollections.observableArrayList();

    //</editor-fold>
    //<editor-fold desc="Order Tab Elements">
    public ListView<FoodOrder> ordersList;
    public TextField titleField;
    public ComboBox<BasicUser> clientList;
    public TextField priceField;
    public ComboBox<Restaurant> restaurantCombBox;
    public ComboBox<OrderStatus> orderStatusComboBox;
    public ComboBox<OrderStatus> filterStatus;
    public ComboBox<BasicUser> filterClients;
    public DatePicker filterFrom;
    public DatePicker filterTo;
    public ListView<Cuisine> foodList;
    //</editor-fold>
    //<editor-fold desc="Cuisine Tab Elements">
    public TextField titleCuisineField;
    public TextArea ingredientsField;
    public ListView<Restaurant> restaurantList;
    public TextField cuisinePriceField;
    public CheckBox isDeadly;
    public CheckBox isVegan;
    public ListView<Cuisine> cuisineList;
    //</editor-fold>
    //<editor-fold desc="Admin chat Elements">
    public Tab chatTab;
    public ListView<Chat> allChats;
    public ListView<Review> chatMessages;
    //</editor-fold>


    private EntityManagerFactory entityManagerFactory;
    private CustomHibernate customHibernate;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userTable.setEditable(true);
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        userTypeCol.setCellValueFactory(new PropertyValueFactory<>("userType"));
        loginCol.setCellValueFactory(new PropertyValueFactory<>("login"));
        passCol.setCellValueFactory(new PropertyValueFactory<>("password"));
        passCol.setCellFactory(TextFieldTableCell.forTableColumn());
        passCol.setOnEditCommit(event -> {
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
        surnameCol.setCellValueFactory(new PropertyValueFactory<>("surname"));
        surnameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        surnameCol.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setSurname(event.getNewValue());
            User user = customHibernate.getEntityById(User.class, event.getTableView().getItems().get(event.getTablePosition().getRow()).getId());
            user.setSurname(event.getNewValue());
            customHibernate.update(user);
        });
        addrCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        addrCol.setCellFactory(TextFieldTableCell.forTableColumn());
        addrCol.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setAddress(event.getNewValue());
            BasicUser user = customHibernate.getEntityById(BasicUser.class, event.getTableView().getItems().get(event.getTablePosition().getRow()).getId());
            user.setAddress(event.getNewValue());
            customHibernate.update(user);
        });
        phoneNrCol.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        phoneNrCol.setCellFactory(TextFieldTableCell.forTableColumn());
        phoneNrCol.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setPhoneNum(event.getNewValue());
            BasicUser user = customHibernate.getEntityById(BasicUser.class, event.getTableView().getItems().get(event.getTablePosition().getRow()).getId());
            user.setPhoneNumber(event.getNewValue());
            customHibernate.update(user);
        });
        /*dateCreatedCol.setCellValueFactory(new PropertyValueFactory<>("dateCreated"));
        dateUpdateCol.setCellValueFactory(new PropertyValueFactory<>("dateUpdated"));
        dateUpdateCol.setCellFactory(TextFieldTableCell.forTableColumn());
        dateUpdateCol.setOnEditCommit(event -> {
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setDateUpdate(event.getNewValue());
            BasicUser user = customHibernate.getEntityById(BasicUser.class, event.getTableView().getItems().get(event.getTablePosition().getRow()).getId());
            user.setDateUpdated(LocalDateTime.parse(event.getNewValue()));
            customHibernate.update(user);
        });*/
    }

    public void setData(EntityManagerFactory entityManagerFactory, User user) {
        this.entityManagerFactory = entityManagerFactory;
        this.currentUser = user;
        this.customHibernate = new CustomHibernate(entityManagerFactory);
        reloadTableData();
        setUserFormVisibility();
    }

    private void setUserFormVisibility() {
        if (!currentUser.isAdmin()) {
            if (currentUser instanceof Restaurant) {
                tabsPane.getTabs().remove(altTab);
                tabsPane.getTabs().remove(userTab);
                tabsPane.getTabs().remove(chatTab);
            } else if (currentUser instanceof BasicUser) {
                tabsPane.getTabs().remove(altTab);
                tabsPane.getTabs().remove(userTab);
                tabsPane.getTabs().remove(foodTab);
                tabsPane.getTabs().remove(managementTab);
                tabsPane.getTabs().remove(chatTab);
            } else if (currentUser instanceof Driver) {
                tabsPane.getTabs().remove(altTab);
                tabsPane.getTabs().remove(userTab);
                tabsPane.getTabs().remove(foodTab);
                tabsPane.getTabs().remove(managementTab);
                tabsPane.getTabs().remove(chatTab);
            } else if (currentUser instanceof User) {
                tabsPane.getTabs().remove(altTab);
                tabsPane.getTabs().remove(userTab);
                tabsPane.getTabs().remove(foodTab);
                tabsPane.getTabs().remove(managementTab);

            }
        }
    }

    //<editor-fold desc="User Tab functionality">
    public void reloadTableData() {
        if (userTab.isSelected()) {
            data.clear();
            userTable.getItems().clear();
            List<User> users = customHibernate.getAllRecords(User.class);
            for (User u : users) {
                UserTableParameters userTableParameters = new UserTableParameters();
                userTableParameters.setId(u.getId());
                userTableParameters.setUserType(u.getClass().getSimpleName());
                userTableParameters.setLogin(u.getLogin());
                userTableParameters.setPassword(u.getPassword());
                userTableParameters.setName(u.getName());
                userTableParameters.setSurname(u.getSurname());
                userTableParameters.setPhoneNum(u.getPhoneNumber());
                //userTableParameters.setDateCreated(u.getDateCreated().toString());
                //userTableParameters.setDateUpdate(u.getDateUpdated().toString());
                if (u instanceof BasicUser) {
                    userTableParameters.setAddress(((BasicUser) u).getAddress());
                }
                if (u instanceof Restaurant) {
                    userTableParameters.setAddress(((Restaurant) u).getAddress());
                }
                if (u instanceof Driver) {
                    userTableParameters.setAddress(((Driver) u).getAddress());
                }
                data.add(userTableParameters);
            }
            userTable.getItems().addAll(data);
        } else if (managementTab.isSelected()) {
            clearAllOrderFields();
            List<FoodOrder> foodOrders = getFoodOrders();
            ordersList.getItems().addAll(foodOrders);
            List<BasicUser> allbasicUser = customHibernate.getAllRecords(BasicUser.class).stream()
                    .filter(u -> u.getClass() == BasicUser.class)
                    .toList();
            clientList.getItems().addAll(allbasicUser);
            filterClients.getItems().addAll(allbasicUser);
            orderStatusComboBox.getItems().addAll(OrderStatus.values());
            filterStatus.getItems().addAll(OrderStatus.values());
            if (currentUser instanceof Restaurant) {
                restaurantCombBox.getItems().addAll((Restaurant) customHibernate.getUserByCredentials(currentUser.getLogin(), currentUser.getPassword()));
            } else if (currentUser.isAdmin()) {
                restaurantCombBox.getItems().addAll(customHibernate.getAllRecords(Restaurant.class));
            }
        } else if (altTab.isSelected()) {
            userListField.getItems().clear();
            List<User> userList = customHibernate.getAllRecords(User.class);
            userListField.getItems().addAll(userList);
        } else if (foodTab.isSelected()) {
            clearAllCuisineFields();
            if (currentUser instanceof Restaurant) {
                restaurantList.getItems().addAll((Restaurant) customHibernate.getUserByCredentials(currentUser.getLogin(), currentUser.getPassword()));
            } else if (currentUser.isAdmin()) {
                restaurantList.getItems().addAll(customHibernate.getAllRecords(Restaurant.class));
            }
        } else if (chatTab.isSelected()) {
            allChats.getItems().addAll(customHibernate.getAllRecords(Chat.class));
        }
    }

    private void clearAllOrderFields() {
        ordersList.getItems().clear();
        clientList.getItems().clear();
        restaurantCombBox.getItems().clear();
        titleField.clear();
        priceField.clear();
    }

    private void clearAllCuisineFields() {
        foodList.getItems().clear();
        cuisinePriceField.clear();
        titleCuisineField.clear();
        ingredientsField.clear();
        isDeadly.setSelected(false);
        isVegan.setSelected(false);
        restaurantList.getItems().clear();
    }


    //</editor-fold>

    //<editor-fold desc="Alternative Tab Functions">

    public void addUser(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("user-form.fxml"));
        Parent parent = fxmlLoader.load();

        UserForm userForm = fxmlLoader.getController();
        userForm.setData(entityManagerFactory, null, false);

        Stage stage = new Stage();
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public void loadUser(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("user-form.fxml"));
        Parent parent = fxmlLoader.load();

        User selectedUser = userListField.getSelectionModel().getSelectedItem();

        UserForm userForm = fxmlLoader.getController();
        userForm.setData(entityManagerFactory, selectedUser, true);

        Stage stage = new Stage();
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public void deleteUser() {
        User selectedUser = userListField.getSelectionModel().getSelectedItem();
        customHibernate.delete(User.class, selectedUser.getId());
    }

    //</editor-fold>

    //<editor-fold desc="Order Tab functionality">
    private List<FoodOrder> getFoodOrders() {
        if (currentUser instanceof Restaurant) {
            return customHibernate.getRestaurantOrders((Restaurant) currentUser);
        } else if (currentUser.isAdmin()) {
            return customHibernate.getAllRecords(FoodOrder.class);
        }
        return null;
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void createOrder() {
        if (isNumeric(priceField.getText())) {
            FoodOrder foodOrder = new FoodOrder(titleField.getText(), Double.parseDouble(priceField.getText()), clientList.getValue(), foodList.getSelectionModel().getSelectedItems(), restaurantCombBox.getValue(), LocalDate.now(), LocalDate.now());
            customHibernate.create(foodOrder);

            //Alternatyvus būdas:
//            FoodOrder foodOrder2 = new FoodOrder(titleField.getText(), Double.parseDouble(priceField.getText()), basicUserList.getSelectionModel().getSelectedItem(), restaurantField.getValue());
//            customHibernate.create(foodOrder2);
            fillOrderLists();
        }
    }

    public void updateOrder() {
        FoodOrder foodOrder = ordersList.getSelectionModel().getSelectedItem();
        foodOrder.setRestaurant(restaurantCombBox.getSelectionModel().getSelectedItem());
        foodOrder.setName(titleField.getText());
        foodOrder.setPrice(Double.valueOf(priceField.getText()));
        foodOrder.setOrderStatus(orderStatusComboBox.getValue());
        foodOrder.setBuyer(clientList.getSelectionModel().getSelectedItem());
        foodOrder.setDateUpdated(LocalDate.now());
        customHibernate.update(foodOrder);
        fillOrderLists();
    }

    public void deleteOrder() {
        FoodOrder selectedOrder = ordersList.getSelectionModel().getSelectedItem();
        customHibernate.delete(FoodOrder.class, selectedOrder.getId());
        fillOrderLists();
    }

    private void fillOrderLists() {
        ordersList.getItems().clear();
        ordersList.getItems().addAll(customHibernate.getAllRecords(FoodOrder.class));
    }

    public void loadOrderInfo() {
        //Not optimal, code duplication
        FoodOrder selectedOrder = ordersList.getSelectionModel().getSelectedItem();
        clientList.getItems().stream()
                .filter(c -> c.getId() == selectedOrder.getBuyer().getId())
                .findFirst()
                .ifPresent(u -> clientList.getSelectionModel().select(u));

        titleField.setText(selectedOrder.getName());
        priceField.setText(String.valueOf(selectedOrder.getPrice()));
        restaurantCombBox.getItems().stream()
                .filter(r -> r.getId() == selectedOrder.getRestaurant().getId())
                .findFirst()
                .ifPresent(u -> restaurantCombBox.getSelectionModel().select(u));
        orderStatusComboBox.getItems().stream()
                .filter(o -> o == selectedOrder.getOrderStatus())
                .findFirst()
                .ifPresent(o -> orderStatusComboBox.getSelectionModel().select(o));
        disableFoodOrderFields();
    }

    private void disableFoodOrderFields() {
        if (orderStatusComboBox.getSelectionModel().getSelectedItem() == OrderStatus.COMPLETED) {
            clientList.setDisable(true);
            priceField.setDisable(true);
            restaurantCombBox.setDisable(true);
            orderStatusComboBox.setDisable(true);
            restaurantCombBox.setDisable(true);
            titleField.setDisable(true);
        } else {
            clientList.setDisable(false);
            priceField.setDisable(false);
            restaurantCombBox.setDisable(false);
            orderStatusComboBox.setDisable(false);
            restaurantCombBox.setDisable(false);
            titleField.setDisable(false);
        }
    }

    public void filterOrders() {
        clearAllOrderFields();
        //ordersList.getItems().addAll(customHibernate.getFilteredRestaurantOrders(filterStatus.getValue(),filterClients.getValue(),filterFrom.getValue(),filterTo.getValue(),restaurantCombBox.getValue()));
    }

    public void loadRestaurantMenuForOrder() {
        foodList.getItems().clear();
        foodList.getItems().addAll(customHibernate.getRestaurantCuisine(restaurantCombBox.getSelectionModel().getSelectedItem()));
    }
    //</editor-fold>

    //<editor-fold desc="Cuisine Tab Functionality">
    public void createNewMenuItem() {
        Cuisine cuisine = new Cuisine(titleCuisineField.getText(), ingredientsField.getText(), Double.parseDouble(cuisinePriceField.getText()), isDeadly.isSelected(), isVegan.isSelected(), restaurantList.getSelectionModel().getSelectedItem());
        customHibernate.create(cuisine);
    }

    public void updateMenuItem(ActionEvent actionEvent) {
    }

    public void loadRestaurantMenu() {
        cuisineList.getItems().clear();
        cuisineList.getItems().addAll(customHibernate.getRestaurantCuisine(restaurantList.getSelectionModel().getSelectedItem()));
    }
    //</editor-fold>

    //<editor-fold desc="Admin Chat Functionality">
    public void loadChatMessages() {
//        chatMessages.getItems().addAll(customHibernate.getChatMessages(allChats.getSelectionModel().getSelectedItem()));
    }

    public void deleteChat() {
    }

    public void deleteMessage() {
    }

    public void loadChatForm(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("chat-form.fxml"));
        Parent parent = fxmlLoader.load();

        ChatForm chatForm = fxmlLoader.getController();
        chatForm.setData(entityManagerFactory, currentUser,ordersList.getSelectionModel().getSelectedItem());

        Stage stage = new Stage();
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
    //</editor-fold>

}
