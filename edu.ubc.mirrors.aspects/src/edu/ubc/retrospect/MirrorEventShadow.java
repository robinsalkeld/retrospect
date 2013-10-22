package edu.ubc.retrospect;

import java.util.Collections;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ConstructorMirrorEntryEvent;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.ThreadMirror;

public abstract class MirrorEventShadow extends Shadow {

    protected final MirrorWorld world;
    protected final MirrorEvent event;
    private MirrorEvaluator evaluator;
    
    protected MirrorEventShadow(MirrorWorld world, MirrorEvent event, Shadow.Kind kind, Member signature, Shadow enclosingShadow) {
        super(kind, signature, enclosingShadow);
        this.world = world;
        this.event = event;
    }

    @Override
    public World getIWorld() {
        return world;
    }

    public MirrorEvaluator getEvaluator() {
        if (evaluator == null) {
            evaluator = new MirrorEvaluator(world, getThread());
        }
        return evaluator;
    }
    
    public Object evaluateExpr(Expr expr) {
        return getEvaluator().evaluateExpr(expr);
    }
    
    public boolean evaluateTest(Test test) {
        return getEvaluator().evaluateTest(test);
    }
    
    public Object evaluateCall(InstanceMirror obj, Member method, Expr[] args) {
        return getEvaluator().evaluateCall(obj, method, args);
    }
    
    
    public abstract boolean isEntry();
    
    @Override
    protected void prepareForMungers() {
        if (isEntry()) {
            Collections.reverse(mungers);
        }
    }
    
    @Override
    public ISourceLocation getSourceLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    public abstract ThreadMirror getThread();
    
    protected Object getThis() {
        return getThread().getStackTrace().get(0).thisObject();
    }
    
    protected Object getArgument(int i) {
        return getThread().getStackTrace().get(0).arguments().get(i);
    }
    
    protected ResolvedType getJoinPointStaticPartType() {
        return world.resolve(UnresolvedType.forName(JoinPoint.StaticPart.class.getName()));
    }
    
    protected ResolvedType getJoinPointType() {
        return world.resolve(UnresolvedType.forName(JoinPoint.class.getName()));
    }
    
    public static MirrorEventShadow make(MirrorWorld world, MirrorEvent event) {
        if (event instanceof ConstructorMirrorEntryEvent) {
            ConstructorMirrorEntryEvent cmee = (ConstructorMirrorEntryEvent)event;
            Member signature = ConstructorMirrorMember.make(world, cmee.constructor());
            return new ConstructorMirrorEntryShadow(world, cmee, signature, null);
        } else if (event instanceof ConstructorMirrorExitEvent) {
            ConstructorMirrorExitEvent cmee = (ConstructorMirrorExitEvent)event;
            Member signature = ConstructorMirrorMember.make(world, cmee.constructor());
            return new ConstructorMirrorExitShadow(world, cmee, signature, null);
        } else if (event instanceof MethodMirrorEntryEvent) {
            MethodMirrorEntryEvent mmee = (MethodMirrorEntryEvent)event;
            Member signature = MethodMirrorMember.make(world, mmee.method());
            return new MethodMirrorEntryShadow(world, mmee, signature, null);
        } else if (event instanceof MethodMirrorExitEvent) {
            MethodMirrorExitEvent mmee = (MethodMirrorExitEvent)event;
            Member signature = MethodMirrorMember.make(world, mmee.method());
            return new MethodMirrorExitShadow(world, mmee, signature, null);
        } else {
            throw new IllegalArgumentException();
        }
    }
    

    @Override
    public Var getThisVar() {
        return null;
    }

    @Override
    public Var getTargetVar() {
        return null;
    }

    @Override
    public Var getArgVar(int i) {
        return null;
    }

    @Override
    public Var getThisJoinPointVar() {
        return null;
    }

    @Override
    public Var getThisJoinPointStaticPartVar() {
        return null;
    }

    @Override
    public Var getThisEnclosingJoinPointStaticPartVar() {
        return null;
    }

    @Override
    public Var getKindedAnnotationVar(UnresolvedType forAnnotationType) {
        return null;
    }

    @Override
    public Var getWithinAnnotationVar(UnresolvedType forAnnotationType) {
        return null;
    }

    @Override
    public Var getWithinCodeAnnotationVar(UnresolvedType forAnnotationType) {
        return null;
    }

    @Override
    public Var getThisAnnotationVar(UnresolvedType forAnnotationType) {
        return null;
    }

    @Override
    public Var getTargetAnnotationVar(UnresolvedType forAnnotationType) {
        return null;
    }

    @Override
    public Var getArgAnnotationVar(int i, UnresolvedType forAnnotationType) {
        return null;
    }

    @Override
    public Member getEnclosingCodeSignature() {
        return null;
    }
    
    @Override
    public Var getThisAspectInstanceVar(ResolvedType aspectType) {
        return null;
    }
    
    @Override
    public String toString() {
        return (isEntry() ? "entering " : "exiting ") + super.toString();
    }
}
