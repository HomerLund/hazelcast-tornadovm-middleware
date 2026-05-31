package kpi.diploma.middleware.server.adapters.accelerator.tornadovm;

import kpi.diploma.middleware.core.logging.Logger;
import kpi.diploma.middleware.server.bootstrap.accelerator.instrumentation.ByteBuddyAgentInstaller;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.jar.asm.*;
import net.bytebuddy.jar.asm.commons.ClassRemapper;
import net.bytebuddy.jar.asm.commons.SimpleRemapper;
import org.apache.commons.math3.analysis.function.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class TornadoPureClassInjector {
    private static final Map<Class<?>, Class<?>> pureClasses = new ConcurrentHashMap<>();

    public static Class<?> getPureClass(Class<?> targetClass){
        return pureClasses.computeIfAbsent(targetClass, clazz -> {
            try{
                String originalName = clazz.getName();
                String pureName = originalName + "_TornadoPure";
                String originalInternal = originalName.replace('.', '/');
                String pureInternal = pureName.replace('.', '/');

                InputStream is = clazz.getClassLoader().getResourceAsStream(originalInternal + ".class");

                if (is == null){
                    throw new ClassNotFoundException(originalName);
                }
                byte[] originalBytes = is.readAllBytes();

                ClassReader classReader = new net.bytebuddy.jar.asm.ClassReader(originalBytes);
                ClassWriter classWriter = new ClassWriter(classReader, 0);

                SimpleRemapper remapper = new SimpleRemapper(originalInternal, pureInternal);

                ClassVisitor classVisitor = new ClassRemapper(classWriter, remapper){
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions){
                        MethodVisitor originalMv = super.visitMethod(access, name, descriptor, signature, exceptions);

                        return new MethodVisitor(Opcodes.ASM9, originalMv) {
                            @Override
                            public AnnotationVisitor visitLocalVariableAnnotation(
                                    int typeRef, TypePath typePath, Label[] start, Label[] end,
                                    int[] index, String descriptor, boolean visible
                            )
                            {
                                Logger.info("TornadoPureClassInjector", "Found local variable annotation: " + descriptor);
                                if ("Lkpi/diploma/middleware/client/api/gpu/GpuParallel;".equals(descriptor)) {
                                    Logger.info("TornadoPureClassInjector", "Replacing GpuParallel with TornadoVM @Paralell");
                                    String tornadoDescriptor = "Luk/ac/manchester/tornado/api/annotations/Parallel;";
                                    return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, tornadoDescriptor, true);
                                }

                                return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
                            }
                        };
                    }

                    @Override
                    public void visitEnd(){
                        MethodVisitor mv =super.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "$getLookup", "()Ljava/lang/invoke/MethodHandles$Lookup;", null, null);
                        if (mv != null){
                            mv.visitCode();
                            mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/invoke/MethodHandles", "lookup", "()Ljava/lang/invoke/MethodHandles$Lookup;", false);
                            mv.visitInsn(Opcodes.ARETURN);
                            mv.visitMaxs(1, 0);
                            mv.visitEnd();
                        }
                        super.visitEnd();
                    }
                };

                classReader.accept(classVisitor, 0);
                byte[] pureBytes = classWriter.toByteArray();

                File targetDir = new File(Paths.get("server", "adapters", "accelerator", "tornadovm", "userPureClasses").toString());

                if (!targetDir.exists()){
                    boolean created = targetDir.mkdirs();
                    if (created){
                        Logger.info("TornadoPureClassInjector", "Created custom directory for pure classes: " + targetDir.getAbsolutePath());
                    }
                }

                File jarFile = new File(targetDir, pureName.replace('.', '_') + ".jar");

                try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile))){
                    jos.putNextEntry(new JarEntry(pureInternal + ".class"));
                    jos.write(pureBytes);
                    jos.closeEntry();
                }

                Instrumentation instrumentation = ByteBuddyAgent.getInstrumentation();
                instrumentation.appendToSystemClassLoaderSearch(new JarFile(jarFile));

                Logger.info("TornadoPureClassInjector", "Dynamically injected pure class from: " + jarFile.getAbsolutePath());

                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(clazz, MethodHandles.lookup());

                return lookup.defineClass(pureBytes);
            }
            catch (Exception e){
                throw new RuntimeException("Could not inject pure class for: " + targetClass.getName(), e);
            }
        });
    }
}
