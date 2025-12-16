package com.example.courseprifs.fxControllers;

import com.example.courseprifs.HelloApplication;
import com.example.courseprifs.hibernateControl.CustomHibernate;
import com.example.courseprifs.model.*;
import com.example.courseprifs.utils.FxUtils;
import com.example.courseprifs.utils.PasswordUtils;
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
    public TextArea messageText;
    @FXML
    public ComboBox<String> userTypeBox;
    @FXML
    public TextField userLoginField;
    @FXML
    public TextField userNameField;
    @FXML
    public TextField userSurnameField;
    @FXML
    public ComboBox<Driver> driverList;

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
    @FXML
    public ListView<Cuisine> orderItems;
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
        
        // Enable multiple selection for foodList and add listener to calculate price when cuisines are selected
        if (foodList != null) {
            foodList.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);
            // Listen to multiple selection changes
            foodList.getSelectionModel().getSelectedItems().addListener((javafx.collections.ListChangeListener<Cuisine>) change -> {
                calculateTotalPrice();
            });
        }
        
        // Add listener to cuisineList to load cuisine info when selected
        if (cuisineList != null) {
            cuisineList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    loadCuisineInfo(newValue);
                } else {
                    // Clear fields when selection is cleared
                    clearCuisineFields();
                }
            });
        }

        userTypeBox.getItems().addAll("All users", "User", "BasicUser", "Restaurant", "Driver");
        passCol.setCellFactory(TextFieldTableCell.forTableColumn());
        passCol.setOnEditCommit(event -> {
            String newPassword = event.getNewValue();
            if (newPassword == null || newPassword.trim().isEmpty()) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Invalid Password", "Password cannot be empty", "Please enter a password.");
                reloadTableData();
                return;
            }
            String hashedPassword = PasswordUtils.hashPassword(newPassword);
            event.getTableView().getItems().get(event.getTablePosition().getRow()).setPassword("****");
            int userId = event.getTableView().getItems().get(event.getTablePosition().getRow()).getId();
            User user = customHibernate.getEntityById(User.class, userId);
            
            if (user != null) {
                user.setPassword(hashedPassword);
                customHibernate.update(user);
            } else {
                FxUtils.generateAlert(Alert.AlertType.ERROR, "Error", "User not found", "Could not find the user to update.");
                reloadTableData();
            }
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
                tabsPane.getTabs().remove(userTab);
                tabsPane.getTabs().remove(chatTab);
                restaurantCombBox.setVisible(false);
                priceField.setDisable(true); // Restaurant can see but not edit price
                driverList.setDisable(true); // Restaurant can see but not change driver
            }
        } else {
            priceField.setDisable(false); // Admin can edit price
        }
    }

    //<editor-fold desc="User Tab functionality">
    private void loadUserTable(List<User> users) {
        data.clear();
        userTable.getItems().clear();
        for (User u : users) {
            UserTableParameters userTableParameters = new UserTableParameters();
            userTableParameters.setId(u.getId());
            userTableParameters.setUserType(u.getClass().getSimpleName());
            userTableParameters.setLogin(u.getLogin());
            // Don't display the hashed password in the table for security
            userTableParameters.setPassword("****");
            userTableParameters.setName(u.getName());
            userTableParameters.setSurname(u.getSurname());
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
    }
    
    public void reloadTableData() {
        if (userTab.isSelected()) {
            List<User> users = customHibernate.getAllRecords(User.class);
            loadUserTable(users);
        } else if (managementTab.isSelected()) {
            reloadManagementTab();
            clearAllOrderFields();
            List<FoodOrder> foodOrders = getFoodOrders();
            ordersList.getItems().addAll(foodOrders);
            List<BasicUser> allbasicUser = customHibernate.getAllRecords(BasicUser.class).stream()
                    .filter(u -> u.getClass() == BasicUser.class)
                    .toList();
            clientList.getItems().addAll(allbasicUser);
            filterClients.getItems().addAll(allbasicUser);
            
            // Populate driverList with all drivers
            List<Driver> allDrivers = customHibernate.getAllRecords(Driver.class);
            driverList.getItems().clear();
            driverList.getItems().addAll(allDrivers);
            
            orderStatusComboBox.getItems().clear();
            orderStatusComboBox.getItems().addAll(OrderStatus.values());
            filterStatus.getItems().addAll(OrderStatus.values());
            if (currentUser instanceof Restaurant) {
                // Restaurant users don't see restaurantCombBox, but we set it for internal use
                restaurantCombBox.getItems().addAll((Restaurant) currentUser);
                restaurantCombBox.getSelectionModel().select((Restaurant) currentUser);
                // Automatically load the restaurant's menu in foodList
                foodList.getItems().clear();
                foodList.getItems().addAll(customHibernate.getRestaurantCuisine((Restaurant) currentUser));
            } else if (currentUser.isAdmin()) {
                restaurantCombBox.getItems().addAll(customHibernate.getAllRecords(Restaurant.class));
            }
        } else if (foodTab.isSelected()) {
            clearAllCuisineFields();
            if (currentUser instanceof Restaurant) {
                restaurantList.getItems().addAll((Restaurant) currentUser);
            } else if (currentUser.isAdmin()) {
                restaurantList.getItems().addAll(customHibernate.getAllRecords(Restaurant.class));
            }
        } else if (chatTab.isSelected()) {
            allChats.getItems().clear();
            allChats.getItems().addAll(customHibernate.getAllRecords(Chat.class));
        }
    }

    private void reloadManagementTab() {
        clientList.getItems().clear();
        filterClients.getItems().clear();
        filterStatus.getItems().clear();
        restaurantList.getItems().clear();
        restaurantCombBox.getItems().clear();
        orderStatusComboBox.getItems().clear();
        driverList.getItems().clear();
        orderItems.getItems().clear();
        foodList.getSelectionModel().clearSelection();
    }

    private void clearAllOrderFields() {
        ordersList.getItems().clear();
        clientList.getItems().clear();
        restaurantCombBox.getItems().clear();
        titleField.clear();
        priceField.clear();
        orderItems.getItems().clear();
        foodList.getSelectionModel().clearSelection();
    }

    private void clearAllCuisineFields() {
        foodList.getItems().clear();
        clearCuisineFields();
        restaurantList.getItems().clear();
    }
    
    private void clearCuisineFields() {
        cuisinePriceField.clear();
        titleCuisineField.clear();
        ingredientsField.clear();
        isDeadly.setSelected(false);
        isVegan.setSelected(false);
        if (restaurantList != null) {
            restaurantList.getSelectionModel().clearSelection();
        }
    }


    //</editor-fold>

    //<editor-fold desc="Alternative Tab Functions">

    public void addUser(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("user-form.fxml"));
        Parent parent = fxmlLoader.load();

        UserForm userForm = fxmlLoader.getController();
        userForm.setData(entityManagerFactory, null, false, currentUser);

        Stage stage = new Stage();
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    @FXML
    private void onDeleteUser() {
            UserTableParameters selected = userTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
        customHibernate.delete(User.class, selected.getId());
        userTable.getItems().remove(selected);
    }

    //</editor-fold>

    //<editor-fold desc="Order Tab functionality">
    private List<FoodOrder> getFoodOrders() {
        if (currentUser instanceof Restaurant) {
            return customHibernate.getRestaurantOrders((Restaurant) currentUser);
        } else if (currentUser.isAdmin()) {
            return customHibernate.getAllFoodOrdersWithItems();
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
        // Validate required fields
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Missing Information", "Order title is required", "Please enter an order title.");
            return;
        }
        
        if (clientList.getValue() == null) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Missing Information", "Client is required", "Please select a client.");
            return;
        }
        
        List<Cuisine> selectedCuisines = new java.util.ArrayList<>(foodList.getSelectionModel().getSelectedItems());
        if (selectedCuisines.isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Missing Information", "No items selected", "Please select at least one cuisine item.");
            return;
        }
        
        Restaurant restaurant;
        if (currentUser instanceof Restaurant) {
            restaurant = (Restaurant) currentUser;
        } else {
            restaurant = restaurantCombBox.getValue();
            if (restaurant == null) {
                FxUtils.generateAlert(Alert.AlertType.WARNING, "Missing Information", "Restaurant is required", "Please select a restaurant.");
                return;
            }
        }
        
        // Calculate price: sum selected cuisine prices + delivery fee
        double totalPrice = 0.0;
        for (Cuisine cuisine : selectedCuisines) {
            if (cuisine.getPrice() != null) {
                totalPrice += cuisine.getPrice();
            }
        }
        // Add restaurant delivery fee
        if (restaurant != null) {
            totalPrice += restaurant.getDeliveryFee();
        }
        
        FoodOrder foodOrder = new FoodOrder(titleField.getText(), totalPrice, clientList.getValue(), selectedCuisines, restaurant, LocalDate.now(), LocalDate.now());
        customHibernate.create(foodOrder);
        
        // Clear fields after creating
        titleField.clear();
        priceField.clear();
        foodList.getSelectionModel().clearSelection();
        clientList.getSelectionModel().clearSelection();
        
        fillOrderLists();
        
        // Show success message
        FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Order Created Successfully", 
                "Order \"" + foodOrder.getName() + "\" has been created successfully.");
    }

    public void updateOrder() {
        FoodOrder foodOrder = ordersList.getSelectionModel().getSelectedItem();
        if (foodOrder == null) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Selection Required", "No order selected", "Please select an order to update.");
            return;
        }
        
        Restaurant restaurant;
        if (currentUser instanceof Restaurant) {
            restaurant = (Restaurant) currentUser;
        } else {
            restaurant = restaurantCombBox.getSelectionModel().getSelectedItem();
        }
        
        if (restaurant == null) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Missing Information", "Restaurant is required", "Please select a restaurant.");
            return;
        }
        
        foodOrder.setRestaurant(restaurant);
        foodOrder.setName(titleField.getText());
        
        // Calculate price: sum selected cuisine prices + delivery fee
        List<Cuisine> selectedCuisines = new java.util.ArrayList<>(foodList.getSelectionModel().getSelectedItems());
        if (!selectedCuisines.isEmpty()) {
            double totalPrice = 0.0;
            // Sum selected cuisine prices
            for (Cuisine cuisine : selectedCuisines) {
                if (cuisine.getPrice() != null) {
                    totalPrice += cuisine.getPrice();
                }
            }
            // Add restaurant delivery fee
            if (restaurant != null) {
                totalPrice += restaurant.getDeliveryFee();
            }
            foodOrder.setPrice(totalPrice);
            // Update cuisine list
            foodOrder.setCuisineList(selectedCuisines);
        } else if (isNumeric(priceField.getText())) {
            // If no cuisines selected but price field has value, use it
            foodOrder.setPrice(Double.valueOf(priceField.getText()));
        }
        
        foodOrder.setOrderStatus(orderStatusComboBox.getValue());
        foodOrder.setBuyer(clientList.getSelectionModel().getSelectedItem());
        foodOrder.setDriver(driverList.getValue());
        foodOrder.setDateUpdated(LocalDate.now());
        customHibernate.update(foodOrder);
        fillOrderLists();
        
        // Reload the updated order info
        loadOrderInfo();
        
        // Show success message
        FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Order Updated Successfully", 
                "Order \"" + foodOrder.getName() + "\" has been updated successfully.");
    }

    public void deleteOrder() {
        FoodOrder selectedOrder = ordersList.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Selection Required", "No order selected", 
                    "Please select an order to delete.");
            return;
        }
        
        String orderName = selectedOrder.getName();
        customHibernate.delete(FoodOrder.class, selectedOrder.getId());
        fillOrderLists();
        
        // Clear form fields after deletion
        clearAllOrderFields();
        
        // Show success message
        FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Order Deleted Successfully", 
                "Order \"" + orderName + "\" has been deleted successfully.");
    }

    private void fillOrderLists() {
        ordersList.getItems().clear();
        List<FoodOrder> orders = getFoodOrders();
        if (orders != null) {
            ordersList.getItems().addAll(orders);
        }
    }

    public void loadOrderInfo() {
        FoodOrder selectedOrder = ordersList.getSelectionModel().getSelectedItem();
        
        if (selectedOrder == null) {
            clientList.getSelectionModel().clearSelection();
            driverList.getSelectionModel().clearSelection();
            titleField.clear();
            priceField.clear();
            orderStatusComboBox.getSelectionModel().clearSelection();
            orderItems.getItems().clear();
            if (currentUser.isAdmin()) {
                restaurantCombBox.getSelectionModel().clearSelection();
            }
            disableFoodOrderFields();
            return;
        }
        
        clientList.getItems().stream()
                .filter(c -> c.getId() == selectedOrder.getBuyer().getId())
                .findFirst()
                .ifPresent(u -> clientList.getSelectionModel().select(u));

        titleField.setText(selectedOrder.getName());
        priceField.setText(String.valueOf(selectedOrder.getPrice()));
        if (currentUser.isAdmin()) {
            restaurantCombBox.getItems().stream()
                    .filter(r -> r.getId() == selectedOrder.getRestaurant().getId())
                    .findFirst()
                    .ifPresent(u -> restaurantCombBox.getSelectionModel().select(u));
        }
        orderStatusComboBox.getItems().stream()
                .filter(o -> o == selectedOrder.getOrderStatus())
                .findFirst()
                .ifPresent(o -> orderStatusComboBox.getSelectionModel().select(o));

        // Select driver if order has one assigned
        if (selectedOrder.getDriver() != null) {
            driverList.getItems().stream()
                    .filter(d -> d.getId() == selectedOrder.getDriver().getId())
                    .findFirst()
                    .ifPresent(d -> driverList.getSelectionModel().select(d));
        } else {
            driverList.getSelectionModel().clearSelection();
        }

        // Reload the order from database with cuisineList eagerly loaded (lazy loading issue)
        FoodOrder orderWithItems = customHibernate.getFoodOrderWithItems(selectedOrder.getId());
        orderItems.getItems().clear();
        if (orderWithItems != null && orderWithItems.getCuisineList() != null) {
            orderItems.getItems().addAll(orderWithItems.getCuisineList());
        }
        
        // Select the cuisines in foodList that are part of this order
        // Also reload the restaurant menu if needed to ensure cuisines are available
        if (orderWithItems != null && orderWithItems.getRestaurant() != null) {
            Restaurant orderRestaurant = orderWithItems.getRestaurant();
            // Make sure the restaurant's menu is loaded in foodList
            if (currentUser.isAdmin()) {
                // For admin, ensure the restaurant is selected and menu is loaded
                if (restaurantCombBox.getValue() == null || restaurantCombBox.getValue().getId() != orderRestaurant.getId()) {
                    restaurantCombBox.getItems().stream()
                            .filter(r -> r.getId() == orderRestaurant.getId())
                            .findFirst()
                            .ifPresent(r -> {
                                restaurantCombBox.getSelectionModel().select(r);
                                loadRestaurantInfo();
                            });
                }
            }
        }
        
        if (orderWithItems != null && orderWithItems.getCuisineList() != null && !orderWithItems.getCuisineList().isEmpty()) {
            foodList.getSelectionModel().clearSelection();
            // Match by ID since objects might be different instances
            for (Cuisine orderCuisine : orderWithItems.getCuisineList()) {
                for (int i = 0; i < foodList.getItems().size(); i++) {
                    Cuisine foodListCuisine = foodList.getItems().get(i);
                    if (foodListCuisine != null && foodListCuisine.getId() == orderCuisine.getId()) {
                        foodList.getSelectionModel().select(i);
                        break;
                    }
                }
            }
        } else {
            foodList.getSelectionModel().clearSelection();
        }
        
        disableFoodOrderFields();
    }

    private void disableFoodOrderFields() {
        if (!currentUser.isAdmin() && orderStatusComboBox.getSelectionModel().getSelectedItem() == OrderStatus.COMPLETED) {
            clientList.setDisable(true);
            priceField.setDisable(true);
            orderStatusComboBox.setDisable(true);
            titleField.setDisable(true);
            driverList.setDisable(true);
        } else {
            clientList.setDisable(false);
            // Price field: Restaurant users always disabled, Admin can edit
            if (currentUser instanceof Restaurant) {
                priceField.setDisable(true);
            } else {
                priceField.setDisable(false);
            }
            orderStatusComboBox.setDisable(false);
            titleField.setDisable(false);
            // Driver field: Restaurant users can see but not change, Admin can edit
            if (currentUser instanceof Restaurant) {
                driverList.setDisable(true);
            } else {
                driverList.setDisable(false);
            }
        }
    }

    public void filterOrders() {
        clearAllOrderFields();
        ordersList.getItems().clear();
        ordersList.getItems().addAll(customHibernate.getFilteredRestaurantOrders(filterStatus.getValue(),filterClients.getValue(),filterFrom.getValue(),filterTo.getValue(),restaurantCombBox.getValue()));
    }

    public void loadRestaurantInfo() {
        foodList.getItems().clear();
        foodList.getSelectionModel().clearSelection();
        priceField.clear();
        Restaurant restaurant;
        if (currentUser instanceof Restaurant) {
            restaurant = (Restaurant) currentUser;
        } else {
            restaurant = restaurantCombBox.getSelectionModel().getSelectedItem();
        }
        if (restaurant != null) {
            foodList.getItems().addAll(customHibernate.getRestaurantCuisine(restaurant));
            // Filter orders list to show only this restaurant's orders
            ordersList.getItems().clear();
            List<FoodOrder> restaurantOrders = customHibernate.getRestaurantOrders(restaurant);
            ordersList.getItems().addAll(restaurantOrders);
            // Recalculate price if there are any selected items (shouldn't happen after clear, but just in case)
            if (!foodList.getSelectionModel().getSelectedItems().isEmpty()) {
                calculateTotalPrice();
            }
        }
    }
    
    private void calculateTotalPrice() {
        if (foodList == null || priceField == null) {
            return;
        }
        
        // Get the current restaurant
        Restaurant restaurant = null;
        if (currentUser instanceof Restaurant) {
            restaurant = (Restaurant) currentUser;
        } else if (restaurantCombBox != null && restaurantCombBox.getValue() != null) {
            restaurant = restaurantCombBox.getValue();
        }
        
        List<Cuisine> selectedCuisines = new java.util.ArrayList<>(foodList.getSelectionModel().getSelectedItems());
        double totalPrice = 0.0;
        
        // Sum selected cuisine prices
        for (Cuisine cuisine : selectedCuisines) {
            if (cuisine != null && cuisine.getPrice() != null) {
                totalPrice += cuisine.getPrice();
            }
        }
        
        // Add restaurant delivery fee
        if (restaurant != null) {
            totalPrice += restaurant.getDeliveryFee();
        }
        
        if (totalPrice > 0.0) {
            priceField.setText(String.format("%.2f", totalPrice));
        } else {
            priceField.clear();
        }
    }

    public void resetFilter(ActionEvent actionEvent) {
        filterStatus.getSelectionModel().clearSelection();
        filterClients.getSelectionModel().clearSelection();
        filterFrom.setValue(null);
        filterTo.setValue(null);
        if (currentUser.isAdmin()) {
            restaurantCombBox.getSelectionModel().clearSelection();
        }
        orderItems.getItems().clear();
        foodList.getSelectionModel().clearSelection();
        titleField.clear();
        priceField.clear();
        List<FoodOrder> foodOrders = getFoodOrders();
        ordersList.getItems().clear();
        ordersList.getItems().addAll(foodOrders);
    }

    public void deleteOrderItem() {
        FoodOrder selectedOrder = ordersList.getSelectionModel().getSelectedItem();
        Cuisine selectedItem = orderItems.getSelectionModel().getSelectedItem();

        if (selectedOrder == null || selectedItem == null) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Selection Required",
                    "No item selected", "Please select an order and an item to delete.");
            return;
        }

        customHibernate.removeCuisineFromOrder(selectedOrder.getId(), selectedItem.getId());
        FoodOrder updatedOrder = customHibernate.getFoodOrderWithItems(selectedOrder.getId());
        orderItems.getItems().clear();
        if (updatedOrder != null && updatedOrder.getCuisineList() != null) {
            orderItems.getItems().addAll(updatedOrder.getCuisineList());
        }
    }
    //</editor-fold>

    //<editor-fold desc="Cuisine Tab Functionality">
    public void createNewMenuItem() {
        // Validate required fields
        if (titleCuisineField.getText() == null || titleCuisineField.getText().trim().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Missing Information", "Cuisine name is required", "Please enter a cuisine name.");
            return;
        }
        
        if (cuisinePriceField.getText() == null || cuisinePriceField.getText().trim().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Missing Information", "Price is required", "Please enter a price.");
            return;
        }
        
        if (!isNumeric(cuisinePriceField.getText())) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Invalid Price", "Price must be a number", "Please enter a valid numeric price.");
            return;
        }
        
        if (restaurantList.getSelectionModel().getSelectedItem() == null) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Missing Information", "Restaurant is required", "Please select a restaurant.");
            return;
        }
        
        Cuisine cuisine = new Cuisine(titleCuisineField.getText(), ingredientsField.getText(), Double.parseDouble(cuisinePriceField.getText()), isDeadly.isSelected(), isVegan.isSelected(), restaurantList.getSelectionModel().getSelectedItem());
        customHibernate.create(cuisine);
        
        String cuisineName = cuisine.getName();
        loadRestaurantMenu();
        
        // Clear fields after creating
        clearCuisineFields();
        if (cuisineList != null) {
            cuisineList.getSelectionModel().clearSelection();
        }
        
        // Show success message
        FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Menu Item Created Successfully", 
                "Menu item \"" + cuisineName + "\" has been created successfully.");
    }
    
    public void updateMenuItem(ActionEvent actionEvent) {
        Cuisine cuisine = cuisineList.getSelectionModel().getSelectedItem();
        if (cuisine == null) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Selection Required", "No menu item selected", 
                    "Please select a menu item to update.");
            return;
        }
        
        // Validate required fields
        if (titleCuisineField.getText() == null || titleCuisineField.getText().trim().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Missing Information", "Cuisine name is required", "Please enter a cuisine name.");
            return;
        }
        
        if (cuisinePriceField.getText() == null || cuisinePriceField.getText().trim().isEmpty()) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Missing Information", "Price is required", "Please enter a price.");
            return;
        }
        
        if (!isNumeric(cuisinePriceField.getText())) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Invalid Price", "Price must be a number", "Please enter a valid numeric price.");
            return;
        }
        
        if (restaurantList.getSelectionModel().getSelectedItem() == null) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Missing Information", "Restaurant is required", "Please select a restaurant.");
            return;
        }
        
        String oldName = cuisine.getName();
        cuisine.setName(titleCuisineField.getText());
        cuisine.setIngredients(ingredientsField.getText());
        cuisine.setRestaurant(restaurantList.getSelectionModel().getSelectedItem());
        cuisine.setPrice(Double.valueOf(cuisinePriceField.getText()));
        cuisine.setVegan(isVegan.isSelected());
        cuisine.setSpicy(isDeadly.isSelected());
        customHibernate.update(cuisine);
        loadRestaurantMenu();
        
        // Show success message
        FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Menu Item Updated Successfully", 
                "Menu item \"" + oldName + "\" has been updated successfully.");
    }
    
    public void deleteMenuItem(ActionEvent actionEvent) {
        Cuisine selectedCuisine = cuisineList.getSelectionModel().getSelectedItem();
        if (selectedCuisine == null) {
            FxUtils.generateAlert(Alert.AlertType.WARNING, "Selection Required", "No menu item selected", 
                    "Please select a menu item to delete.");
            return;
        }
        
        String cuisineName = selectedCuisine.getName();
        customHibernate.delete(Cuisine.class, selectedCuisine.getId());
        loadRestaurantMenu();
        
        // Clear fields after deletion
        clearCuisineFields();
        if (cuisineList != null) {
            cuisineList.getSelectionModel().clearSelection();
        }
        
        // Show success message
        FxUtils.generateAlert(Alert.AlertType.INFORMATION, "Success", "Menu Item Deleted Successfully", 
                "Menu item \"" + cuisineName + "\" has been deleted successfully.");
    }
    public void loadRestaurantMenu() {
        cuisineList.getItems().clear();
        cuisineList.getItems().addAll(customHibernate.getRestaurantCuisine(restaurantList.getSelectionModel().getSelectedItem()));
    }
    
    private void loadCuisineInfo(Cuisine cuisine) {
        if (cuisine == null) {
            return;
        }
        
        // Fill in all fields with cuisine information
        titleCuisineField.setText(cuisine.getName() != null ? cuisine.getName() : "");
        ingredientsField.setText(cuisine.getIngredients() != null ? cuisine.getIngredients() : "");
        
        if (cuisine.getPrice() != null) {
            cuisinePriceField.setText(String.format("%.2f", cuisine.getPrice()));
        } else {
            cuisinePriceField.clear();
        }
        
        isDeadly.setSelected(cuisine.isSpicy());
        isVegan.setSelected(cuisine.isVegan());
        
        // Select the restaurant in the restaurantList
        if (cuisine.getRestaurant() != null && restaurantList != null) {
            for (int i = 0; i < restaurantList.getItems().size(); i++) {
                Restaurant restaurant = restaurantList.getItems().get(i);
                if (restaurant != null && restaurant.getId() == cuisine.getRestaurant().getId()) {
                    restaurantList.getSelectionModel().select(i);
                    break;
                }
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Admin Chat Functionality">
    //<editor-fold desc="ChatControl">
    public void loadChatMessages() {
        chatMessages.getItems().clear();
        chatMessages.getItems().addAll(customHibernate.getReviewsByChatId(allChats.getSelectionModel().getSelectedItem().getId()));
    }
    public void deleteChat() {
        FoodOrder foodOrder = customHibernate.getFoodOrderByChatId(allChats.getSelectionModel().getSelectedItem().getId());
        foodOrder.setChat(null);
        customHibernate.update(foodOrder);
        customHibernate.delete(Chat.class, allChats.getSelectionModel().getSelectedItem().getId());
        allChats.getItems().clear();
        allChats.getItems().addAll(customHibernate.getAllRecords(Chat.class));
        chatMessages.getItems().clear();
    }
    //</editor-fold>
    //<editor-fold desc="MessageControl">
    public void loadMessageInText() {
        messageText.clear();
        messageText.setText(chatMessages.getSelectionModel().getSelectedItem().getReviewText());
    }
    public void deleteMessage() {
        customHibernate.delete(Review.class, chatMessages.getSelectionModel().getSelectedItem().getId());
        loadChatMessages();
    }
    public void updateMessage() {
        chatMessages.getSelectionModel().getSelectedItem().setReviewText(messageText.getText());
        customHibernate.update(chatMessages.getSelectionModel().getSelectedItem());
        loadChatMessages();
    }
    public void createMessage(ActionEvent actionEvent) {
        Review message = new Review(messageText.getText(), LocalDate.now(), currentUser,
                (customHibernate.getFoodOrderByChatId(allChats.getSelectionModel().getSelectedItem().getId())).getBuyer(),
                allChats.getSelectionModel().getSelectedItem());
        customHibernate.create(message);
        loadChatMessages();
    }
    //</editor-fold>
    //</editor-fold>

    public void loadChatForm(ActionEvent actionEvent) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("chat-form.fxml"));
        Parent parent = fxmlLoader.load();

        ChatForm chatForm = fxmlLoader.getController();
        chatForm.setData(entityManagerFactory, currentUser, ordersList.getSelectionModel().getSelectedItem());

        Stage stage = new Stage();
        Scene scene = new Scene(parent);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public void filterUsers(ActionEvent actionEvent) {
        String userType = userTypeBox.getValue();
        String login = userLoginField.getText();
        String name = userNameField.getText();
        String surname = userSurnameField.getText();

        List<User> filteredUsers = customHibernate.getFilteredUsers(userType, login, name, surname);
        loadUserTable(filteredUsers);
    }


}
