package edu.ubc.mirrors.test;

import static edu.ubc.mirrors.Reflection.replaceMethod;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
import edu.ubc.mirrors.MirrorLocation;
import edu.ubc.mirrors.MirrorLocationRequest;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.retrospect.MirrorWorld;

public class RetroactiveWeaving {

    public static String weave(VirtualMachineMirror vm, ThreadMirror thread, String aspectPath, File hologramClassPath, ByteArrayOutputStream mergedOutput) throws Exception {
        System.out.println("Booting up holographic VM...");
        if (mergedOutput == null) {
            mergedOutput = new ByteArrayOutputStream();
        }
        OutputStream teedOut = new TeeOutputStream(mergedOutput, System.out);
        OutputStream teedErr = new TeeOutputStream(mergedOutput, System.err);
        
        final VirtualMachineHolograph vmh = new VirtualMachineHolograph(vm, hologramClassPath,
                Collections.singletonMap("/", "/"));
        vmh.setSystemOut(teedOut);
        vmh.setSystemErr(teedErr);
        final ThreadMirror finalThread = (ThreadMirror)vmh.getWrappedMirror(thread);

        Reflection.withThread(finalThread, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
             // Avoid the side-effects of loading aspects themselves
                relocateField(vmh, "java.lang.String", "hash");
                relocateField(vmh, "java.util.zip.ZipCoder", "enc");
                relocateField(vmh, "java.nio.charset.Charset", "cache1");
                relocateField(vmh, "java.nio.charset.Charset", "cache2");
                relocateField(vmh, "java.lang.Thread", "threadInitNumber");
                relocateFieldInitializeWithDefaultConstructor(vmh, "sun.nio.cs.ThreadLocalCoders$Cache", "cache");
                relocateFieldInitializeWithDefaultConstructor(vmh, "java.lang.ThreadLocal", "nextHashCode");
                relocateFieldInitializeWithDefaultConstructor(vmh, "java.net.URLClassLoader", "closeables");
                aroundThreadLocals(vmh);
                hardCodeHashing(vmh);
                classLoaderLocking(vmh);
                aroundThreadGroups(vmh);
                return null;
            }
        });
        
        vmh.addBootstrapPathURL(EvalConstants.GuardAspectsBin.toURI().toURL());
        vmh.addBootstrapPathURL(MirrorWorld.aspectRuntimeJar);
        for (String aspectPathPart : aspectPath.split(File.pathSeparator)) {
            URL partURL;
            String escaped = new File(aspectPathPart).toURI().toURL().getFile();
            if (aspectPathPart.endsWith(".jar")) {
                partURL = new URL("jar", null, -1, "file:" + escaped + "!/");
            } else {
                partURL = new URL("file", null, -1, escaped);
            }
            vmh.addBootstrapPathURL(partURL);
        }
        MirrorWorld world = new MirrorWorld(finalThread, null);
        world.weave();
        
        vmh.resume();
        vmh.dispatch().run();
        
        return mergedOutput.toString();
//        return Reflection.withThread(thread, new Callable<String>() {
//          @Override
//          public String call() throws Exception {
//              ClassMirror guardAspect = vmh.findBootstrapClassMirror("edu.ubc.aspects.JDKAroundFieldSets");
//              ObjectMirror newOut = guardAspect.getStaticFieldValues().get(guardAspect.getDeclaredField("newStderrBaos"));
//              String output = Reflection.toString(newOut, finalThread);
//              System.out.print(output);
//              return output;
//          }
//      });
    }
    
    private static void cflowCounterBreakpoint(VirtualMachineHolograph vmh) {
        FieldMirrorSetHandlerRequest setRequest = vmh.eventRequestManager().createFieldMirrorSetHandlerRequest(
                "org.aspectj.runtime.internal.cflowstack.ThreadStackFactoryImpl$ThreadCounterImpl$Counter", "value");
        vmh.addCallback(setRequest, new Callback<MirrorEvent>() {
            public MirrorEvent handle(MirrorEvent t) {
                System.out.println(t.arguments());
                if (((Integer)t.arguments().get(1)).intValue() == 0) {
                    return t;
                }
                return t;
            }
        });
        setRequest.enable();
        
        FieldMirrorGetHandlerRequest getRequest = vmh.eventRequestManager().createFieldMirrorGetHandlerRequest(
                "org.aspectj.runtime.internal.cflowstack.ThreadStackFactoryImpl$ThreadCounterImpl$Counter", "value");
        vmh.addCallback(getRequest, new Callback<MirrorEvent>() {
            public MirrorEvent handle(MirrorEvent t) {
                System.out.println(t.arguments());
                return t;
            }
        });
        getRequest.enable();
        
        methodCallBreakpoint(vmh, "org.aspectj.runtime.internal.cflowstack.ThreadStackFactoryImpl$ThreadCounterImpl", "isNotZero"
                );
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
        FieldMirrorSetHandlerRequest setRequest = vm.eventRequestManager().createFieldMirrorSetHandlerRequest(className, fieldName);
        vm.dispatch().addCallback(setRequest, new Callback<MirrorEvent>() {
            @Override
            public MirrorEvent handle(MirrorEvent t) {
                t.setProceed(new MirrorInvocationHandler() {
                    public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                        Object target = args.get(0);
                        Object newValue = args.get(1);
                        relocatedValues.put(target, newValue);
                        return null;
                    }
                });
                return t;
            }
        });
        setRequest.enable();
        
        FieldMirrorGetHandlerRequest getRequest = vm.eventRequestManager().createFieldMirrorGetHandlerRequest(className, fieldName);
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
    
    private static void hardCodeHashing(VirtualMachineMirror vm) {
        replaceMethod(vm, "sun.misc.Hashing", "randomHashSeed", Collections.singletonList(Object.class.getName()), new MirrorInvocationHandler() {
            public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                return 47;
            }
        });
    }
    
    private static void classLoaderLocking(VirtualMachineMirror vm) {
        replaceMethod(vm, "java.lang.ClassLoader", "getClassLoadingLock", Collections.singletonList(String.class.getName()), new MirrorInvocationHandler() {
            public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                // returning the ClassLoader target itself
                return args.get(0);
            }
        });
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
    
    private static void aroundThreadLocals(VirtualMachineMirror vm) throws Exception {
        ClassMirror threadLocalClass = vm.findBootstrapClassMirror("java.lang.ThreadLocal");
        
        final MethodMirror initialValueMethod = threadLocalClass.getDeclaredMethod("initialValue");
        replaceMethod(vm, "java.lang.ThreadLocal", "get", Collections.<String>emptyList(), new MirrorInvocationHandler() {
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
        
        replaceMethod(vm, "java.lang.ThreadLocal", "set", Collections.singletonList("java.lang.Object"), new MirrorInvocationHandler() {
            public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                InstanceMirror threadLocal = (InstanceMirror)args.get(0);
                Map<ThreadMirror, Object> map = threadLocalMap(threadLocal);
                return map.put(thread, args.get(1));
            }
        });
        
        replaceMethod(vm, "java.lang.ThreadLocal", "remove", Collections.<String>emptyList(), new MirrorInvocationHandler() {
            public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                InstanceMirror threadLocal = (InstanceMirror)args.get(0);
                Map<ThreadMirror, Object> map = threadLocalMap(threadLocal);
                return map.remove(thread);
            }
        });
    }
    
    private static InstanceMirror newThreadGroup;
    
    private static void aroundThreadGroups(final VirtualMachineMirror vm) {
        MethodMirrorHandlerRequest request = vm.eventRequestManager().createMethodMirrorHandlerRequest();
        request.setMethodFilter("java.lang.Thread", "init", Arrays.asList(ThreadGroup.class.getName(), Runnable.class.getName(), String.class.getName(), "long"));
        vm.addCallback(request, new Callback<MirrorEvent>() {
            public MirrorEvent handle(MirrorEvent t) {
                final MirrorInvocationHandler original = t.getProceed();
                t.setProceed(new MirrorInvocationHandler() {
                    public Object invoke(ThreadMirror thread, List<Object> args) throws MirrorInvocationTargetException {
                        if (newThreadGroup == null) {
                            ClassMirror threadGroupClass = vm.findBootstrapClassMirror(ThreadGroup.class.getName());
                            try {
                                newThreadGroup = threadGroupClass.getConstructor("java.lang.String").newInstance(thread, vm.makeString("retroactive thread group"));
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            } catch (NoSuchMethodException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        
                        List<Object> newArgs = new ArrayList<Object>(args);
                        newArgs.set(1, newThreadGroup);
                        return original.invoke(thread, newArgs);
                    }
                });
                return t;
            }
        });
        request.enable();
    }
    
    private static void methodCallBreakpoint(final VirtualMachineMirror vm, String declaringClass, String name, String... paramterTypeNames) {
        MethodMirrorHandlerRequest methodRequest = vm.eventRequestManager().createMethodMirrorHandlerRequest();
        methodRequest.setMethodFilter(declaringClass, name, Arrays.asList(paramterTypeNames));
        vm.addCallback(methodRequest, BREAKPOINT);
        methodRequest.enable();
    }
    
    private static void locationBreakpoint(final VirtualMachineMirror vm, String className, int lineNumber) {
        ClassMirror klass = vm.findAllClasses(className, false).iterator().next();
        MirrorLocation location = klass.locationOfLine(lineNumber);
        MirrorLocationRequest request = vm.eventRequestManager().createLocationRequest(location);
        vm.addCallback(request, BREAKPOINT);
        request.enable();
    }
    
    private static Callback<MirrorEvent> BREAKPOINT = new Callback<MirrorEvent>() {
        public MirrorEvent handle(MirrorEvent t) {
            return t;
        }
    };
}
