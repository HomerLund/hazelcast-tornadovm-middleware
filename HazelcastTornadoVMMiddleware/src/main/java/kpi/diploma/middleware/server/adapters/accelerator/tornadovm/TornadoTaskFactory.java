package kpi.diploma.middleware.server.adapters.accelerator.tornadovm;

import kpi.diploma.middleware.core.logging.Logger;
import uk.ac.manchester.tornado.api.common.TornadoFunctions;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TornadoTaskFactory {
    private static final Map<Method, Object> taskCache = new ConcurrentHashMap<>();

    public static Object createTornadoTask(Method targetMethod){
        return taskCache.computeIfAbsent(targetMethod, m -> {
            try {
                int argsCount = m.getParameterCount();

                Class<?> taskInterface = determineTornadoInterface(argsCount);

                m.setAccessible(true);

                Method getLookupMethod = m.getDeclaringClass().getMethod("$getLookup");
                MethodHandles.Lookup lookup = (MethodHandles.Lookup) getLookupMethod.invoke(null);

                MethodHandle targetHandle = lookup.unreflect(m);

                MethodType invokedType = MethodType.methodType(taskInterface);

                Class<?>[] interfaceArgs = new Class<?>[argsCount];
                Arrays.fill(interfaceArgs, Object.class);
                MethodType samMethodType = MethodType.methodType(void.class, interfaceArgs);

                MethodType instantiatedMethodType = targetHandle.type().wrap().changeReturnType(void.class);

                CallSite callSite = LambdaMetafactory.altMetafactory(lookup, "apply", invokedType, samMethodType, targetHandle, instantiatedMethodType, LambdaMetafactory.FLAG_SERIALIZABLE);
                return callSite.getTarget().invoke();
            }
            catch (Throwable t){
                throw new RuntimeException("Task generation error for TornadoVM", t);
            }
        });
    }

    private static Method extractPureMethod(Method instrumentedMethod){
        Class<?> clazz = instrumentedMethod.getDeclaringClass();
        String originalName = instrumentedMethod.getName();
        Class<?>[] paramTypes = instrumentedMethod.getParameterTypes();

        for (Method m : clazz.getDeclaredMethods()){
            if (m.getName().startsWith(originalName + "$") && Arrays.equals(m.getParameterTypes(), paramTypes)){
                Logger.info("TornadoTaskFactory", "Found Pure method: " + m.getName());
                return m;
            }
        }

        return instrumentedMethod;
    }

    private static Class<?> determineTornadoInterface(int count){
        return switch (count) {
            case 1 -> TornadoFunctions.Task1.class;
            case 2 -> TornadoFunctions.Task2.class;
            case 3 -> TornadoFunctions.Task3.class;
            case 4 -> TornadoFunctions.Task4.class;
            case 5 -> TornadoFunctions.Task5.class;
            case 6 -> TornadoFunctions.Task6.class;
            case 7 -> TornadoFunctions.Task7.class;
            case 8 -> TornadoFunctions.Task8.class;
            case 9 -> TornadoFunctions.Task9.class;
            case 10 -> TornadoFunctions.Task10.class;
            case 11 -> TornadoFunctions.Task11.class;
            case 12 -> TornadoFunctions.Task12.class;
            case 13 -> TornadoFunctions.Task13.class;
            case 14 -> TornadoFunctions.Task14.class;
            case 15 -> TornadoFunctions.Task15.class;
            default -> throw new IllegalArgumentException("TornadoVM does not support: " + count + " arguments");
        };
    }
}
