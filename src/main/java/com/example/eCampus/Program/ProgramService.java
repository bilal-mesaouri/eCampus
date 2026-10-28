package com.example.eCampus.Program;

import com.example.eCampus.LearningObject.LearningObjectLifecycleEvent;
import com.example.eCampus.Module.Module;
import com.example.eCampus.Module.ModuleRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ProgramService {

    private final ProgramRepository programRepository;
    private final ModuleRepository moduleRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ProgramService(
        ProgramRepository programRepository,
        ModuleRepository moduleRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this.programRepository = programRepository;
        this.moduleRepository = moduleRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<Program> getAllPrograms() {
        return programRepository.findAll();
    }

    public Program getProgramById(Long id) {
        return programRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Program not found"));
    }

    public Program addModuleToProgram(Long programId, Long moduleId) {
        Program program = getProgramById(programId);
        Module module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));
        validateModuleDates(program, List.of(module));
        moduleRepository.save(module);
        if (program.getModules() == null) {
            program.setModules(new ArrayList<>());
        }
        if (program.getModules().stream().noneMatch(existing -> moduleId.equals(existing.getId()))) {
            program.getModules().add(module);
        }
        return programRepository.save(program);
    }

    public Program createProgram(Program program) {
        program.setId(null);
        List<Module> modules = resolveModules(program.getModules());
        validateModuleDates(program, modules);
        moduleRepository.saveAll(modules);
        program.setModules(modules);
        return programRepository.save(program);
    }

    public Program updateProgram(Long id, Program programDetails) {
        Program program = getProgramById(id);
        LocalDate previousStartDate = program.getStartDate();
        LocalDate previousEndDate = program.getEndDate();
        program.setTitle(programDetails.getTitle());
        program.setDescription(programDetails.getDescription());
        program.setStartDate(programDetails.getStartDate());
        program.setEndDate(programDetails.getEndDate());
        List<Module> modules = resolveModules(programDetails.getModules());
        validateModuleDates(program, modules);
        moduleRepository.saveAll(modules);
        program.setModules(modules);
        Program savedProgram = programRepository.save(program);

        if (!Objects.equals(previousStartDate, savedProgram.getStartDate())
            || !Objects.equals(previousEndDate, savedProgram.getEndDate())) {
            eventPublisher.publishEvent(new LearningObjectLifecycleEvent(
                savedProgram.getId(),
                LearningObjectLifecycleEvent.Type.RESCHEDULED,
                savedProgram.getStartDate()
            ));
        }
        return savedProgram;
    }

    private void validateModuleDates(Program program, List<Module> modules) {
        if (modules.isEmpty()) {
            return;
        }
        if (program.getStartDate() != null && program.getEndDate() != null
            && program.getStartDate().isAfter(program.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Program start date must not be after its end date");
        }

        for (Module module : modules) {
            if (module.getStartDate() == null) {
                module.setStartDate(program.getStartDate());
            }
            if (module.getEndDate() == null) {
                module.setEndDate(program.getEndDate());
            }
            if (module.getStartDate() != null && module.getEndDate() != null
                && module.getStartDate().isAfter(module.getEndDate())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Module start date must not be after its end date");
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

    public void deleteProgram(Long id) {
        programRepository.delete(getProgramById(id));
    }

    private List<Module> resolveModules(List<Module> modules) {
        List<Module> resolvedModules = new ArrayList<>();
        if (modules == null) {
            return resolvedModules;
        }
        for (Module module : modules) {
            if (module == null || module.getId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each module must have an ID");
            }
            Module existingModule = moduleRepository.findById(module.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Module not found"));
            if (!resolvedModules.contains(existingModule)) {
                resolvedModules.add(existingModule);
            }
        }
        return resolvedModules;
    }
}