package edu.ubc.retrospect;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMemberImpl;
import org.aspectj.weaver.UnresolvedType;

import edu.ubc.mirrors.ConstructorMirror;

public class ConstructorMirrorMember extends ResolvedMemberImpl {

    private final ConstructorMirror cons;
    
    private ConstructorMirrorMember(ConstructorMirror cons, UnresolvedType declaringType, int modifiers, UnresolvedType returnType, 
            String name, UnresolvedType[] parameterTypes) {
        super(Member.CONSTRUCTOR, declaringType, modifiers, returnType, name, parameterTypes);
        this.cons = cons;
    }

    public ConstructorMirror getConstructor() {
        return cons;
    }
    
    public static ConstructorMirrorMember make(MirrorWorld world, ConstructorMirror cons) {
        UnresolvedType declaringType = world.resolve(cons.getDeclaringClass());
        UnresolvedType returnType = declaringType;
        
        UnresolvedType[] parameterTypes = new UnresolvedType[cons.getParameterTypeNames().size()];
        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = UnresolvedType.forName(cons.getParameterTypeNames().get(i));
        }
        
        return new ConstructorMirrorMember(cons, declaringType, cons.getModifiers(), returnType, "<init>", parameterTypes);
    }
}
