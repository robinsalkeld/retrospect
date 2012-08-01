package edu.ubc.mirrors.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.mirages.MirageClassGenerator;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.MirageClassMirrorLoader;
import edu.ubc.mirrors.raw.NativeClassMirror;
import edu.ubc.mirrors.raw.NativeClassMirrorLoader;

public class JarVerifier implements IApplication {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
//        String jarPath = args[0];
//
//        JarFile jar = new JarFile(jarPath);
//        ClassLoader thisLoader = JarVerifier.class.getClassLoader();
//        MirageClassLoader.traceDir = new File(System.getProperty("edu.ubc.mirrors.mirages.tracepath"));
//        MirageClassLoader mirageLoader = new MirageClassLoader(null, new NativeClassMirrorLoader(thisLoader));
        new JarVerifier().verifyJars(null);
    }
    
    int unclassified = 0;
    int missing = 0;
    int illegal = 0;
    int implemented = 0;
    
    public void verifyJars(MirageClassLoader loader) throws IOException {
        String bootPath = (String)System.getProperties().get("sun.boot.class.path");
        String[] paths = bootPath.split(File.pathSeparator);
        NativeMethodCounter counter = new NativeMethodCounter();
        for (String path : paths) {
            if (new File(path).exists()) {
                JarFile jarFile = new JarFile(path);
                for (JarEntry entry : Collections.list(jarFile.entries())) {
                    String name = entry.getName();
                    if (name.endsWith(".class")) {
                        new ClassReader(jarFile.getInputStream(entry)).accept(counter, 0);
        //                String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');
        //                if (testClass(loader, jar.getInputStream(entry), className)) good++;
                    }
                }
            }
        }
        
        StringBuilder missingMethods = new StringBuilder();
        StringBuilder unclassifiedMethods = new StringBuilder();
        for (Map.Entry<String, Set<MethodNode>> entry : counter.classesWithNativeMethods.entrySet()) {
            String fullName = entry.getKey().replace('/', '.');
            int lastDot = fullName.lastIndexOf('.');
            String packageName = fullName.substring(0, lastDot);
            String className = fullName.substring(lastDot + 1);
            
            Class<?> stubsClass = ClassHolograph.getNativeStubsClass(fullName);
            Map<Method, java.lang.reflect.Method> stubsMethods = MirageClassGenerator.indexStubMethods(stubsClass);
            
            for (MethodNode method : entry.getValue()) {
                Method stubMethod = new Method(method.name, MirageClassGenerator.getStubMethodType(fullName, method.access, Type.getType(method.desc)).getDescriptor());
                String classification;
                if (stubsMethods.containsKey(stubMethod)) {
                    classification = "<Implemented>";
                    implemented++;
                    continue;
                }
                
                String message = ClassHolograph.getIllegalNativeMethodMessage(fullName, stubMethod);
                if (message != null) {
                    classification = "Illegal: " + message;
                    illegal++;
                    continue;
                }
                
                message = ClassHolograph.getMissingNativeMethodMessage(fullName, stubMethod);
                if (message != null) {
                    classification = "Missing: " + message;
                    missing++;
                    missingMethods.append(fullName + '#' + method.name + "\n");
                    continue;
                }
                
                classification = "???????";
                unclassified++;
                unclassifiedMethods.append(fullName + '#' + method.name + "\n");
            }
        }
        System.out.println("Implemented: " + implemented);
        System.out.println("Missing: " + missing);
        System.out.println(missingMethods);
        System.out.println("Illegal: " + illegal);
        System.out.println("Unclassified: " + unclassified);
        System.out.println(unclassifiedMethods);
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
