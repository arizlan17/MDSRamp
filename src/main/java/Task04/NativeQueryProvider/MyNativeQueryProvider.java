package Task04.NativeQueryProvider;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.batch.item.database.orm.AbstractJpaQueryProvider;

public class MyNativeQueryProvider extends AbstractJpaQueryProvider {

    @Override
    public Query createQuery() {
        EntityManager em = getEntityManager();
        String sql = "SELECT c.name as c_name, d.dept_name as d_name " +
                "FROM customer c " +
                "JOIN department d ON c.dept_id = d.dept_id";
        return em.createNativeQuery(sql, "CustomerDeptMapping");
    }

    @Override
    public void afterPropertiesSet() throws Exception { }
}