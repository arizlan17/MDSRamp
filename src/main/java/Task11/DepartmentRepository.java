package Task11;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {


    Department findDepartmentById(Long id);
    Department findByDeptName(String deptName);

}
