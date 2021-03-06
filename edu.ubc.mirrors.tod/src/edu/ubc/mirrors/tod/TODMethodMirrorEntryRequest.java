package edu.ubc.mirrors.tod;

import java.util.List;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryRequest;

public class TODMethodMirrorEntryRequest extends TODBehaviorEventRequest implements MethodMirrorEntryRequest {

    public TODMethodMirrorEntryRequest(TODVirtualMachineMirror vm) {
        super(vm);
    }

    @Override
    protected boolean matchConstructors() {
        return false;
    }
    
    @Override
    protected boolean matchExits() {
        return false;
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
