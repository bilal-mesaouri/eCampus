package com.example.eCampus.LearningObject;

import java.time.LocalDate;

public record LearningObjectLifecycleEvent(Long learningObjectId, Type type, LocalDate date) {

    public enum Type {
        STARTING,
        ENDING,
        RESCHEDULED
    }
}
