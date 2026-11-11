package com.example.eCampus.Program;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramRepository extends JpaRepository<Program, Long> {
    List<Program> findByModules_Id(Long moduleId);
}