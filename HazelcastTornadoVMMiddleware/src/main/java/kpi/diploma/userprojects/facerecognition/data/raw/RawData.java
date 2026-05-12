package kpi.diploma.userprojects.facerecognition.data.raw;

import java.io.Serializable;

public record RawData(byte[] content, String name) implements Serializable {
}
