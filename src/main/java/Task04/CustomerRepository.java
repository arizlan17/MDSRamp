package Task04;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Task04.Customer, Long> {

    List<Task04.Customer> findAll();
    Optional<Task04.Customer> findById(Long id);

    @Query("SELECT c FROM Customer c JOIN c.department d WHERE d.id = :deptId")
    List<Task04.Customer> findCustomersByDepartment(@Param("deptId") Long deptId);
    List<Task04.Customer> findCustomersByEmail(String email);
    List<Task04.Customer> findByNameAndEmailContaining(String name, String domain);


}
