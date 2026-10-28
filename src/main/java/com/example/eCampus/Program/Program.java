package com.example.eCampus.Program;

import com.example.eCampus.LearningObject.LearningObject;
import com.example.eCampus.Module.Module;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity
public class Program extends LearningObject {

    @ManyToMany
    @JoinTable(
        name = "program_module",
        joinColumns = @JoinColumn(name = "program_id"),
        inverseJoinColumns = @JoinColumn(name = "module_id")
    )
    private List<Module> modules = new ArrayList<>();

    public Program() {
    }

    public List<Module> getModules() {
        return modules;
    }

    public void setModules(List<Module> modules) {
        this.modules = modules;
    }

    @Override
    public Program duplicate() {
        Program duplicate = new Program();
        duplicate.setTitle(getTitle());
        duplicate.setDescription(getDescription());
        duplicate.setStartDate(getStartDate());
        duplicate.setEndDate(getEndDate());
        duplicate.setModules(
            modules == null ? new ArrayList<>() : new ArrayList<>(modules)
        );
        return duplicate;
    }
}