package edu.ubc.retrospect;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.reflect.Factory;
import org.aspectj.weaver.AdviceKind;
import org.aspectj.weaver.AjcMemberMaker;
import org.aspectj.weaver.BindingScope;
import org.aspectj.weaver.ConcreteTypeMunger;
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
import org.aspectj.weaver.ast.Var;
import org.aspectj.weaver.loadtime.DefaultMessageHandler;
import org.aspectj.weaver.loadtime.definition.Definition;
import org.aspectj.weaver.loadtime.definition.DocumentParser;
import org.aspectj.weaver.patterns.ExposedState;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.Pointcut;
import org.objectweb.asm.Type;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InputStreamMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodHandle;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.raw.NativeByteArrayMirror;
import edu.ubc.mirrors.raw.NativeClassMirror;

/**
 * Where good is evil, and evil good. Or something like that.
 * 
 * @author robinsalkeld
 */
public class MirrorWorld extends World {

    public static AdviceKind[] SUPPORTED_ADVICE_KINDS = { AdviceKind.Before, AdviceKind.After, AdviceKind.Around };
    
    public static URL aspectRuntimeJar;
    static {
    	try {
    		// TODO-RS: Automatically determine this
    	    aspectRuntimeJar = new URL("jar:file:///Users/robinsalkeld/Documents/UBC/Code/Retrospect/edu.ubc.mirrors.aspects/lib/aspectjrt-1.7.3.jar!/");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
    }
    
    final VirtualMachineMirror vm;
    final ClassMirrorLoader loader;
    final ThreadMirror thread;
    private final Map<ClassMirror, InstanceMirror> ajFactories = new HashMap<ClassMirror, InstanceMirror>();
    private final ConstructorMirror factoryConstructor;
    private final ClassMirror aspectAnnotClass;
    private final ClassMirror pointcutAnnotClass;
    private final ClassMirror aroundClosureClass;

    private final Map<String, ClassMirror> classMirrors =
            new HashMap<String, ClassMirror>();
    
    private final MirrorWeavingSupport weavingSupport = new MirrorWeavingSupport(this);
    private Definition definition;
    private final ThreadLocal<Set<MirrorEventShadow>> joinpointShadowsTL = new ThreadLocal<Set<MirrorEventShadow>>() {
        @Override
        protected Set<MirrorEventShadow> initialValue() {
            return new HashSet<MirrorEventShadow>();
        }
    };
    
    public MirrorWorld(VirtualMachineMirror vm, ThreadMirror thread, URL aspectPath) throws ClassNotFoundException, NoSuchMethodException, MirrorInvocationTargetException {
        this.vm = vm;
        
        System.out.println("Creating class loader for aspects...");
        this.loader = Reflection.newURLClassLoader(vm, thread, null, new URL[] {aspectPath, MirrorWorld.aspectRuntimeJar});
        
        this.thread = thread;
        ClassMirror factoryClass = Reflection.classMirrorForType(vm, thread, Type.getType(Factory.class), true, loader);
        this.factoryConstructor = factoryClass.getConstructor(String.class.getName(), Class.class.getName());
        this.aspectAnnotClass = Reflection.classMirrorForType(vm, thread, Type.getType(Aspect.class), false, loader);
        this.pointcutAnnotClass = Reflection.classMirrorForType(vm, thread, Type.getType(org.aspectj.lang.annotation.Pointcut.class), false, loader);
        
        this.aroundClosureClass = Reflection.withThread(thread, new Callable<ClassMirror>() {
            public ClassMirror call() throws Exception {
                ClassMirror nativeClass = new NativeClassMirror(MirrorInvocationHandlerAroundClosure.class);
                byte[] bytecode = nativeClass.getBytecode();
                return loader.defineClass(nativeClass.getClassName(), new NativeByteArrayMirror(bytecode), 
                        0, bytecode.length, null, null, false);
            }
        });
        
        if (Boolean.getBoolean("edu.ubc.mirrors.aspects.debugWeaving")) {
            setMessageHandler(IMessageHandler.SYSTEM_ERR);
        } else {
            setMessageHandler(new DefaultMessageHandler());
        }
        
        // These ones just have to be loaded because they are argument types in some of the factory methods
        Reflection.classMirrorForType(vm, thread, Type.getType(SourceLocation.class), true, loader);
        resolve(UnresolvedType.forName(JoinPoint.StaticPart.class.getName()));
    }

    public ClassMirror getPointcutAnnotClass() {
        return pointcutAnnotClass;
    }
    
    public ClassMirror getAroundClosureClass() {
        return aroundClosureClass;
    }
    
    public Var makeInvocationHandlerAroundClosureVar(final MirrorInvocationHandler handler) {
        return Reflection.withThread(thread, new Callable<Var>() {
            public Var call() throws Exception {
                ResolvedType aroundClosureType = resolve(getAroundClosureClass());
                InstanceMirror closure = getAroundClosureClass().newRawInstance();
                FieldMirror handlerField = getAroundClosureClass().getDeclaredField("handler");
                closure.set(handlerField, new AroundClosureMirror(vm, handler));
                return new MirrorEventVar(aroundClosureType, closure);
            }
        });
    }
    
    @Override
    protected ReferenceTypeDelegate resolveDelegate(ReferenceType ty) {
        String typeName = ty.getName();
        ClassMirror klass = classMirrors.get(typeName);
        if (klass == null) {
            try {
                klass = Reflection.classMirrorForName(vm, thread, typeName, true, loader);
            } catch (ClassNotFoundException e) {
                return null;
            } catch (MirrorInvocationTargetException e) {
                return null;
            }
            classMirrors.put(typeName, klass);
        }
        return new MirrorReferenceTypeDelegate(ty, klass);
    }

    public ResolvedType resolve(ClassMirror klass) {
        String className = klass.getClassName();
        classMirrors.put(className, klass);
        return resolve(UnresolvedType.forName(className));
    }
    
    @Override
    public IWeavingSupport getWeavingSupport() {
        return weavingSupport;
    }

    @Override
    public boolean isLoadtimeWeaving() {
        return true;
    }
    
    @Override
    public boolean isXmlConfigured() {
        return true;
    }
    
    @Override
    public boolean isAspectIncluded(ResolvedType aspectType) {
        return definition.getAspectClassNames().contains(aspectType.getName());
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
    
    private InstanceMirror makeLockSignature(ThreadMirror thread, ClassMirror monitorClass) {
        InstanceMirror factory = getAJFactory(thread, monitorClass);
        
        return (InstanceMirror)new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((Factory)null).makeLockSig((Class<?>)null);
            }
        }.invoke(factory, thread, monitorClass);
    }
    
    private InstanceMirror makeFieldSignature(ThreadMirror thread, FieldMirror field) {
        InstanceMirror factory = getAJFactory(thread, field.getDeclaringClass());
        
        return (InstanceMirror)new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((Factory)null).makeFieldSig(0, null, null, null);
            }
        }.invoke(factory, thread, field.getModifiers(), vm.makeString(field.getName()), 
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
            parameterNames.set(i, vm.makeString("arg" + i));
        }
        ObjectArrayMirror exceptionTypes = Reflection.toArray(classClass); //constructor.getExceptionTypes());
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
            parameterNames.set(i, vm.makeString("arg" + i));
        }
        ObjectArrayMirror exceptionTypes = Reflection.toArray(classClass, method.getParameterTypes());
        return (InstanceMirror)new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((Factory)null).makeMethodSig(0, null, null, null, null, null, null);
            }
        }.invoke(factory, thread, method.getModifiers(), vm.makeString(method.getName()), method.getDeclaringClass(), 
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
    
    public InstanceMirror makeSynchronizationStaticJoinPoint(ThreadMirror thread, String kind, InstanceMirror monitor) {
        InstanceMirror factory = getAJFactory(thread, monitor.getClassMirror());
        InstanceMirror signature = makeLockSignature(thread, monitor.getClassMirror());
        return (InstanceMirror)new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((Factory)null).makeSJP(null, null, null);
            }
        }.invoke(factory, thread, vm.getInternedString(kind), signature, null);
    }
    
    public ClassMirror getAnnotClassMirror(AdviceKind kind) {
        UnresolvedType annotationClass;
        if (kind.equals(AdviceKind.Before)) {
            annotationClass = AjcMemberMaker.BEFORE_ANNOTATION;
        } else if (kind.equals(AdviceKind.After)) {
            annotationClass = AjcMemberMaker.AFTER_ANNOTATION;
        } else if (kind.equals(AdviceKind.Around)) {
            annotationClass = AjcMemberMaker.AROUND_ANNOTATION;
        } else {
            throw new IllegalArgumentException();
        }
        try {
            return Reflection.classMirrorForType(vm, thread, Type.getType(annotationClass.getSignature()), true, loader);
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
        return Reflection.getAnnotation(klass.getAnnotations(thread), aspectAnnotClass) != null;
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
    
    private void parseConfiguration() {
        String definitionPath = "META-INF/aop-ajc.xml";
        InstanceMirror definitionStream = (InstanceMirror)Reflection.invokeMethodHandle(loader, thread, new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((ClassLoader)null).getResourceAsStream((String)null);
            }
        }, vm.makeString(definitionPath));
        FakeURLStreamHandler handler = new FakeURLStreamHandler(new InputStreamMirror(thread, definitionStream));
        try {
            URL fakeURL = new URL("file", "", 0, definitionPath, handler);
            definition = DocumentParser.parse(fakeURL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void weave() throws ClassNotFoundException, NoSuchMethodException, InterruptedException, MirrorInvocationTargetException {
        Reflection.withThread(thread, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ThreadHolograph.raiseMetalevel();
        
                showMessage(IMessage.DEBUG, "Loading weaving configuration...", null, null);
                parseConfiguration();
                
                showMessage(IMessage.DEBUG, "Loading aspects...", null, null);
                for (String aspectClassName : definition.getAspectClassNames()) {
                    ReferenceType aspectType = (ReferenceType)resolve(aspectClassName);
                    getCrosscuttingMembersSet().addOrReplaceAspect(aspectType);
                }
                
                showMessage(IMessage.DEBUG, "Weaving aspects...", null, null);
                for (ConcreteTypeMunger munger : getCrosscuttingMembersSet().getTypeMungers()) {
                    MirrorTypeMunger mirrorMunger = (MirrorTypeMunger)munger;
                    mirrorMunger.munge(MirrorWorld.this);
                }
                
                for (ShadowMunger munger : getCrosscuttingMembersSet().getShadowMungers()) {
                    MirrorAdvice advice = (MirrorAdvice)munger;
                    showMessage(IMessage.DEBUG, "Installing event requests for advice: " + advice, null, null);
                    PointcutMirrorRequestExtractor.installCallback(MirrorWorld.this, advice, new Callback<MirrorEventShadow>() {
                        public Object handle(MirrorEventShadow shadow) {
                            // For consistency with other forms of weaving, skip shadows from bootstrap classes (by default)
                            // See also: http://www.eclipse.org/aspectj/doc/released/devguide/ltw-specialcases.html
                            if (!Boolean.getBoolean("edu.ubc.mirrors.aspects.weaveCoreClasses")) {
                                // TODO-RS: This is specified in the documentation for loadtime weaving,
                                // so this check is probably already coded somewhere in the weaving library...
                                String declaringClassName = shadow.getDeclaringClass().getClassName();
                                if (declaringClassName.startsWith("org.aspectj.") || 
                                    declaringClassName.startsWith("java.") || 
                                    declaringClassName.startsWith("javax.") ||
                                    declaringClassName.startsWith("sun.reflect.")) {
                                    return null;
                                }
                            }
                            
                            joinpointShadowsTL.get().add(shadow);
                            
                            return null;
                        }
                    });
                    showMessage(IMessage.DEBUG, "Done.", null, null);
                }
                
                vm.dispatch().addSetCallback(new Runnable() {
                    public void run() {
                        Set<MirrorEventShadow> shadowSet = joinpointShadowsTL.get();
                        for (MirrorEventShadow shadow : shadowSet) {
                            showMessage(IMessage.DEBUG, shadow.toString(), null, null);
                            for (ShadowMunger munger : getCrosscuttingMembersSet().getShadowMungers()) {
                                if (munger.match(shadow, MirrorWorld.this)) {
                                    shadow.addMunger(munger);
                                }
                            }
                            shadow.implement();
                        }
                        shadowSet.clear();
                    }
                });
                
                ThreadHolograph.lowerMetalevel();
                
                return null;
            }
        });
    }
}
