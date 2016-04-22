package edu.ubc.retrospect;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import org.aspectj.bridge.Constants;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.runtime.internal.AroundClosure;
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
import org.aspectj.weaver.loadtime.DefaultMessageHandler;
import org.aspectj.weaver.loadtime.definition.Definition;
import org.aspectj.weaver.loadtime.definition.DocumentParser;
import org.aspectj.weaver.patterns.ExposedState;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.PatternParser;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerSingleton;
import org.aspectj.weaver.patterns.Pointcut;
import org.objectweb.asm.Type;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.EnumerationMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InputStreamMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodHandle;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorInvocationHandler;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
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
public class MirrorWorld extends World implements Callback<MirrorEventShadow> {

    public static AdviceKind[] SUPPORTED_ADVICE_KINDS = { AdviceKind.Before, AdviceKind.After, AdviceKind.AfterReturning, AdviceKind.Around };
    
    public static URL aspectRuntimeJar;
    static {
    	try {
    	    // TODO-RS: Automatically determine this
    	    aspectRuntimeJar = new URL("jar:file:///Users/robinsalkeld/Documents/UBC/Code/Retrospect/edu.ubc.mirrors.aspects/lib/aspectjrt-1.7.3.jar!/");
    	} catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static Set<String> implicitPointcutFormalTypes = new HashSet<String>();
    static {
        implicitPointcutFormalTypes.add(JoinPoint.class.getName());
        implicitPointcutFormalTypes.add(ProceedingJoinPoint.class.getName());
        implicitPointcutFormalTypes.add(JoinPoint.StaticPart.class.getName());
        implicitPointcutFormalTypes.add(JoinPoint.EnclosingStaticPart.class.getName());
        implicitPointcutFormalTypes.add(AroundClosure.class.getName());
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
    private List<Definition> definitions;
    
    // Mild hack - allows loading and weaving aspects in stages to carefully control side-effects in the bootstraping stage
    private Set<ResolvedType> wovenAspects = new HashSet<ResolvedType>();
    
    public static ClassMirrorLoader makeClassLoaderMirror(VirtualMachineMirror vm, ThreadMirror thread, URL...aspectPaths) {
//        showMessage(IMessage.DEBUG, "Creating class loader for aspects...", null, null);
        URL[] paths = new URL[aspectPaths.length + 1];
        System.arraycopy(aspectPaths, 0, paths, 0, aspectPaths.length);
        paths[paths.length - 1] = MirrorWorld.aspectRuntimeJar;
        return Reflection.newURLClassLoader(vm, thread, null, paths);
    }
    
    public MirrorWorld(VirtualMachineMirror vm, ThreadMirror thread, URL...aspectPaths) throws ClassNotFoundException, NoSuchMethodException, MirrorInvocationTargetException {
        this(thread, makeClassLoaderMirror(vm, thread, aspectPaths));
    }
    
    public MirrorWorld(ThreadMirror thread, final ClassMirrorLoader loader) throws ClassNotFoundException, NoSuchMethodException, MirrorInvocationTargetException {
        this.vm = thread.getClassMirror().getVM();
        
        if (Boolean.getBoolean("edu.ubc.mirrors.aspects.debugWeaving")) {
            setMessageHandler(IMessageHandler.SYSTEM_ERR);
        } else {
            setMessageHandler(new DefaultMessageHandler());
        }
        
        this.thread = thread;
        this.loader = loader;
        ClassMirror factoryClass = Reflection.classMirrorForType(vm, thread, Type.getType(Factory.class), true, loader);
        this.factoryConstructor = factoryClass.getConstructor(String.class.getName(), Class.class.getName());
        this.aspectAnnotClass = Reflection.classMirrorForType(vm, thread, Type.getType(Aspect.class), false, loader);
        this.pointcutAnnotClass = Reflection.classMirrorForType(vm, thread, Type.getType(org.aspectj.lang.annotation.Pointcut.class), false, loader);
        
        this.aroundClosureClass = Reflection.withThread(thread, new Callable<ClassMirror>() {
            public ClassMirror call() throws Exception {
                ClassMirror nativeClass = new NativeClassMirror(MirrorInvocationHandlerAroundClosure.class);
                String className = nativeClass.getClassName();
                byte[] bytecode = nativeClass.getBytecode();
                NativeByteArrayMirror bytecodeMirror = new NativeByteArrayMirror(bytecode);
                if (loader == null) {
                    return vm.defineBootstrapClass(className, bytecodeMirror, 0, bytecode.length);
                } else {
                    return loader.defineClass(className, bytecodeMirror, 
                            0, bytecode.length, null, null, false);
                }
            }
        });
    }

    public ClassMirror getPointcutAnnotClass() {
        return pointcutAnnotClass;
    }
    
    public ClassMirror getAroundClosureClass() {
        return aroundClosureClass;
    }
    
    public InstanceMirror makeInvocationHandlerAroundClosure(ThreadMirror thread, final MirrorInvocationHandler handler) {
        return Reflection.withThread(thread, new Callable<InstanceMirror>() {
            public InstanceMirror call() throws Exception {
                InstanceMirror closure = getAroundClosureClass().newRawInstance();
                FieldMirror handlerField = getAroundClosureClass().getDeclaredField("handler");
                closure.set(handlerField, new AroundClosureMirror(vm, handler));
                return closure;
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
                if (e.getTargetException().getClassMirror().getClassName().equals(ClassNotFoundException.class.getName())) {
                    return null;
                }
                throw new RuntimeException(e);
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
        // Some synthetic advice has no declaring aspect, which leads to passing null here
        if (aspectType == null) {
            return true;
        }

        for (Definition definition : definitions) {
            if (definition.getAspectClassNames().contains(aspectType.getName())) {
                return true;
            }
        }
        return false;
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
    
    private InstanceMirror makeClassNameList(List<String> classNames) {
        List<String> fixedNames = new ArrayList<String>(classNames.size());
        for (String className : classNames) {
            if (className.contains("[]")) {
                className = Reflection.descriptorForTypeName(className).replace("/", ".");
            }
            fixedNames.add(className);
        }
        return vm.makeString(Reflection.join(fixedNames, ":"));
    }
    
    private InstanceMirror makeConstructorSignature(ThreadMirror thread, ConstructorMirror constructor) {
        InstanceMirror factory = getAJFactory(thread, constructor.getDeclaringClass());
        
        InstanceMirror parameterTypes = makeClassNameList(constructor.getParameterTypeNames());
        
        // TODO-RS: Check this against LTW behaviour
        int numParams = constructor.getParameterTypeNames().size();
        List<String> parameterNames = new ArrayList<String>();
        for (int i = 0; i < numParams; i++) {
            parameterNames.add("arg" + i);
        }
        InstanceMirror parameterNamesStr = vm.makeString(Reflection.join(parameterNames, ":"));
        
        InstanceMirror exceptionTypes = vm.makeString(""); //constructor.getExceptionTypeNames());
        
        return (InstanceMirror)new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((Factory)null).makeConstructorSig("0", null, null, null, null);
            }
        }.invoke(factory, thread, vm.makeString(Integer.toString(constructor.getModifiers(), 16)), 
                    vm.makeString(constructor.getDeclaringClass().getClassName()), 
                    parameterTypes, parameterNamesStr, exceptionTypes);
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
        } else if (kind.equals(AdviceKind.AfterReturning)) {
            annotationClass = AjcMemberMaker.AFTERRETURNING_ANNOTATION;
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
    
    
    public AnnotationMirror getAspectAnnotation(ClassMirror klass) {
        return Reflection.getAnnotation(klass.getAnnotations(thread), aspectAnnotClass);
    }
    
    public Pointcut parsePointcut(String expr) {
        // Crappy workaround to the AspectJ compiler transforming "lock()" to "lock(* *)",
        // which the weaver parser then can't parse!
        expr = expr.replaceAll(Pattern.quote("lock(* *)"), "lock()");
        
        return new PatternParser(expr, SourceContextImpl.UNKNOWN_SOURCE_CONTEXT).parsePointcut();
    }
    
    public PerClause parsePerClause(String expr) {
        PerClause result = new PatternParser(expr, SourceContextImpl.UNKNOWN_SOURCE_CONTEXT).maybeParsePerClause();
        return result == null ? new PerSingleton() : result;
    }
    
    public Pointcut resolvePointcut(Member signature, Pointcut pointcut) {
        String[] parameterNames = signature.getParameterNames(this);
        FormalBinding[] formals = new FormalBinding[parameterNames.length];
        for (int i = 0; i < formals.length; i++) {
            UnresolvedType paramType = signature.getParameterTypes()[i];
            if (implicitPointcutFormalTypes.contains(paramType.getName())) {
                formals[i] = new FormalBinding.ImplicitFormalBinding(paramType, parameterNames[i], i);   
            } else {
                formals[i] = new FormalBinding(paramType, parameterNames[i], i);
            }
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
    
    private static final String AOP_XML = Constants.AOP_AJC_XML + ";" + Constants.AOP_USER_XML;
    
    private void parseConfiguration() {
        definitions = new ArrayList<Definition>();
        // TODO: This should be fetching the property from inside the VMM instead 
        String definitionPath = System.getProperty("org.aspectj.weaver.loadtime.configuration", AOP_XML);
        for (String definitionPathPart : definitionPath.split(";")) {
            try {
                parseConfiguration(definitionPathPart);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    private void parseConfiguration(String nextDefinition) throws Exception {
        if (nextDefinition.startsWith("file:")) {
            try {
                    String fpath = new URL(nextDefinition).getFile();
                    File configFile = new File(fpath);
                    if (!configFile.exists()) {
                            MessageUtil.warn(getMessageHandler(), "configuration does not exist: " + nextDefinition);
                    } else {
                            definitions.add(DocumentParser.parse(configFile.toURL()));
                    }
            } catch (MalformedURLException mue) {
                    MessageUtil.error(getMessageHandler(), "malformed definition url: " + nextDefinition);
            }
            return;
        }
            
        InstanceMirror urlsEnumeration;
        if (loader == null) {
            Enumeration<URL> urls = vm.findBootstrapResources(nextDefinition);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try {
                    definitions.add(DocumentParser.parse(url));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return;
        }
        
        urlsEnumeration = (InstanceMirror)Reflection.invokeMethodHandle(loader, thread, new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((ClassLoader)null).getResources((String)null);
            }
        }, vm.makeString(nextDefinition));
        Enumeration<ObjectMirror> urls = new EnumerationMirror(thread, urlsEnumeration);
        while (urls.hasMoreElements()) {
            InstanceMirror urlMirror = (InstanceMirror)urls.nextElement();
            InstanceMirror streamMirror = (InstanceMirror)Reflection.invokeMethodHandle(urlMirror, thread, new MethodHandle() {
                protected void methodCall() throws Throwable {
                    ((URL)null).openStream();
                }
            });
            URL url = FakeURLStreamHandler.makeURL(new InputStreamMirror(thread, streamMirror));
            try {
                definitions.add(DocumentParser.parse(url));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public void weave() throws ClassNotFoundException, InterruptedException {
        Reflection.withThread(thread, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ThreadHolograph.raiseMetalevel();
        
                showMessage(IMessage.DEBUG, "Loading weaving configuration...", null, null);
                parseConfiguration();
                
                showMessage(IMessage.DEBUG, "Loading aspects...", null, null);
                for (Definition definition : definitions) {
                    for (String aspectClassName : definition.getAspectClassNames()) {
                        showMessage(IMessage.DEBUG, "Loading aspect: " + aspectClassName, null, null);
                        ResolvedType aspectType = resolve(aspectClassName);
                        if (aspectType.isMissing()) {
                            throw new ClassNotFoundException("Couldn't load aspect: " + aspectClassName); 
                        }
                        
                        if (wovenAspects.contains(aspectType)) {
                            continue;
                        }
                        
                        if (!aspectType.isAbstract()) {
                            getCrosscuttingMembersSet().addOrReplaceAspect((ReferenceType)aspectType);
                            wovenAspects.add(aspectType);
                        }
                    }
                }
                
                showMessage(IMessage.DEBUG, "Weaving aspects...", null, null);
                for (ShadowMunger munger : getCrosscuttingMembersSet().getShadowMungers()) {
                    MirrorAdvice advice = (MirrorAdvice)munger;
                    showMessage(IMessage.DEBUG, "Installing event requests for advice: " + advice, null, null);
                    PointcutMirrorRequestExtractor.installCallback(MirrorWorld.this, advice);
                }
                
                for (ConcreteTypeMunger munger : getCrosscuttingMembersSet().getTypeMungers()) {
                    MirrorTypeMunger mirrorMunger = (MirrorTypeMunger)munger;
                    mirrorMunger.munge(MirrorWorld.this);
                }
                
                ThreadHolograph.lowerMetalevel();
                
                return null;
            }
        });
    }
    
    private final Callback<MirrorEvent> eventCallback = new Callback<MirrorEvent>() {
        public MirrorEvent handle(MirrorEvent event) {
            Shadow.Kind shadowKind = (Shadow.Kind)event.request().getProperty(PointcutMirrorRequestExtractor.SHADOW_KIND_PROPERTY_KEY);
            if (shadowKind != null) {
                MirrorEventShadow shadow = MirrorEventShadow.make(MirrorWorld.this, event, shadowKind);
                if (shadow != null) {
                    MirrorWorld.this.handle(shadow);
                }
            }
            return event;
        };
    };
    
    public Callback<MirrorEvent> eventCallback() {
        return eventCallback;
    }
    
    private static final boolean WEAVE_CORE_CLASSES = Boolean.getBoolean("edu.ubc.mirrors.aspects.weaveCoreClasses");
    
    public static boolean weaveClass(String className) {
        if (WEAVE_CORE_CLASSES) {
            return true;
        } else {
            // For consistency with other forms of weaving, skip shadows from bootstrap classes (by default)
            // See also: http://www.eclipse.org/aspectj/doc/released/devguide/ltw-specialcases.html
            // TODO-RS: This is specified in the documentation for loadtime weaving,
            // so this check is probably already coded somewhere in the weaving library...
            return !(className.startsWith("org.aspectj.") || 
                     className.startsWith("java.") || 
                     className.startsWith("javax.") ||
                     className.startsWith("sun.reflect."));
        }
    }
    
    @Override
    public MirrorEventShadow handle(MirrorEventShadow shadow) {
        if (!weaveClass(shadow.getDeclaringClass().getClassName())) {
            return null;
        }
    
        showMessage(IMessage.DEBUG, shadow.toString(), null, null);
        
        for (ShadowMunger munger : getCrosscuttingMembersSet().getShadowMungers()) {
            if (munger.match(shadow, MirrorWorld.this)) {
                shadow.addMunger(munger);
            }
        }
        shadow.implement();
        
        return shadow;
    }
}
