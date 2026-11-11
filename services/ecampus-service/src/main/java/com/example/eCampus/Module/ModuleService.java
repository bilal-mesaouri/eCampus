package com.example.eCampus.Module;

import com.example.eCampus.Activity.Activity;
import com.example.eCampus.Activity.ActivityRepository;
import com.example.eCampus.Program.Program;
import com.example.eCampus.Program.ProgramRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ModuleService {

    private final ModuleRepository moduleRepository;
    private final ProgramRepository programRepository;
    private final ActivityRepository activityRepository;
    
    public ModuleService(
        ModuleRepository moduleRepository,
        ProgramRepository programRepository,
        ActivityRepository activityRepository
    ) {
        this.moduleRepository = moduleRepository;
        this.programRepository = programRepository;
        this.activityRepository = activityRepository;
    }
    
    // Create
    public Module createModule(Module module) {
        validateDateRange(module);
        return moduleRepository.save(module);
    }
    
    // Read - Get all modules
    public List<Module> getAllModules() {
        return moduleRepository.findAll();
    }
    
    // Read - Get module by ID
    public Optional<Module> getModuleById(Long id) {
        return moduleRepository.findById(id);
    }

    public Module addActivityToModule(Long moduleId, Long activityId) {
        Module module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Activity not found"));
        inheritActivityDates(module, activity);
        validateActivityDates(module, List.of(activity));
        activityRepository.save(activity);

        if (module.getActivities() == null) {
            module.setActivities(new ArrayList<>());
        }
        if (module.getActivities().stream().noneMatch(existing -> activityId.equals(existing.getId()))) {
            module.addActivity(activity);
        }
        return moduleRepository.save(module);
    }
    
    // Update
    public Module updateModule(Long id, Module moduleDetails) {
        Optional<Module> module = moduleRepository.findById(id);
        if (module.isPresent()) {
            Module existingModule = module.get();
            existingModule.setTitle(moduleDetails.getTitle());
            existingModule.setDescription(moduleDetails.getDescription());
            existingModule.setThreeshold(moduleDetails.getThreeshold());
            existingModule.setActivities(moduleDetails.getActivities());
            if (moduleDetails.getStartDate() != null || moduleDetails.getEndDate() != null) {
                validateDateRange(moduleDetails);
                validateProgramDateRanges(id, moduleDetails);
                existingModule.setStartDate(moduleDetails.getStartDate());
                existingModule.setEndDate(moduleDetails.getEndDate());
            }
            return moduleRepository.save(existingModule);
        }
        return null;
    }

    private void validateDateRange(Module module) {
        if (module.getStartDate() == null && module.getEndDate() == null) {
            return;
        }
        if (module.getStartDate() == null || module.getEndDate() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Module start and end dates are both required");
        }
        if (module.getStartDate().isAfter(module.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Module start date must not be after its end date");
        }
    }

    private void validateActivityDates(Module module, List<Activity> activities) {
        if (activities.isEmpty()) {
            return;
        }
        if (module.getStartDate() != null && module.getEndDate() != null
            && module.getStartDate().isAfter(module.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Module start date must not be after its end date");
        }

        for (Activity activity : activities) {
            if (activity.getStartDate() != null && activity.getEndDate() != null
                && activity.getStartDate().isAfter(activity.getEndDate())) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity start date must not be after its end date"
                );
            }
            if ((module.getStartDate() != null && activity.getStartDate() != null
                    && activity.getStartDate().isBefore(module.getStartDate()))
                || (module.getEndDate() != null && activity.getEndDate() != null
                    && activity.getEndDate().isAfter(module.getEndDate()))) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Activity dates must be within the module dates"
                );
            }
        }
    }

    private void inheritActivityDates(Module module, Activity activity) {
        if (activity.getStartDate() == null && module.getStartDate() != null) {
            activity.setStartDate(module.getStartDate());
        }
        if (activity.getEndDate() == null && module.getEndDate() != null) {
            activity.setEndDate(module.getEndDate());
        }
    }

    private void validateProgramDateRanges(Long moduleId, Module module) {
        for (Program program : programRepository.findByModules_Id(moduleId)) {
            if (program.getStartDate() != null && program.getEndDate() != null
                && program.getStartDate().isAfter(program.getEndDate())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Program start date must not be after its end date");
            }
            if ((program.getStartDate() != null && module.getStartDate() != null
                    && module.getStartDate().isBefore(program.getStartDate()))
                || (program.getEndDate() != null && module.getEndDate() != null
                    && module.getEndDate().isAfter(program.getEndDate()))) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Module dates must be within the program dates"
                );
            }
        }
    }
    
    // Delete
    public void deleteModule(Long id) {
        moduleRepository.deleteById(id);
    }
}
