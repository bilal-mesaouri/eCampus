package com.example.eCampus.bootstrap;

import java.util.ArrayList;
import java.util.List;
import com.example.eCampus.Activity.Activity;
import com.example.eCampus.Activity.ActivityRepository;
import com.example.eCampus.Module.Module;
import com.example.eCampus.Module.ModuleRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component 
@Order(2)
public class ModuleDataSeeder implements StartupDataSeeder {

    private static final List<String> SHARED_ACTIVITY_TITLES = List.of(
            "Getting Started with eCampus",
            "Campus Tour",
            "Academic Integrity Quiz");

    private final ModuleRepository moduleRepository;
    private final ActivityRepository activityRepository;

    public ModuleDataSeeder(ModuleRepository moduleRepository, ActivityRepository activityRepository) {
        this.moduleRepository = moduleRepository;
        this.activityRepository = activityRepository;
    }

    @Override
    @Transactional
    public void seed() {
        List<Activity> activities = activityRepository.findAll().stream()
            .filter(activity -> SHARED_ACTIVITY_TITLES.contains(activity.getTitle()))
            .toList();
        List<Module> seededModules = new ArrayList<>();

        seededModules.add(findOrCreateModule("Introduction to eCampus", "Learn the basics of using eCampus.", 1));
        seededModules.add(findOrCreateModule("Advanced eCampus Features", "Explore advanced features of eCampus.", 2));
        seededModules.add(findOrCreateModule("eCampus Best Practices", "Discover best practices for using eCampus effectively.", 3));

        for (Module module : seededModules) {
            for (Activity activity : activities) {
                boolean alreadyLinked = module.getActivities().stream()
                        .anyMatch(existing -> existing.getId().equals(activity.getId()));
                if (!alreadyLinked) {
                    module.addActivity(activity);
                }
            }
            moduleRepository.save(module);
        }
    }

    private Module findOrCreateModule(String title, String description, int threshold) {
        return moduleRepository.findAll().stream()
                .filter(module -> title.equals(module.getTitle()))
                .findFirst()
                .orElseGet(() -> moduleRepository.save(new Module(null, title, description, threshold)));
    }
    
}
