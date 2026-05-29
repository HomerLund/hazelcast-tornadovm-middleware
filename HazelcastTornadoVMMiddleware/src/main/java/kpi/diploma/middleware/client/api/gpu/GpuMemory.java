package kpi.diploma.middleware.client.api.gpu;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GpuMemory {
    TransferMode mode() default TransferMode.EVER_EXECUTION;
    public enum TransferMode { ONCE, EVER_EXECUTION}
}
