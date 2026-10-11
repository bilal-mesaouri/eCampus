package com.example.eCampus.Module;

import com.example.eCampus.Activity.Activity;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity 
public class Module {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String description;
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
        this.id = id;
        this.title = title;
        this.description = description;
        this.threeshold = threeshold;
        this.id = id;
        this.title = title;
        this.threeshold = threeshold;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getThreeshold() {
        return threeshold;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
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


}
