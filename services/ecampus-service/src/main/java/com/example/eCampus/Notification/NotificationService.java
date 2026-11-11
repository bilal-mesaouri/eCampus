package com.example.eCampus.Notification;

import com.example.eCampus.Learner.Learner;
import com.example.eCampus.LearningObject.LearningObjectLifecycleEvent;

public interface NotificationService {

    void notify(Learner learner, LearningObjectLifecycleEvent event);
}
