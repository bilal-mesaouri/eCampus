package com.example.eCampus.LearningObject;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningObjectRepository extends JpaRepository<LearningObject, Long> {

    List<LearningObject> findByStartDate(LocalDate startDate);

    List<LearningObject> findByEndDate(LocalDate endDate);
}
