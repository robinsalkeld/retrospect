package edu.ubc.mirrors.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.util.ConsoleProgressListener;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.fieldmap.CalculatedObjectArrayMirror;
import edu.ubc.mirrors.holograms.HologramClassLoader;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.raw.NativeClassMirror;

public class CDTBugTest implements IApplication {

    public static final String CPPASTName = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName";
    
    public static void main(String[] args) throws Exception {
        long before = System.currentTimeMillis();
        
        String snapshotPath = args[0];
        MethodMirror method = getNameKeyMethod(snapshotPath);
    	analyse(method);
        System.out.println("Total time: " + (System.currentTimeMillis() - before));
        
    }
    
    public static MethodMirror getNameKeyMethod(String snapshotPath) throws IOException, SecurityException, NoSuchMethodException, SnapshotException {
        ISnapshot snapshot = SnapshotFactory.openSnapshot(
                new File(snapshotPath), 
                Collections.<String, String>emptyMap(), 
                new ConsoleProgressListener(System.out));

        final VirtualMachineHolograph holographVM = HeapDumpVirtualMachineMirror.holographicVMWithIniFile(snapshot);
        
        if (HologramClassLoader.debug) {
            System.out.println("Finding target class...");
        }
        final ClassMirror nameClass = holographVM.findAllClasses(CPPASTName, false).get(0);
        if (HologramClassLoader.debug) {
            System.out.println("Finding target instance...");
        }
        VirtualMachineMirror vm = nameClass.getVM();
        ThreadMirror thread = vm.getThreads().get(0);
        if (HologramClassLoader.debug) {
            System.out.println("Injecting bytecode...");
        }
        
        // select * from org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName n where toString(n.name) = "return_type_N_prot"
        String analyzerClassName = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameDuplicateAnalysis";
        String path = "/Users/robinsalkeld/Documents/UBC/Code/org.eclipse.cdt.git/core/org.eclipse.cdt.core/bin/" +analyzerClassName.replace('.', '/') + ".class"; 
        String classPath = "/Users/robinsalkeld/Documents/UBC/Code/org.eclipse.cdt.git/core/org.eclipse.cdt.core/bin/org/eclipse/cdt/internal/core/dom/parser/cpp/CPPASTNameDuplicateAnalysis.class";
        FileInputStream fis = new FileInputStream(path);
        byte[] analyzerClassBytecode = NativeClassMirror.readFully(fis);
        fis.close();
        final ClassMirror analyzerClass = Reflection.injectBytecode(vm, thread, nameClass.getLoader(), 
                analyzerClassName, analyzerClassBytecode);
        return Reflection.withThread(thread, new Callable<MethodMirror>() {
            public MethodMirror call() throws Exception {
               return analyzerClass.getDeclaredMethod("nameWithLocation", nameClass);
            } 
        });
    }
    
    public static void analyse(final MethodMirror method) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, MirrorInvocationTargetException, IOException {
        final ThreadMirror thread = method.getDeclaringClass().getVM().getThreads().get(0);
        Reflection.withThread(thread, new Callable<Void>() {
            public Void call() throws Exception {
        
                ClassMirror nameClass = method.getParameterTypes().get(0);
                final List<ObjectMirror> instances = nameClass.getInstances();
                
                System.out.println();
                System.out.println("***");
                System.out.println("*** Starting analysis...");
                System.out.println("***");
                System.out.println();
                
                int count = 0;
                int blankCount = 0;
                SortedMap<String, Integer> nameCounts = new TreeMap<String, Integer>();
                String blankKey = " - []";
                for (ObjectMirror name : instances) {
                    String nameString = Reflection.getRealStringForMirror((InstanceMirror)method.invoke(thread, null, name));
        //            Integer nameCount = nameCounts.get(nameString);
        //            if (nameCount == null) {
        //                    nameCount = 0;
        //            }
        //            nameCounts.put(nameString, nameCount + 1);
        //            if (nameString.equals(blankKey)) {
        //                    blankCount++;
        //            }
                    if (++count % 10 == 0) {
                            System.out.print(".");
                    }
                    if (count % 1000 == 0) {
                            System.out.println(count);
                    }
                }
                
                System.out.println();
                System.out.println("***");
                System.out.println("*** Finished analysis.");
                System.out.println("***");
                System.out.println();
                
                // Print out any duplicates
                int dupCount = 0;
                for (Map.Entry<String, Integer> entry : nameCounts.entrySet()) {
                        String key = entry.getKey();
                        int nameCount = entry.getValue();
                        if (nameCount > 1) {
                                System.out.println(key + ": " + nameCount);
                                dupCount += nameCount;
                        }
                }
                System.out.println("Total duplicates: " + dupCount);
                return null;
            }
        });
    }
    
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
}

