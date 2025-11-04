package com.example.courseprifs.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Restaurant extends BasicUser {
    private String restaurantName;
    private String description;
    private String email;

    private CuisineType cuisineType;
    private LocalTime openingHour;
    private LocalTime closingHour;
    private double deliveryFee;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Cuisine> menu;
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FoodOrder> foodOrders;

    public Restaurant(String login, String password, String name, String surname, String phoneNumber, String address) {
        super(login, password, name, surname, phoneNumber, address);
    }
    public Restaurant(String login, String password, String name, String surname, String phoneNumber, String address, String restaurantName, String description, String email, CuisineType cuisineType, LocalTime openingHour, LocalTime closingHour, double deliveryFee) {
        super(login, password, name, surname, phoneNumber, address);
        this.restaurantName = restaurantName;
        this.description = description;
        this.email = email;
        this.cuisineType = cuisineType;
        this.deliveryFee = deliveryFee;
        this.openingHour = openingHour;
        this.closingHour = closingHour;
    }

    public Restaurant(String login, String password, String name, String surname, String phoneNumber, LocalDateTime dateCreated, String address, String restaurantName, String description, String email, CuisineType cuisineType, LocalTime openingHour, LocalTime closingHour, double deliveryFee) {
        super(login, password, name, surname, phoneNumber, dateCreated, address);
        this.restaurantName = restaurantName;
        this.description = description;
        this.email = email;
        this.cuisineType = cuisineType;
        this.openingHour = openingHour;
        this.closingHour = closingHour;
        this.deliveryFee = deliveryFee;
    }

    @Override
    public String toString() {
        return "Restaurant: " + restaurantName;
    }
}
