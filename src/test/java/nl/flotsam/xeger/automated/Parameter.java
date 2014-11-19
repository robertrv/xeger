package nl.flotsam.xeger.automated;

import lombok.Data;
import lombok.experimental.Builder;

/**
 * Created by robertrv
 */
@Builder
@Data
public class Parameter {
    private final boolean works;
    private final String regex;
    private Class<? extends Throwable> expected;
    private int iterationsOverride;
}
