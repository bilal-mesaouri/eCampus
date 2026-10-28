package com.example.eCampus.LearningObject;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Subject side of the observer pattern: since start/end are time-driven,
 * this job detects upcoming learning objects and publishes lifecycle events.
 */
@Component
public class LearningObjectLifecycleScheduler {

    private final LearningObjectRepository learningObjectRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final int leadDays;

    public LearningObjectLifecycleScheduler(
        LearningObjectRepository learningObjectRepository,
        ApplicationEventPublisher eventPublisher,
        @Value("${ecampus.notifications.lead-days:1}") int leadDays
    ) {
        this.learningObjectRepository = learningObjectRepository;
        this.eventPublisher = eventPublisher;
        this.leadDays = leadDays;
    }

    @Scheduled(cron = "${ecampus.notifications.cron:0 0 7 * * *}")
    public void publishUpcomingLifecycleEvents() {
        LocalDate targetDate = LocalDate.now().plusDays(leadDays);

        for (LearningObject learningObject : learningObjectRepository.findByStartDate(targetDate)) {
            eventPublisher.publishEvent(new LearningObjectLifecycleEvent(
                learningObject.getId(), LearningObjectLifecycleEvent.Type.STARTING, targetDate));
        }
        for (LearningObject learningObject : learningObjectRepository.findByEndDate(targetDate)) {
            eventPublisher.publishEvent(new LearningObjectLifecycleEvent(
                learningObject.getId(), LearningObjectLifecycleEvent.Type.ENDING, targetDate));
        }
    }
}
