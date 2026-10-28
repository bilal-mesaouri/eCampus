package com.example.eCampus.Program;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.eCampus.Module.Module;
import com.example.eCampus.Module.ModuleRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ProgramServiceDateCohesionTest {

    private static final Long PROGRAM_ID = 1L;
    private static final Long MODULE_ID = 2L;
    private static final LocalDate PROGRAM_START = LocalDate.of(2025, 1, 1);
    private static final LocalDate PROGRAM_END = LocalDate.of(2025, 12, 31);

    private ProgramRepository programRepository;
    private ModuleRepository moduleRepository;
    private ProgramService programService;

    @BeforeEach
    void setUp() {
        programRepository = mock(ProgramRepository.class);
        moduleRepository = mock(ModuleRepository.class);
        programService = new ProgramService(
            programRepository,
            moduleRepository,
            mock(ApplicationEventPublisher.class)
        );
    }

    @Test
    void addingModuleInheritsMissingDatesAndPersistsThem() {
        Program program = programWithDates(PROGRAM_START, PROGRAM_END);
        Module module = moduleWithDates(null, null);
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(program));
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(moduleRepository.save(module)).thenReturn(module);
        when(programRepository.save(program)).thenReturn(program);

        Program result = programService.addModuleToProgram(PROGRAM_ID, MODULE_ID);

        assertSame(program, result);
        assertEquals(PROGRAM_START, module.getStartDate());
        assertEquals(PROGRAM_END, module.getEndDate());
        assertEquals(List.of(module), program.getModules());
        verify(moduleRepository).save(module);
        verify(programRepository).save(program);
    }

    @Test
    void addingModulePreservesExplicitDatesAtProgramBoundaries() {
        Program program = programWithDates(PROGRAM_START, PROGRAM_END);
        Module module = moduleWithDates(PROGRAM_START, PROGRAM_END);
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(program));
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(moduleRepository.save(module)).thenReturn(module);
        when(programRepository.save(program)).thenReturn(program);

        programService.addModuleToProgram(PROGRAM_ID, MODULE_ID);

        assertEquals(PROGRAM_START, module.getStartDate());
        assertEquals(PROGRAM_END, module.getEndDate());
    }

    @Test
    void addingModuleInheritsOnlyMissingDateAndPreservesExplicitDate() {
        Program program = programWithDates(PROGRAM_START, PROGRAM_END);
        LocalDate explicitStart = PROGRAM_START.plusMonths(1);
        Module module = moduleWithDates(explicitStart, null);
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(program));
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(moduleRepository.save(module)).thenReturn(module);
        when(programRepository.save(program)).thenReturn(program);

        programService.addModuleToProgram(PROGRAM_ID, MODULE_ID);

        assertEquals(explicitStart, module.getStartDate());
        assertEquals(PROGRAM_END, module.getEndDate());
    }

    @Test
    void addingModuleAllowsMissingProgramDatesAndInheritsAvailableBoundOnly() {
        Program program = programWithDates(PROGRAM_START, null);
        Module module = moduleWithDates(null, null);
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(program));
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(moduleRepository.save(module)).thenReturn(module);
        when(programRepository.save(program)).thenReturn(program);

        programService.addModuleToProgram(PROGRAM_ID, MODULE_ID);

        assertEquals(PROGRAM_START, module.getStartDate());
        assertNull(module.getEndDate());
    }

    @Test
    void addingModuleRejectsDatesOutsideProgramRange() {
        Program program = programWithDates(PROGRAM_START, PROGRAM_END);
        Module module = moduleWithDates(PROGRAM_START.minusDays(1), PROGRAM_END);
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(program));
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> programService.addModuleToProgram(PROGRAM_ID, MODULE_ID)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void addingModuleRejectsEndDateAfterProgramRange() {
        Program program = programWithDates(PROGRAM_START, PROGRAM_END);
        Module module = moduleWithDates(PROGRAM_START, PROGRAM_END.plusDays(1));
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(program));
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> programService.addModuleToProgram(PROGRAM_ID, MODULE_ID)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void addingModuleRejectsReversedModuleDates() {
        Program program = programWithDates(PROGRAM_START, PROGRAM_END);
        Module module = moduleWithDates(PROGRAM_END, PROGRAM_START);
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(program));
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> programService.addModuleToProgram(PROGRAM_ID, MODULE_ID)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void addingModuleRejectsReversedProgramDates() {
        Program program = programWithDates(PROGRAM_END, PROGRAM_START);
        Module module = moduleWithDates(null, null);
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(program));
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> programService.addModuleToProgram(PROGRAM_ID, MODULE_ID)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void addingSameModuleTwiceDoesNotDuplicateAssociation() {
        Program program = programWithDates(PROGRAM_START, PROGRAM_END);
        Module module = moduleWithDates(null, null);
        program.setModules(List.of(module));
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(program));
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(moduleRepository.save(module)).thenReturn(module);
        when(programRepository.save(program)).thenReturn(program);

        programService.addModuleToProgram(PROGRAM_ID, MODULE_ID);

        assertEquals(1, program.getModules().size());
    }

    @Test
    void creatingProgramInheritsMissingDatesForResolvedModules() {
        Program program = programWithDates(PROGRAM_START, PROGRAM_END);
        Module module = moduleWithDates(null, null);
        module.setId(MODULE_ID);
        program.setModules(List.of(module));
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(moduleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(programRepository.save(any(Program.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Program result = programService.createProgram(program);

        assertSame(program, result);
        assertEquals(PROGRAM_START, module.getStartDate());
        assertEquals(PROGRAM_END, module.getEndDate());
    }

    @Test
    void updatingProgramInheritsMissingDatesForResolvedModules() {
        Program existingProgram = programWithDates(PROGRAM_START, PROGRAM_END);
        existingProgram.setId(PROGRAM_ID);
        Program programDetails = programWithDates(PROGRAM_START, PROGRAM_END);
        Module module = moduleWithDates(null, null);
        module.setId(MODULE_ID);
        programDetails.setModules(List.of(module));
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(existingProgram));
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(moduleRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(programRepository.save(existingProgram)).thenReturn(existingProgram);

        Program result = programService.updateProgram(PROGRAM_ID, programDetails);

        assertSame(existingProgram, result);
        assertEquals(PROGRAM_START, module.getStartDate());
        assertEquals(PROGRAM_END, module.getEndDate());
    }

    private Program programWithDates(LocalDate startDate, LocalDate endDate) {
        Program program = new Program();
        program.setStartDate(startDate);
        program.setEndDate(endDate);
        return program;
    }

    private Module moduleWithDates(LocalDate startDate, LocalDate endDate) {
        Module module = new Module();
        module.setId(MODULE_ID);
        module.setStartDate(startDate);
        module.setEndDate(endDate);
        return module;
    }
}
