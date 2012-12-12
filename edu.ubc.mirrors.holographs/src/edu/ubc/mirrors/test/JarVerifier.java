package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.util.ConsoleProgressListener;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.MirageClassGenerator;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;

public class JarVerifier implements IApplication {
    public static void main(String[] args) throws Exception {
        String snapshotPath = args[0];
        
        ISnapshot snapshot = SnapshotFactory.openSnapshot(
                new File(snapshotPath), 
                Collections.<String, String>emptyMap(), 
                new ConsoleProgressListener(System.out));
        
        // Create an instance of the mirrors API backed by the snapshot
        HeapDumpVirtualMachineMirror vm = new HeapDumpVirtualMachineMirror(snapshot);
        
        // Create a holograph VM
        Map<String, String> mappedFiles = Reflection.getStandardMappedFiles();
        
        VirtualMachineHolograph holographVM = new VirtualMachineHolograph(vm, 
                mappedFiles);
        
        new JarVerifier().verifyJars(holographVM);
    }
    
    int classes = 0;
    int noBytecode = 0;
    int errors = 0;
    int unclassified = 0;
    int missing = 0;
    int illegal = 0;
    int implemented = 0;
    
    public void verifyJars(final VirtualMachineHolograph vm) throws Exception {
        String bootPath = (String)System.getProperties().get("sun.boot.class.path");
        String[] paths = bootPath.split(File.pathSeparator);
        NativeMethodCounter counter = new NativeMethodCounter();
        ThreadMirror thread = vm.getThreads().get(0);
        for (String path : paths) {
            if (new File(path).exists()) {
                JarFile jarFile = new JarFile(path);
                for (JarEntry entry : Collections.list(jarFile.entries())) {
                    String name = entry.getName();
                    if (name.endsWith(".class")) {
                        classes++;
                        new ClassReader(jarFile.getInputStream(entry)).accept(counter, 0);
                        
                        final String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');
                        try {
                            Reflection.withThread(thread, new Callable<Void>() {
                                @Override
                                public Void call() throws Exception {
                                    ClassMirror classMirror = vm.findBootstrapClassMirror(className);
                                    if (classMirror == null) {
                                        noBytecode++;
                                        return null;
                                    }
                                    MirageClassLoader loader = ClassHolograph.getMirageClassLoader(classMirror);
                                    loader.getMirageClass(classMirror, true);
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                            errors++;
                            e.printStackTrace();
                        }
                        
                        if (classes % 100 == 0) {
                            System.out.println("Processed " + classes);
                        }
                    }
                }
            }
        }
        
        StringBuilder missingMethods = new StringBuilder();
        StringBuilder unclassifiedMethods = new StringBuilder();
        for (Map.Entry<String, Set<MethodNode>> entry : counter.classesWithNativeMethods.entrySet()) {
            String fullName = entry.getKey().replace('/', '.');
            
            Class<?> stubsClass = ClassHolograph.getNativeStubsClass(fullName);
            Map<Method, java.lang.reflect.Method> stubsMethods = MirageClassGenerator.indexStubMethods(stubsClass);
            
            for (MethodNode method : entry.getValue()) {
                Method stubMethod = new Method(method.name, MirageClassGenerator.getStubMethodType(fullName, method.access, Type.getType(method.desc)).getDescriptor());
                if (stubsMethods.containsKey(stubMethod)) {
                    implemented++;
                    continue;
                }
                
                String message = ClassHolograph.getIllegalNativeMethodMessage(fullName, stubMethod);
                if (message != null) {
                    illegal++;
                    continue;
                }
                
                message = ClassHolograph.getMissingNativeMethodMessage(fullName, stubMethod);
                if (message != null) {
                    missing++;
                    missingMethods.append(fullName + '#' + method.name + method.desc + "\n");
                    continue;
                }
                
                unclassified++;
                unclassifiedMethods.append(fullName + '#' + method.name + method.desc + "\n");
            }
        }
        System.out.println("Classes: " + classes);
        System.out.println("Implemented: " + implemented);
        System.out.println("Missing: " + missing);
        System.out.println(missingMethods);
        System.out.println("Illegal: " + illegal);
        System.out.println("Unclassified: " + unclassified);
        System.out.println(unclassifiedMethods);
        
        System.out.println("No bytecode? " + noBytecode);
        System.out.println("Errors: " + errors);
        MirageClassLoader.printStats();
    }
    
    private static boolean testClass(MirageClassLoader loader, InputStream bytesIn, String className) {
        try {
            try {
                CheckClassAdapter.verify(new ClassReader(bytesIn), loader, false, new PrintWriter(System.out));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            loader.loadClass("mirage." + className).getMethods();
            return true;
        } catch (Throwable t) {
            System.out.println(className + " - " + t.getClass().getName() + ": " + t.getMessage());
        }
        return false;
    }
    
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
}
