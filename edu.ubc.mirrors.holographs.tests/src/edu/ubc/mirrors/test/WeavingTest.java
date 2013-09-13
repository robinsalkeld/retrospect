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
