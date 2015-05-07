package edu.ubc.mirrors.test;

import java.util.List;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorHandlerEvent;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;

public class ThesisExamples {

    public static void main() throws SecurityException, NoSuchMethodException {
        
        VirtualMachineMirror vmm = null;
        ClassMirror stringClass = vmm.findBootstrapClassMirror("java.lang.String");
        MethodMirror hashCodeMethod = stringClass.getDeclaredMethod("hashCode");

        final MirrorInvocationHandler newVersion = new MirrorInvocationHandler() {
            public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                return 47;
            }
        };
        
        MethodMirrorHandlerRequest request = vmm.eventRequestManager().createMethodMirrorHandlerRequest();
        request.setMethodFilter(hashCodeMethod);
        Callback<MirrorEvent> callback = new Callback<MirrorEvent>() {
            public MirrorEvent handle(MirrorEvent event) {
                MethodMirrorHandlerEvent methodHandlerEvent = (MethodMirrorHandlerEvent)event;
                methodHandlerEvent.setProceed(newVersion);
                return methodHandlerEvent;
            }
        };
        vmm.addCallback(request, callback);
        
    }
    
}
