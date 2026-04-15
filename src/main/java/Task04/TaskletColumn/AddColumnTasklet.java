package Task04.TaskletColumn;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class AddColumnTasklet implements Tasklet {
    private final JdbcTemplate jdbcTemplate;

    public AddColumnTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        System.out.println(">>> [Tasklet] Altering table to add 'processing_status' column...");

        String sql = "ALTER TABLE customer ADD COLUMN IF NOT EXISTS processing_status VARCHAR(50)";
        jdbcTemplate.execute(sql);
        System.out.println(">>> [Tasklet] Schema update completed.");
        return RepeatStatus.FINISHED;
    }

}
