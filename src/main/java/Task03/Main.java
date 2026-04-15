package Task03;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommandLineRunner demo(CustomerRepository customerRepo, DepartmentRepository deptRepo) {
        return (args) -> {
            // --- 1. CREATE DEPARTMENTS ---
            Department sales = new Department();
            sales.setDeptName("Sales");

            Department tech = new Department();
            tech.setDeptName("Technology");

            deptRepo.saveAll(List.of(sales, tech));

            // --- 2. CREATE MULTIPLE CUSTOMERS ---
            Customer c1 = new Customer("John Doe", "john@example.com", sales);
            Customer c2 = new Customer("Jane Smith", "jane@techcorp.com", tech);
            Customer c3 = new Customer("Bob Wilson", "bob@example.com", sales);
            Customer c4 = new Customer("Alice Tech", "alice@techcorp.com", tech);
            Customer c5 = new Customer("Charlie Brown", "charlie@gmail.com", sales);

            customerRepo.saveAll(List.of(c1, c2, c3, c4, c5));

            System.out.println("--- Data Initialization  ---\n");



            System.out.println("\n--- Update Customer  ---\n");

            c1.setEmail("john.doe@updated.com");
            customerRepo.save(c1);


            System.out.println("\n --- Print All---\n");

            List<Customer>allusers = customerRepo.findAll();
            System.out.println("(All Customers): " + allusers.stream().map(Customer::toString).toList());

            System.out.println("\n --- Find By---\n");

            List<Customer> exampleUsers = customerRepo.findByNameAndEmailContaining("Bob Wilson", "example.com");
            System.out.println("Bob: " + exampleUsers.size());



            System.out.println("\n --- Find By dept Id---\n");


            List<Customer> techStaff = customerRepo.findCustomersByDepartment(tech.getId());
            System.out.println("(Tech Dept): " + techStaff.size());
            techStaff.forEach(c -> System.out.println(" - " + c.getName()));

            System.out.println("\n --- Delete---\n");
            System.out.println("Total Customers remaining: " + customerRepo.count());
            System.out.println("Deleting Charlie...");
            customerRepo.delete(c5);
            System.out.println("Total Customers remaining: " + customerRepo.count());
        };
    }
}