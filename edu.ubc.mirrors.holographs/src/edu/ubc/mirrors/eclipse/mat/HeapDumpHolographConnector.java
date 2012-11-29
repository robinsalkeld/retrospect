package edu.ubc.mirrors.eclipse.mat;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.launching.IVMConnector;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.util.ConsoleProgressListener;

import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;

import edu.ubc.mirrors.asjdi.MirrorsVirtualMachine;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.Reflection;

public class HeapDumpHolographConnector implements IVMConnector {

    private class PathArgument implements Connector.StringArgument {

        private static final long serialVersionUID = 6669351099678827867L;

        private String value;
        
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
    public void connect(Map arguments, IProgressMonitor monitor, ILaunch launch) throws CoreException {
        String snapshotPath = ((Connector.StringArgument)arguments.get("Path")).value();
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
                Reflection.getBootstrapPath(),
                Reflection.getStandardMappedFiles());
        
        // Finally, adapt to the JDI model
        VirtualMachine jdiVM = new MirrorsVirtualMachine(holographVM);
        IDebugTarget debugTarget= JDIDebugModel.newDebugTarget(launch, jdiVM, jdiVM.name(), null, true, true);
        launch.addDebugTarget(debugTarget);
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
    public Map getDefaultArguments() throws CoreException {
        return Collections.singletonMap("Path", new PathArgument());
    }

    @Override
    public List getArgumentOrder() {
        return Collections.singletonList("Path");
    }

}
