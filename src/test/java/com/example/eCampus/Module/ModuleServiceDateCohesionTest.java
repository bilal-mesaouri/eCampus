package com.example.eCampus.Module;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.eCampus.Activity.Activity;
import com.example.eCampus.Activity.ActivityRepository;
import com.example.eCampus.Activity.ActivityTypes;
import com.example.eCampus.Program.ProgramRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class ModuleServiceDateCohesionTest {

    private static final Long MODULE_ID = 1L;
    private static final Long ACTIVITY_ID = 2L;
    private static final LocalDate MODULE_START = LocalDate.of(2025, 1, 1);
    private static final LocalDate MODULE_END = LocalDate.of(2025, 12, 31);

    private ModuleRepository moduleRepository;
    private ActivityRepository activityRepository;
    private ModuleService moduleService;

    @BeforeEach
    void setUp() {
        moduleRepository = mock(ModuleRepository.class);
        activityRepository = mock(ActivityRepository.class);
        moduleService = new ModuleService(
            moduleRepository,
            mock(ProgramRepository.class),
            activityRepository
        );
    }

    @Test
    void addingActivityInheritsMissingDatesAndPersistsThem() {
        Module module = moduleWithDates(MODULE_START, MODULE_END);
        Activity activity = activityWithDates(null, null);
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));
        when(activityRepository.save(activity)).thenReturn(activity);
        when(moduleRepository.save(module)).thenReturn(module);

        Module result = moduleService.addActivityToModule(MODULE_ID, ACTIVITY_ID);

        assertSame(module, result);
        assertEquals(MODULE_START, activity.getStartDate());
        assertEquals(MODULE_END, activity.getEndDate());
        assertEquals(List.of(activity), module.getActivities());
        verify(activityRepository).save(activity);
        verify(moduleRepository).save(module);
    }

    @Test
    void addingActivityPreservesExplicitDatesAtModuleBoundaries() {
        Module module = moduleWithDates(MODULE_START, MODULE_END);
        Activity activity = activityWithDates(MODULE_START, MODULE_END);
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));
        when(activityRepository.save(activity)).thenReturn(activity);
        when(moduleRepository.save(module)).thenReturn(module);

        moduleService.addActivityToModule(MODULE_ID, ACTIVITY_ID);

        assertEquals(MODULE_START, activity.getStartDate());
        assertEquals(MODULE_END, activity.getEndDate());
    }

    @Test
    void addingActivityInheritsOnlyMissingDateAndPreservesExplicitDate() {
        Module module = moduleWithDates(MODULE_START, MODULE_END);
        LocalDate explicitStart = MODULE_START.plusMonths(1);
        Activity activity = activityWithDates(explicitStart, null);
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));
        when(activityRepository.save(activity)).thenReturn(activity);
        when(moduleRepository.save(module)).thenReturn(module);

        moduleService.addActivityToModule(MODULE_ID, ACTIVITY_ID);

        assertEquals(explicitStart, activity.getStartDate());
        assertEquals(MODULE_END, activity.getEndDate());
    }

    @Test
    void addingActivityAllowsMissingModuleDates() {
        Module module = moduleWithDates(null, null);
        Activity activity = activityWithDates(null, null);
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));
        when(activityRepository.save(activity)).thenReturn(activity);
        when(moduleRepository.save(module)).thenReturn(module);

        moduleService.addActivityToModule(MODULE_ID, ACTIVITY_ID);

        assertNull(activity.getStartDate());
        assertNull(activity.getEndDate());
    }

    @Test
    void addingActivityInheritsOnlyAvailableModuleBound() {
        Module module = moduleWithDates(MODULE_START, null);
        Activity activity = activityWithDates(null, null);
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));
        when(activityRepository.save(activity)).thenReturn(activity);
        when(moduleRepository.save(module)).thenReturn(module);

        moduleService.addActivityToModule(MODULE_ID, ACTIVITY_ID);

        assertEquals(MODULE_START, activity.getStartDate());
        assertNull(activity.getEndDate());
    }

    @Test
    void addingActivityRejectsDatesOutsideModuleRange() {
        Module module = moduleWithDates(MODULE_START, MODULE_END);
        Activity activity = activityWithDates(MODULE_START.minusDays(1), MODULE_END);
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> moduleService.addActivityToModule(MODULE_ID, ACTIVITY_ID)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void addingActivityRejectsEndDateAfterModuleRange() {
        Module module = moduleWithDates(MODULE_START, MODULE_END);
        Activity activity = activityWithDates(MODULE_START, MODULE_END.plusDays(1));
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> moduleService.addActivityToModule(MODULE_ID, ACTIVITY_ID)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void addingActivityRejectsReversedActivityDates() {
        Module module = moduleWithDates(MODULE_START, MODULE_END);
        Activity activity = activityWithDates(MODULE_END, MODULE_START);
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> moduleService.addActivityToModule(MODULE_ID, ACTIVITY_ID)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void addingActivityRejectsReversedModuleDates() {
        Module module = moduleWithDates(MODULE_END, MODULE_START);
        Activity activity = activityWithDates(null, null);
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> moduleService.addActivityToModule(MODULE_ID, ACTIVITY_ID)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void addingSameActivityTwiceDoesNotDuplicateAssociation() {
        Module module = moduleWithDates(MODULE_START, MODULE_END);
        Activity activity = activityWithDates(null, null);
        module.setActivities(List.of(activity));
        when(moduleRepository.findById(MODULE_ID)).thenReturn(Optional.of(module));
        when(activityRepository.findById(ACTIVITY_ID)).thenReturn(Optional.of(activity));
        when(activityRepository.save(activity)).thenReturn(activity);
        when(moduleRepository.save(module)).thenReturn(module);

        moduleService.addActivityToModule(MODULE_ID, ACTIVITY_ID);

        assertEquals(1, module.getActivities().size());
    }

    private Module moduleWithDates(LocalDate startDate, LocalDate endDate) {
        Module module = new Module();
        module.setId(MODULE_ID);
        module.setStartDate(startDate);
        module.setEndDate(endDate);
        return module;
    }

    private Activity activityWithDates(LocalDate startDate, LocalDate endDate) {
        Activity activity = new Activity(ACTIVITY_ID, "Activity", "Description", ActivityTypes.QUIZ, 10);
        activity.setStartDate(startDate);
        activity.setEndDate(endDate);
        return activity;
    }
}
