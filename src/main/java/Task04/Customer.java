package Task04;
import Task04.dto.CustomerDeptDto;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

@Entity
@SqlResultSetMapping(
        name = "CustomerDeptMapping",
        classes = @ConstructorResult(
                targetClass = CustomerDeptDto.class,
                columns = {
                        @ColumnResult(name = "c_name", type = String.class),
                        @ColumnResult(name = "d_name", type = String.class)
                }
        )
)
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;
    @Column(name = "email")
    private String email;

    @ManyToOne
    @JoinColumn(name = "dept_id")
    @JsonManagedReference
    private Department department;

    public Customer() {
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", department=" + department.getDeptName() +
                '}';
    }

    public Customer(String name, String email, Department department) {
        this.name = name;
        this.email = email;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }
    @Column(name = "processing_status") // The exact name added by your Tasklet
    private String processing_status;

    public String getProcessing_status() { return processing_status; }
    public void setProcessing_status(String addedStatus) { this.processing_status = addedStatus; }





}
