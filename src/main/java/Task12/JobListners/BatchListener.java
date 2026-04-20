package Task12.JobListners;

import Task12.Customer;
import Task12.dto.CustomerDeptDto;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.*;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class BatchListener {
    private StepExecution stepExecution;




    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        System.out.println("=======================================");
        System.out.println("JOB STARTING: "+ jobExecution.getJobInstance()+" ------  " + jobExecution.getJobInstance().getJobName());
        System.out.println("=======================================");
        ExecutionContext jobContext = jobExecution.getExecutionContext();
        jobContext.put("total_processed_count", 0);

        System.out.println(">>> [BEFORE JOB] Initialized Execution Context: total_processed_count = 0");
    }




    @BeforeStep
    public void prepareData(StepExecution stepExe) {
            this.stepExecution = stepExe;
            String targetDeptName = stepExe.getJobParameters().getString("targetDept");
    }

    @AfterStep
    public ExitStatus afterStep(StepExecution stepExecution) {
        System.out.println("--- [AFTER STEP] Finished: " + stepExecution.getStepName() + " ---");
        System.out.println("Read Count: " + stepExecution.getReadCount());
        System.out.println("Write Count: " + stepExecution.getWriteCount());
        return stepExecution.getExitStatus();
    }

    @BeforeChunk
    public void beforeChunk(ChunkContext context) {
        String stepName = context.getStepContext().getStepName();
        int chunkCount = Math.toIntExact(context.getStepContext().getStepExecution().getCommitCount());

        System.out.printf("Step [%s]: Preparing to process chunk number %d%n", stepName, chunkCount + 1);
    }

    @AfterChunk
    public void afterChunk(ChunkContext context) {
        System.out.println(" << Chunk completed and committed successfully.");
    }
    @BeforeRead
    public void beforeRead() {
        // This runs BEFORE the reader tries to fetch a row
        System.out.println(" > Attempting to read a customer row...");
    }

//
//    @AfterRead
//    public void afterRead(Customer item) {
//        // This runs AFTER a row is successfully mapped to  Customer object
//        if (item != null) {
//            System.out.println(" > Successfully read: " + item.getName());
//        }
//    }
    @AfterRead
    public void afterRead(Object item) {
        if (item instanceof Customer customer) {
            System.out.println(" > Successfully read customer: " + customer.getName());
            return;
        }
        if (item instanceof CustomerDeptDto dto) {
            System.out.println(" > Successfully read join row: " + dto.getCustomerName());
        }
    }


    @OnReadError
    public void onReadError(Exception ex) {
        // This triggers if the SQL fails or the RowMapper crashes
        System.err.println(" !!! [READ ERROR] !!!");
        System.err.println(" Error Message: " + ex.getMessage());
    }




//    @BeforeProcess
//    public void beforeProcess(Customer item) {
//        // This runs AFTER @AfterRead but BEFORE the code in your ItemProcessor
//        System.out.println(" >> [PROCESS START] About to process: " + item.getName());
//    }
    @BeforeProcess
    public void beforeProcess(Object item) {
        if (item instanceof Customer customer) {
            System.out.println(" >> [PROCESS START] About to process customer: " + customer.getName());
            return;
        }
        if (item instanceof CustomerDeptDto dto) {
            System.out.println(" >> [PROCESS START] About to process join row: " + dto.getCustomerName());
        }
    }


    @OnProcessError
    public void onProcessError(Object item, Exception e) {
        if (item instanceof Customer customer) {
            System.err.println(" !!! [PROCESS ERROR] Failed to process customer: " + customer.getName());
            return;
        }
        if (item instanceof CustomerDeptDto dto) {
            System.err.println(" !!! [PROCESS ERROR] Failed to process join row: " + dto.getCustomerName());
            return;
        }
        System.err.println(" !!! [PROCESS ERROR] Failed to process item.");
    }
//    @AfterProcess
//    public void afterProcess(Customer item, Object result) {
//        // 'result' is what the processor is sending to the writer (String or Customer)
//        System.out.println(" << [PROCESS END] Finished processing " + item.getName());
//    }
    @AfterProcess
    public void afterProcess(Object item, Object result) {
        ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();
        int currentCount = jobContext.getInt("total_processed_count", 0);
        jobContext.put("total_processed_count", currentCount + 1);
        System.out.println(jobContext.getInt("total_processed_count") + " items processed so far in this job.");


        if (item instanceof Customer customer) {
            System.out.println(" << [PROCESS END] Finished processing customer: " + customer.getName());
            return;
        }
        if (item instanceof CustomerDeptDto dto) {
            System.out.println(" << [PROCESS END] Finished processing join row: " + dto.getCustomerName());
        }

    }


    @BeforeWrite
    public void beforeWrite(Chunk<? extends Object> items) {
        // Triggers BEFORE the ItemWriter starts saving to File/Printing to Console
        System.out.println(" >>> [WRITER START] Preparing to write " + items.size() + " items.");
    }

    @AfterWrite
    public void afterWrite(Chunk<? extends Object> items) {
        // Triggers AFTER the ItemWriter finishes successfully
        System.out.println(" <<< [WRITER END] Successfully wrote " + items.size() + " items.");
    }

    @OnWriteError
    public void onWriteError(Exception exception, Chunk<? extends Object> items) {
        // Triggers if the JSON file cannot be written or the Console fails
        System.err.println(" !!! [WRITE ERROR] !!!");
        System.err.println(" Failed to write a chunk of " + items.size() + " items.");
        System.err.println(" Error: " + exception.getMessage());
    }





    @AfterChunkError
    public void afterChunkError(ChunkContext context) {
        // This is triggered only if an exception occurs during a chunk
        System.err.println("!!! [CHUNK ERROR] An error occurred while processing this chunk !!!");
        String stepName = context.getStepContext().getStepName();

        String errorMessage = context.getStepContext()
                .getStepExecution()
                .getFailureExceptions()
                .stream()
                .map(Throwable::getMessage)
                .collect(java.util.stream.Collectors.joining(" | "));

        System.err.println("!!! [CHUNK ERROR DETAILS] !!!");
        System.err.println("Location: " + stepName);
        System.err.println("Reason: " + (errorMessage.isEmpty() ? "Check console logs for StackTrace" : errorMessage));
        System.err.println("Current Read Count: " + context.getStepContext().getStepExecution().getReadCount());
    }



    @AfterJob
    public void cleanupOrReport(JobExecution jobExecution) {
        long duration = Objects.requireNonNull(jobExecution.getEndTime()).getSecond() - Objects.requireNonNull(jobExecution.getStartTime()).getSecond();

        System.out.println("=======================================");
        System.out.println("JOB FINISHED: "+ jobExecution.getJobInstance()+" ------  " + jobExecution.getStatus());
        System.out.println("=======================================");

        if (jobExecution.getStatus().toString().equals("COMPLETED")) {
            System.out.println("Check JsonOutput/customers.json for results!");
            System.out.println("=======================================");

        }
    }

    @OnSkipInRead
    public void onSkipInRead(Throwable t) {
        System.err.println(" !!! [SKIP READ] A record was skipped during reading.");
        System.err.println(" Reason: " + t.getMessage());
    }

    @OnSkipInProcess
    public void onSkipInProcess(Object item, Throwable t) {
        // Triggers if the ItemProcessor throws a skippable exception for a specific item
        if (item instanceof Customer customer) {
            System.err.println(" !!! [SKIP PROCESS] Customer skipped: " + customer.getName());
        } else if (item instanceof CustomerDeptDto dto) {
            System.err.println(" !!! [SKIP PROCESS] Join row skipped: " + dto.getCustomerName());
        } else {
            System.err.println(" !!! [SKIP PROCESS] Item skipped.");
        }
        System.err.println(" Reason: " + t.getMessage());
    }

    @OnSkipInWrite
    public void onSkipInWrite(Object item, Throwable t) {
        // Triggers if the JSON writer or Console writer fails for a specific item
        System.err.println(" !!! [SKIP WRITE] An item failed to write and was skipped.");
        System.err.println(" Reason: " + t.getMessage());
    }
}
