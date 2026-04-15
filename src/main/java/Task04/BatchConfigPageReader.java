//package Task04;
//
//import Task04.JobListners.BatchListener;
//import Task04.NativeQueryProvider.MyNativeQueryProvider;
//import Task04.dto.CustomerDeptDto;
//import jakarta.persistence.EntityManagerFactory;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.batch.item.database.JpaPagingItemReader;
//import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.transaction.PlatformTransactionManager;
//
//@Configuration
//public class BatchConfigPageReader {
//
//
//    @Bean(name = "customerJpaReader")
//    public JpaPagingItemReader<Customer> jpaReader(EntityManagerFactory entityManagerFactory) {
//        return new JpaPagingItemReaderBuilder<Customer>()
//                .name("customerJpaReader")
//                .entityManagerFactory(entityManagerFactory)
//                .queryString("SELECT c FROM Customer c")
//                .pageSize(10)
//                .build();
//    }
//
//    //  Native Join Reader (Exercise 06)
//    @Bean(name = "joinReader")
//    public JpaPagingItemReader<CustomerDeptDto> joinReader(EntityManagerFactory entityManagerFactory) {
//        return new JpaPagingItemReaderBuilder<CustomerDeptDto>()
//                .name("joinNativeReader")
//                .entityManagerFactory(entityManagerFactory)
//                .queryProvider(new MyNativeQueryProvider())
//                .pageSize(10)
//                .build();
//    }
//
//    @Bean
//    public ItemProcessor<Customer, String> jpaProcessorToString(BatchListener listener) {
//        return customer -> {
////            customer.setDepartment(listener.getDept());
//            return "JPA READ -> Customer ID: " + customer.getId() + ", Name: " + customer.getName();
//        };
//    }
//
//    @Bean
//    public ItemProcessor<Customer, Customer> jpaProcessorToEntity(BatchListener listener) {
//        return customer -> {
////            customer.setDepartment(listener.getDept());
//            customer.setName(customer.getName().toUpperCase() + " [UPDATED]");
//            return customer;
//        };
//    }
//
//    @Bean(name = "jpaConsoleWriter")
//    public ItemWriter<String> jpaConsoleWriter() {
//        return chunk -> {
//            System.out.println(" >>> Writing Chunk to Console...");
//            for (String item : chunk) {
//                System.out.println(item);
//            }
//        };
//    }
//
//    @Bean(name = "jpaRepositoryWriter")
//    public ItemWriter<Customer> jpaRepositoryWriter(CustomerRepository repository) {
//        return chunk -> {
//            System.out.println(" >>> Writing Chunk to Database via JPA Repository...");
//            repository.saveAll(chunk);
//        };
//    }
//
//    @Bean
//    public Step jpaStep01(JobRepository jobRepo, PlatformTransactionManager tm,
//                          @Qualifier("customerJpaReader") JpaPagingItemReader<Customer> reader,
//                          @Qualifier("jpaProcessorToString") ItemProcessor<Customer, String> processor,
//                          @Qualifier("jpaConsoleWriter") ItemWriter<String> writer,
//                          BatchListener listener) {
//        return new StepBuilder("JpaStep01", jobRepo)
//                .<Customer, String>chunk(10, tm)
//                .listener(listener)
//                .reader(reader)
//                .processor(processor)
//                .writer(writer)
//                .faultTolerant()
//                .skipLimit(10)
//                .skip(Exception.class)
//                .build();
//    }
//
//    @Bean
//    public Step jpaStep02(JobRepository jobRepo, PlatformTransactionManager tm,
//                          @Qualifier("customerJpaReader") JpaPagingItemReader<Customer> reader,
//                          @Qualifier("jpaProcessorToEntity") ItemProcessor<Customer, Customer> processor,
//                          @Qualifier("jpaRepositoryWriter") ItemWriter<Customer> writer,
//                          BatchListener listener) {
//        return new StepBuilder("JpaStep02", jobRepo)
//                .<Customer, Customer>chunk(10, tm)
//                .listener(listener)
//                .reader(reader)
//                .processor(processor)
//                .writer(writer)
//                .faultTolerant()
//                .skipLimit(10)
//                .skip(Exception.class)
//                .build();
//    }
//
//    // EXERCISE 06 STEP
//    @Bean
//    public Step joinStep(JobRepository jobRepo, PlatformTransactionManager tm,
//                         @Qualifier("joinReader") JpaPagingItemReader<CustomerDeptDto> reader,
//                         BatchListener listener) {
//        return new StepBuilder("JoinStep", jobRepo)
//                .<CustomerDeptDto, String>chunk(10, tm)
//                .reader(reader)
//                .processor(dto -> "EX-06 LOG: " + dto.toString())
//                .writer(chunk -> {
//                    System.out.println(">>> Writing Joined Native Results <<<");
//                    for (String s : chunk) System.out.println(s);
//                })
//                .listener(listener)
//                .build();
//    }
//
//    // EXERCISE 06 JOB
//    @Bean
//    public Job joinJob(JobRepository jobRepo, @Qualifier("joinStep") Step joinStep, BatchListener listener) {
//        return new JobBuilder("JoinNativeJob", jobRepo)
//                .listener(listener)
//                .start(joinStep)
//                .build();
//    }
//
//    // EXERCISE 04 JOB
////    @Bean
////    public Job jpaBatchJob(JobRepository jobRepo,
////                           @Qualifier("jpaStep01") Step step1,
////                           @Qualifier("jpaStep02") Step step2,
////                           BatchListener listener) {
////        return new JobBuilder("JpaBatchJob", jobRepo)
////                .listener(listener)
////                .start(step1)
////                .next(step2)
////                .build();
////    }
//}