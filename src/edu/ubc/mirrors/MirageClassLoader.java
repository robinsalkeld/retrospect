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
        
        public MirageClassLoader(ClassLoader originalLoader, String traceDir) {
            super(originalLoader);
            this.traceDir = traceDir;
            
            if (traceDir != null) {
                for (File f : new File(traceDir).listFiles()) {
                    f.delete();
                }
            }
        }
        
        private final String traceDir;
        
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
                    NativeObjectMirror.class,
                    Class.class, Throwable.class, String.class);
        }
        
//        @Override
//        protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//            Class<?> c = COMMON_CLASSES.get(name);
//            if (c != null) {
//                return c;
//            }
//            
//            String mirageClassName = MirageClassGenerator.getMirageBinaryClassName(name);
//            
//            // First, check if the class has already been loaded
//            c = findLoadedClass(mirageClassName);
//            if (c == null) {
//                // If still not found, then invoke findClass in order
//                // to find the class.
//                c = findClass(name);
//            }
//            if (resolve) {
//                resolveClass(c);
//            }
//            return c;
//        }
        
        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            // If the class was loaded before the instrumentation agent was set up,
            // try accessing the .class file as a resource from the system class loader
            // (which is exactly what the ClassReader(String) constructor does).
            ClassReader readerFromClassFile;
            String originalName = MirageClassGenerator.getOriginalClassName(name);
            try {
                readerFromClassFile = new ClassReader(originalName);
            } catch (IOException e) {
                throw new ClassNotFoundException("Class not trapped by agent and .class file not accessible: " + originalName, e);
            }
            
            return defineMirageClass(name, readerFromClassFile);
        }
        
        public Class<?> defineMirageClass(String name, ClassReader originalReader) {
            String mirageClassName = MirageClassGenerator.getMirageBinaryClassName(name);
            byte[] b = MirageClassGenerator.generate(mirageClassName, originalReader, traceDir);
            if (traceDir != null && name.equals("mirage.examples.MirageTest")) {
                String fileName = traceDir + mirageClassName + ".class";
                try {
                    CheckClassAdapter.verify(new ClassReader(new FileInputStream(fileName)), this, true, new PrintWriter(System.out));
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
    }