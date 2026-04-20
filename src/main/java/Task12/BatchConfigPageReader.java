package Task12;

import Task12.TaskletColumn.AddColumnTasklet;
import Task12.JobListners.BatchListener;
import Task12.dto.CustomerDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;

@Configuration
public class BatchConfigPageReader {


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


    @Bean(name = "customerJpaReader")
    public JpaPagingItemReader<Customer> jpaReader(EntityManagerFactory entityManagerFactory) {
        return new JpaPagingItemReaderBuilder<Customer>()
                .name("customerJpaReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT c FROM Customer c")
                .pageSize(10)
                .build();
    }

    //------------------------------------------------------------------------------------------


    @Bean(name = "csvToJpaProcessor")
    public ItemProcessor<CustomerDTO, Customer> csvToJpaprocessor(EntityManager entityManager) {

        return dto -> {
            Customer customer = new Customer();
            customer.setName(dto.getName());
            if (dto.getEmail().isBlank()) {
                customer.setEmail(null);
                customer.setProcessing_status("Task 12 - No Email Provided - ");
            }else {
                customer.setEmail(dto.getEmail());
                customer.setProcessing_status("Task 12 - Email Provided");
            }
            Department dept = entityManager.getReference(Department.class, dto.getDepartmentId());
            customer.setDepartment(dept);

            return customer;
        };
    }


    @Bean(name = "jpaProcessorToEntity")
    public ItemProcessor<Customer, Customer> jpaProcessorToEntity(BatchListener listener) {
        return customer -> {
            if (customer.getEmail() == null) {
                throw new NullPointerException("Task 11 - email is null for customer: " + customer.getId());
            }

            customer.setName(customer.getName());
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

    @Bean(name = "taggingProcessor")
    public ItemProcessor<Customer, Customer> taggingProcessor(BatchListener listener) {
        return customer -> {
            customer.setName(customer.getName());
            return customer;
        };
    }


    @Bean(name = "compositeProcessor")
    public CompositeItemProcessor<Customer, Customer> compositeProcessor(
            @Qualifier("jpaProcessorToEntity")ItemProcessor<Customer, Customer> p0,
            @Qualifier("upperCaseProcessor") ItemProcessor<Customer, Customer> p1,
            @Qualifier("taggingProcessor") ItemProcessor<Customer, Customer> p2) {

        CompositeItemProcessor<Customer, Customer> composite = new CompositeItemProcessor<>();
        composite.setDelegates(Arrays.asList(p0,p1, p2));
        return composite;
    }


    //------------------------------------------------------------------------------------------

    @Bean(name = "jpaWriter")
    public JpaItemWriter<Customer> jpawriter(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<Customer>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }


    @Bean(name = "jpaRepositoryWriter")
    public ItemWriter<Customer> jpaRepositoryWriter(CustomerRepository repository) {
        return chunk -> {
            System.out.println(" >>> Writing Chunk to Database via JPA Repository...");
            repository.saveAll(chunk);
        };
    }

    @Bean(name = "logWriter")
    public ItemWriter<Customer> logWriter() {
        return chunk -> {
            System.out.println("Logging items: " + chunk.getItems().size());
        };
    }

    @Bean(name = "compositeWriter")
    public CompositeItemWriter<Customer> compositeWriter(
            @Qualifier("jpaRepositoryWriter") ItemWriter<Customer> w1,
            @Qualifier("logWriter") ItemWriter<Customer> w2) {

        CompositeItemWriter<Customer> composite = new CompositeItemWriter<>();
        composite.setDelegates(Arrays.asList(w1, w2));
        return composite;
    }

    //-------------------------------------------------------------------------


    @Bean(name = "schemaUpdateStep")
    public Step schemaUpdateStep(JobRepository jobRepo,
                                 PlatformTransactionManager ptm,
                                 AddColumnTasklet AddColumntasklet) {
        return new StepBuilder("schemaUpdateStep", jobRepo)
                .tasklet(AddColumntasklet, ptm)
                .build();
    }



    @Bean(name = "jpaStep02")
    public Step jpaStep02(JobRepository jobRepo, PlatformTransactionManager tm,
                          @Qualifier("customerJpaReader") JpaPagingItemReader<Customer> reader,
                          @Qualifier("compositeProcessor") CompositeItemProcessor<Customer, Customer> processor,
                          @Qualifier("compositeWriter") CompositeItemWriter<Customer> writer,
                          BatchListener listener) {
        return new StepBuilder("JpaStep02", jobRepo)
                .<Customer, Customer>chunk(10, tm)
                .listener(listener)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(10)
                .skip(Exception.class)
                .build();
    }

    @Bean(name = "csvImportStep")
    public Step csvImportStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              @Qualifier("customerCsvReader") FlatFileItemReader<CustomerDTO> reader,
                              @Qualifier("csvToJpaProcessor") ItemProcessor<CustomerDTO, Customer> processor,
                              @Qualifier("jpaWriter") JpaItemWriter<Customer> writer) {
        return new StepBuilder("csvImportStep", jobRepository)
                .<CustomerDTO, Customer>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }



    @Bean(name = "jpaBatchJob")
    public Job jpaBatchJob(JobRepository jobRepo,
                           @Qualifier("schemaUpdateStep") Step step1,
                           @Qualifier("csvImportStep") Step step3,
                           @Qualifier("jpaStep02") Step step2,
                           BatchListener listener) {
        return new JobBuilder("JpaBatchJob", jobRepo)
                .listener(listener)
                .start(step1)
                .next(step2)
                .next(step3)
                .build();
    }
}