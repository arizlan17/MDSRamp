package Task12;

import org.springframework.batch.core.Job;
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

@SpringBootApplication(scanBasePackages = "Task12")
@EntityScan(basePackages = "Task12")
@EnableJpaRepositories(basePackages = "Task12")
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

@Bean
    public CommandLineRunner runBatch(CustomerRepository customerRepo,
                                      DepartmentRepository deptRepo,
                                      JobLauncher jobLauncher,
                                      @Qualifier("jpaBatchJob") Job jpaBatchJobEx07
)
    {

        return args -> {
            String Sales = "Sales";
            if (deptRepo.findByDeptName(Sales)== null) {
                Department sales = new Department();
                sales.setDeptName("Sales");

                deptRepo.save(sales);
            }

            String Tech = "Technology";
            if (deptRepo.findByDeptName(Tech)== null) {

                Department tech = new Department();
                tech.setDeptName("Technology");

                deptRepo.save(tech);
            }


            System.out.println("\n--- Starting Exercise 07");
            jobLauncher.run(jpaBatchJobEx07, new JobParametersBuilder()
                    .addLong("start-time", System.currentTimeMillis())
                    .addString("targetDept", "Sales")
                    .toJobParameters());


        };
    }
}