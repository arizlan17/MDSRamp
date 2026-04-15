//package Task04;
//
//import Task04.JobListners.BatchListener;
//import Task04.RowMappers.CustomerRowMapper;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.Step;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
//import org.springframework.batch.item.json.JsonFileItemWriter;
//import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.batch.item.database.JdbcCursorItemReader;
//import org.springframework.core.io.FileSystemResource;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import javax.sql.DataSource;
//
//@Configuration
//public class BatchConfig {
//
//
//    @Bean
//    public JdbcCursorItemReader <Customer> reader (DataSource dataSource){
//            JdbcCursorItemReader <Customer> reader = new JdbcCursorItemReader<>();
//            reader.setDataSource(dataSource);
////            reader.setSql("SELECT id, name, email FROM customer");
//            reader.setRowMapper(new CustomerRowMapper());
//            reader.setSaveState(false);
//            return reader;
//    }
//
//
//
//    @Bean
//    public ItemProcessor<Customer ,String> processor(BatchListener batchListener){
//        System.out.println("Batch Processor -  Console Print");
//        return customer -> {
//            return "Customer ID: " + customer.getId() + ", Name: " + customer.getName() + ", Email: " + customer.getEmail();
//        };
//    }
//
//    @Bean
//    public ItemProcessor<Customer ,Customer> processorForJson(BatchListener batchListener){
//        System.out.println("Batch Processor  - Json Write");
//
//        return customer -> {
//            return customer;
//        };
//    }
//
//
//
//    @Bean(name = "Writer")
//    public ItemWriter<String> writer(){
//        return chunk -> {
//            for (String customerItem : chunk) {
//                System.out.println("Batch Writer");
//                System.out.println(customerItem);
//            }
//        };
//    }
//
//
//    @Bean(name = "jsonWriter")
//    public JsonFileItemWriter<Customer> jsonWriter(){
//        return new JsonFileItemWriterBuilder<Customer>()
//                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
//                .resource(new FileSystemResource("JsonOutput/customers.json"))
//                .name("jsonItemWriter")
//                .build();
//    }
//
//
//
//
//    @Bean
//    public Step batchStep01(JobRepository jobRepo, PlatformTransactionManager transactionManager,
//                          JdbcCursorItemReader<Customer> reader, @Qualifier("processor") ItemProcessor<Customer, String> processor,
//                          @Qualifier("Writer") ItemWriter<String> writer, BatchListener batchListner){
//        return new StepBuilder("Step01", jobRepo)
//                .<Customer, String>chunk(10, transactionManager)
//                .listener(batchListner)
//                .reader(reader)
//                .processor(processor)
//                .faultTolerant()
//                .skipLimit(10)
//                .skip(Exception.class)
//                .writer(writer)
//                .build();
//    }
//
//    @Bean
//    public Step batchStep02(JobRepository jobRepo, PlatformTransactionManager transactionManager,
//                            JdbcCursorItemReader<Customer> reader, @Qualifier("processorForJson") ItemProcessor<Customer, Customer> ProcessorForJson,
//                            @Qualifier("jsonWriter") ItemWriter<Customer> Jsonwriter, BatchListener batchListner){
//        return new StepBuilder("Step02", jobRepo)
//
//                .<Customer, Customer>chunk(10, transactionManager)
//                .listener(batchListner)
//                .faultTolerant()
//                .skipLimit(10)
//                .skip(Exception.class)
//                .reader(reader)
//                .processor(ProcessorForJson)
//                .writer(Jsonwriter)
//                .build();
//    }
//
////    @Bean
////    public Job runBatchJob(JobRepository jobRepository,
////                           @Qualifier("batchStep01") Step step1,
////                           @Qualifier("batchStep02") Step step2,
////                           BatchListener batchListener) {
////        return new JobBuilder("BatchJob", jobRepository)
////                .listener(batchListener)
////                .start(step1)
////                .next(step2)
////                .build();
////    }
//
//
//}
