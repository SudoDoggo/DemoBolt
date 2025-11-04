package com.example.courseprifs.fxControllers;

import com.example.courseprifs.hibernateControl.CustomHibernate;
import com.example.courseprifs.model.*;
import jakarta.persistence.EntityManagerFactory;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.net.URL;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

public class ChatForm implements Initializable {

    public ListView messageList;
    public TextArea messageBody;
    private EntityManagerFactory entityManagerFactory;
    private CustomHibernate customHibernate;
    private User currentUser;
    private FoodOrder currentFoodOrder;

    public void setData(EntityManagerFactory entityManagerFactory, User currentUser, FoodOrder currentFoodOrder) {
        this.entityManagerFactory = entityManagerFactory;
        this.currentUser = currentUser;
        this.currentFoodOrder = currentFoodOrder;
        this.customHibernate = new CustomHibernate(entityManagerFactory);
        loadMessages();
    }

    public void sendMessage() {
        if (currentFoodOrder.getChat() == null) {
            Chat chat = new Chat("Chat no " + currentFoodOrder.getName(), currentFoodOrder);
            customHibernate.create(chat);
            List<Chat> allChat = customHibernate.getAllRecords(Chat.class);
            for(Chat c : allChat) {
                if(c.getName().equals(chat.getName())) {
                    currentFoodOrder.setChat(c);
                    customHibernate.update(currentFoodOrder);
                    break;
                }
            }
        }
        FoodOrder foodOrder = customHibernate.getEntityById(FoodOrder.class, currentFoodOrder.getId());
        Review message = new Review(messageBody.getText(), LocalDate.now(), (BasicUser) currentUser, foodOrder.getBuyer(), foodOrder.getChat());
        customHibernate.create(message);

        loadMessages();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //LoadMessages();
    }

    private void loadMessages() {
        messageList.getItems().clear();
        List<Review> allReviews = customHibernate.getEntityById(Review.class, currentFoodOrder.getChat().getId());
        if (allReviews == null) {
            return;
        } else {
                messageList.getItems().add(allReviews);
        }
    }

}