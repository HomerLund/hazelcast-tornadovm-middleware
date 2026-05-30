package kpi.diploma.middleware.server.adapters.accelerator.execution;

import kpi.diploma.middleware.client.api.gpu.GpuMemory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class GpuMemoryExtractor {
    public record ExtractGpuBuffers(
        Object[] onceBuffers,
        Object[] everExecutionBuffers
    ) {}

    public static ExtractGpuBuffers extractAnnotationBuffers(Object target){
        List<Object> once = new ArrayList<>();
        List<Object> ever = new ArrayList<>();

        scanObject(target, once, ever);

        return new ExtractGpuBuffers(once.toArray(), ever.toArray());
    }

    private static void scanObject(Object obj, List<Object> once, List<Object> ever){
        if (obj == null) return;
        Class<?> objClass = obj.getClass();

        for (Field field : objClass.getDeclaredFields()){
            field.setAccessible(true);
            try {
                Object fieldValue = field.get(obj);

                if (fieldValue == null) {
                    continue;
                }

                if (field.isAnnotationPresent(GpuMemory.class)){
                    GpuMemory annotation = field.getAnnotation(GpuMemory.class);

                    if (annotation.mode() == GpuMemory.TransferMode.ONCE){
                        once.add(fieldValue);
                    }
                    else{
                        ever.add(fieldValue);
                    }
                } else if (fieldValue instanceof  Iterable<?>) {
                    for (Object item : (Iterable<?>) fieldValue){
                        scanObject(item, once, ever);
                    }
                }
                else{
                    Package pkg = fieldValue.getClass().getPackage();

                    if (pkg != null){
                        String pkgName = pkg.getName();
                        if (!pkgName.startsWith("java.") && !pkgName.startsWith("javax.") && !pkgName.startsWith("sun.") && !pkgName.startsWith("jdk.")) {
                            scanObject(fieldValue, once, ever);
                        }
                    }
                }
            }
            catch (IllegalAccessException e){
                System.err.println("Failed to access field: " + field.getName() + " " + e.getMessage());
            }
        }
    }


}
