package Task04;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dept_id")
    private Long id;

    @Column(name = "dept_name")
    private String deptName;

    @OneToMany(mappedBy = "department")
    @JsonBackReference
    private List<Customer> customers = new ArrayList<>();

    public Department(Long id ,String newDep) {
        this.id = id;
        this.deptName = newDep;
    }

    public Department() {

    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;

    }


        public void addCustomerToDep(Customer customer) {
        customers.add(customer);
    }



    public String toStringForCustomer() {
        return "Department{" +
                "id=" + id +
                ", deptName='" + deptName + '\'' +
                '}';
    }
}