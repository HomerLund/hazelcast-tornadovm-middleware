package kpi.diploma.middleware.server.adapters.accelerator.tornadovm;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import uk.ac.manchester.tornado.api.common.TornadoFunctions;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TornadoTaskFactory {
    private static final Map<Method, Object> taskCache = new ConcurrentHashMap<>();

    public static Object createTornadoTask(Method targetMethod){
        return taskCache.computeIfAbsent(targetMethod, m -> {
            try {
                int argsCount = m.getParameterCount();

                Class<?> taskInterface = determineTornadoInterface(argsCount);


                return new ByteBuddy()
                        .subclass(taskInterface)
                        .method(ElementMatchers.named("run"))
                        .intercept(MethodCall.invoke(m).withAllArguments())
                        .make()
                        .load(targetMethod.getDeclaringClass().getClassLoader())
                        .getLoaded()
                        .getDeclaredConstructor()
                        .newInstance();
            }
            catch (Exception e){
                throw new RuntimeException("Task generation error for TornadoVM", e);
            }
        });
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
