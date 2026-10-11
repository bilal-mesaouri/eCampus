package com.example.eCampus.Program;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProgramController {
    @GetMapping("/programs")
    public String getPrograms() {
        return "List of programs";
    }
}
