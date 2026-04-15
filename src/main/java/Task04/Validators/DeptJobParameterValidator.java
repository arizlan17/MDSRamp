package Task04.Validators;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.JobParametersValidator;
import org.springframework.util.StringUtils;

public class DeptJobParameterValidator implements JobParametersValidator {
    @Override
    public void validate(JobParameters parameters) throws JobParametersInvalidException {
        String dept = parameters.getString("targetDept");
        if (!StringUtils.hasLength(dept) ||  !StringUtils.hasText(dept) ) {
            throw new JobParametersInvalidException("Error: 'targetDept' parameter is required!");
        }
    }
}
