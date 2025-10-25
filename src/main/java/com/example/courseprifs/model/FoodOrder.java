package com.example.courseprifs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class FoodOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private Double price;
    @ManyToOne
    private BasicUser buyer;
    @ManyToMany
    private List<Cuisine> cuisineList;
    @OneToOne
    private Chat chat;
    @ManyToOne
    private Restaurant restaurant;

}
