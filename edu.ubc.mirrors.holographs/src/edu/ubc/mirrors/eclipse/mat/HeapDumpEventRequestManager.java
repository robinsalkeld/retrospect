package edu.ubc.mirrors.eclipse.mat;

import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorPrepareRequest;
import edu.ubc.mirrors.ConstructorMirrorEntryRequest;
import edu.ubc.mirrors.ConstructorMirrorExitRequest;
import edu.ubc.mirrors.FieldMirrorSetRequest;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.ThreadMirrorDeathRequest;

public class HeapDumpEventRequestManager implements MirrorEventRequestManager {

    private final HeapDumpVirtualMachineMirror vm;
    
    public HeapDumpEventRequestManager(HeapDumpVirtualMachineMirror vm) {
        this.vm = vm;
    }

    @Override
    public MethodMirrorEntryRequest createMethodMirrorEntryRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MethodMirrorEntryRequest> methodMirrorEntryRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public MethodMirrorExitRequest createMethodMirrorExitRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<MethodMirrorExitRequest> methodMirrorExitRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConstructorMirrorEntryRequest createConstructorMirrorEntryRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ConstructorMirrorEntryRequest> constructorMirrorEntryRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConstructorMirrorExitRequest createConstructorMirrorExitRequest() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<ConstructorMirrorExitRequest> constructorMirrorExitRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public FieldMirrorSetRequest createFieldMirrorSetRequest(ClassMirror klass,
            String fieldName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<FieldMirrorSetRequest> fieldMirrorSetRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ClassMirrorPrepareRequest createClassMirrorPrepareRequest() {
        return new HeapDumpClassMirrorPrepareRequest(vm);
    }

    @Override
    public List<ClassMirrorPrepareRequest> classMirrorPrepareRequests() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ThreadMirrorDeathRequest createThreadMirrorDeathRequest() {
        return new HeapDumpThreadMirrorDeathRequest(vm);
    }
    
    @Override
    public void deleteMirrorEventRequest(MirrorEventRequest request) {
        // TODO Auto-generated method stub
        
    }

}
