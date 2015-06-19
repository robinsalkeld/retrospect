package edu.ubc.mirrors.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Collections;
import java.util.concurrent.Callable;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorSetHandlerRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.retrospect.MirrorWorld;

public class RetroactiveWeaving {

    public static String weave(VirtualMachineMirror vm, ThreadMirror thread, File aspectPath, File hologramClassPath) throws Exception {
        System.out.println("Booting up holographic VM...");
        ByteArrayOutputStream mergedOutput = new ByteArrayOutputStream();
        OutputStream teedOut = new TeeOutputStream(mergedOutput, System.out);
        OutputStream teedErr = new TeeOutputStream(mergedOutput, System.err);
        
        final VirtualMachineHolograph vmh = new VirtualMachineHolograph(vm, hologramClassPath,
                Collections.singletonMap("/", "/"));
        vmh.setSystemOut(teedOut);
        vmh.setSystemErr(teedErr);
        final ThreadMirror finalThread = (ThreadMirror)vmh.getWrappedMirror(thread);

        avoidStringHashSets(vmh);
        
        vmh.addBootstrapPathURL(MirrorWorld.aspectRuntimeJar);
        vmh.addBootstrapPathURL(EvalConstants.GuardAspectsBin.toURI().toURL());
        vmh.addBootstrapPathURL(aspectPath.toURI().toURL());
        
        MirrorWorld world = new MirrorWorld(finalThread, null);
        world.weave();
        
        vmh.resume();
        vmh.dispatch().run();
        
//        return mergedOutput.toString();
        return Reflection.withThread(thread, new Callable<String>() {
          @Override
          public String call() throws Exception {
              ClassMirror guardAspect = vmh.findBootstrapClassMirror("edu.ubc.aspects.JDKAroundFieldSets");
              ObjectMirror newOut = guardAspect.getStaticFieldValues().get(guardAspect.getDeclaredField("newStderrBaos"));
              String output = Reflection.toString(newOut, finalThread);
              System.out.print(output);
              return output;
          }
      });
    }
    
    
    public static void avoidStringHashSets(VirtualMachineMirror vm) {
        ClassMirror stringClass = vm.findBootstrapClassMirror(String.class.getName());
        FieldMirror hashField = stringClass.getDeclaredField("hash");
        FieldMirrorSetHandlerRequest request = vm.eventRequestManager().createFieldMirrorSetHandlerRequest(hashField);
        vm.dispatch().addCallback(request, new Callback<MirrorEvent>() {
            @Override
            public MirrorEvent handle(MirrorEvent t) {
                t.setProceed(MirrorInvocationHandler.NONE);
                return t;
            }
        });
        request.enable();
    }
}
