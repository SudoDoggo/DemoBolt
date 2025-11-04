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
            FoodOrder foodOrder = customHibernate.getEntityById(FoodOrder.class, currentFoodOrder.getId());
            Review message = new Review(messageBody.getText(), LocalDate.now(), currentUser, foodOrder.getBuyer(), foodOrder.getChat());
            customHibernate.create(message);
        }else
        {
            FoodOrder foodOrder = customHibernate.getEntityById(FoodOrder.class, currentFoodOrder.getId());
            Review message = new Review(messageBody.getText(), LocalDate.now(),  currentUser, foodOrder.getBuyer(), foodOrder.getChat());
            customHibernate.create(message);
        }
        messageBody.clear();
        loadMessages();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //LoadMessages();
    }

    private void loadMessages() {
        messageList.getItems().clear();
        if (currentFoodOrder.getChat()==null) {
            return;
        } else {
            List<Review> allReviews = customHibernate.getReviewsByChatId(currentFoodOrder.getChat().getId());
            messageList.getItems().addAll(allReviews);
        }
    }

}