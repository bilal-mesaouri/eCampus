package com.example.eCampus.Module;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ModuleService {

    private final ModuleRepository moduleRepository;
    
    public ModuleService(ModuleRepository moduleRepository) {
        this.moduleRepository = moduleRepository;
    }
    
    // Create
    public Module createModule(Module module) {
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
    
    // Update
    public Module updateModule(Long id, Module moduleDetails) {
        Optional<Module> module = moduleRepository.findById(id);
        if (module.isPresent()) {
            Module existingModule = module.get();
            existingModule.setTitle(moduleDetails.getTitle());
            existingModule.setDescription(moduleDetails.getDescription());
            existingModule.setThreeshold(moduleDetails.getThreeshold());
            existingModule.setActivities(moduleDetails.getActivities());
            return moduleRepository.save(existingModule);
        }
        return null;
    }
    
    // Delete
    public void deleteModule(Long id) {
        moduleRepository.deleteById(id);
    }
}
