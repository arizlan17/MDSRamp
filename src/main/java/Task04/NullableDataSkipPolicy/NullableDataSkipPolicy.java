package Task04.NullableDataSkipPolicy;

import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;

public class NullableDataSkipPolicy implements SkipPolicy {

    private static final int MAX_SKIP_COUNT = 250;

    @Override
    public boolean shouldSkip(Throwable t, long skipCount) throws SkipLimitExceededException {
        if (skipCount >= MAX_SKIP_COUNT){
            return false;
        }
        if (t instanceof NullPointerException){
            return true;
        }
        return false;
    }
}
