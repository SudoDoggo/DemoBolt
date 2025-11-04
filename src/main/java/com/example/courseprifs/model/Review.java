package com.example.courseprifs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int rating;
    private String reviewText;
    private LocalDate dateCreated;
    @ManyToOne
    private BasicUser commentOwner;
    @ManyToOne
    private BasicUser feedbackUser;
    @ManyToOne
    private Chat chat;

    public Review(String reviewText, BasicUser commentOwner, Chat chat) {
        this.reviewText = reviewText;
        this.commentOwner = commentOwner;
        this.chat = chat;
    }

    public Review(String reviewText, LocalDate dateCreated, BasicUser commentOwner, BasicUser feedbackUser, Chat chat) {
        this.reviewText = reviewText;
        this.dateCreated = dateCreated;
        this.commentOwner = commentOwner;
        this.feedbackUser = feedbackUser;
        this.chat = chat;
    }

    @Override
    public String toString() {
        return reviewText;
    }
}
