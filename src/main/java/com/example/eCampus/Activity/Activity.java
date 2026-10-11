package com.example.eCampus.Activity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Activity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
    @Enumerated(EnumType.STRING)
    private ActivityTypes type;
    private int points;
    private int duration;

    public Activity() {
    }

    public Activity(Long id, String title, String description, ActivityTypes type, int points) {
        this(id, title, description, type, points, 0);
    }

    public Activity(Long id, String title, String description, ActivityTypes type, int points, int duration) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.points = points;
        this.duration = duration;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ActivityTypes getType() {
        return type;
    }

    public void setType(ActivityTypes type) {
        this.type = type;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
