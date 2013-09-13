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
package edu.ubc.mirrors.eclipse.mat.aseclipsejdi;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.util.ConsoleProgressListener;

import com.sun.jdi.connect.Connector;

import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;
import edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror;
import edu.ubc.mirrors.eclipse.mat.plugins.HolographVMRegistry;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;

public class HeapDumpHolographConnector implements IVMConnector {

    private class PathArgument implements Connector.StringArgument {

        private static final long serialVersionUID = 6669351099678827867L;

        private String value = "<...>";
        
        @Override
        public String description() {
            return "Path to *.hprof file.";
        }

        @Override
        public String label() {
            return "Heap dump file path";
        }

        @Override
        public boolean mustSpecify() {
            return true;
        }

        @Override
        public String name() {
            return "Path";
        }

        @Override
        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public boolean isValid(String value) {
            return true;
        }
        
    }
    
    @Override
    public void connect(Map arguments, IProgressMonitor monitor, final ILaunch launch) throws CoreException {
        String snapshotPath = (String)arguments.get("Path");
        ISnapshot snapshot;
        try {
            snapshot = SnapshotFactory.openSnapshot(
                    new File(snapshotPath), 
                    Collections.<String, String>emptyMap(), 
                    new ConsoleProgressListener(System.out));
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
        
        // Create an instance of the mirrors API backed by the snapshot
        HeapDumpVirtualMachineMirror vm = new HeapDumpVirtualMachineMirror(snapshot);
        
        // Create a holograph VM
        final VirtualMachineHolograph holographVM = new VirtualMachineHolograph(vm, 
                HeapDumpVirtualMachineMirror.defaultHolographicVMClassCacheDir(snapshot),
                Reflection.getStandardMappedFiles());
        
        // Finally, adapt to the JDI model
        ThreadMirror thread = holographVM.getThreads().get(0);
        MirrorsVirtualMachine jdiVM = new MirrorsVirtualMachine(holographVM);
        HolographVMRegistry.makeDebugTarget(thread, jdiVM, launch);
    }

    @Override
    public String getName() {
        return "Holographic Heap Dump Connect";
    }

    @Override
    public String getIdentifier() {
        return HeapDumpHolographConnector.class.getName();
    }

    @Override
    public Map<String, Connector.Argument> getDefaultArguments() throws CoreException {
        return Collections.<String, Connector.Argument>singletonMap("Path", new PathArgument());
    }

    @Override
    public List<String> getArgumentOrder() {
        return Collections.singletonList("Path");
    }

}
