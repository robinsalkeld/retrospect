package edu.ubc.retrospect;

import java.util.List;

import com.sun.jdi.event.MethodEntryEvent;

import abc.aspectj.ast.ClassTypeDotId;
import abc.aspectj.ast.ClassnamePatternExpr;
import abc.aspectj.ast.ConstructorPattern;
import abc.aspectj.ast.DotDotFormalPattern;
import abc.aspectj.ast.FormalPattern;
import abc.aspectj.ast.MethodPattern;
import abc.aspectj.ast.SimpleNamePattern;
import abc.aspectj.ast.TPEBinary;
import abc.aspectj.ast.TPENot;
import abc.aspectj.ast.TPERefTypePat;
import abc.aspectj.ast.TPEType;
import abc.aspectj.ast.TPEUniversal;
import abc.aspectj.ast.ThrowsPattern;
import abc.aspectj.ast.TypeFormalPattern;
import abc.aspectj.ast.TypePatternExpr;
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
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.retrospect.AspectJMirrors.AdviceKind;
import edu.ubc.retrospect.AspectJMirrors.AdviceMirror;
import edu.ubc.retrospect.AspectJMirrors.AspectMirror;

public class RetroactiveWeaver {
    
    public static void weave(ClassMirror aspect) throws ClassNotFoundException {
        VirtualMachineMirror vm = aspect.getVM();
        ThreadHolograph threadHolograph = (ThreadHolograph)vm.getThreads().get(0);
        threadHolograph.enterHologramExecution();
        
        ClassMirrorLoader loader = aspect.getLoader();
        AspectJMirrors mirrors = new AspectJMirrors(loader);
        AspectMirror aspectMirror = mirrors.getAspectMirror(aspect);
        mirrors.resolve();
        for (AdviceMirror advice : aspectMirror.getAdvice()) {
            adviceMatchesEvent(advice.getKind(), advice.getPointcut().makeAIPointcut(), null, true);
            adviceMatchesEvent(advice.getKind(), advice.getPointcut().makeAIPointcut(), null, false);
        }
        
        threadHolograph.exitHologramExecution();
    }
    
    
    
}

