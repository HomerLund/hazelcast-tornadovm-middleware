package kpi.diploma.middleware.server.adapters.accelerator.tornadovm.bytebuddy;

import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.*;
import net.bytebuddy.pool.TypePool;

public class TornadoVMAnnotationRemapper implements AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper {
    @Override
    public MethodVisitor wrap(TypeDescription instrumentedType, MethodDescription instrumentedMethod, MethodVisitor methodVisitor, Implementation.Context implementationContext, TypePool typePool, int writerFlags, int readerFlags) {
        return new MethodVisitor(Opcodes.ASM9, methodVisitor) {
            @Override
            public AnnotationVisitor visitLocalVariableAnnotation(
                    int typeRef, TypePath typePath,
                    Label[] start, Label[] end,
                    int[] index, String descriptor, boolean visible) {
                if ("Lkpi/diploma/middleware/client/api/gpu/GpuParallel;".equals(descriptor)) {
                    String tornadoDescriptor = "Luk/ac/manchester/tornado/api/annotations/Parallel;";
                    return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, tornadoDescriptor, visible);
                }

                return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
            }
        };
    }
}
