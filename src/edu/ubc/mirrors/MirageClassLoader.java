package edu.ubc.mirrors;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.verifier.Verifier;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

public class MirageClassLoader extends ClassLoader {
        
    public static String traceClass = null;
    public static String traceDir = null;
    public final File myTraceDir;
    
    public MirageClassLoader(ClassLoader originalLoader) {
        super(originalLoader);
        if (traceDir != null) {
            myTraceDir = new File(traceDir, hashCode() + "/");
            myTraceDir.mkdir();
            for (File f : myTraceDir.listFiles()) {
                f.delete();
            }
        } else {
            myTraceDir = null;
        }
    }
    
    public static void setTraceDir(String newTraceDir) {
        traceDir = newTraceDir;
        for (File dir : new File(traceDir + "/").listFiles()) {
            for (File f : dir.listFiles()) {
                f.delete();
            }
            dir.delete();
        }
        
    }
    
    public ClassLoader getOriginalLoader() {
        return getParent();
    }
    
    public static final Map<String, Class<?>> COMMON_CLASSES = new HashMap<String, Class<?>>();
    private static void registerCommonClasses(Class<?> ... classes) {
        for (Class<?> c : classes) {
            COMMON_CLASSES.put(c.getName(), c);
        }
    }
    
    static {
        registerCommonClasses(Object.class, ObjectMirage.class, ObjectMirror.class, FieldMirror.class,
                FieldMapMirror.class,
                Class.class, 
                // Necessary because only subclasses of this can be thrown.
                // Probably need to introduce a new root subclass as with ObjectMirage.
                Throwable.class, 
                // Not sure about this one...
                String.class,
                // Definitely wrong, but see if this gets us going on real examples for now
                System.class);
    }
    
    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = COMMON_CLASSES.get(name);
        if (c != null) {
            return c;
        } else {
            return super.loadClass(name, resolve);
        }
    }
    
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        // If the class was loaded before the instrumentation agent was set up,
        // try accessing the .class file as a resource from the system class loader
        // (which is exactly what the ClassReader(String) constructor does).
        if (name.startsWith("mirage")) {
            ClassReader readerFromClassFile;
            String originalName = MirageClassGenerator.getOriginalClassName(name);
            try {
                readerFromClassFile = new ClassReader(originalName);
            } catch (IOException e) {
                throw new ClassNotFoundException("Class not trapped by agent and .class file not accessible: " + originalName, e);
            }
            
            return defineMirageClass(name, readerFromClassFile);
        } else if (name.startsWith("native")) {
            ClassReader readerFromClassFile;
            String originalName = NativeClassGenerator.getOriginalClassName(name);
            try {
                readerFromClassFile = new ClassReader(originalName);
            } catch (IOException e) {
                throw new ClassNotFoundException("Class not trapped by agent and .class file not accessible: " + originalName, e);
            }
            
            return defineNativeClass(name, readerFromClassFile);
        } else {
            throw new ClassNotFoundException();
        }
    }
    
    public Class<?> defineMirageClass(String name, ClassReader originalReader) {
        String mirageClassName = MirageClassGenerator.getMirageBinaryClassName(name);
        byte[] b = MirageClassGenerator.generate(mirageClassName, originalReader);
        
        ByteArrayInputStream is = new ByteArrayInputStream(b);
        try {
            JavaClass javaClass = new ClassParser(is, "").parse();
            Repository.addClass(javaClass);
        } catch (ClassFormatException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        if (myTraceDir != null) {
            File file = new File(myTraceDir, mirageClassName + ".class");
            try {
                OutputStream classFile = new FileOutputStream(file);
                classFile.write(b);
                classFile.flush();
                classFile.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            
            if (name.equals(traceClass)) {
                try {
                    CheckClassAdapter.verify(new ClassReader(new FileInputStream(file)), this, false, new PrintWriter(System.out));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
//                Verifier.main(new String[] {name});
            }
        }
        
        return defineClass(mirageClassName, b, 0, b.length);
    }
    
    public Class<?> defineNativeClass(String name, ClassReader originalReader) {
        byte[] b = NativeClassGenerator.generate(name, originalReader);
        
        if (myTraceDir != null) {
            try {
                File file = new File(myTraceDir, name + ".class");
                OutputStream classFile = new FileOutputStream(file);
                classFile.write(b);
                classFile.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        
        return defineClass(name, b, 0, b.length);
    }
}