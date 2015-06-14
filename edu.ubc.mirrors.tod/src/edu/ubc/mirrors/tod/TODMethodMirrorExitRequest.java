package edu.ubc.mirrors.tod;

import java.util.List;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorExitRequest;

public class TODMethodMirrorExitRequest extends TODBehaviorEventRequest implements MethodMirrorExitRequest {

    public TODMethodMirrorExitRequest(TODVirtualMachineMirror vm) {
        super(vm);
    }
    
    @Override
    protected boolean matchConstructors() {
        return false;
    }
    
    @Override
    protected boolean matchExits() {
        return true;
    }
    
    @Override
    public void setMethodFilter(String declaringClass, String name, List<String> parameterTypeNames) {
        TODClassMirror klass = vm.findBootstrapClassMirror(declaringClass);
        MethodMirror method;
        try {
            method = klass.getDeclaredMethod2(name, parameterTypeNames.toArray(new String[parameterTypeNames.size()]));
            this.behaviorInfo = ((TODMethodOrConstructorMirror)method).behaviourInfo;
        } catch (NoSuchMethodException e) {
            // Method never encountered in trace, so make it return no events
            this.empty = true;
        }
    }
}
