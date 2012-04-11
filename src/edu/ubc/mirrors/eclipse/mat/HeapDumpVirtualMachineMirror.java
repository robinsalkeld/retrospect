package edu.ubc.mirrors.eclipse.mat;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.model.IClass;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class HeapDumpVirtualMachineMirror implements VirtualMachineMirror {

    private final ISnapshot snapshot;
    
    public HeapDumpVirtualMachineMirror(ISnapshot snapshot) {
        this.snapshot = snapshot;
    }
    
    private Map<String, HeapDumpClassMirror> bootstrapClasses = 
            new HashMap<String, HeapDumpClassMirror>();
     
    private void initBootstrapClasses() {
        if (bootstrapClasses == null) {
            bootstrapClasses = new HashMap<String, HeapDumpClassMirror>();
            try {
                for (IClass c : snapshot.getClasses()) {
                    if (c.getClassLoaderId() == 0) {
                        bootstrapClasses.put(c.getName(), new HeapDumpClassMirror(null, c));
                    }
                }
            } catch (SnapshotException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    @Override
    public ClassMirror findBootstrapClassMirror(String name) {
        initBootstrapClasses();
        return bootstrapClasses.get(name);
    }
    
    
}
