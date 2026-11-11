package com.example.eCampus.Learner;

import com.example.eCampus.User.User;
import jakarta.persistence.Entity;

@Entity
public class Learner extends User {

    private String preferredLearningLanguage;

    public Learner() {
    }

    public Learner(Long id, String login, String hashPassword, String preferredLearningLanguage) {
        super(id, login, hashPassword);
        this.preferredLearningLanguage = preferredLearningLanguage;
    }

    public String getPreferredLearningLanguage() {
        return preferredLearningLanguage;
    }

    public void setPreferredLearningLanguage(String preferredLearningLanguage) {
        this.preferredLearningLanguage = preferredLearningLanguage;
    }

    public boolean hasPreferredLearningLanguage() {
        return preferredLearningLanguage != null && !preferredLearningLanguage.isBlank();
    }
}