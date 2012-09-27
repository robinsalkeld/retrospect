package edu.ubc.retrospect;

import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Before;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.mirages.MethodHandle;
import edu.ubc.mirrors.mirages.Reflection;

import java_cup.runtime.Symbol;
import polyglot.ast.Node;
import polyglot.ast.NodeFactory;
import polyglot.frontend.Job;
import polyglot.types.Flags;
import polyglot.util.StdErrorQueue;
import polyglot.visit.AmbiguityRemover;
import polyglot.visit.NodeVisitor;
import polyglot.visit.StringPrettyPrinter;
import abc.aspectj.ExtensionInfo;
import abc.aspectj.ast.AJNodeFactory_c;
import abc.aspectj.ast.PCName;
import abc.aspectj.ast.PCName_c;
import abc.aspectj.ast.Pointcut;
import abc.aspectj.parse.Grm;
import abc.aspectj.parse.Lexer_c;
import abc.aspectj.types.AJTypeSystem;
import abc.aspectj.types.AJTypeSystem_c;
import abc.aspectj.visit.InitClasses;
import abc.aspectj.visit.NoSourceJob;
import abc.main.CompilerAbortedException;

public class AspectJMirrors {

    private final VirtualMachineMirror vm;
    private final ThreadMirror thread;
    private final ClassMirrorLoader loader;
    
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
    
	
    // Used for pointcut declarations as well (annotation.getClassMirror() == pointcutClass)
    public class AdviceMirror {
	// Name of the pointcut or
	// Name of the advice - @AdviceName for code-style advice, method name for annotation-style advice
	private final String name;
	// @Before, @Pointcut, etc
	private final AdviceKind kind;
	// This holds the formal declarations
	private final ObjectMirror method;
	// Null for abstract pointcuts. Non-final because of the resolution phase.
	private Pointcut pc;
	
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
	    
	    int pointcutFlags = (Integer)Reflection.invokeMethodHandle(method, new MethodHandle() {
		protected void methodCall() throws Throwable {
		    ((Method)null).getModifiers();
		}   
	    });
	    if (Modifier.isAbstract(pointcutFlags)) {
		pc = null;
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
		ObjectArrayMirror parameterTypes = (ObjectArrayMirror)Reflection.invokeMethodHandle(method, new MethodHandle() {
		    protected void methodCall() throws Throwable {
			((Method)null).getParameterTypes();
		    } 
		}); 
		for (int j = 0; j < parameterNames.length; j++) {
		    ClassMirror type = (ClassMirror)parameterTypes.get(j);
		    locals.put(parameterNames[j], type.getClassName());
		}

		pc = parsePointcut(locals, pointcut);
	    }
	}
    }

    public AdviceMirror getAdviceForMethod(ObjectMirror method) {
	for (AdviceKind kind : AdviceKind.values()) {
	    ObjectMirror annot = (ObjectMirror)Reflection.invokeMethodHandle(method, new MethodHandle() {
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
	
	public AspectMirror(ClassMirror klass) {
	    this.klass = klass;
	    
	    ObjectArrayMirror methods = (ObjectArrayMirror)Reflection.invokeMethodHandle(klass, new MethodHandle() {
		protected void methodCall() throws Throwable {
		    ((Class<?>)null).getDeclaredMethods();
		}
	    });
	    int n = methods.length();
	    for (int i = 0; i < n; i++) {
		ObjectMirror method = methods.get(i);
		AdviceMirror advice = getAdviceForMethod(method);
		if (advice != null) {
		    adviceDecls.add(advice);
		    if (advice.kind == AdviceKind.POINTCUT) {
			pointcutDecls.put(advice.name, advice);
		    }
		}
	    }
	}
    }



    private final ExtensionInfo ext = new ExtensionInfo(Collections.<String>emptyList(), Collections.<String>emptyList());
    private final StdErrorQueue eq = new StdErrorQueue(System.err, 80, "pointcut parsing");
    private final AJTypeSystem ts = new AJTypeSystem_c();
    private final NodeFactory nf = new AJNodeFactory_c();
    private final Job job = new NoSourceJob(ext);
    
    private final Map<String, AspectMirror> aspects = new HashMap<String, AspectMirror>();
    
    static {
	// Has to be called for the side-effect of setting the AbcExtension,
        // since the Lexer_c constructor reads it. Grr.
        try {
            new abc.main.Main(new String[] { "foo" } );
        } catch (CompilerAbortedException e) {
            // Don't care.
        }
    }
    
    public AspectJMirrors(ClassMirrorLoader loader) throws ClassNotFoundException {
        this.loader = loader;
	this.vm = loader.getClassMirror().getVM();
        this.thread = vm.getThreads().get(0);
    }
    
    public Pointcut parsePointcut(Map<String, String> locals, String expr) {
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
        
        Pointcut pc = (Pointcut)result.value;
        
        new InitClasses(ExtensionInfo.INIT_CLASSES, ext, ts).run();
        
        AmbiguityRemover v = new AmbiguityRemover(job, ts, nf, AmbiguityRemover.ALL);
        v = (AmbiguityRemover)v.begin();
        for (Map.Entry<String, String> entry : locals.entrySet()) {
            v.context().addVariable(ts.localInstance(null,Flags.FINAL,null,entry.getKey()));
        }
	pc = (Pointcut)pc.visit(v);
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
		return pcDecl.pc;
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
	    for (AdviceMirror advice : aspect.adviceDecls) {
		advice.pc = (Pointcut)advice.pc.visit(v);
		System.out.println(new StringPrettyPrinter(Integer.MAX_VALUE).toString(advice.pc));
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
}
