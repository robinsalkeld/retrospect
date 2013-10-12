package edu.ubc.retrospect;

import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.ast.And;
import org.aspectj.weaver.ast.Call;
import org.aspectj.weaver.ast.FieldGetCall;
import org.aspectj.weaver.ast.HasAnnotation;
import org.aspectj.weaver.ast.ITestVisitor;
import org.aspectj.weaver.ast.Instanceof;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Not;
import org.aspectj.weaver.ast.Or;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.internal.tools.MatchingContextBasedTest;

import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;

public class MirrorTestEvaluator implements ITestVisitor {

    private MirrorWorld world;
    private boolean success;
    
    public static boolean evaluate(Test t) {
        MirrorTestEvaluator e = new MirrorTestEvaluator();
        t.accept(e);
        return e.success;
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
        MethodMirror method = ((MethodMirrorMember)call.getMethod()).getMethod();
        
        // TODO-RS: Will need this for CFlow...
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(FieldGetCall fieldGetCall) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(HasAnnotation hasAnnotation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void visit(MatchingContextBasedTest matchingContextTest) {
        throw new UnsupportedOperationException();
    }
}
