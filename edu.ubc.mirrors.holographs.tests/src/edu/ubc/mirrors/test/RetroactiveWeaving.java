package edu.ubc.mirrors.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorGetHandlerRequest;
import edu.ubc.mirrors.FieldMirrorSetHandlerRequest;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorHandlerRequest;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;
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

        relocateField(vmh, "java.lang.String", "hash");
        relocateField(vmh, "java.util.zip.ZipCoder", "enc");
        relocateFieldInitializeWithDefaultConstructor(vmh, "sun.nio.cs.ThreadLocalCoders$Cache", "cache");
        relocateFieldInitializeWithDefaultConstructor(vmh, "java.lang.ThreadLocal", "nextHashCode");
        aroundThreadLocals(vmh);
        hardCodeHashing(vmh);
        
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
    
    private static void relocateField(VirtualMachineMirror vm, String className, String fieldName) {
        relocateField(vm, className, fieldName, null);
    }
    
    private static void relocateFieldInitializeWithDefaultConstructor(VirtualMachineMirror vm, String className, String fieldName) throws SecurityException, NoSuchMethodException {
        ClassMirror klass = vm.findBootstrapClassMirror(className);
        FieldMirror field = klass.getDeclaredField(fieldName);
        final ConstructorMirror constructor = field.getType().getConstructor();
        relocateField(vm, className, fieldName, new Callable<Object>() {
            public Object call() throws Exception {
                return constructor.newInstance(ThreadHolograph.currentThreadMirror());
            } 
        });
    }
    
    private static void relocateField(VirtualMachineMirror vm, String className, String fieldName, final Callable<Object> initializer) {
        final Map<Object, Object> relocatedValues = new HashMap<Object, Object>();
        ClassMirror klass = vm.findBootstrapClassMirror(className);
        FieldMirror field = klass.getDeclaredField(fieldName);
        FieldMirrorSetHandlerRequest setRequest = vm.eventRequestManager().createFieldMirrorSetHandlerRequest(field);
        vm.dispatch().addCallback(setRequest, new Callback<MirrorEvent>() {
            @Override
            public MirrorEvent handle(MirrorEvent t) {
                t.setProceed(new MirrorInvocationHandler() {
                    public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                        Object target = args.get(0);
                        Object encoder = args.get(1);
                        relocatedValues.put(target, encoder);
                        return null;
                    }
                });
                return t;
            }
        });
        setRequest.enable();
        
        FieldMirrorGetHandlerRequest getRequest = vm.eventRequestManager().createFieldMirrorGetHandlerRequest(field);
        vm.dispatch().addCallback(getRequest, new Callback<MirrorEvent>() {
            @Override
            public MirrorEvent handle(MirrorEvent t) {
                t.setProceed(new MirrorInvocationHandler() {
                    public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                        Object target = args.get(0);
                        if (!relocatedValues.containsKey(target) && initializer != null) {
                            try {
                                relocatedValues.put(target, initializer.call());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return relocatedValues.get(target);
                    }
                });
                return t;
            }
        });
        getRequest.enable();
    }
    
    public static void hardCodeHashing(VirtualMachineMirror vm) {
        MethodMirrorHandlerRequest request = vm.eventRequestManager().createMethodMirrorHandlerRequest();
        request.setMethodFilter("sun.misc.Hashing", "randomHashSeed", Collections.singletonList(Object.class.getName()));
        vm.dispatch().addCallback(request, new Callback<MirrorEvent>() {
            @Override
            public MirrorEvent handle(MirrorEvent t) {
                t.setProceed(new MirrorInvocationHandler() {
                    public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                        return 47;
                    }
                });
                return t;
            }
        });
        request.enable();
    }
    
    private static final Map<Object, Map<ThreadMirror, Object>> newThreadLocalValues 
        = new HashMap<Object, Map<ThreadMirror, Object>>();
    
    private static Map<ThreadMirror, Object> threadLocalMap(Object threadLocal) {
        Map<ThreadMirror, Object> result = newThreadLocalValues.get(threadLocal);
        if (result == null) {
            result = new HashMap<ThreadMirror, Object>();
            newThreadLocalValues.put(threadLocal, result);
        }
        return result;
    }
    
    private static void aroundMethod(VirtualMachineMirror vm, String declaringClass, String name, List<String> paramterTypeNames, final MirrorInvocationHandler handler) {
        MethodMirrorHandlerRequest request = vm.eventRequestManager().createMethodMirrorHandlerRequest();
        request.setMethodFilter(declaringClass, name, paramterTypeNames);
        vm.addCallback(request, new Callback<MirrorEvent>() {
            public MirrorEvent handle(MirrorEvent t) {
                t.setProceed(handler);
                return t;
            }
        });
        request.enable();
    }
    
    private static void aroundThreadLocals(VirtualMachineMirror vm) throws Exception {
        ClassMirror threadLocalClass = vm.findBootstrapClassMirror("java.lang.ThreadLocal");
        
        final MethodMirror initialValueMethod = threadLocalClass.getDeclaredMethod("initialValue");
        aroundMethod(vm, "java.lang.ThreadLocal", "get", Collections.<String>emptyList(), new MirrorInvocationHandler() {
            public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                InstanceMirror threadLocal = (InstanceMirror)args.get(0);
                Map<ThreadMirror, Object> map = threadLocalMap(threadLocal);
                if (!map.containsKey(thread)) {
                    try {
                        map.put(thread, initialValueMethod.invoke(thread, threadLocal));
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (MirrorInvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
                return map.get(thread);
            }
        });
        
        aroundMethod(vm, "java.lang.ThreadLocal", "set", Collections.singletonList("java.lang.Object"), new MirrorInvocationHandler() {
            public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                InstanceMirror threadLocal = (InstanceMirror)args.get(0);
                Map<ThreadMirror, Object> map = threadLocalMap(threadLocal);
                return map.put(thread, args.get(1));
            }
        });
        
        aroundMethod(vm, "java.lang.ThreadLocal", "remove", Collections.<String>emptyList(), new MirrorInvocationHandler() {
            public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                InstanceMirror threadLocal = (InstanceMirror)args.get(0);
                Map<ThreadMirror, Object> map = threadLocalMap(threadLocal);
                return map.remove(thread);
            }
        });
    }
    
}
