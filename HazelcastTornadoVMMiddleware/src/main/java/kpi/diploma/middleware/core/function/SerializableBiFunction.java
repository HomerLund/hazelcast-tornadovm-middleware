package kpi.diploma.middleware.core.function;

import java.io.Serializable;

public interface SerializableBiFunction<T, U, R> extends Serializable {
    R apply(T t, U u);
}
