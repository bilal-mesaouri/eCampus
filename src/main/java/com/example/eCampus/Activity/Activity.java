package com.example.eCampus.Activity;

import com.example.eCampus.LearningObject.LearningObject;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Activity extends LearningObject {
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
        super(id, title, description);
        this.type = type;
        this.points = points;
        this.duration = duration;
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

    @Override
    public Activity duplicate() {
        return new Activity(null, getTitle(), getDescription(), type, points, duration);
    }
}
