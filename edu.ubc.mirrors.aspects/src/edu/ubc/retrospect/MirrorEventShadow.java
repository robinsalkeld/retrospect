package edu.ubc.retrospect;

import java.util.Collections;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.MemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirrorEntryEvent;
import edu.ubc.mirrors.ConstructorMirrorExitEvent;
import edu.ubc.mirrors.FieldMirrorGetEvent;
import edu.ubc.mirrors.FieldMirrorSetEvent;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirrorEntryEvent;
import edu.ubc.mirrors.MethodMirrorExitEvent;
import edu.ubc.mirrors.MirrorEvent;
import edu.ubc.mirrors.MirrorLocationEvent;
import edu.ubc.mirrors.ThreadMirror;

public abstract class MirrorEventShadow extends Shadow {

    protected final MirrorWorld world;
    protected final MirrorEvent event;
    private MirrorEvaluator evaluator;
    
    public static final Object SHADOW_KIND_PROPERTY_KEY = new Object();
    public static final Object IS_ENTRY_PROPERTY_KEY = new Object();
    
    protected MirrorEventShadow(MirrorWorld world, MirrorEvent event, Shadow.Kind kind, Member signature, Shadow enclosingShadow) {
        super(kind, signature, enclosingShadow);
        this.world = world;
        this.event = event;
    }

    /**
     * This equality is defined in terms of shadows, not instantaneous events.
     * That is, two events that arise from the same place in the code, but perhaps at
     * different times with different dynamic properties, should still compare equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        return equals((MirrorEventShadow)obj);
    }
    
    protected abstract boolean equals(MirrorEventShadow other);
    
    @Override
    public abstract int hashCode();
    
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
    
    @Override
    public ResolvedType getEnclosingType() {
        return world.resolve(getDeclaringClass());
    }
    
    protected abstract ClassMirror getDeclaringClass();
    
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
    
    protected InstanceMirror getThis() {
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
        Shadow.Kind shadowKind = (Shadow.Kind)event.request().getProperty(SHADOW_KIND_PROPERTY_KEY); 
        Boolean isEntry = (Boolean)event.request().getProperty(IS_ENTRY_PROPERTY_KEY); 
        
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
            if (shadowKind == Shadow.SynchronizationLock) {
                return new SynchronizedMethodMirrorEntryShadow(world, mmee, MemberImpl.monitorEnter(), null);
            } else {
                Member signature = MethodMirrorMember.make(world, mmee.method());
                return new MethodMirrorEntryShadow(world, mmee, signature, null);
            }
        } else if (event instanceof MethodMirrorExitEvent) {
            MethodMirrorExitEvent mmee = (MethodMirrorExitEvent)event;
            if (shadowKind == Shadow.SynchronizationUnlock) {
                return new SynchronizedMethodMirrorExitShadow(world, mmee, MemberImpl.monitorExit(), null);
            } else {
                Member signature = MethodMirrorMember.make(world, mmee.method());
                return new MethodMirrorExitShadow(world, mmee, signature, null);
            }
        } else if (event instanceof FieldMirrorGetEvent) {
            FieldMirrorGetEvent fmge = (FieldMirrorGetEvent)event;
            Member signature = FieldMirrorMember.make(world, fmge.field());
            return new FieldMirrorGetShadow(world, fmge, signature, null);
        } else if (event instanceof FieldMirrorSetEvent) {
            FieldMirrorSetEvent fmge = (FieldMirrorSetEvent)event;
            Member signature = FieldMirrorMember.make(world, fmge.field());
            return new FieldMirrorSetShadow(world, fmge, signature, null);
        } else if (shadowKind != null && event instanceof MirrorLocationEvent) {
            MirrorLocationEvent mle = (MirrorLocationEvent)event;
            if (shadowKind == Shadow.SynchronizationLock) {
                return new MirrorMonitorEnterShadow(world, mle, isEntry, MemberImpl.monitorEnter(), null);
            } else {
                return new MirrorMonitorExitShadow(world, mle, isEntry, MemberImpl.monitorExit(), null);
            }
            
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    public static MirrorEventShadow makeForSynchronization(MirrorWorld world, MirrorEvent event) {
        if (event instanceof MethodMirrorEntryEvent) {
            MethodMirrorEntryEvent mmee = (MethodMirrorEntryEvent)event;
            Member signature = MethodMirrorMember.make(world, mmee.method());
            return new MethodMirrorEntryShadow(world, mmee, signature, null);
        } else if (event instanceof MethodMirrorExitEvent) {
            MethodMirrorExitEvent mmee = (MethodMirrorExitEvent)event;
            Member signature = MethodMirrorMember.make(world, mmee.method());
            return new MethodMirrorExitShadow(world, mmee, signature, null);
        } else if (event instanceof FieldMirrorGetEvent) {
            FieldMirrorGetEvent fmge = (FieldMirrorGetEvent)event;
            Member signature = FieldMirrorMember.make(world, fmge.field());
            return new FieldMirrorGetShadow(world, fmge, signature, null);
        } else if (event instanceof FieldMirrorSetEvent) {
            FieldMirrorSetEvent fmge = (FieldMirrorSetEvent)event;
            Member signature = FieldMirrorMember.make(world, fmge.field());
            return new FieldMirrorSetShadow(world, fmge, signature, null);
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
        ResolvedType joinPointStaticPartType = world.resolve(UnresolvedType.forName(JoinPoint.StaticPart.class.getName()));
        return new MirrorEventVar(joinPointStaticPartType, world.makeStaticJoinPoint(getThread(), event));
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
