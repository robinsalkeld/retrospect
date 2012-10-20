package edu.ubc.retrospect;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java_cup.runtime.Symbol;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;
import org.aspectj.runtime.reflect.Factory;

import polyglot.ast.Node;
import polyglot.ast.TypeNode;
import polyglot.frontend.Job;
import polyglot.types.Flags;
import polyglot.types.Named;
import polyglot.types.SemanticException;
import polyglot.util.StdErrorQueue;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.StringPrettyPrinter;
import abc.aspectj.ExtensionInfo;
import abc.aspectj.ast.AJNodeFactory;
import abc.aspectj.ast.AJNodeFactory_c;
import abc.aspectj.ast.CPEBinary;
import abc.aspectj.ast.CPEName;
import abc.aspectj.ast.CPENot;
import abc.aspectj.ast.CPESubName;
import abc.aspectj.ast.CPEUniversal;
import abc.aspectj.ast.ClassTypeDotId;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.ConstructorPattern;
import abc.aspectj.ast.DotDotFormalPattern;
import abc.aspectj.ast.DotDotNamePattern;
import abc.aspectj.ast.DotNamePattern;
import abc.aspectj.ast.FormalPattern;
import abc.aspectj.ast.MethodPattern;
import abc.aspectj.ast.ModifierPattern;
import abc.aspectj.ast.NamePattern;
import abc.aspectj.ast.PCName;
import abc.aspectj.ast.PCName_c;
import abc.aspectj.ast.SimpleNamePattern;
import abc.aspectj.ast.TPEArray;
import abc.aspectj.ast.TPEBinary;
import abc.aspectj.ast.TPENot;
import abc.aspectj.ast.TPERefTypePat;
import abc.aspectj.ast.TPEType;
import abc.aspectj.ast.TPEUniversal;
import abc.aspectj.ast.ThrowsPattern;
import abc.aspectj.ast.TypeFormalPattern;
import abc.aspectj.ast.TypePatternExpr;
import abc.aspectj.parse.Grm;
import abc.aspectj.parse.Lexer_c;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.AJTypeSystem_c;
import abc.aspectj.visit.InitClasses;
import abc.aspectj.visit.NoSourceJob;
import abc.main.CompilerAbortedException;
import abc.om.ast.CPEFlags;
import abc.weaving.aspectinfo.AndPointcut;
import abc.weaving.aspectinfo.Execution;
import abc.weaving.aspectinfo.MethodCall;
import abc.weaving.aspectinfo.NotPointcut;
import abc.weaving.aspectinfo.OrPointcut;
import abc.weaving.aspectinfo.Pointcut;
import abc.weaving.aspectinfo.Within;
import abc.weaving.aspectinfo.WithinConstructor;
import abc.weaving.aspectinfo.WithinMethod;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;
import edu.ubc.mirrors.EventDispatch;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorEventRequest;
import edu.ubc.mirrors.MirrorEventRequestManager;
import edu.ubc.mirrors.MirrorEventSet;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.mirrors.mirages.MethodHandle;
import edu.ubc.mirrors.mirages.Reflection;
import edu.ubc.mirrors.raw.nativestubs.java.lang.StringStubs;

public class AspectJMirrors {

    private final VirtualMachineHolograph vm;
    private final ThreadMirror thread;
    private final ClassMirrorLoader loader;
    private final ConstructorMirror factoryConstructor;
    
    // org.aspectj.lang.annotation classes
    public enum AdviceKind {
	BEFORE(Before.class),
	AFTER(After.class),
	AFTER_RETURNING(AfterReturning.class),
	AFTER_THROWING(AfterThrowing.class),
	AROUND(Around.class),
	POINTCUT(org.aspectj.lang.annotation.Pointcut.class);
	
	private AdviceKind(Class<?> annotationClass) {
	    this.annotationClass = annotationClass;
	}
	
	private final Class<?> annotationClass;
    }
    
    private ClassMirror getAnnotClassMirror(AdviceKind kind) {
	try {
	    return Reflection.classMirrorForName(vm, thread, kind.annotationClass.getName(), true, loader);
	} catch (ClassNotFoundException e) {
	    throw new RuntimeException(e);
	}
    }
    
	
    // Used for pointcut declarations as well
    public class AdviceMirror {
	// Name of the pointcut or
	// Name of the advice - @AdviceName for code-style advice, method name for annotation-style advice
	private final String name;
	// @Before, @Pointcut, etc
	private final AdviceKind kind;
	// This holds the formal declarations
	private final ObjectMirror method;
	private final MethodMirror methodMirror;
	// Null for abstract pointcuts. Non-final because of the resolution phase.
	private abc.aspectj.ast.Pointcut astPC;
	private abc.weaving.aspectinfo.Pointcut aiPC;
	
	private String getPointcutName(ObjectMirror method) {
	    String name = Reflection.getRealStringForMirror((InstanceMirror)new MethodHandle() {
		protected void methodCall() throws Throwable {
		    ((Method)null).getName();
		}
	    }.invoke(method));
	    // Stolen from org.aspectj.internal.lang.reflect.AjTypeImpl#asPointcut(Method)
	    if (name.startsWith("ajc$")) {
		// extract real name
		int nameStart = name.indexOf("$$");
		name = name.substring(nameStart +2,name.length());
		int nextDollar = name.indexOf("$");
		if (nextDollar != -1) name = name.substring(0,nextDollar);
	    }
	    return name;
	}
	
	public AdviceMirror(AdviceKind kind, ObjectMirror method, ObjectMirror annotation) {
	    this.method = method;
	    this.kind = kind;
	    this.name = getPointcutName(method);
	    this.methodMirror = Reflection.methodMirrorForMethodInstance((InstanceMirror)method);
	    
	    int pointcutFlags = (Integer)Reflection.invokeMethodHandle(method, thread, new MethodHandle() {
		protected void methodCall() throws Throwable {
		    ((Method)null).getModifiers();
		}   
	    });
	    if (Modifier.isAbstract(pointcutFlags)) {
		astPC = null;
	    } else {
		String pointcut;
		String parameterNamesString;
		try {
		    pointcut = Reflection.getRealStringForMirror((InstanceMirror)annotation.getClassMirror().getMethod("value").invoke(thread, annotation));
		    parameterNamesString = Reflection.getRealStringForMirror((InstanceMirror)annotation.getClassMirror().getMethod("argNames").invoke(thread, annotation));
		} catch (IllegalAccessException e) {
		    throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
		    throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
		    throw new RuntimeException(e);
		}
		Map<String, String> locals = new HashMap<String, String>();
		String[] parameterNames = parameterNamesString.isEmpty() ? new String[0] : parameterNamesString.split(",");
		ObjectArrayMirror parameterTypes = (ObjectArrayMirror)Reflection.invokeMethodHandle(method, thread, new MethodHandle() {
		    protected void methodCall() throws Throwable {
			((Method)null).getParameterTypes();
		    } 
		}); 
		for (int j = 0; j < parameterNames.length; j++) {
		    ClassMirror type = (ClassMirror)parameterTypes.get(j);
		    locals.put(parameterNames[j], type.getClassName());
		}

		astPC = parsePointcut(locals, pointcut);
	    }
	}

	public Pointcut getAIPointcut() {
	    return aiPC;
	}

	public AdviceKind getKind() {
	    return kind;
	}

	public Node getASTPointcut() {
	    return astPC;
	}
    }

    public AdviceMirror getAdviceForMethod(ObjectMirror method) {
	for (AdviceKind kind : AdviceKind.values()) {
	    ObjectMirror annot = (ObjectMirror)Reflection.invokeMethodHandle(method, thread, new MethodHandle() {
		protected void methodCall() throws Throwable {
		    ((Method)null).getAnnotation(null);
		}
	    }, getAnnotClassMirror(kind));
	    if (annot != null) {
		return new AdviceMirror(kind, method, annot);
	    }
	}
	return null;
    }
    
    public class AspectMirror {
	private final ClassMirror klass;
	private final List<AdviceMirror> adviceDecls = new ArrayList<AdviceMirror>();
	private final Map<String, AdviceMirror> pointcutDecls = new HashMap<String, AdviceMirror>();
	
	// TODO-RS: This will have to be more complex when we deal with other "per" clauses
	private InstanceMirror instance;
	
	public AspectMirror(ClassMirror klass) {
	    this.klass = klass;
	    
	    ObjectArrayMirror methods = (ObjectArrayMirror)Reflection.invokeMethodHandle(klass, thread, new MethodHandle() {
		protected void methodCall() throws Throwable {
		    ((Class<?>)null).getDeclaredMethods();
		}
	    });
	    int n = methods.length();
	    for (int i = 0; i < n; i++) {
		ObjectMirror method = methods.get(i);
		AdviceMirror advice = getAdviceForMethod(method);
		if (advice != null) {
		    if (advice.kind == AdviceKind.POINTCUT) {
			pointcutDecls.put(advice.name, advice);
		    } else {
			adviceDecls.add(advice);
		    }
		}
	    }
	}
	
	public List<AdviceMirror> getAdvice() {
	    return Collections.unmodifiableList(adviceDecls);
	}

	public void installRequests() {
	    MirrorEventRequestManager manager = vm.eventRequestManager();
	    final Set<MirrorEvent> joinpointEvents = new HashSet<MirrorEvent>();
	    for (AdviceMirror advice : adviceDecls) {
		Pointcut pc = advice.getAIPointcut();
		Pointcut dnf = pc.dnf().makePointcut(pc.getPosition());
		List<List<Pointcut>> actualDNF = disjuncts(dnf);
		
		for (List<Pointcut> disjunct : actualDNF) {
		    MirrorEventRequest request = extractRequest(manager, advice.getKind(), disjunct);
		    
		    vm.dispatch().addCallback(request, new EventDispatch.EventCallback() {
		        @Override
		        public void handle(MirrorEvent event) {
		            joinpointEvents.add(event);
		        }
		    });
		    request.enable();
		}
	    }
	    
	    vm.dispatch().addSetCallback(new Runnable() {
		@Override
		public void run() {
		    executeAdvice(joinpointEvents);
		    joinpointEvents.clear();
		}
	    });
	}
	
	private MirrorEventRequest extractRequest(MirrorEventRequestManager manager, AdviceKind adviceKind, List<Pointcut> disjunct) {
	    boolean isExecution = false;
	    boolean isConstructor = false;
	    for (Iterator<Pointcut> pcIter = disjunct.iterator(); pcIter.hasNext();) {
		Pointcut pc = pcIter.next();
		
		// TODO-RS: Be more rigorous about conflicting specifications
		if (pc instanceof Execution) {
		    isExecution = true;
		    pcIter.remove();
		} else if (pc instanceof WithinConstructor) {
		    isConstructor = true;
		}
	    }
	    
	    MirrorEventRequest request;
	    if (isExecution) {
		if (adviceKind == AdviceKind.BEFORE) {
		    if (isConstructor) {
			request = manager.createConstructorMirrorEntryRequest();
		    } else {
			request = manager.createMethodMirrorEntryRequest();
		    }
		} else {
		    if (isConstructor) {
			request = manager.createConstructorMirrorExitRequest();
		    } else {
			request = manager.createMethodMirrorExitRequest();
		    }
		}
	    } else {
		throw new IllegalStateException("Unsupported pointcut: " + disjunct);
	    }
	    
	    for (Pointcut otherPC : disjunct) {
		// TODO-RS: Should remove clauses that are completely expressed
		// as request filters, to avoid redundant checking.
		if (otherPC instanceof Within) {
		    Within within = (Within)otherPC;
		    // TODO-RS: toString() is likely wrong here. Need to decide on 
		    // what pattern DSL the mirrors API should accept.
		    request.addClassFilter(within.getPattern().toString());
		}
	    }
	    
	    return request;
	}

	// Collect DNF assuming the argument is already a DNF pointcut
	private List<List<Pointcut>> disjuncts(Pointcut pc) {
	    if (pc instanceof OrPointcut) {
		OrPointcut or = (OrPointcut)pc;
		List<List<Pointcut>> result = new ArrayList<List<Pointcut>>();
		result.addAll(disjuncts(or.getLeftPointcut()));
		result.addAll(disjuncts(or.getRightPointcut()));
		return result;
	    } else {
		List<List<Pointcut>> result = new ArrayList<List<Pointcut>>(1);
		result.add(conjuncts(pc));
		return result;
	    }
	}

	private List<Pointcut> conjuncts(Pointcut pc) {
	    if (pc instanceof AndPointcut) {
		AndPointcut or = (AndPointcut)pc;
		List<Pointcut> result = new ArrayList<Pointcut>();
		result.addAll(conjuncts(or.getLeftPointcut()));
		result.addAll(conjuncts(or.getRightPointcut()));
		return result;
	    } else {
		List<Pointcut> result = new ArrayList<Pointcut>(1);
		result.add(pc);
		return result;
	    }
	}

	public void executeAdvice(Set<MirrorEvent> eventSet) {
	    for (AdviceMirror advice : adviceDecls) {
		// Note the break below - each advice should only apply at most once to
		// each joinpoint, even if there are multiple events at that joinpoint
		// that match its pointcut
		for (MirrorEvent event : eventSet) {
		    if (adviceMatchesEvent(advice.getKind(), advice.getAIPointcut(), event)) {
			InstanceMirror staticJoinPoint = makeStaticJoinPoint(event);
			InstanceMirror aspectInstance = getInstance();
			try {
			    advice.methodMirror.invoke(thread, aspectInstance, staticJoinPoint);
			    break;
			} catch (IllegalAccessException e) {
			    throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
			    // TODO-RS: Think about exceptions in general. Advice throwing exceptions
			    // would perturb the original execution so they need to be handled specially.
			    throw new RuntimeException(e);
			}
		    }
		}
	    }
	}

	private InstanceMirror getInstance() {
	    if (instance == null) {
		try {
		    ConstructorMirror constructor = klass.getConstructor();
		    instance = constructor.newInstance(thread);
		} catch (InstantiationException e) {
		    throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
		    throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
		    throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
		    throw new RuntimeException(e);
		}
	    }
	    return instance;	
	}
    }



    private final ExtensionInfo ext = new ExtensionInfo(Collections.<String>emptyList(), Collections.<String>emptyList());
    private final StdErrorQueue eq = new StdErrorQueue(System.err, 80, "pointcut parsing");
    private final AJTypeSystem ts = new AJTypeSystem_c();
    private final AJNodeFactory nf = new AJNodeFactory_c();
    private final Job job = new NoSourceJob(ext);
    
    private final Map<String, AspectMirror> aspects = new HashMap<String, AspectMirror>();
    private final Map<ClassMirror, InstanceMirror> ajFactories = new HashMap<ClassMirror, InstanceMirror>();
    
    static {
	// Has to be called for the side-effect of setting the AbcExtension,
        // since the Lexer_c constructor reads it. Grr.
        try {
            new abc.main.Main(new String[] { "foo" } );
        } catch (CompilerAbortedException e) {
            // Don't care.
        }
    }
    
    public AspectJMirrors(ClassMirrorLoader loader, ThreadMirror thread) throws ClassNotFoundException, NoSuchMethodException {
        this.loader = loader;
	this.vm = (VirtualMachineHolograph)loader.getClassMirror().getVM();
	this.thread = thread;
        ClassMirror factoryClass = Reflection.classMirrorForName(vm, thread, Factory.class.getName(), false, loader);
	this.factoryConstructor = factoryClass.getConstructor(
		vm.findBootstrapClassMirror(String.class.getName()), 
		vm.findBootstrapClassMirror(Class.class.getName()));
    }
    
    public abc.aspectj.ast.Pointcut parsePointcut(Map<String, String> locals, String expr) {
        Reader reader = new StringReader(expr);
        
        Lexer_c lexer = new Lexer_c(reader, "<runtime expression>", eq);
        lexer.enterLexerState(lexer.pointcut_state());
        
        Grm grm = new Grm(lexer, ts, nf, eq);
        Symbol result;
        try {
            result = grm.parsePointcut();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        // No point since we can't reuse it.
        // lexer.returnToPrevState();
        
        abc.aspectj.ast.Pointcut pc = (abc.aspectj.ast.Pointcut)result.value;
        
        new InitClasses(ExtensionInfo.INIT_CLASSES, ext, ts).run();
        
        AmbiguityRemover v = new AmbiguityRemover(job, ts, nf, AmbiguityRemover.ALL);
        v = (AmbiguityRemover)v.begin();
        for (Map.Entry<String, String> entry : locals.entrySet()) {
            v.context().addVariable(ts.localInstance(null,Flags.FINAL,null,entry.getKey()));
        }
	pc = (abc.aspectj.ast.Pointcut)pc.visit(v);
	v.finish();
        return pc;
    }
    
    
    
    private class PCResolver extends NodeVisitor {
	
	private AspectMirror aspect;
	
	@Override
	public Node leave(Node old, Node n, NodeVisitor v) {
	    if (n instanceof PCName) {
		PCName_c pcName = (PCName_c)n;
		String name = pcName.name();
		AdviceMirror pcDecl = aspect.pointcutDecls.get(name);
		if (pcDecl == null) {
		    throw new IllegalStateException();
		}
		// TODO-RS: Handle argument binding
		if (!pcName.arguments().isEmpty()) {
		    throw new UnsupportedOperationException("Argument binding not yet supported");
		}
		return pcDecl.getASTPointcut();
	    } else {
		return super.leave(old, n, v);
	    }
	}
    }
    
    /**
     * Replaces all instances of PCName (a reference to a named pointcut) with the referenced
     * pointcut;
     */
    public void resolve() {
	PCResolver v = new PCResolver();
	for (AspectMirror aspect : aspects.values()) {
	    v.aspect = aspect;
	    for (AdviceMirror advice : aspect.pointcutDecls.values()) {
		advice.astPC = (abc.aspectj.ast.Pointcut)advice.getASTPointcut().visit(v);
		advice.aiPC = advice.astPC.makeAIPointcut();
	    }
	    for (AdviceMirror advice : aspect.adviceDecls) {
		advice.astPC = (abc.aspectj.ast.Pointcut)advice.getASTPointcut().visit(v);
		advice.aiPC = advice.astPC.makeAIPointcut();
	    }
	}
    }
    
    public AspectMirror getAspectMirror(ClassMirror klass) {
	AspectMirror aspect = aspects.get(klass.getClassName());
	if (aspect == null) {
	    aspect = new AspectMirror(klass);
	    aspects.put(klass.getClassName(), aspect);
	}
	return aspect;
    }
    
    // TODO-RS: Extend to bind pointcut formals
    // TODO-RS: Will also have to be stateful to handle cflow and similar dynamic tests
    public boolean adviceMatchesEvent(AdviceKind kind, abc.weaving.aspectinfo.Pointcut pc, MirrorEvent event) {
	if (pc instanceof AndPointcut) {
	    AndPointcut andPC = (AndPointcut)pc;
	    return adviceMatchesEvent(kind, andPC.getLeftPointcut(), event) 
		&& adviceMatchesEvent(kind, andPC.getRightPointcut(), event);
	} else if (pc instanceof OrPointcut) {
	    OrPointcut orPC = (OrPointcut)pc;
	    return adviceMatchesEvent(kind, orPC.getLeftPointcut(), event) 
		|| adviceMatchesEvent(kind, orPC.getRightPointcut(), event);
	} else if (pc instanceof NotPointcut) {
	    NotPointcut notPC = (NotPointcut)pc;
	    return !adviceMatchesEvent(kind, notPC, event);
	} else if (pc instanceof Execution) {
	    if ((event instanceof MethodMirrorEntryEvent || event instanceof ConstructorMirrorEntryEvent) && kind == AdviceKind.BEFORE) {
		return true;
	    } else if ((event instanceof MethodMirrorExitEvent || event instanceof ConstructorMirrorExitEvent) && kind == AdviceKind.AFTER) {
		return true;
	    } else {
		return false;
	    }
	} else if (pc instanceof MethodCall) {
	    // TODO-RS: Need to either find method calls in bytecode and set breakpoints 
	    // or fake things by hiding the extra stack frame (probably the former)
	    return false;
	} else if (pc instanceof Within) {
	    Within within = (Within)pc;
	    ClassnamePatternExpr pattern = within.getPattern().getPattern();
	    
	    if (event instanceof ConstructorMirrorEntryEvent && kind == AdviceKind.BEFORE) {
		ConstructorMirrorEntryEvent mmee = (ConstructorMirrorEntryEvent)event;
		ClassMirror klass = mmee.constructor().getDeclaringClass();
		return classMatches(klass, pattern);
	    } else if (event instanceof ConstructorMirrorExitEvent && kind == AdviceKind.AFTER) {
		ConstructorMirrorExitEvent mmee = (ConstructorMirrorExitEvent)event;
		ClassMirror klass = mmee.constructor().getDeclaringClass();
		return classMatches(klass, pattern);
	    } else if (event instanceof MethodMirrorEntryEvent && kind == AdviceKind.BEFORE) {
		MethodMirrorEntryEvent mmee = (MethodMirrorEntryEvent)event;
		ClassMirror klass = mmee.method().getDeclaringClass();
		return classMatches(klass, pattern);
	    } else if (event instanceof MethodMirrorExitEvent && kind == AdviceKind.AFTER) {
		MethodMirrorExitEvent mmee = (MethodMirrorExitEvent)event;
		ClassMirror klass = mmee.method().getDeclaringClass();
		return classMatches(klass, pattern);
	    } else {
		return false;
	    }
	} else if (pc instanceof WithinMethod) {
	    WithinMethod withinMethod = (WithinMethod)pc;
	    MethodPattern pattern = withinMethod.getPattern().getPattern();
	    
	    if (event instanceof MethodMirrorEntryEvent && kind == AdviceKind.BEFORE) {
		MethodMirrorEntryEvent mmee = (MethodMirrorEntryEvent)event;
		MethodMirror method = mmee.method();
		return methodMatches(method, pattern);
	    } else if (event instanceof MethodMirrorExitEvent && kind == AdviceKind.AFTER) {
		MethodMirrorExitEvent mmee = (MethodMirrorExitEvent)event;
		MethodMirror method = mmee.method();
		return methodMatches(method, pattern);
	    } else {
		return false;
	    }
	} else if (pc instanceof WithinConstructor) {
	    WithinConstructor within = (WithinConstructor)pc;
	    ConstructorPattern pattern = within.getPattern().getPattern();
	    
	    if (event instanceof ConstructorMirrorEntryEvent && kind == AdviceKind.BEFORE) {
		ConstructorMirrorEntryEvent cmee = (ConstructorMirrorEntryEvent)event;
		ConstructorMirror constructor = cmee.constructor();
		return constructorMatches(constructor, pattern);
	    } else if (event instanceof ConstructorMirrorExitEvent && kind == AdviceKind.AFTER) {
		ConstructorMirrorExitEvent mmee = (ConstructorMirrorExitEvent)event;
		ConstructorMirror constructor = mmee.constructor();
		return constructorMatches(constructor, pattern);
	    } else {
		return false;
	    }
	} else {
	    throw new IllegalArgumentException("Unsupported pointcut type: " + pc);
	}
    }

    private boolean constructorMatches(ConstructorMirror constructor, ConstructorPattern pattern) {
	return classMatches(constructor.getDeclaringClass(), pattern.getName().base())
		    && modifiersMatch(constructor.getModifiers(), pattern.getModifiers())
		    && formalsMatch(constructor.getParameterTypes(), pattern.getFormals())
		    && exceptionsMatch(constructor.getExceptionTypes(), pattern.getThrowspats());
    }

    private boolean methodMatches(MethodMirror method, MethodPattern pattern) {
	ClassTypeDotId namePattern = pattern.getName();
	return classMatches(method.getDeclaringClass(), namePattern.base())
	    && nameMatches(method.getName(), namePattern.name())
	    && modifiersMatch(method.getModifiers(), pattern.getModifiers())
	    && formalsMatch(method.getParameterTypes(), pattern.getFormals())
	    && exceptionsMatch(method.getExceptionTypes(), pattern.getThrowspats());
    }

    private boolean exceptionsMatch(List<ClassMirror> exceptionTypes, List<?> throwsPatterns) {
	// Empty seems to means no pattern provided
	int size = throwsPatterns.size();
	if (size == 0) {
	    return true;
	}
	if (exceptionTypes.size() != size) {
	    return false;
	}
	for (int i = 0; i < size; i++) {
	    ThrowsPattern pattern = (ThrowsPattern)throwsPatterns.get(i);
	    ClassMirror exceptionType = exceptionTypes.get(i);
	    if (pattern.positive() != classMatches(exceptionType, pattern.type())) {
		return false;
	    }
	}
	return true;
    }

    private boolean formalsMatch(List<ClassMirror> parameterTypes, List formalPatterns) {
	int size = formalPatterns.size();
	for (int i = 0; i < size; i++) {
	    FormalPattern pattern = (FormalPattern)formalPatterns.get(i);
	    if (pattern instanceof DotDotFormalPattern) {
		if (i == size - 1) {
		    return true;
		} else {
		    // TODO-RS: Support ".." in the beginning/middle of the list,
		    // which requires some more complicated backtracking/forking
		    throw new IllegalArgumentException("'..' is only supported at the end of formal pattern lists");
		}
	    } else {
		TypeFormalPattern tfp = (TypeFormalPattern)pattern;
		if (i >= parameterTypes.size()) {
		    return false;
		}
		if (!typeMatches(parameterTypes.get(i), tfp.expr())) {
		    return false;
		}
	    }
	}
	return true;
    }

    private boolean typeMatches(ClassMirror classMirror, TypePatternExpr pattern) {
    	if (pattern instanceof TPEUniversal) {
	    return true;
    	} else if (pattern instanceof TPEType) {
	    TPEType tpeType = (TPEType)pattern;
	    TypeNode typeNode = tpeType.type();
	    return classMirror.getClassName().equals(((Named)typeNode).name());
	} else if (pattern instanceof TPERefTypePat) {
	    try {
		return classMatches(classMirror, ((TPERefTypePat)pattern).transformToClassnamePattern(nf));
	    } catch (SemanticException e) {
		// TODO-RS: I imagine this shouldn't happen but I'm not sure
		throw new RuntimeException(e);
	    }
	} else if (pattern instanceof TPENot) {
	    return !typeMatches(classMirror, ((TPENot)pattern).getTpe());
	} else if (pattern instanceof TPEBinary) {
	    TPEBinary tpeBinary = (TPEBinary)pattern;
	    if (tpeBinary.op() == TPEBinary.COND_AND) {
		return typeMatches(classMirror, tpeBinary.left()) && typeMatches(classMirror, tpeBinary.right());
	    } else {
		return typeMatches(classMirror, tpeBinary.left()) || typeMatches(classMirror, tpeBinary.right());
	    }
	} else if (pattern instanceof TPEArray) {
	    if (!classMirror.isArray()) {
		return false;
	    }
	    TPEArray tpeArray = (TPEArray)pattern;
	    // Not efficient but should work
	    TPEArray componentPattern = nf.TPEArray(tpeArray.position(), tpeArray.base(), tpeArray.dims() - 1);
	    return typeMatches(classMirror.getComponentClassMirror(), componentPattern);
	} else {
	    throw new IllegalArgumentException("Unexpected TypePatternExpr: " + pattern);
	}
    }

    private boolean modifiersMatch(int modifiers, List<?> modifierPatterns) {
	for (Object o : modifierPatterns) {
	    ModifierPattern mp = (ModifierPattern)o;
	    if (mp.positive() == flagsMatch(modifiers, mp.modifier())) {
		return false;
	    }
	}
	return true;
    }

    private boolean nameMatches(String name, NamePattern pattern) {
	if (pattern instanceof SimpleNamePattern) {
	    return ((SimpleNamePattern)pattern).getPattern().matcher(name).matches();
	} else {
	    int dotIndex = name.lastIndexOf('.');
	    if (dotIndex == -1) {
		return false;
	    }
	    String nameInit = name.substring(0, dotIndex);
	    if (pattern instanceof DotDotNamePattern) {
		return nameMatches(nameInit, ((DotDotNamePattern)pattern).getInit());
	    } else if (pattern instanceof DotNamePattern) {
		DotNamePattern dotNamePattern = (DotNamePattern)pattern;
		String nameLast = name.substring(dotIndex + 1);
		return nameMatches(nameInit, dotNamePattern.getInit())
		    && nameMatches(nameLast, dotNamePattern.getLast());
	    } else {
		throw new IllegalArgumentException("Unknown NamePattern: " + pattern);
	    }
	}
    }

    private boolean classMatches(ClassMirror classMirror, ClassnamePatternExpr pattern) {
    	if (pattern instanceof CPEUniversal) {
	    return true;
	    
    	} else if (pattern instanceof CPEName) {
	    return nameMatches(classMirror.getClassName(), ((CPEName)pattern).getNamePattern());
	
    	} else if (pattern instanceof CPESubName) {
	    if (nameMatches(classMirror.getClassName(), ((CPEName)pattern).getNamePattern())) {
		return true;
	    }
	    ClassMirror superClassMirror = classMirror.getSuperClassMirror();
	    if (superClassMirror != null && classMatches(superClassMirror, pattern)) {
		return true;
	    }
	    for (ClassMirror interfaceMirror : classMirror.getInterfaceMirrors()) {
		if (classMatches(interfaceMirror, pattern)) {
		    return true;
		}
	    }
	    return false;
	
    	} else if (pattern instanceof CPENot) {
	    return !classMatches(classMirror, ((CPENot)pattern).getCpe());
	
    	} else if (pattern instanceof CPEBinary) {
	    CPEBinary cpeBinary = (CPEBinary)pattern;
	    if (cpeBinary.getOperator() == CPEBinary.COND_AND) {
		return classMatches(classMirror, cpeBinary.getLeft()) && classMatches(classMirror, cpeBinary.getRight());
	    } else {
		return classMatches(classMirror, cpeBinary.getLeft()) || classMatches(classMirror, cpeBinary.getRight());
	    }
	
    	} else if (pattern instanceof CPEFlags) {
	    CPEFlags cpeFlags = (CPEFlags)pattern;
	    return classMatches(classMirror, cpeFlags.getCpe())
		&& flagsMatch(classMirror.getModifiers(), cpeFlags.getFlags());
	
    	} else {
	    throw new IllegalArgumentException("Unexpected ClassnamePatternExpr: " + pattern);
	}	
    }

    private boolean flagsMatch(int modifiers, Flags flags) {
	// How unfortunate that the Flags bits aren't *quite* consistent...
	if (flags.isPublic() && !Modifier.isPublic(modifiers)) return false;
	if (flags.isProtected() && !Modifier.isProtected(modifiers)) return false;
	if (flags.isPrivate() && !Modifier.isPrivate(modifiers)) return false;
	if (flags.isStatic() && !Modifier.isStatic(modifiers)) return false;
	if (flags.isFinal() && !Modifier.isFinal(modifiers)) return false;
	if (flags.isSynchronized() && !Modifier.isSynchronized(modifiers)) return false;
	if (flags.isTransient() && !Modifier.isTransient(modifiers)) return false;
	if (flags.isNative() && !Modifier.isNative(modifiers)) return false;
	if (flags.isInterface() && !Modifier.isInterface(modifiers)) return false;
	if (flags.isAbstract() && !Modifier.isAbstract(modifiers)) return false;
	if (flags.isVolatile() && !Modifier.isVolatile(modifiers)) return false;
	if (flags.isStrictFP() && !Modifier.isStrict(modifiers)) return false;
	return true;
    }
    
    private InstanceMirror getAJFactory(ClassMirror classMirror) {
	InstanceMirror factory = ajFactories.get(classMirror);
	if (factory == null) {
	    try {
		factory = factoryConstructor.newInstance(thread, null, classMirror);
	    } catch (IllegalAccessException e) {
		throw new RuntimeException(e);
	    } catch (InvocationTargetException e) {
		throw new RuntimeException(e);
	    } catch (InstantiationException e) {
		throw new RuntimeException(e);
	    }
	    ajFactories.put(classMirror, factory);
	}
	return factory;
    }
    
    private InstanceMirror makeConstructorSignature(ConstructorMirror constructor) {
	InstanceMirror factory = getAJFactory(constructor.getDeclaringClass());
	
	ClassMirror classClass = vm.findBootstrapClassMirror(Class.class.getName());
	ClassMirror stringClass = vm.findBootstrapClassMirror(String.class.getName());
	
	ObjectArrayMirror parameterTypes = Reflection.toArray(classClass, constructor.getParameterTypes());
	// TODO-RS: Check this against LTW behaviour
	int numParams = parameterTypes.length();
	ObjectArrayMirror parameterNames = (ObjectArrayMirror)stringClass.newArray(numParams);
	for (int i = 0; i < numParams; i++) {
	    parameterNames.set(i, Reflection.makeString(vm, "arg" + i));
	}
	ObjectArrayMirror exceptionTypes = Reflection.toArray(classClass, constructor.getParameterTypes());
	return (InstanceMirror)new MethodHandle() {
	    protected void methodCall() throws Throwable {
		((Factory)null).makeConstructorSig(0, null, null, null, null);
	    }
	}.invoke(factory, constructor.getModifiers(), constructor.getDeclaringClass(), 
		parameterTypes, parameterNames, exceptionTypes);
    }
    
    private InstanceMirror makeMethodSignature(MethodMirror method) {
	InstanceMirror factory = getAJFactory(method.getDeclaringClass());
	
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
	}.invoke(factory, method.getModifiers(), Reflection.makeString(vm, method.getName()), method.getDeclaringClass(), 
		parameterTypes, parameterNames, exceptionTypes, method.getReturnType());
    }
    
    public InstanceMirror makeStaticJoinPoint(String kind, ConstructorMirror constructor) {
	InstanceMirror factory = getAJFactory(constructor.getDeclaringClass());
	InstanceMirror signature = makeConstructorSignature(constructor);
	return (InstanceMirror)new MethodHandle() {
	    protected void methodCall() throws Throwable {
		((Factory)null).makeSJP(null, null, null);
	    }
	}.invoke(factory, StringStubs.internMirror(Reflection.makeString(vm, kind)), signature, null);
    }
    
    public InstanceMirror makeStaticJoinPoint(String kind, MethodMirror method) {
	InstanceMirror factory = getAJFactory(method.getDeclaringClass());
	InstanceMirror signature = makeMethodSignature(method);
	return (InstanceMirror)new MethodHandle() {
	    protected void methodCall() throws Throwable {
		((Factory)null).makeSJP(null, null, null);
	    }
	}.invoke(factory, StringStubs.internMirror(Reflection.makeString(vm, kind)), signature, null);
    }
    

    private InstanceMirror makeStaticJoinPoint(MirrorEvent event) {
	if (event instanceof ConstructorMirrorEntryEvent) {
	    return makeStaticJoinPoint(org.aspectj.lang.JoinPoint.METHOD_EXECUTION, ((ConstructorMirrorEntryEvent)event).constructor());
	} else if (event instanceof ConstructorMirrorExitEvent) {
	    return makeStaticJoinPoint(org.aspectj.lang.JoinPoint.METHOD_EXECUTION, ((ConstructorMirrorExitEvent)event).constructor());
	} else if (event instanceof MethodMirrorEntryEvent) {
	    return makeStaticJoinPoint(org.aspectj.lang.JoinPoint.METHOD_EXECUTION, ((MethodMirrorEntryEvent)event).method());
	} else if (event instanceof MethodMirrorExitEvent) {
	    return makeStaticJoinPoint(org.aspectj.lang.JoinPoint.METHOD_EXECUTION, ((MethodMirrorExitEvent)event).method());
	} else {
	    throw new IllegalArgumentException("Unsupported event type: " + event);
	}
    }
}
