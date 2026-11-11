package com.example.eCampus.Module;

import com.example.eCampus.Activity.Activity;
import com.example.eCampus.LearningObject.LearningObject;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity 
public class Module extends LearningObject {
    
    private int threeshold;
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "module_activity",
        joinColumns = @JoinColumn(name = "module_id"),
        inverseJoinColumns = @JoinColumn(name = "activity_id")
    )
    private List<Activity> activities = new ArrayList<>();

    public Module() {
    }

    public Module(Long id, String title, String description, int threeshold) {
        super(id, title, description);
        this.threeshold = threeshold;
    }

    public int getThreeshold() {
        return threeshold;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setThreeshold(int threeshold) {
        this.threeshold = threeshold;
    }

    public void addActivity(Activity activity) {
        if (this.activities == null) {
            this.activities = new ArrayList<>();
        }
        this.activities.add(activity);
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public int getModuleDuration() {
        int totalDuration = 0;
        if (this.activities != null) {
            for (Activity activity : this.activities) {
                totalDuration += activity.getDuration();
            }
        }
        return totalDuration;
    }

    public int getTotalPoints() {
        int totalPoints = 0;
        if (this.activities != null) {
            for (Activity activity : this.activities) {
                totalPoints += activity.getPoints();
            }
        }
        return totalPoints;
    }

    public boolean isCompleted(int completedPoints) {
        return completedPoints >= this.threeshold;
    }

    @Override
    public Module duplicate() {
        Module duplicate = new Module(null, getTitle(), getDescription(), threeshold);
        duplicate.setStartDate(getStartDate());
        duplicate.setEndDate(getEndDate());
        duplicate.setActivities(
            activities == null ? new ArrayList<>() : new ArrayList<>(activities)
        );
        return duplicate;
    }

}
