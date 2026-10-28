package com.example.eCampus.Notification;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.example.eCampus.Enrollement.Enrollement;
import com.example.eCampus.Enrollement.EnrollementRepository;
import com.example.eCampus.Learner.Learner;
import com.example.eCampus.LearningObject.LearningObject;
import com.example.eCampus.LearningObject.LearningObjectLifecycleEvent;
import com.example.eCampus.LearningObject.LearningObjectLifecycleScheduler;
import com.example.eCampus.Module.Module;
import com.example.eCampus.Module.ModuleRepository;
import com.example.eCampus.Program.Program;
import com.example.eCampus.Program.ProgramRepository;
import com.example.eCampus.Program.ProgramService;
import com.example.eCampus.User.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * End-to-end test of the observer chain:
 *
 *   trigger (scheduler / ProgramService.updateProgram)
 *     -> LearningObjectLifecycleEvent published
 *       -> EnrolledLearnersNotifier (@Async, @TransactionalEventListener)
 *         -> EnrollementRepository.findSubscribedLearners(...)
 *           -> NotificationService.notify(learner, event)   <-- we assert HERE
 *
 * Everything is real (Spring context, H2 database, event bus, async executor)
 * except NotificationService, which is mocked: it is the boundary we observe.
 *
 * GOTCHAS (read before writing the tests):
 *
 * 1. Do NOT annotate this class (or the tests) with @Transactional.
 *    - @TransactionalEventListener only fires AFTER COMMIT; a test transaction
 *      is rolled back, so the RESCHEDULED listener would never run.
 *    - @Async runs the listener on another thread, which cannot see data that
 *      is not committed yet.
 *    => Let each repository.save(...) commit, and clean up in @AfterEach.
 *
 * 2. The listener is @Async: the notification happens AFTER the trigger
 *    returns. Never use a plain verify(...). Use:
 *      verify(notificationService, timeout(2_000)).notify(...)          // should happen
 *      verify(notificationService, after(1_000).never()).notify(...)    // should NOT happen
 *    (static imports from org.mockito.Mockito)
 *
 * 3. Entities don't override equals(): the Learner passed to notify() is a
 *    different instance than the one you saved. Match on the id:
 *      argThat(learner -> learner.getId().equals(alice.getId()))
 *    and for the event:
 *      argThat(event -> event.learningObjectId().equals(program.getId())
 *                    && event.type() == LearningObjectLifecycleEvent.Type.STARTING)
 *    (argThat comes from org.mockito.ArgumentMatchers)
 *
 * 4. Don't wait for the cron. The cron is disabled below ("-") and the test
 *    calls scheduler.publishUpcomingLifecycleEvents() directly.
 *
 * 5. The scheduler looks for dates == today + lead-days. With lead-days=1,
 *    use TOMORROW as the start/end date for "about to start/end" objects and
 *    any other date (e.g. today + 30) for objects that must NOT trigger.
 *
 * 6. Startup seeders insert modules without dates; they never match the
 *    scheduler query, so they don't interfere. Still, only delete the
 *    entities YOU created (seeded modules must survive for other tests).
 */
@SpringBootTest(properties = {
    "ecampus.notifications.lead-days=1",
    "ecampus.notifications.cron=-"
})
public class NotifierTest {

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);
    private static final LocalDate FAR_FUTURE = LocalDate.now().plusDays(30);

    // Replaces LoggingNotificationService in the context (Boot 4: @MockitoBean, not @MockBean).
    @MockitoBean
    private NotificationService notificationService;

    // Triggers (the "subject" side).
    @Autowired
    private LearningObjectLifecycleScheduler scheduler;

    @Autowired
    private ProgramService programService;

    // Repositories used to arrange and clean up data.
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private ModuleRepository moduleRepository;

    @Autowired
    private EnrollementRepository enrollementRepository;

    // Track what each test creates so @AfterEach only removes test data.
    private final List<Enrollement> createdEnrollements = new ArrayList<>();
    private final List<Program> createdPrograms = new ArrayList<>();
    private final List<Module> createdModules = new ArrayList<>();
    private final List<Learner> createdLearners = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        enrollementRepository.deleteAll(createdEnrollements);
        programRepository.deleteAll(createdPrograms);
        moduleRepository.deleteAll(createdModules);
        userRepository.deleteAll(createdLearners);

        createdEnrollements.clear();
        createdPrograms.clear();
        createdModules.clear();
        createdLearners.clear();
    }

    // ---------------------------------------------------------------------
    // Scenarios triggered by the scheduler (STARTING / ENDING)
    // ---------------------------------------------------------------------

    @Test
    void shouldNotifyEnrolledLearnerWhenProgramIsAboutToStart() {
        // GIVEN a program starting TOMORROW (end date FAR_FUTURE)
        //   and a learner enrolled in that program
        Program program = saveProgram("Java 101", TOMORROW, FAR_FUTURE);
        Learner learner = saveLearner("messi");
        enroll(learner, program);

        // WHEN the daily job runs
        scheduler.publishUpcomingLifecycleEvents();

        // THEN the learner receives a STARTING notification for the program
        verify(notificationService, timeout(1_000)).notify(
            argThat(notified -> notified.getId().equals(learner.getId())),
            argThat(event -> event.learningObjectId().equals(program.getId())
                && event.type() == LearningObjectLifecycleEvent.Type.STARTING)
        );
    }

    @Test
    void shouldNotifyEnrolledLearnerWhenProgramIsAboutToEnd() {
        // GIVEN a program that started in the past and ends TOMORROW
        //   and a learner enrolled in it
        Program program = saveProgram("Java 101", TOMORROW.minusDays(2), TOMORROW);
        Learner learner = saveLearner("messi");
        enroll(learner, program);
        // WHEN the daily job runs
        scheduler.publishUpcomingLifecycleEvents();

        // THEN the learner receives an ENDING notification (and NOT a STARTING one)
        verify(notificationService, timeout(1_000)).notify(
            argThat(notified -> notified.getId().equals(learner.getId())),
            argThat(event -> event.learningObjectId().equals(program.getId())
                && event.type() == LearningObjectLifecycleEvent.Type.ENDING)
        );

        verify(notificationService, after(1_000).never()).notify(
            argThat(notified -> notified.getId().equals(learner.getId())),
            argThat(event -> event.learningObjectId().equals(program.getId())
                && event.type() == LearningObjectLifecycleEvent.Type.STARTING)
        );
    }

    @Test
    void shouldNotifyProgramLearnersWhenOneOfItsModulesIsAboutToStart() {
        // Inherited subscription: enrolled in the Program => notified about its Modules.

        // GIVEN a module starting TOMORROW
        //   and a program (dates FAR_FUTURE) containing that module
        //   and a learner enrolled in the PROGRAM only (not in the module)
        Module module = saveModule("Streams", TOMORROW, FAR_FUTURE);
        Program program = saveProgram("Java 101", FAR_FUTURE, FAR_FUTURE.plusDays(60), module);
        Learner bob = saveLearner("bob");
        enroll(bob, program);

        // WHEN the daily job runs
        scheduler.publishUpcomingLifecycleEvents();

        // THEN bob receives a STARTING notification whose learningObjectId is the MODULE id
        verify(notificationService, timeout(1_000)).notify(
            argThat(notified -> notified.getId().equals(bob.getId())),
            argThat(event -> event.learningObjectId().equals(module.getId())
                && event.type() == LearningObjectLifecycleEvent.Type.STARTING)
        );
    }

    @Test
    void shouldNotNotifyLearnersWhoAreNotEnrolled() {
        // GIVEN a program starting TOMORROW
        //   and alice enrolled in it, carol NOT enrolled in anything
        Program program = saveProgram("Java 101", TOMORROW, FAR_FUTURE);
        Learner alice = saveLearner("alice");
        Learner carol = saveLearner("carol");
        enroll(alice, program);

        // WHEN the daily job runs
        scheduler.publishUpcomingLifecycleEvents();

        // THEN alice is notified, carol never is
        verify(notificationService, timeout(1_000)).notify(
            argThat(notified -> notified.getId().equals(alice.getId())),
            argThat(event -> event.learningObjectId().equals(program.getId())
                && event.type() == LearningObjectLifecycleEvent.Type.STARTING)
        );
        verify(notificationService, after(1_000).never()).notify(
            argThat(notified -> notified.getId().equals(carol.getId())),
            any()
        );
    }

    @Test
    void shouldNotNotifyAnyoneWhenNothingStartsOrEndsSoon() {
        // GIVEN a program with dates far away (FAR_FUTURE) and an enrolled learner
        Program program = saveProgram("Java 101", FAR_FUTURE, FAR_FUTURE.plusDays(60));
        Learner learner = saveLearner("alice");
        enroll(learner, program);

        // WHEN the daily job runs
        scheduler.publishUpcomingLifecycleEvents();

        // THEN the notification service is never called
        verify(notificationService, after(1_000).never()).notify(any(), any());
    }

    // ---------------------------------------------------------------------
    // Scenarios triggered by a program update (RESCHEDULED)
    // ---------------------------------------------------------------------

    @Test
    void shouldNotifyEnrolledLearnerWhenProgramIsRescheduled() {
        // GIVEN a saved program with dates (FAR_FUTURE, FAR_FUTURE + 60) and an enrolled learner
        Program program = saveProgram("Java 101", FAR_FUTURE, FAR_FUTURE.plusDays(60));
        Learner learner = saveLearner("alice");
        enroll(learner, program);

        // WHEN the program is updated through the SERVICE with a different start date
        //   (go through ProgramService, not the repository: the service publishes the event,
        //    and its @Transactional commit is what releases the @TransactionalEventListener)
        LocalDate newStartDate = FAR_FUTURE.plusDays(7);
        Program changes = new Program();
        changes.setTitle(program.getTitle());
        changes.setDescription(program.getDescription());
        changes.setStartDate(newStartDate);
        changes.setEndDate(program.getEndDate());
        programService.updateProgram(program.getId(), changes);

        // THEN the learner receives a RESCHEDULED notification
        //   whose date() equals the NEW start date
        verify(notificationService, timeout(1_000)).notify(
            argThat(notified -> notified.getId().equals(learner.getId())),
            argThat(event -> event.learningObjectId().equals(program.getId())
                && event.type() == LearningObjectLifecycleEvent.Type.RESCHEDULED
                && event.date().equals(newStartDate))
        );
    }

    @Test
    void shouldNotNotifyWhenProgramUpdateKeepsSameDates() {
        // GIVEN a saved program and an enrolled learner
        Program program = saveProgram("Java 101", FAR_FUTURE, FAR_FUTURE.plusDays(60));
        Learner learner = saveLearner("alice");
        enroll(learner, program);

        // WHEN only the title/description is updated (same start and end dates)
        Program changes = new Program();
        changes.setTitle("Java 101 - updated");
        changes.setDescription("updated description");
        changes.setStartDate(program.getStartDate());
        changes.setEndDate(program.getEndDate());
        programService.updateProgram(program.getId(), changes);

        // THEN no notification is sent
        verify(notificationService, after(1_000).never()).notify(any(), any());
    }

    // ---------------------------------------------------------------------
    // Arrange helpers: each one saves (=> commits) and registers for cleanup
    // ---------------------------------------------------------------------

    private Learner saveLearner(String login) {
        Learner learner = new Learner(null, login, "irrelevant-hash", "en");
        Learner savedLearner = (Learner) userRepository.save(learner);
        createdLearners.add(savedLearner);
        return savedLearner;
    }

    private Module saveModule(String title, LocalDate startDate, LocalDate endDate) {
        Module module = new Module(null, title, "desc", 0);
        module.setStartDate(startDate);
        module.setEndDate(endDate);
        Module savedModule = moduleRepository.save(module);
        createdModules.add(savedModule);
        return savedModule;
    }

    private Program saveProgram(String title, LocalDate startDate, LocalDate endDate, Module... modules) {
        Program program = new Program();
        program.setTitle(title);
        program.setDescription("desc");
        program.setStartDate(startDate);
        program.setEndDate(endDate);
        program.setModules(List.of(modules));
        Program savedProgram = programRepository.save(program);
        createdPrograms.add(savedProgram);
        return savedProgram;
    }

    private Enrollement enroll(Learner learner, LearningObject learningObject) {
        Enrollement enrollement = new Enrollement();
        enrollement.setLearner(learner);
        enrollement.setLearningObject(learningObject);
        Enrollement savedEnrollement = enrollementRepository.save(enrollement);
        createdEnrollements.add(savedEnrollement);
        return savedEnrollement;
    }
}
