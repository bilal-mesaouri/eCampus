package com.example.eCampus.Program;

import com.example.eCampus.Module.Module;
import com.example.eCampus.Module.ModuleRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class ProgramService {

    private final ProgramRepository programRepository;
    private final ModuleRepository moduleRepository;

    public ProgramService(ProgramRepository programRepository, ModuleRepository moduleRepository) {
        this.programRepository = programRepository;
        this.moduleRepository = moduleRepository;
    }

    public List<Program> getAllPrograms() {
        return programRepository.findAll();
    }

    public Program getProgramById(Long id) {
        return programRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Program not found"));
    }

    public Program createProgram(Program program) {
        program.setId(null);
        program.setModules(resolveModules(program.getModules()));
        return programRepository.save(program);
    }

    public Program updateProgram(Long id, Program programDetails) {
        Program program = getProgramById(id);
        program.setTitle(programDetails.getTitle());
        program.setDescription(programDetails.getDescription());
        program.setStartDate(programDetails.getStartDate());
        program.setEndDate(programDetails.getEndDate());
        program.setModules(resolveModules(programDetails.getModules()));
        return programRepository.save(program);
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