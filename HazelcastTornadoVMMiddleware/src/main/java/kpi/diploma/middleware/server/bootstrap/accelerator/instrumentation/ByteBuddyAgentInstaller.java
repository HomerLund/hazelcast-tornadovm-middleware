package kpi.diploma.middleware.server.bootstrap.accelerator.instrumentation;

import kpi.diploma.middleware.client.api.gpu.GpuKernel;
import kpi.diploma.middleware.client.api.gpu.GpuParallel;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.jar.asm.*;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;

public class ByteBuddyAgentInstaller {

    public static void install(AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper annotationRemapper) {
        try {
            ByteBuddyAgent.install();

            new AgentBuilder.Default()
                    .type(ElementMatchers.declaresMethod(ElementMatchers.isAnnotatedWith(GpuKernel.class)))
                    .transform(((builder, typeDescription, classLoader, module, protectionDomain) -> builder
                            .method(ElementMatchers.isAnnotatedWith(GpuKernel.class))
                            .intercept(MethodDelegation.to(GpuKernelInterceptor.class))
                            .visit(new AsmVisitorWrapper.ForDeclaredMethods()
                                    .method(ElementMatchers.isAnnotatedWith(GpuKernel.class), annotationRemapper)))
                    )
                    .installOnByteBuddyAgent();

            System.out.println("ByteBuddy Agent successfully installed");
        }
        catch (Exception e){
            System.err.println("Failed to install ByteBuddy Agent: " + e.getMessage());
        }
    }


}
