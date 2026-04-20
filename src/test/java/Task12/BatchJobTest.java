package Task12;

import org.junit.jupiter.api.*;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = Main.class)
@SpringBatchTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
// allows @BeforeAll / @AfterAll to be non-static
class BatchJobTest {

    // ── Spring-injected beans ────────────────────────────────────────────────

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;
    // provided by @SpringBatchTest
//    This helpfull to register
//    JobLauncherTestUtils (launch jobs/steps in tests)
//    JobRepositoryTestUtils (clean up batch metadata tables)
//    StepScopeTestExecutionListener (@StepScope beans work in tests)
//    JobScopeTestExecutionListener (@JobScope beans work in tests)

    @Autowired
    @Qualifier("jpaBatchJob")
    private Job jpaBatchJob;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Per-test state

    private static int testCounter = 0;
    // incremented in @BeforeEach to show per-test state


    //  @BeforeAll — one-time class-level setup

    /**
     * Runs ONCE before any test method in this class executes.
     *Wire the job into the test launcher (required when there are multiple Job beans in the context).
     *Seed the department rows that every subsequent test depends on.
     */
    @BeforeAll
    void setupSuite() {
        System.out.println("========================================================== @BeforeAll  — Suite-level setup starting... ==========================================================");

        // Point the shared launcher at the specific job under test.
        jobLauncherTestUtils.setJob(jpaBatchJob);
        jobLauncherTestUtils.setJobLauncher(jobLauncher);

        System.out.println("  JobLauncherTestUtils wired to 'jpaBatchJob'");
        System.out.println("  Suite setup complete.\n");
    }

    //  @BeforeEach — reset mutable state before every individual test

    /**
     * Runs before EACH @Test method.
     *
     * Responsibilities:
     *Increment and display a per-test counter (illustrates that this hook fires once per test).
     *Seed the Department rows that tests rely on (idempotent —
     *uses findByDeptName so duplicates are never inserted).
     *Clear all Customer rows so each test owns a clean slate.
     */
    @BeforeEach
    void resetState() {
        testCounter++;
        System.out.printf("  @BeforeEach  — preparing test #%d%n", testCounter);

        if (departmentRepository.findByDeptName("Sales") == null) {
            departmentRepository.save(new Department(null, "Sales"));
        }
        if (departmentRepository.findByDeptName("Technology") == null) {
            departmentRepository.save(new Department(null, "Technology"));
        }

        customerRepository.deleteAll();
        System.out.println("Customer table cleared. Department rows verified.");
    }

    //  @AfterEach — per-test teardown

    /**
     * Runs after EACH @Test method, regardless of pass/fail.
     *
     * Responsibilities:
     * Log which test just finished and whether it passed.
     * Remove any Customer rows the test created so subsequent
     */
    @AfterEach
    void cleanupAfterTest(TestInfo testInfo) {
        System.out.printf("%n  @AfterEach  — '%s' finished.%n",
                testInfo.getDisplayName());

        customerRepository.deleteAll();
        System.out.println(" -----------------------------------------------------------Post-test customer rows removed. -----------------------------------------------------------");
    }

    //  @AfterAll — one-time class-level teardown

    /**
     * Runs ONCE after ALL test methods in this class have completed.
     *
     * Responsibilities:
     * Drop the processing_status column added by the Tasklet step so
     * other test classes that reset the schema don't see it unexpectedly.
     * Print a suite summary showing how many tests ran.
     */
    @AfterAll
    void teardownSuite() {
        System.out.println("║  @AfterAll  — Suite-level teardown starting...   ║");

        jdbcTemplate.execute(
                "ALTER TABLE customer DROP COLUMN IF EXISTS processing_status");
        System.out.println(" 'processing_status' column removed from customer table.");


        System.out.printf("  Total test methods executed in this suite: %d%n", testCounter);
        System.out.println("  Suite teardown complete.");
    }


    //  TEST 1 — Full Job execution: COMPLETED status

    /**
     * Launches the entire batch job (schemaUpdateStep → jpaStep02 → csvImportStep)
     * and asserts that Spring Batch reports COMPLETED.
     *
     */
    @Test
    @DisplayName("Full job execution should complete with COMPLETED status")
    void testFullJobCompletesSuccessfully() throws Exception {
        System.out.println("\n  ------------------------------------------------------------------  TEST 1: Full job execution");

        JobParameters params = uniqueParams();
        JobExecution execution = jobLauncherTestUtils.launchJob(params);

        System.out.println("  Job exit status : " + execution.getExitStatus().getExitCode());
        System.out.println("  Batch status    : " + execution.getStatus());

        assertEquals(BatchStatus.COMPLETED, execution.getStatus(),
                "Expected the job to complete with COMPLETED status");
        assertEquals(ExitStatus.COMPLETED.getExitCode(),
                execution.getExitStatus().getExitCode(),
                "Exit code should be COMPLETED");
    }

    //  TEST 2 — Schema Tasklet: processing_status column exists after job

    /**
     * Verifies that the AddColumnTasklet step actually added the
     * 'processing_status' column to the customer table.
     *
     */
    @Test
    @DisplayName("Tasklet step should add 'processing_status' column to customer table")
    void testSchemaTaskletAddsProcessingStatusColumn() throws Exception {
        System.out.println("\n  TEST 2: Schema Tasklet column creation");

        jobLauncherTestUtils.launchJob(uniqueParams());
        Integer count = customerRepository.countProcessingStatusColumn();

        System.out.println("  Columns named 'processing_status' found: " + count);

        assertNotNull(count, "Column check query must not return null");
        assertTrue(count >= 1,
                "'processing_status' column must exist in customer table after Tasklet step");
    }

    //  TEST 3 — CSV Import Step: records written to DB

    /**
     * Launches only the csvImportStep in isolation using
     * jobLauncherTestUtils.launchStep()
     */
    @Test
    @DisplayName("csvImportStep should persist at least one Customer record")
    void testCsvImportStepPersistsRecords() throws Exception {
        System.out.println("\n   TEST 3 : CSV import step — record persistence");

        // Pre-condition: ensure processing_status column exists
        jdbcTemplate.execute(
                "ALTER TABLE customer ADD COLUMN IF NOT EXISTS processing_status VARCHAR(50)");

        JobExecution stepExecution = jobLauncherTestUtils.launchStep("csvImportStep");

        System.out.println("  csvImportStep exit status: " +
                stepExecution.getExitStatus().getExitCode());

        long customerCount = customerRepository.count();
        System.out.println("  Customers in DB after step: " + customerCount);

        assertTrue(customerCount > 0,
                "At least one Customer should be saved after the CSV import step");
    }

    //  TEST 4 — Processing step: name is upper-cased

    /**
     * to cnfirm that upperCaseProcessor inside the composite processor
     * has converted every customer's name to upper case.
     *
     * Insert a known customer directly via the repository.
     * Run jpaStep02 (which reads from DB, upper-cases, and writes back).
     * Reload the entity and assert the name is fully upper-case.
     */
    @Test
    @DisplayName("jpaStep02 composite processor should upper-case customer names")
    void testJpaStepUpperCasesNames() throws Exception {
        System.out.println("\n  Upper-case processor");

        // Ensure column exists before running the step
        jdbcTemplate.execute(
                "ALTER TABLE customer ADD COLUMN IF NOT EXISTS processing_status VARCHAR(50)");

        Department sales = departmentRepository.findByDeptName("Sales");
        assertNotNull(sales, "Sales department must exist before inserting test customer");

        Customer testCustomer = new Customer("alice", "alice@example.com", sales);
        testCustomer.setProcessing_status("Task 12 - Email Provided");
        customerRepository.save(testCustomer);

        System.out.println("  Inserted test customer: " + testCustomer.getName());

        JobExecution stepExec = jobLauncherTestUtils.launchStep("JpaStep02");
        System.out.println("  JpaStep02 exit status: " + stepExec.getExitStatus().getExitCode());

        List<Customer> customers = customerRepository.findAll();
        assertFalse(customers.isEmpty(), "Customer table should not be empty after step");

        customers.forEach(c -> {
            System.out.println("  Name in DB: " + c.getName());
            assertEquals(c.getName().toUpperCase(), c.getName(),
                    "Customer name must be fully upper-case after processing step. Got: " + c.getName());
        });
    }

    //  TEST 5 — Field validation: processing_status is populated after CSV import

    /**
     * Validates the business rule encoded in csvToJpaProcessor:
     *  • Customers WITH an email    → processing_status = "Task 11 - Email Provided"
     *  • Customers WITHOUT an email → processing_status starts with "Task 11 - No Email"
     *
     * This is a field-level DB validation: after the job runs, every row
     * in the customer table must have a non-null, non-empty processing_status.
     */
    @Test
    @DisplayName("Every Customer row should have a non-null processing_status after CSV import")
    void testProcessingStatusIsNeverNull() throws Exception {
        System.out.println("\n  TEST 5: processing_status field validation");

        jdbcTemplate.execute(
                "ALTER TABLE customer ADD COLUMN IF NOT EXISTS processing_status VARCHAR(50)");

        jobLauncherTestUtils.launchStep("csvImportStep");

        List<Customer> all = customerRepository.findAll();
        assertTrue(all.size() > 0, "At least one customer must be present");

        all.forEach(c -> {
            System.out.printf("  Customer [%s] → processing_status = '%s'%n",
                    c.getName(), c.getProcessing_status());

            assertNotNull(c.getProcessing_status(),
                    "processing_status must not be null for: " + c.getName());
            assertFalse(c.getProcessing_status().isBlank(),
                    "processing_status must not be blank for: " + c.getName());
            assertTrue(c.getProcessing_status().contains("Task 12"),
                    "processing_status should start with 'Task 12' for: " + c.getName());
        });
    }

    //  TEST 6 — Field validation: email field constraints

    /**
     * Verifies that the CSV import processor correctly stores a null email when the CSV row has no email value.
     * The jpaStep02 processor then throws NullPointerException for such rows,
     * which is skipped (skipLimit = 10), so the record persists with email = null.
     * This test confirms the null-storage path is working correctly.
     */
    @Test
    @DisplayName("Customers with blank CSV email should be stored with null email")
    void testBlankEmailStoredAsNull() throws Exception {
        System.out.println("\n  TEST 6: Blank email → null in DB");

       jdbcTemplate.execute(
                "ALTER TABLE customer ADD COLUMN IF NOT EXISTS processing_status VARCHAR(50)");

        jobLauncherTestUtils.launchStep("csvImportStep");

        List<Customer> noEmailCustomers = customerRepository.findCustomersByEmail(null);
        System.out.println("  Customers with null email: " + noEmailCustomers.size());

        // If any null-email customers exist, validate their processing_status tag
        noEmailCustomers.forEach(c -> {
            System.out.printf("  [%s] email=null, status='%s'%n",
                    c.getName(), c.getProcessing_status());
            assertTrue(
                    c.getProcessing_status() != null &&
                            c.getProcessing_status().contains("No Email"),
                    "Null-email customer should have 'No Email' in processing_status. Got: "
                            + c.getProcessing_status());
        });

        System.out.println("  Null-email validation complete.");

        List<Customer> EmailCustomers = customerRepository.findCustomersWithEmail();
        System.out.println("  Customers with  email: " + EmailCustomers.size());

        // If any email customers exist, validate their processing_status tag
        EmailCustomers.forEach(c -> {
            System.out.printf("  [%s] email=null, status='%s'%n",
                    c.getName(), c.getProcessing_status());
            assertTrue(
                    c.getProcessing_status() != null &&
                            c.getProcessing_status().contains("Email Provided"),
                    "Null-email customer should have ' Email Provided' in processing_status. Got: "
                            + c.getProcessing_status());
        });

        System.out.println("  email validation complete.");
    }

    //  TEST 7 — Department FK integrity: every Customer has a valid Department

    /**
     * after the CSV import step, no Customer row should have a null department reference.
     */
    @Test
    @DisplayName("Every imported Customer must have a non-null Department reference")
    void testEveryCustomerHasDepartment() throws Exception {
        System.out.println("\n  TEST 07: Customer → Department FK integrity");

        jdbcTemplate.execute(
                "ALTER TABLE customer ADD COLUMN IF NOT EXISTS processing_status VARCHAR(50)");

        jobLauncherTestUtils.launchStep("csvImportStep");

        List<Customer> all = customerRepository.findAll();
        assertTrue(all.size() > 0, "Customer table must not be empty");

        all.forEach(c -> {
            System.out.printf("  Customer [%s] → dept = %s%n",
                    c.getName(),
                    c.getDepartment() == null ? "NULL " : c.getDepartment().getDeptName() + " ");
            assertNotNull(c.getDepartment(),
                    "Customer must have a department. Null found for: " + c.getName());
            assertNotNull(c.getDepartment().getId(),
                    "Department ID must not be null for: " + c.getName());
        });
    }

    //  TEST 8 — Step-level metrics: read count == write count (no silent drops)

    /**
     * the number of items read must equal the number of items written.
     * TO ENSURE THE ALL READ RECORDS ARE WRITTEN TO THE DB,
     *
     */
    @Test
    @DisplayName("csvImportStep read count should equal write count (no silent drops)")
    void testCsvStepReadCountEqualsWriteCount() throws Exception {
        System.out.println("\n  ▶  TEST 08: Read count == Write count in csvImportStep");

        jdbcTemplate.execute(
                "ALTER TABLE customer ADD COLUMN IF NOT EXISTS processing_status VARCHAR(50)");

        JobExecution jobExec = jobLauncherTestUtils.launchStep("csvImportStep");

        StepExecution stepExec = jobExec.getStepExecutions()
                .stream()
                .filter(s -> s.getStepName().equals("csvImportStep"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("csvImportStep not found in execution"));

        long readCount  = stepExec.getReadCount();
        long writeCount = stepExec.getWriteCount();

        System.out.printf("  Read: %d | Written: %d%n", readCount, writeCount);

        assertEquals(readCount, writeCount,
                String.format("Read count (%d) must equal write count (%d) — no items should be silently dropped",
                        readCount, writeCount));
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Helpers
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Generates a unique JobParameters instance for each test run.
     * Spring Batch refuses to re-run a job with identical parameters
     * once it has reached COMPLETED, so every launch needs a unique key.
     */
    private JobParameters uniqueParams() {
        return new JobParametersBuilder()
                .addLong("start-time", System.currentTimeMillis())
                .addString("targetDept", "Sales")
                .toJobParameters();
    }
}