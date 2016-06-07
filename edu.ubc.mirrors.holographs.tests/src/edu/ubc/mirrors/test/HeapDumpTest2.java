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
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.util.ConsoleProgressListener;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.raw.NativeClassMirror;

public class HeapDumpTest2 implements IApplication {

    public static void main(String[] args) throws Exception {
        String snapshotPath = args[0];
        
        // Open memory snapshot and find the Bundle class
        ISnapshot snapshot = SnapshotFactory.openSnapshot(
                new File(snapshotPath), 
                Collections.<String, String>emptyMap(), 
                new ConsoleProgressListener(System.out));
        printJRubyThreadsFromSnapshot(snapshot);
    }
    
  public static void printJRubyThreadsFromSnapshot(ISnapshot snapshot) throws Exception {

    final VirtualMachineHolograph holographVM = HeapDumpVirtualMachineMirror.holographicVMWithIniFile(snapshot);
    
    Reflection.withThread(holographVM.getThreads().get(0), new Callable<Void>() {
        @Override
        public Void call() throws Exception {
    
            // Create a new class loader in the holograph VM and define more bytecode.
            ClassMirror rubyClass = holographVM.findAllClasses("org.jruby.Ruby", false).get(0);
            ThreadMirror thread = holographVM.getThreads().get(0);
            NativeClassMirror nativePrinterClass = new NativeClassMirror(JRubyStackTraces.class);
            ClassMirror printerClass = Reflection.injectBytecode(holographVM, thread, 
        	    rubyClass.getLoader(), nativePrinterClass.getClassName(), nativePrinterClass.getBytecode());

            // Redirect standard out
            InstanceMirror baos = (InstanceMirror)printerClass.getDeclaredMethod("redirectStdErr").invoke(thread, null);

            // For each class instance (in this case we only expect one)...
            MethodMirror method = printerClass.getDeclaredMethod("printStackTraces", rubyClass.getClassName());
            for (ObjectMirror ruby : rubyClass.getInstances()) {
        	// Invoke JRubyStackTraces#printStackTraces reflectively.
        	method.invoke(thread, null, ruby);
        	System.out.println(Reflection.toString(baos, thread));
            }
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
