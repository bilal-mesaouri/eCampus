package com.example.eCampus.Notification;

import com.example.eCampus.Enrollement.EnrollementRepository;
import com.example.eCampus.Learner.Learner;
import com.example.eCampus.LearningObject.LearningObjectLifecycleEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Observer side: reacts to learning object lifecycle events and notifies
 * the learners subscribed through their enrollments.
 */
@Component
public class EnrolledLearnersNotifier {

    private final EnrollementRepository enrollementRepository;
    private final NotificationService notificationService;

    public EnrolledLearnersNotifier(
        EnrollementRepository enrollementRepository,
        NotificationService notificationService
    ) {
        this.enrollementRepository = enrollementRepository;
        this.notificationService = notificationService;
    }

    // Runs after commit when published inside a transaction, immediately otherwise (scheduler).
    @Async
    @TransactionalEventListener(fallbackExecution = true)
    public void onLifecycleEvent(LearningObjectLifecycleEvent event) {
        for (Learner learner : enrollementRepository.findSubscribedLearners(event.learningObjectId())) {
            notificationService.notify(learner, event);
        }
    }
}
