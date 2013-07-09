package edu.ubc.mirrors.test;

import java.io.File;
import java.io.FileInputStream;
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
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.util.ConsoleProgressListener;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.fieldmap.CalculatedObjectArrayMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.MirageClassLoader;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.NativeClassMirror;

public class CDTBugTest implements IApplication {

    private static final String CPPASTName = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName";
    
    public static void main(String[] args) throws Exception {
        long before = System.currentTimeMillis();
        
        String snapshotPath = args[0];
        
        ISnapshot snapshot = SnapshotFactory.openSnapshot(
                new File(snapshotPath), 
                Collections.<String, String>emptyMap(), 
                new ConsoleProgressListener(System.out));

        final VirtualMachineHolograph holographVM = VirtualMachineHolograph.fromSnapshotWithIniFile(snapshot);
        
        Reflection.withThread(holographVM.getThreads().get(0), new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (MirageClassLoader.debug) {
                    System.out.println("Finding target class...");
                }
        	ClassMirror nameClass = holographVM.findAllClasses(CPPASTName, false).get(0);
        	if (MirageClassLoader.debug) {
                    System.out.println("Finding target instance...");
                }
        	analyse(nameClass);
                return null;
            }
	});
        System.out.println("Total time: " + (System.currentTimeMillis() - before));
        
    }
    
    public static void analyse(ClassMirror nameClass) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, MirrorInvocationTargetException, IOException {
        // Note we need a class loader that can see the classes in the OSGi API 
        // as well as our additional code (i.e. PrintOSGiBundles).
        VirtualMachineMirror vm = nameClass.getVM();
        // TODO-RS: Does this make sense?
        ThreadMirror thread = vm.getThreads().get(0);
        if (MirageClassLoader.debug) {
            System.out.println("Injecting bytecode...");
        }
        
        // select * from org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName n where toString(n.name) = "return_type_N_prot"
        String analyzerClassName = "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameDuplicateAnalysis";
        String path = "/Users/robinsalkeld/Documents/UBC/Code/org.eclipse.cdt.git/core/org.eclipse.cdt.core/bin/" +analyzerClassName.replace('.', '/') + ".class"; 
        String classPath = "/Users/robinsalkeld/Documents/UBC/Code/org.eclipse.cdt.git/core/org.eclipse.cdt.core/bin/org/eclipse/cdt/internal/core/dom/parser/cpp/CPPASTNameDuplicateAnalysis.class";
        FileInputStream fis = new FileInputStream(path);
        byte[] analyzerClassBytecode = NativeClassMirror.readFully(fis);
        fis.close();
        ClassMirror analyzerClass = Reflection.injectBytecode(vm, thread, nameClass.getLoader(), 
                analyzerClassName, analyzerClassBytecode);
        MethodMirror method = analyzerClass.getMethod("nameKey", nameClass);
        
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
    }
    
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
}

