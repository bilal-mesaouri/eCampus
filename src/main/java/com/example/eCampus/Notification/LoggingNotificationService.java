package com.example.eCampus.Notification;

import com.example.eCampus.Learner.Learner;
import com.example.eCampus.LearningObject.LearningObjectLifecycleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Placeholder until the real notification service (email, push, ...) is developed.
 * Replace this bean with the real implementation.
 */
@Service
public class LoggingNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(LoggingNotificationService.class);

    @Override
    public void notify(Learner learner, LearningObjectLifecycleEvent event) {
        log.info("Notify learner {} : learning object {} {} on {}",
            learner.getId(), event.learningObjectId(), event.type(), event.date());
    }
}
