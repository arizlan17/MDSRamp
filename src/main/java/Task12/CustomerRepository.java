package Task12;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findAll();
    Optional<Customer> findById(Long id);

    @Query("SELECT c FROM Customer c JOIN c.department d WHERE d.id = :deptId")
    List<Customer> findCustomersByDepartment(@Param("deptId") Long deptId);
    List<Customer> findCustomersByEmail(String email);
    List<Customer> findByNameAndEmailContaining(String name, String domain);
    @Query(value =
            "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE UPPER(table_name) = 'CUSTOMER' " +
                    "AND UPPER(column_name) = 'PROCESSING_STATUS'",
            nativeQuery = true)
    Integer countProcessingStatusColumn();

    @Query("SELECT c FROM Customer c WHERE c.email IS NOT NULL")
    List<Customer> findCustomersWithEmail();


}
