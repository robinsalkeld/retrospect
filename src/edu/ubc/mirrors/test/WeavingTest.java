package edu.ubc.mirrors.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import org.aspectj.weaver.loadtime.definition.Definition;
import org.aspectj.weaver.loadtime.definition.DocumentParser;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

public class WeavingTest {

    public static void main(String[] args) throws MalformedURLException, Exception {
        File file = new File("/Users/robinsalkeld/Documents/workspace/Tracing Example/bin/META-INF/aop-ajc.xml");
        Definition aop = DocumentParser.parse(file.toURL());
        aop.getAspectClassNames();
        
        ClassReader reader = new ClassReader(new FileInputStream("/Users/robinsalkeld/Documents/workspace/Tracing Example/bin/tracing/version3/TraceMyClasses.class"));
        PrintWriter textFileWriter = new PrintWriter(System.out);
        ClassVisitor visitor = new TraceClassVisitor(null, textFileWriter);
        reader.accept(visitor, 0);
    }
    
}
