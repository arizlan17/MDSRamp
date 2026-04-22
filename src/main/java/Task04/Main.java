package Task04;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;

@SpringBootApplication(scanBasePackages = "Task04")
@EntityScan(basePackages = "Task04")
@EnableJpaRepositories(basePackages = "Task04")
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

@Bean
public CommandLineRunner runBatch(CustomerRepository customerRepo,
                                      DepartmentRepository deptRepo,
                                      JobLauncher jobLauncher,
                                      @Qualifier("jpaBatchJobEx07") Job jpaBatchJobEx07,
                                  @Qualifier("jobSchemaUpdateStep") Job jobSchemaUpdateStep,
                                  @Qualifier("jobCSVImport") Job jobCSVImport,
                                  @Qualifier("jobWithParameters") Job jobWithParameters,
                                  @Qualifier("jobWithOutParameters") Job jobWithOutParameters,
                                  @Qualifier("jobPartitionJob") Job jobPartitionJob


                                  )
    {

        return args -> {

            if (deptRepo.count() == 0) {
                Department sales = new Department();
                sales.setDeptName("Sales");

                Department tech = new Department();
                tech.setDeptName("Technology");

                deptRepo.saveAll(List.of(sales, tech));
            }


            JobParameters params = new JobParametersBuilder()
                    .addString("targetDept", "Sales")
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

//            System.out.println("\n--- Starting Exercise 07");
//            jobLauncher.run(jpaBatchJobEx07, params);
//
            System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\n");
            System.out.println("\n--- Starting jobSchemaUpdateStep");
            jobLauncher.run(jobSchemaUpdateStep, params);
            System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\n");



            System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\n");
            System.out.println("\n--- Starting jobCSVImport");
            jobLauncher.run(jobCSVImport, params);
            System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\n");

            System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\n");
            System.out.println("\n--- Starting jobWithParameters");
            jobLauncher.run(jobWithParameters, params);


//            System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\n");
//            System.out.println("\n--- Starting jobWithOutParameters");
//            jobLauncher.run(jobWithOutParameters, params);
//            System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\n");
//
//            System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\n");
//            System.out.println("\n--- Starting jobPartitionJob");
//            jobLauncher.run(jobPartitionJob, params);
//            System.out.println("|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||\n");




        };
    }
}