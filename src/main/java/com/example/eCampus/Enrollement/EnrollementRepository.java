package com.example.eCampus.Enrollement;

import com.example.eCampus.Learner.Learner;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EnrollementRepository extends JpaRepository<Enrollement, Long> {

    /**
     * Subscribers of a learning object: learners enrolled in it directly, plus
     * learners enrolled in a program that contains it (when it is a module).
     */
    @Query("""
        select distinct l from Enrollement e join e.learner l
        where e.learningObject.id = :learningObjectId
           or e.learningObject.id in (
               select p.id from Program p join p.modules m where m.id = :learningObjectId
           )
        """)
    List<Learner> findSubscribedLearners(@Param("learningObjectId") Long learningObjectId);
}
