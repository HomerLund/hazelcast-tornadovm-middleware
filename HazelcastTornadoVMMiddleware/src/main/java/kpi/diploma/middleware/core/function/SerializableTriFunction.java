package kpi.diploma.middleware.core.function;

import java.io.Serializable;

public interface SerializableTriFunction<T, U, V, R> extends Serializable {
    R apply(T t, U u, V v);
}
