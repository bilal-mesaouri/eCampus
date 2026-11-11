package com.example.eCampus.bootstrap;

import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.example.eCampus.Activity.Activity;
import com.example.eCampus.Activity.ActivityRepository;
import com.example.eCampus.Activity.ActivityTypes;

@Component
@Order(1)
public class ActivityDataSeeder implements StartupDataSeeder {

    private final ActivityRepository activityRepository;

    public ActivityDataSeeder(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    @Override
    public void seed() {
        List<Activity> sampleActivities = List.of(
                new Activity(null, "Getting Started with eCampus", "Read the eCampus orientation guide.",
                    ActivityTypes.READING, 1, 5),
                new Activity(null, "Campus Tour", "Watch a video tour of the campus.", ActivityTypes.VIDEO, 2, 8),
                new Activity(null, "Academic Integrity Quiz", "Check your understanding of academic integrity.",
                    ActivityTypes.QUIZ, 3, 10));

        List<Activity> missingActivities = sampleActivities.stream()
                .filter(activity -> !activityRepository.existsByTitle(activity.getTitle()))
                .toList();

        activityRepository.saveAll(missingActivities);
    }
}