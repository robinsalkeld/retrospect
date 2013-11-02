/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
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
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holograms.HologramClassLoader;
import edu.ubc.mirrors.holographs.ClassHolograph;
import edu.ubc.mirrors.holographs.MirrorInvocationHandler;
import edu.ubc.mirrors.holographs.MirrorInvocationHandlerProvider;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.holographs.jdkplugins.JDKNativeStubsProvider;

public class JarVerifier implements IApplication {
    public static void main(String[] args) throws Exception {
        String snapshotPath = args[0];
        
        ISnapshot snapshot = SnapshotFactory.openSnapshot(
                new File(snapshotPath), 
                Collections.<String, String>emptyMap(), 
                new ConsoleProgressListener(System.out));
        
        VirtualMachineHolograph holographVM = HeapDumpVirtualMachineMirror.holographicVMWithIniFile(snapshot);
        
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
                try {
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
                                        HologramClassLoader loader = ClassHolograph.getHologramClassLoader(classMirror);
                                        loader.getHologramClass(classMirror, true);
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
                } finally {
                    jarFile.close();
                }
            }
        }
        
        StringBuilder missingMethods = new StringBuilder();
        StringBuilder unclassifiedMethods = new StringBuilder();
        MirrorInvocationHandlerProvider provider = new JDKNativeStubsProvider();
        for (Map.Entry<String, Set<MethodNode>> entry : counter.classesWithNativeMethods.entrySet()) {
            String fullInternalName = entry.getKey();
            String fullName = fullInternalName.replace('/', '.');
            ClassMirror klass = vm.findAllClasses(fullName, false).get(0);
            
            for (MethodNode method : entry.getValue()) {
                MethodMirror methodMirror = Reflection.getDeclaredMethod(thread, klass, method.name, Type.getType(method.desc));
                
                MirrorInvocationHandler handler = provider.getInvocationHandler(methodMirror);
                if (handler != null) {
                    implemented++;
                    continue;
                }
                
                String message = ClassHolograph.getIllegalNativeMethodMessage(fullName, methodMirror);
                if (message != null) {
                    illegal++;
                    continue;
                }
                
                message = ClassHolograph.getMissingNativeMethodMessage(fullName, methodMirror);
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
        HologramClassLoader.printStats();
    }
    
    private static boolean testClass(HologramClassLoader loader, InputStream bytesIn, String className) {
        try {
            try {
                CheckClassAdapter.verify(new ClassReader(bytesIn), loader, false, new PrintWriter(System.out));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            loader.loadClass("hologram." + className).getMethods();
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
