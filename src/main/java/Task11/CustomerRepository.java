package Task11;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Task11.Customer, Long> {

    List<Customer> findAll();
    Optional<Customer> findById(Long id);

    @Query("SELECT c FROM Customer c JOIN c.department d WHERE d.id = :deptId")
    List<Customer> findCustomersByDepartment(@Param("deptId") Long deptId);

    List<Customer> findCustomersByEmail(String email);
    List<Customer> findByNameAndEmailContaining(String name, String domain);


}
