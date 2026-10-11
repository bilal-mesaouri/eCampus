package com.example.eCampus.Activity;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ActivityService {

	private final ActivityRepository activityRepository;

	public ActivityService(ActivityRepository activityRepository) {
		this.activityRepository = activityRepository;
	}

	public List<Activity> getAllActivities() {
		return activityRepository.findAll();
	}

	public Activity getActivityById(Long id) {
		return activityRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
						"Activity not found with id " + id));
	}

	public Activity createActivity(Activity activity) {
		activity.setId(null);
		return activityRepository.save(activity);
	}

	public Activity updateActivity(Long id, Activity updatedActivity) {
		Activity activity = getActivityById(id);
		activity.setTitle(updatedActivity.getTitle());
		activity.setDescription(updatedActivity.getDescription());
		activity.setType(updatedActivity.getType());
		activity.setPoints(updatedActivity.getPoints());
		activity.setDuration(updatedActivity.getDuration());
		return activityRepository.save(activity);
	}

	public void deleteActivity(Long id) {
		Activity activity = getActivityById(id);
		activityRepository.delete(activity);
	}
}
