package kpi.diploma.middleware.server.adapters.accelerator.tornadovm;

import kpi.diploma.middleware.core.logging.Logger;
import kpi.diploma.middleware.server.bootstrap.accelerator.ComputeGraph;
import kpi.diploma.middleware.server.bootstrap.accelerator.ComputePlan;
import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.TornadoExecutionPlan;
import uk.ac.manchester.tornado.api.common.TornadoFunctions;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;

import java.lang.reflect.Method;
import java.util.*;

public class TornadoVMGraph implements ComputeGraph {
    private final TaskGraph tornadoTaskGraph;
    private int taskCounter = 0;
    private final Set<Object> registeredMemory = Collections.newSetFromMap(new IdentityHashMap<>());

    public TornadoVMGraph(String name){
        this.tornadoTaskGraph = new TaskGraph(name);
    }

    @Override
    public ComputeGraph allocateOnDevice(Object... memoryBuffers) {
        if (memoryBuffers.length > 0) {
            Collections.addAll(registeredMemory, memoryBuffers);
            tornadoTaskGraph.transferToDevice(DataTransferMode.FIRST_EXECUTION, memoryBuffers);
        }
        return this;
    }

    @Override
    public ComputeGraph copyToDevice(Object... memoryBuffers) {
        if (memoryBuffers.length > 0) {
            Collections.addAll(registeredMemory, memoryBuffers);
            tornadoTaskGraph.transferToDevice(DataTransferMode.EVERY_EXECUTION, memoryBuffers);
        }
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ComputeGraph addDynamicKernel(String name, Method method, Object[] args) {
        taskCounter++;
        String taskName = name + "_" + taskCounter;

        List<Object> unregisteredArrays = new ArrayList<>();
        for (Object arg : args){
            if (arg != null && arg.getClass().isArray()){
                if (!registeredMemory.contains(arg)){
                    unregisteredArrays.add(arg);
                    registeredMemory.add(arg);
                }
            }
        }

        if (!unregisteredArrays.isEmpty()){
            Logger.info("TornadoVMGraph", "Auto-registering dynamic arrays for task: " + taskName);
            Object[] dynamicMem = unregisteredArrays.toArray();

            tornadoTaskGraph.transferToDevice(DataTransferMode.EVERY_EXECUTION, dynamicMem);
            tornadoTaskGraph.transferToHost(DataTransferMode.EVERY_EXECUTION, dynamicMem);
        }

        Logger.info("TornadoVMGraph", "Trying to create and add task: " + taskName);

        Object generatedTask;

        try{
            Class<?> pureClass = TornadoPureClassInjector.getPureClass(method.getDeclaringClass());
            Method pureMethod = pureClass.getMethod(method.getName(), method.getParameterTypes());
            generatedTask = TornadoTaskFactory.createTornadoTask(pureMethod);
            Logger.info("TornadoVMGraph", "Task generated successfully. Class: " + generatedTask.getClass().getName());
        }
        catch (Throwable t){
            Logger.error("TornadoVMGraph", "Error during generating");
            t.printStackTrace();
            throw new RuntimeException(t);
        }

        int argsCount = args.length;

        try {
            switch (argsCount) {
                case 1 -> tornadoTaskGraph.task(taskName, (TornadoFunctions.Task1<Object>) generatedTask, args[0]);
                case 2 ->
                        tornadoTaskGraph.task(taskName, (TornadoFunctions.Task2<Object, Object>) generatedTask, args[0], args[1]);
                case 3 ->
                        tornadoTaskGraph.task(taskName, (TornadoFunctions.Task3<Object, Object, Object>) generatedTask, args[0], args[1], args[2]);
                case 4 ->
                        tornadoTaskGraph.task(taskName, (TornadoFunctions.Task4<Object, Object, Object, Object>) generatedTask, args[0], args[1], args[2], args[3]);
                case 5 ->
                        tornadoTaskGraph.task(taskName, (TornadoFunctions.Task5<Object, Object, Object, Object, Object>) generatedTask, args[0], args[1], args[2], args[3], args[4]);
                case 6 ->
                        tornadoTaskGraph.task(taskName, (TornadoFunctions.Task6<Object, Object, Object, Object, Object, Object>) generatedTask, args[0], args[1], args[2], args[3], args[4], args[5]);
                case 7 ->
                        tornadoTaskGraph.task(taskName, (TornadoFunctions.Task7<Object, Object, Object, Object, Object, Object, Object>) generatedTask, args[0], args[1], args[2], args[3], args[4], args[5], args[6]);
                case 8 ->
                        tornadoTaskGraph.task(taskName, (TornadoFunctions.Task8<Object, Object, Object, Object, Object, Object, Object, Object>) generatedTask, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
                case 9 ->
                        tornadoTaskGraph.task(taskName, (TornadoFunctions.Task9<Object, Object, Object, Object, Object, Object, Object, Object, Object>) generatedTask, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
                case 10 ->
                        tornadoTaskGraph.task(taskName, (TornadoFunctions.Task10<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object>) generatedTask, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9]);
                case 11 ->
                        tornadoTaskGraph.task(taskName, (TornadoFunctions.Task11<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object>) generatedTask, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10]);
                case 12 ->
                        tornadoTaskGraph.task(taskName, (TornadoFunctions.Task12<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object>) generatedTask, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11]);
                case 13 ->
                        tornadoTaskGraph.task(taskName, (TornadoFunctions.Task13<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object>) generatedTask, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12]);
                case 14 ->
                        tornadoTaskGraph.task(taskName, (TornadoFunctions.Task14<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object>) generatedTask, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13]);
                case 15 ->
                        tornadoTaskGraph.task(taskName, (TornadoFunctions.Task15<Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object, Object>) generatedTask, args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8], args[9], args[10], args[11], args[12], args[13], args[14]);
                default ->
                        throw new IllegalArgumentException("TornadoVM does not support: " + argsCount + " arguments");
            }
            Logger.info("TornadoVMGraph", "Task successfully added to graph");
        }
        catch (Throwable t){
            Logger.error("TornadoVMGraph", "Type casting or assignment error in TornadoVM");
            t.printStackTrace();
            throw new RuntimeException(t);
        }
        return this;

    }

    @Override
    public ComputeGraph copyToHost(Object... memoryBuffers) {
        tornadoTaskGraph.transferToHost(DataTransferMode.EVERY_EXECUTION, memoryBuffers);
        return this;
    }

    @Override
    public ComputePlan compile() {
        TornadoExecutionPlan plan = new TornadoExecutionPlan(tornadoTaskGraph.snapshot());
        return new TornadoVMPlan(plan);
    }
}
