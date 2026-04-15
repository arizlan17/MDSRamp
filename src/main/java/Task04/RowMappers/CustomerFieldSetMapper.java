package Task04.RowMappers;


import Task04.Customer;
import Task04.dto.CustomerDTO;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

public class CustomerFieldSetMapper implements FieldSetMapper<CustomerDTO> {
    @Override
    public CustomerDTO mapFieldSet(FieldSet fieldSet) {
        CustomerDTO customer = new CustomerDTO();
        customer.setName(fieldSet.readString("name"));
        customer.setEmail(fieldSet.readString("email"));
        customer.setDepartmentId(fieldSet.readLong("dept_id"));
        return customer;
    }
}