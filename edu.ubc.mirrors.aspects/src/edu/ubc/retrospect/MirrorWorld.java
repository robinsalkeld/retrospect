package edu.ubc.retrospect;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Pattern;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.runtime.reflect.Factory;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.BindingScope;
import org.aspectj.weaver.IWeavingSupport;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ReferenceTypeDelegate;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.SourceContextImpl;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.ExposedState;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.Pointcut;
import org.objectweb.asm.Type;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.FieldMirrorGetEvent;
import edu.ubc.mirrors.FieldMirrorSetEvent;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodHandle;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;

/**
 * Where good is evil, and evil good. Or something like that.
 * 
 * @author robinsalkeld
 */
public class MirrorWorld extends World {

    public static AdviceKind[] SUPPORTED_ADVICE_KINDS = { AdviceKind.Before, AdviceKind.After };
    
    final VirtualMachineHolograph vm;
    private final ClassMirrorLoader loader;
    final ThreadMirror thread;
    private final Map<ClassMirror, InstanceMirror> ajFactories = new HashMap<ClassMirror, InstanceMirror>();
    private final ConstructorMirror factoryConstructor;
    private final ClassMirror aspectAnnotClass;
    private final ClassMirror pointcutAnnotClass;

    private final Map<ReferenceType, ReferenceTypeDelegate> delegates =
            new HashMap<ReferenceType, ReferenceTypeDelegate>();
    
    private final MirrorWeavingSupport weavingSupport = new MirrorWeavingSupport(this);
    
    public MirrorWorld(VirtualMachineHolograph vm, ClassMirrorLoader loader, ThreadMirror thread) throws ClassNotFoundException, NoSuchMethodException, MirrorInvocationTargetException {
        this.vm = vm;
        this.loader = loader;
        this.thread = thread;
        ClassMirror factoryClass = Reflection.classMirrorForType(vm, thread, Type.getType(Factory.class), false, loader);
        this.factoryConstructor = factoryClass.getConstructor(String.class.getName(), Class.class.getName());
        this.aspectAnnotClass = Reflection.classMirrorForType(vm, thread, Type.getType(Aspect.class), false, loader);
        this.pointcutAnnotClass = Reflection.classMirrorForType(vm, thread, Type.getType(org.aspectj.lang.annotation.Pointcut.class), false, loader);
    }

    public ClassMirror getPointcutAnnotClass() {
        return pointcutAnnotClass;
    }
    
    @Override
    protected ReferenceTypeDelegate resolveDelegate(ReferenceType ty) {
        ReferenceTypeDelegate result = delegates.get(ty);
        if (result == null) {
            ClassMirror klass;
            try {
                klass = Reflection.classMirrorForName(vm, thread, ty.getName(), false, loader);
            } catch (ClassNotFoundException e) {
                return null;
            } catch (MirrorInvocationTargetException e) {
                return null;
            }
            result = new MirrorReferenceTypeDelegate(ty, klass);
            delegates.put(ty, result);
        }
        return result;
    }

    public ResolvedType resolve(ClassMirror klass) {
        return resolve(UnresolvedType.forName(klass.getClassName()));
    }
    
    @Override
    public IWeavingSupport getWeavingSupport() {
        return weavingSupport;
    }

    @Override
    public boolean isLoadtimeWeaving() {
        return true;
    }
    
    private InstanceMirror getAJFactory(ThreadMirror thread, ClassMirror classMirror) {
        InstanceMirror factory = ajFactories.get(classMirror);
        if (factory == null) {
            try {
                factory = factoryConstructor.newInstance(thread, null, classMirror);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (MirrorInvocationTargetException e) {
                throw new RuntimeException(e);
            }
            ajFactories.put(classMirror, factory);
        }
        return factory;
    }
    
    private InstanceMirror makeFieldSignature(ThreadMirror thread, FieldMirror field) {
        InstanceMirror factory = getAJFactory(thread, field.getDeclaringClass());
        
        return (InstanceMirror)new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((Factory)null).makeFieldSig(0, null, null, null);
            }
        }.invoke(factory, thread, field.getModifiers(), Reflection.makeString(vm, field.getName()), 
                field.getDeclaringClass(), field.getType());
    }
    
    private InstanceMirror makeConstructorSignature(ThreadMirror thread, ConstructorMirror constructor) {
        InstanceMirror factory = getAJFactory(thread, constructor.getDeclaringClass());
        
        ClassMirror classClass = vm.findBootstrapClassMirror(Class.class.getName());
        ClassMirror stringClass = vm.findBootstrapClassMirror(String.class.getName());
        
        ObjectArrayMirror parameterTypes = Reflection.toArray(classClass, constructor.getParameterTypes());
        // TODO-RS: Check this against LTW behaviour
        int numParams = parameterTypes.length();
        ObjectArrayMirror parameterNames = (ObjectArrayMirror)stringClass.newArray(numParams);
        for (int i = 0; i < numParams; i++) {
            parameterNames.set(i, Reflection.makeString(vm, "arg" + i));
        }
        ObjectArrayMirror exceptionTypes = Reflection.toArray(classClass, constructor.getExceptionTypes());
        return (InstanceMirror)new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((Factory)null).makeConstructorSig(0, null, null, null, null);
            }
        }.invoke(factory, thread, constructor.getModifiers(), constructor.getDeclaringClass(), 
                parameterTypes, parameterNames, exceptionTypes);
    }
    
    private InstanceMirror makeMethodSignature(ThreadMirror thread, MethodMirror method) {
        InstanceMirror factory = getAJFactory(thread, method.getDeclaringClass());
        
        ClassMirror classClass = vm.findBootstrapClassMirror(Class.class.getName());
        ClassMirror stringClass = vm.findBootstrapClassMirror(String.class.getName());
        
        ObjectArrayMirror parameterTypes = Reflection.toArray(classClass, method.getParameterTypes());
        // TODO-RS: Check this against LTW behaviour
        int numParams = parameterTypes.length();
        ObjectArrayMirror parameterNames = (ObjectArrayMirror)stringClass.newArray(numParams);
        for (int i = 0; i < numParams; i++) {
            parameterNames.set(i, Reflection.makeString(vm, "arg" + i));
        }
        ObjectArrayMirror exceptionTypes = Reflection.toArray(classClass, method.getParameterTypes());
        return (InstanceMirror)new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((Factory)null).makeMethodSig(0, null, null, null, null, null, null);
            }
        }.invoke(factory, thread, method.getModifiers(), Reflection.makeString(vm, method.getName()), method.getDeclaringClass(), 
                parameterTypes, parameterNames, exceptionTypes, method.getReturnType());
    }
    
    public InstanceMirror makeStaticJoinPoint(ThreadMirror thread, String kind, FieldMirror field) {
        InstanceMirror factory = getAJFactory(thread, field.getDeclaringClass());
        InstanceMirror signature = makeFieldSignature(thread, field);
        return (InstanceMirror)new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((Factory)null).makeSJP(null, null, null);
            }
        }.invoke(factory, thread, vm.getInternedString(kind), signature, null);
    }
    
    public InstanceMirror makeStaticJoinPoint(ThreadMirror thread, String kind, ConstructorMirror constructor) {
        InstanceMirror factory = getAJFactory(thread, constructor.getDeclaringClass());
        InstanceMirror signature = makeConstructorSignature(thread, constructor);
        return (InstanceMirror)new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((Factory)null).makeSJP(null, null, null);
            }
        }.invoke(factory, thread, vm.getInternedString(kind), signature, null);
    }
    
    public InstanceMirror makeStaticJoinPoint(ThreadMirror thread, String kind, MethodMirror method) {
        InstanceMirror factory = getAJFactory(thread, method.getDeclaringClass());
        InstanceMirror signature = makeMethodSignature(thread, method);
        return (InstanceMirror)new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((Factory)null).makeSJP(null, null, null);
            }
        }.invoke(factory, thread, vm.getInternedString(kind), signature, null);
    }
    

    public InstanceMirror makeStaticJoinPoint(ThreadMirror thread, MirrorEvent event) {
        if (event instanceof ConstructorMirrorEntryEvent) {
            return makeStaticJoinPoint(thread, org.aspectj.lang.JoinPoint.METHOD_EXECUTION, ((ConstructorMirrorEntryEvent)event).constructor());
        } else if (event instanceof ConstructorMirrorExitEvent) {
            return makeStaticJoinPoint(thread, org.aspectj.lang.JoinPoint.METHOD_EXECUTION, ((ConstructorMirrorExitEvent)event).constructor());
        } else if (event instanceof MethodMirrorEntryEvent) {
            return makeStaticJoinPoint(thread, org.aspectj.lang.JoinPoint.METHOD_EXECUTION, ((MethodMirrorEntryEvent)event).method());
        } else if (event instanceof MethodMirrorExitEvent) {
            return makeStaticJoinPoint(thread, org.aspectj.lang.JoinPoint.METHOD_EXECUTION, ((MethodMirrorExitEvent)event).method());
        } else if (event instanceof FieldMirrorGetEvent) {
            return makeStaticJoinPoint(thread, org.aspectj.lang.JoinPoint.FIELD_GET, ((FieldMirrorGetEvent)event).field());
        } else if (event instanceof FieldMirrorSetEvent) {
            return makeStaticJoinPoint(thread, org.aspectj.lang.JoinPoint.FIELD_SET, ((FieldMirrorSetEvent)event).field());
        } else {
            throw new IllegalArgumentException("Unsupported event type: " + event);
        }
    }
    
    public static class CflowStack {
        
        private Map<ThreadMirror, Stack<ExposedState>> stateStacks = new HashMap<ThreadMirror, Stack<ExposedState>>();
        
        private Stack<ExposedState> stackForThread(ThreadMirror thread) {
            Stack<ExposedState> stack = stateStacks.get(thread);
            if (stack == null) {
                stack = new Stack<ExposedState>();
                stateStacks.put(thread, stack);
            }
            return stack;
        }
        
        public void pushState(ThreadMirror thread, ExposedState state) {
            stackForThread(thread).push(state);
        }
        
        public void popState(ThreadMirror thread) {
            stackForThread(thread).pop();
        }
        
        public ExposedState getBinding(ThreadMirror thread) {
            Stack<ExposedState> stack = stackForThread(thread);
            return stack.empty() ? null : stack.peek();
        }
    }
    
    public ClassMirror getAnnotClassMirror(AdviceKind kind) {
        UnresolvedType signature = kind.equals(AdviceKind.After) ? AjcMemberMaker.AFTER_ANNOTATION : AjcMemberMaker.BEFORE_ANNOTATION;
        try {
            return Reflection.classMirrorForType(vm, thread, Type.getType(signature.getSignature()), true, loader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    public interface PointcutCallback {
        public void call(MirrorEventShadow shadow, ExposedState state);
    }
    
    
    public boolean isAspect(ClassMirror klass) {
        return Reflection.getAnnotation(klass.getAnnotations(), aspectAnnotClass) != null;
    }
    
    public Pointcut parsePointcut(String expr) {
        // Crappy workaround to the AspectJ compiler transforming "lock()" to "lock(* *)",
        // which the weaver parser then can't parse!
        expr = expr.replaceAll(Pattern.quote("lock(* *)"), "lock()");
        
        return new PatternParser(expr, SourceContextImpl.UNKNOWN_SOURCE_CONTEXT).parsePointcut();
    }
    
    public Pointcut resolvePointcut(Member signature, Pointcut pointcut) {
        String[] parameterNames = signature.getParameterNames(this);
        FormalBinding[] formals = new FormalBinding[parameterNames.length];
        for (int i = 0; i < formals.length; i++) {
            UnresolvedType paramType = signature.getParameterTypes()[i];
            formals[i] = new FormalBinding(paramType, parameterNames[i], i);
        }
        BindingScope scope = new BindingScope((ResolvedType)signature.getDeclaringType(), pointcut.getSourceContext(), formals);
        return pointcut.resolve(scope);
    }
    
    public ClassMirror mirrorForType(ResolvedType type) {
        return ((MirrorReferenceTypeDelegate)((ReferenceType)type).getDelegate()).klass;
    }
    
    @Override
    public void reportMatch(ShadowMunger munger, Shadow shadow) {
//        System.out.println("Match: " + munger + " on " + shadow);
    }
}
