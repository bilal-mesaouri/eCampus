package com.example.eCampus.Activity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
	boolean existsByTitle(String title);
}
