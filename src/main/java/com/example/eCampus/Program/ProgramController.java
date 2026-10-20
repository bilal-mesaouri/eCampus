package com.example.eCampus.Program;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/programs")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @GetMapping
    public List<Program> getPrograms() {
        return programService.getAllPrograms();
    }

    @GetMapping("/{id}")
    public Program getProgramById(@PathVariable Long id) {
        return programService.getProgramById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Program createProgram(@RequestBody Program program) {
        return programService.createProgram(program);
    }

    @PutMapping("/{id}")
    public Program updateProgram(@PathVariable Long id, @RequestBody Program program) {
        return programService.updateProgram(id, program);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProgram(@PathVariable Long id) {
        programService.deleteProgram(id);
    }
}
