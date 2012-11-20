package edu.ubc.mirrors.raw;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Type;

import edu.ubc.mirrors.ByteArrayMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MirrorEventQueue;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class BytecodeOnlyVirtualMachineMirror implements VirtualMachineMirror {

    private final VirtualMachineMirror baseVM;
    
    // May be null
    private final ClassLoader bootstrapLoader;
    
    private final Map<String, ClassMirror> bootstrapClasses = new HashMap<String, ClassMirror>();
    
    public BytecodeOnlyVirtualMachineMirror(VirtualMachineMirror baseVM, ClassLoader bootstrapLoader) {
        this.baseVM = baseVM;
        this.bootstrapLoader = bootstrapLoader;
    }
    
    @Override
    public ClassMirror findBootstrapClassMirror(String name) {
        ClassMirror result = bootstrapClasses.get(name);
        if (result != null) {
            return result;
        }
        
        // Note using null as the ClassMirrorLoader since the bootstrapLoader acts like the
        // VM's bootstrap loader, which is sometimes (and in this case) represented by null.
        result = BytecodeClassMirrorLoader.loadBytecodeClassMirror(this, null, bootstrapLoader, Type.getObjectType(name.replace('.', '/')));
        bootstrapClasses.put(name, result);
        return result;
    }
    
    @Override
    public ClassMirror defineBootstrapClass(String name, ByteArrayMirror b, int off, int len) {
        throw new UnsupportedOperationException(); 
    }
    
    @Override
    public List<ClassMirror> findAllClasses(String name, boolean includeSubclasses) {
        throw new UnsupportedOperationException(); 
    }    
    
    @Override
    public List<ThreadMirror> getThreads() {
        throw new UnsupportedOperationException(); 
    }
    
    @Override
    public ClassMirror getPrimitiveClass(String name) {
        return baseVM.getPrimitiveClass(name);
    }
    
    @Override
    public ClassMirror getArrayClass(int dimensions, ClassMirror elementClass) {
        return baseVM.getArrayClass(dimensions, elementClass);
    }

    @Override
    public MirrorEventRequestManager eventRequestManager() {
	throw new UnsupportedOperationException(); 
    }
    
    @Override
    public MirrorEventQueue eventQueue() {
	throw new UnsupportedOperationException(); 
    }

    @Override
    public void resume() {
	throw new UnsupportedOperationException(); 
    }
    
    @Override
    public void suspend() {
	throw new UnsupportedOperationException(); 
    }
    
    @Override
    public boolean canBeModified() {
        return false;
    }
}
