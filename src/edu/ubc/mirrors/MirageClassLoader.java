package edu.ubc.mirrors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;

public class MirageClassLoader extends ClassLoader {
        
        public MirageClassLoader(ClassLoader originalLoader, String mirageTraceDir, String nativeTraceDir) {
            super(originalLoader);
            this.mirageTraceDir = mirageTraceDir;
            this.nativeTraceDir = nativeTraceDir;
            
            if (mirageTraceDir != null) {
                for (File f : new File(mirageTraceDir).listFiles()) {
                    f.delete();
                }
            }
            if (nativeTraceDir != null) {
                for (File f : new File(nativeTraceDir).listFiles()) {
                    f.delete();
                }
            }
        }
        
        private final String mirageTraceDir;
        private final String nativeTraceDir;
        
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
                    NativeObjectMirror.class, FieldMapMirror.class,
                    Class.class, Throwable.class, String.class);
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
            byte[] b = MirageClassGenerator.generate(mirageClassName, originalReader, mirageTraceDir);
            if (mirageTraceDir != null) {
                String fileName = mirageTraceDir + mirageClassName + ".class";
                try {
                    CheckClassAdapter.verify(new ClassReader(new FileInputStream(fileName)), this, false, new PrintWriter(System.out));
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return defineClass(mirageClassName, b, 0, b.length);
        }
        
        public Class<?> defineNativeClass(String name, ClassReader originalReader) {
            byte[] b = NativeClassGenerator.generate(name, originalReader, nativeTraceDir);
            return defineClass(name, b, 0, b.length);
        }
    }