package kpi.diploma.middleware.core.data.io;

import java.io.Serializable;

public interface RemoteSourceLoader<T> extends Serializable {
    byte[] loadContent(T metadata);
}
