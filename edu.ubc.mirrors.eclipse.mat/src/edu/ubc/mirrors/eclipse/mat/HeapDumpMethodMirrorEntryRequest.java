package edu.ubc.mirrors.eclipse.mat;

import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirrorEntryRequest;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirrorEventRequest;

public class HeapDumpMethodMirrorEntryRequest extends FieldMapMirrorEventRequest implements MethodMirrorEntryRequest {

    public HeapDumpMethodMirrorEntryRequest(VirtualMachineMirror vm) {
        super(vm);
    }

    @Override
    public void addClassFilter(ClassMirror klass) {
        // No-op
    }

    @Override
    public void setMethodFilter(String declaringClass, String name, List<String> paramterTypeNames) {
        // No-op
    }
}