package edu.ubc.mirrors.eclipse.mat;

import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirrorExitRequest;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.fieldmap.FieldMapMirrorEventRequest;

public class HeapDumpMethodMirrorExitRequest extends FieldMapMirrorEventRequest implements MethodMirrorExitRequest {

    public HeapDumpMethodMirrorExitRequest(VirtualMachineMirror vm) {
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