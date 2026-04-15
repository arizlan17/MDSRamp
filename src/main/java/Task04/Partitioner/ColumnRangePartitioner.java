package Task04.Partitioner;

import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.HashMap;
import java.util.Map;

public class ColumnRangePartitioner implements Partitioner {
    @Autowired
    private JdbcTemplate jdbcTemplate; // For dynamic bounds checking

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        Integer min = jdbcTemplate.queryForObject("SELECT MIN(id) FROM customer", Integer.class);
        Integer max = jdbcTemplate.queryForObject("SELECT MAX(id) FROM customer", Integer.class);

        int targetSize = (max - min) / gridSize +1 ;
        System.out.println("targetSize: " + targetSize);
        Map<String, ExecutionContext> result = new HashMap<>();


        int number =0;
        int start = min;
        int end = start + targetSize -1;

        while (start <= max) {
            ExecutionContext value = new ExecutionContext();
            result.put("partition" + number, value);
            if (end >= max) {
                end = max;
            }
            value.put("minValue", start);
            value.put("maxValue", end);
            start += targetSize;
            end += targetSize;
            number++;
        }
        System.out.println("result: " + result.toString());
        return result;
    }
}
