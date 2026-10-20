package com.example.eCampus.Program;

import com.example.eCampus.LearningObject.LearningObject;
import com.example.eCampus.Module.Module;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

@Entity
public class Program extends LearningObject {

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToMany
    @JoinTable(
        name = "program_module",
        joinColumns = @JoinColumn(name = "program_id"),
        inverseJoinColumns = @JoinColumn(name = "module_id")
    )
    private List<Module> modules = new ArrayList<>();

    public Program() {
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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
        duplicate.setStartDate(startDate);
        duplicate.setEndDate(endDate);
        duplicate.setModules(
            modules == null ? new ArrayList<>() : new ArrayList<>(modules)
        );
        return duplicate;
    }
}