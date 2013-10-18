package edu.ubc.retrospect;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ast.And;
import org.aspectj.weaver.ast.Call;
import org.aspectj.weaver.ast.CallExpr;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.FieldGetCall;
import org.aspectj.weaver.ast.HasAnnotation;
import org.aspectj.weaver.ast.ITestVisitor;
import org.aspectj.weaver.ast.Instanceof;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Not;
import org.aspectj.weaver.ast.Or;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.internal.tools.MatchingContextBasedTest;

import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;

public class MirrorEvaluator implements ITestVisitor {

    private final MirrorWorld world;
    private final ThreadMirror thread;
    private boolean success;
    
    public MirrorEvaluator(MirrorWorld world, ThreadMirror thread) {
        this.world = world;
        this.thread = thread;
    }

    public boolean evaluateTest(Test t) {
        t.accept(this);
        return success;
    }
    
    @Override
    public void visit(And e) {
        e.getLeft().accept(this);
        if (!success) return;
        e.getRight().accept(this);
    }

    @Override
    public void visit(Instanceof i) {
        MirrorEventVar var = (MirrorEventVar)i.getVar();
        if (var instanceof ObjectMirror) {
            ResolvedType targetType = world.resolve(i.getType());
            success = targetType.isAssignableFrom(world.resolve(((ObjectMirror)var).getClassMirror()));
        } else {
            // etc for primitive values
            success = false;
        }
    }

    @Override
    public void visit(Not not) {
        not.getBody().accept(this);
        success = !success;
    }

    @Override
    public void visit(Or or) {
        or.getLeft().accept(this);
        if (success) return;
        or.getRight().accept(this);
    }

    @Override
    public void visit(Literal literal) {
        success = (literal == Literal.TRUE ? true : false);
    }

    @Override
    public void visit(Call call) {
        Object result = evaluateCall(null, call.getMethod(), call.getArgs());
        success = ((Boolean)result).booleanValue();
    }

    @Override
    public void visit(FieldGetCall fieldGetCall) {
        Member field = fieldGetCall.getField();
        
        // If this isn't wrapping a mirror member, look it up so it is.
        if (field instanceof ResolvedMemberImpl) {
            field = ((ReferenceType)field.getDeclaringType()).lookupField(field);
        }
        
        FieldMirror fieldMirror = ((FieldMirrorMember)field).getField();
        try {
            InstanceMirror obj = (InstanceMirror)fieldMirror.getDeclaringClass().getStaticFieldValues().get(fieldMirror);
            Object result = evaluateCall(obj, fieldGetCall.getMethod(), fieldGetCall.getArgs());
            success = ((Boolean)result).booleanValue();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(HasAnnotation hasAnnotation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(MatchingContextBasedTest matchingContextTest) {
        throw new UnsupportedOperationException();
    }
    
    public Object evaluateExpr(Expr expr) {
        if (expr instanceof MirrorEventVar) {
            return ((MirrorEventVar)expr).getValue();
        } else if (expr instanceof CallExpr) {
            CallExpr call = (CallExpr)expr;
            // Assuming static method
            return evaluateCall(null, call.getMethod(), call.getArgs());
        } else {
            throw new IllegalArgumentException("Unsupported expression type: " + expr);
        }
    }
    
    private Object evaluateCall(InstanceMirror obj, Member member, Expr[] args) {
        // If this isn't wrapping a mirror member, look it up so it is.
        if (member instanceof ResolvedMemberImpl) {
            member = ((ReferenceType)member.getDeclaringType()).lookupMethod(member);
        }
        
        Object[] argMirrors = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            argMirrors[i] = evaluateExpr(args[i]);
        }
        
        try {
            if (member instanceof MethodMirrorMember) {
                MethodMirror method = ((MethodMirrorMember)member).getMethod();
                return method.invoke(thread, obj, argMirrors);
            } else {
                throw new IllegalArgumentException("Unsupported member type: " + member);
            }
        } catch (IllegalArgumentException | IllegalAccessException | MirrorInvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
