package com.example.eCampus.Module;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController 
@RequestMapping ("/modules")
public class ModuleController {
    
    private ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }
    
    @GetMapping
    public Module[] getModules() {
        return moduleService.getAllModules().toArray(new Module[0]);
    }

    @GetMapping("/{id}")
    public Module getModuleById(@PathVariable Long id) {
        return moduleService.getModuleById(id).orElse(null);
    }

    @PostMapping
    public Module createModule(@RequestBody Module module) {
        return moduleService.createModule(module);
    }
}
