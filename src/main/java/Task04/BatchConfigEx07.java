package Task04;


import Task04.JobListners.BatchListener;
import Task04.NullableDataSkipPolicy.NullableDataSkipPolicy;
import Task04.Partitioner.ColumnRangePartitioner;
import Task04.TaskletColumn.AddColumnTasklet;
import Task04.dto.CustomerDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Map;

@Configuration
public class BatchConfigEx07 {


    @Bean(name = "customJobRepository")
    // Rename to avoid direct collision
    @Primary
    // Tells Spring Boot to prefer this bean over its autoconfigured one
    public JobRepository jobRepository(DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setIsolationLevelForCreate("ISOLATION_READ_COMMITTED");
        factory.afterPropertiesSet();
        return factory.getObject();
    }
//    -------------------------------------- Readers ----------------------------------

    @Bean(name = "customerCsvReader")
    public FlatFileItemReader<CustomerDTO> reader() {
        return new FlatFileItemReaderBuilder<CustomerDTO>()
                .name("customerCsvReader")
                .resource(new ClassPathResource("customer.csv"))
                .linesToSkip(1)
                .delimited()
                .names("name", "email", "departmentId")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(CustomerDTO.class);
                }})
                .build();
    }



    @Bean(name = "jpaFilteredReader")
    @StepScope
    public JpaPagingItemReader<Customer> jpaFilteredReader(EntityManagerFactory entityManagerFactory,@Value("#{jobParameters['targetDept']}") String deptName) {
        return new JpaPagingItemReaderBuilder<Customer>()
                .name("customerJpaFilteredReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT c FROM Customer c WHERE c.department.deptName = :dept")
                .pageSize(10)
                .parameterValues((Map.of("dept", deptName)))
                .build();
    }
    @Bean(name = "jpaFilteredReader02")
    @StepScope
    public JpaPagingItemReader<Customer> jpaFilteredReader02(EntityManagerFactory entityManagerFactory,
                                                             @Value("#{jobParameters['targetDept']}") String deptName) {
        return new JpaPagingItemReaderBuilder<Customer>()
                .name("customerJpaFilteredReader02")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT c FROM Customer c WHERE c.department.deptName != :dept")
                .pageSize(10)
                .parameterValues(Map.of("dept", deptName))
                .build();
    }


    @Bean(name = "partitionedJpaReader")
    @StepScope
    public JpaPagingItemReader<Customer> partitionedJpaReader(
            EntityManagerFactory entityManagerFactory,
            @Value("#{stepExecutionContext['minValue']}") Long minValue,
            @Value("#{stepExecutionContext['maxValue']}") Long maxValue) {

        return new JpaPagingItemReaderBuilder<Customer>()
                .name("partitionedJpaReader")
                .entityManagerFactory(entityManagerFactory)
                // Range-based query for parallel efficiency
                .queryString("SELECT c FROM Customer c WHERE c.id >= :min AND c.id <= :max")
                .parameterValues(Map.of("min", minValue, "max", maxValue))
                .pageSize(100)
                .build();
    }





//---------------------------------------- Partition ----------------------------------

    @Bean
    public ColumnRangePartitioner columnRangePartitioner() {
        return new ColumnRangePartitioner();
    }

    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // The "always-on" threads
        executor.setCorePoolSize(5);
        //      no of cores + 1
        executor.setMaxPoolSize(10);
        // The waiting area for tasks
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("BatchThread-");
        executor.initialize();
        return executor;
    }


    @Bean
    public PartitionHandler partitionHandler(@Qualifier("workerStep") Step workerStep,@Qualifier("taskExecutor") TaskExecutor taskExecutor) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setGridSize(5); // Target number of partitions
        handler.setTaskExecutor(taskExecutor);
        handler.setStep(workerStep);
        try {
            handler.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return handler;
    }



 //    -------------------------------------- Processors ----------------------------------


    @Bean(name = "csvToJpaProcessor")
    public ItemProcessor<CustomerDTO, Customer> csvToJpaprocessor(EntityManager entityManager) {

        return dto -> {
            Customer customer = new Customer();
            customer.setName(dto.getName());
            if (dto.getEmail().isBlank()) {
                customer.setEmail(null);
            }else {
                customer.setEmail(dto.getEmail());
                customer.setName(dto.getName());
            }
            Department dept = entityManager.getReference(Department.class, dto.getDepartmentId());
            customer.setDepartment(dept);

            return customer;
        };
    }

    @Bean(name = "jpaProcessorToEntityEx07")
    public ItemProcessor<Customer, Customer> jpaProcessorToEntityEx07(BatchListener listener) {
        return customer -> {
            if (customer.getEmail() == null) {
                customer.setProcessing_status("Failed - Missing Email - Task 04");
                throw new NullPointerException("email is null for customer: " + customer.getId());
            }else {
                customer.setProcessing_status("Processed Successfully - Task 04");
            }
            return customer;
        };
    }

    @Bean(name = "upperCaseProcessor")
    public ItemProcessor<Customer, Customer> upperCaseProcessor(BatchListener listener) {
        return customer -> {
            customer.setName(customer.getName().toUpperCase());
            return customer;
        };
    }


    @Bean(name = "compositeProcessor")
    public CompositeItemProcessor<Customer, Customer> compositeProcessor(
            @Qualifier("jpaProcessorToEntityEx07")ItemProcessor<Customer, Customer> p0,
            @Qualifier("upperCaseProcessor") ItemProcessor<Customer, Customer> p1
            ) {

        CompositeItemProcessor<Customer, Customer> composite = new CompositeItemProcessor<>();
        composite.setDelegates(Arrays.asList(p0,p1));
        return composite;
    }

    @Bean(name = "taggingProcessor")
    public ItemProcessor<Customer, Customer> taggingProcessor(BatchListener listener) {
        return customer -> {
            customer.setProcessing_status(customer.getProcessing_status()+"----- processed by Worker Step -----");
            return customer;
        };
    }


    @Bean(name = "compositeProcessorForPartitionWorker")
    public CompositeItemProcessor<Customer, Customer> compositeProcessorForPartitionWorker(
            @Qualifier("jpaProcessorToEntityEx07")ItemProcessor<Customer, Customer> p0,
            @Qualifier("upperCaseProcessor") ItemProcessor<Customer, Customer> p1,
            @Qualifier("taggingProcessor")ItemProcessor<Customer, Customer> p2
    ) {

        CompositeItemProcessor<Customer, Customer> composite = new CompositeItemProcessor<>();
        composite.setDelegates(Arrays.asList(p0,p1,p2));
        return composite;
    }


    //    -------------------------------------- Writers ----------------------------------
    @Bean(name = "jpaWriter")
    public JpaItemWriter<Customer> jpawriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<Customer>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }


    @Bean(name = "jpaRepositoryWriterEx07")
    public ItemWriter<Customer> jpaRepositoryWriterEx07(CustomerRepository repository) {
        return chunk -> {
            System.out.println(" >>> Writing Chunk to Database via JPA Repository...");
            repository.saveAll(chunk);
        };
    }

    @Bean(name = "logWriter")
    public ItemWriter<Customer> logWriter() {
        return chunk -> {

            System.out.println("=================================================================================\n");
            System.out.println("Logging items: " + chunk.getItems().size());
            System.out.println("=================================================================================\n");

        };
    }


    @Bean(name = "compositeWriter")
    public CompositeItemWriter<Customer> compositeWriter(
            @Qualifier("jpaRepositoryWriterEx07") ItemWriter<Customer> w1,
            @Qualifier("logWriter") ItemWriter<Customer> w2) {

        CompositeItemWriter<Customer> composite = new CompositeItemWriter<>();
        composite.setDelegates(Arrays.asList(w1, w2));
        return composite;
    }


//    ---------------------------------------- Step -------------------------------------------



    @Bean(name = "schemaUpdateStep")
    public Step schemaUpdateStep(JobRepository jobRepo,
                                 PlatformTransactionManager ptm,
                                 AddColumnTasklet AddColumntasklet,BatchListener listener) {
        return new StepBuilder("schemaUpdateStep", jobRepo)
                .tasklet(AddColumntasklet, ptm)
                .listener(listener)
                .build();
    }



    @Bean(name = "managerStep")
    public Step managerStep(JobRepository jobRepository,
                            PartitionHandler partitionHandler,
                            ColumnRangePartitioner partitioner,BatchListener listener) {
        return new StepBuilder("managerStep", jobRepository)
                .partitioner("workerStep", partitioner)
                .listener(listener)
                .partitionHandler(partitionHandler)
                .build();
    }


    @Bean(name = "csvImportStep")
    public Step csvImportStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              @Qualifier("customerCsvReader") FlatFileItemReader<CustomerDTO> reader,
                              @Qualifier("csvToJpaProcessor") ItemProcessor<CustomerDTO, Customer> processor,
                              @Qualifier("jpaWriter") JpaItemWriter<Customer> writer,BatchListener listener) {
        return new StepBuilder("csvImportStep", jobRepository)
                .<CustomerDTO, Customer>chunk(100, transactionManager)
                .reader(reader)
                .listener(listener)
                .processor(processor)
                .writer(writer)
                .build();
    }



    @Bean(name = "batchStepEx07")
    public Step batchStepEx07(JobRepository jobRepo,
                              PlatformTransactionManager transactionManager,
                              @Qualifier("jpaFilteredReader") JpaPagingItemReader<Customer> reader,
                              @Qualifier("compositeProcessor") CompositeItemProcessor<Customer, Customer> processor,
                              @Qualifier("compositeWriter") CompositeItemWriter<Customer> writer,  BatchListener listener) {
        return new StepBuilder("StepEx07", jobRepo)
                .<Customer, Customer>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor).listener(listener)
                .writer(writer)
                .faultTolerant()
                .skipPolicy(new NullableDataSkipPolicy())
                .build();
    }

    @Bean(name = "batchStepEx07_02")
    public Step batchStepEx07_02(JobRepository jobRepo,
                              PlatformTransactionManager transactionManager,
                              @Qualifier("jpaFilteredReader02") JpaPagingItemReader<Customer> reader,
                              @Qualifier("compositeProcessor") CompositeItemProcessor<Customer, Customer> processor,
                              @Qualifier("compositeWriter") CompositeItemWriter<Customer> writer,  BatchListener listener) {
        return new StepBuilder("StepEx07_02", jobRepo)
                .<Customer, Customer>chunk(100, transactionManager)
                .reader(reader)
                .listener(listener)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipPolicy(new NullableDataSkipPolicy())
                .build();
    }

    @Bean(name = "workerStep")
    public Step workerStep(JobRepository jobRepo,
                           PlatformTransactionManager transactionManager,
                           BatchListener listener,
                           @Qualifier("partitionedJpaReader") JpaPagingItemReader<Customer> reader,
                           @Qualifier("compositeProcessorForPartitionWorker") CompositeItemProcessor<Customer, Customer> processor,
                           @Qualifier("compositeWriter") CompositeItemWriter<Customer> writer) {
        return new StepBuilder("workerStep", jobRepo)
                .<Customer, Customer>chunk(100, transactionManager)
                .reader(reader)
                .listener(listener)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipPolicy(new NullableDataSkipPolicy())
                .build();
    }





//    /----------------------------------JOBs---------------------------------------------------------------------


    @Bean(name = "jpaBatchJobEx07")
    public Job jpaBatchJobEx07(JobRepository jobRepo,
                               @Qualifier("schemaUpdateStep") Step schemaUpdateStep,
                               @Qualifier("csvImportStep") Step csvImportStep,
                               @Qualifier("managerStep") Step managerStep,
                               @Qualifier("batchStepEx07") Step batchStepEx07,
                               @Qualifier("batchStepEx07_02") Step batchStepEx07_02,
                               BatchListener listener) {
        return new JobBuilder("JpaBatchJobEx07", jobRepo)
                .listener(listener)
                .start(schemaUpdateStep)
                .next(csvImportStep)
                .next(batchStepEx07)
                .next(batchStepEx07_02)
                .next(managerStep)// This runs workerSteps in parallel
                .build();
    }



    @Bean(name = "jobSchemaUpdateStep")
    public Job jobschemaUpdateStep (JobRepository jobRepo,
                               @Qualifier("schemaUpdateStep") Step schemaUpdateStep,
                               BatchListener listener) {
        return new JobBuilder("jobSchemaUpdateStep", jobRepo)
                .listener(listener)
                .start(schemaUpdateStep)
                .build();
    }



@Bean(name = "jobCSVImport")
    public Job jobCSVImport(JobRepository jobRepo,
                               @Qualifier("csvImportStep") Step csvImportStep,
                               BatchListener listener) {
        return new JobBuilder("jobCSVImport", jobRepo)
                .listener(listener)
                .start(csvImportStep)
                .build();
    }



    @Bean(name = "jobWithParameters")
    public Job jobWithParameters(JobRepository jobRepo,
                               @Qualifier("batchStepEx07") Step batchStepEx07,
                               BatchListener listener) {
        return new JobBuilder("jobWithParameters", jobRepo)
                .listener(listener)
                .start(batchStepEx07)
                .build();
    }


    @Bean(name = "jobWithOutParameters")
    public Job jobWithOutParameters(JobRepository jobRepo,
                                    @Qualifier("batchStepEx07_02") Step batchStepEx07_02,
                               BatchListener listener) {
        return new JobBuilder("jobWithOutParameters", jobRepo)
                .listener(listener)
                .start(batchStepEx07_02)
                .build();
    }


    @Bean(name = "jobPartitionJob")
    public Job jobPartitionJob(JobRepository jobRepo,
                               @Qualifier("managerStep") Step managerStep,
                               BatchListener listener) {
        return new JobBuilder("jobPartitionJob", jobRepo)
                .listener(listener)
                .start(managerStep)// This runs workerSteps in parallel
                .build();
    }


}