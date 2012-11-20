package edu.ubc.mirrors.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import org.aspectj.lang.annotation.Before;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

public class WeavingTest {

    public static void main(String[] args) throws MalformedURLException, Exception {
//        File file = new File("/Users/robinsalkeld/Documents/workspace/Tracing Example/bin/META-INF/aop-ajc.xml");
//        Definition aop = DocumentParser.parse(file.toURL());
//        aop.getAspectClassNames();
        
        File binDir = new File("/Users/robinsalkeld/Documents/UBC/Code/Tracing Example Aspects/bin");
        
        ClassReader reader = new ClassReader(new FileInputStream(binDir + "/tracing/version3/TraceMyClasses.class"));
        PrintWriter textFileWriter = new PrintWriter(System.out);
        ClassVisitor visitor = new TraceClassVisitor(null, textFileWriter);
        reader.accept(visitor, 0);
        
        URL urlPath = binDir.toURI().toURL();
        ClassLoader loader = new URLClassLoader(new URL[] {urlPath} );
        Class<?> klass = loader.loadClass("tracing.version3.TraceMyClasses");
        for (Method method : klass.getMethods()) {
            for (Annotation a : method.getAnnotations()) {
                System.out.println(a);
            }
        }
    }
    
}
