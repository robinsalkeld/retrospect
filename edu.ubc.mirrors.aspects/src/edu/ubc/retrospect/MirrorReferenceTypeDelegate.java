package edu.ubc.retrospect;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.aspectj.weaver.AbstractReferenceTypeDelegate;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.AnnotationTargetKind;
import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedPointcutDefinition;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.patterns.Declare;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerSingleton;
import org.aspectj.weaver.patterns.Pointcut;

import edu.ubc.mirrors.AnnotationMirror;
import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ConstructorMirror;
import edu.ubc.mirrors.FieldMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.Reflection;

public class MirrorReferenceTypeDelegate extends AbstractReferenceTypeDelegate {

    final ClassMirror klass;
    
    private boolean membersResolved = false;
    private ResolvedMember[] declaredMethods;
    private ResolvedMember[] declaredPointcuts;
    private ResolvedMember[] declaredFields;
    
    private InstanceMirror instance;
    
    public MirrorReferenceTypeDelegate(ReferenceType resolvedTypeX, ClassMirror klass) {
        super(resolvedTypeX, true);
        this.klass = klass;
    }

    private MirrorWorld getWorld() {
        return (MirrorWorld)getResolvedTypeX().getWorld();
    }
    
    public static String getPointcutName(MethodMirror method) {
        String name = method.getName();
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
    
    private Object memberForMethodMirror(MethodMirror m) {
        AnnotationMirror annot = Reflection.getAnnotation(m.getAnnotations(), getWorld().getPointcutAnnotClass());
        if (annot != null && !Modifier.isAbstract(m.getModifiers())) {
            String parameterNamesString = (String)annot.getValue("argNames");
            String[] parameterNames = parameterNamesString.isEmpty() ? new String[0] : parameterNamesString.split(",");
            
            UnresolvedType[] parameterTypes = new UnresolvedType[m.getParameterTypeNames().size()];
            for (int i = 0; i < parameterTypes.length; i++) {
                parameterTypes[i] = UnresolvedType.forName(m.getParameterTypeNames().get(i));
            }
            
            String name = getPointcutName(m);
            
            String pointcut = (String)annot.getValue("value");
            Pointcut pc = getWorld().parsePointcut(pointcut);
            
            ResolvedPointcutDefinition def = new ResolvedPointcutDefinition(getResolvedTypeX(), m.getModifiers(), name,
                    parameterTypes, pc);
            def.setParameterNames(parameterNames);
            
            return def;
        } else {
            return MethodMirrorMember.make(getWorld(), m);
        }
    }
    
    private void resolveMembers() {
        if (!membersResolved) {
            List<ResolvedMember> methods = new ArrayList<ResolvedMember>();
            List<ResolvedMember> pointcuts = new ArrayList<ResolvedMember>();
            for (MethodMirror m : klass.getDeclaredMethods(false)) {
                Object member = memberForMethodMirror(m);
                if (member instanceof ResolvedPointcutDefinition) {
                    pointcuts.add((ResolvedPointcutDefinition)member);
                } else if (member instanceof MethodMirrorMember) {
                    methods.add((MethodMirrorMember)member);
                }                
            }
            
            declaredMethods = methods.toArray(new ResolvedMember[methods.size()]);
            declaredPointcuts = pointcuts.toArray(new ResolvedMember[pointcuts.size()]);
            
            List<ResolvedMember> fields = new ArrayList<ResolvedMember>();
            for (FieldMirror field : klass.getDeclaredFields()) {
                fields.add(FieldMirrorMember.make(getWorld(), field));            
            }
            
            declaredFields = fields.toArray(new ResolvedMember[fields.size()]);
            
            membersResolved = true;
            
            for (Member m : declaredPointcuts) {
                ResolvedPointcutDefinition rpc = (ResolvedPointcutDefinition)m;
                rpc.setPointcut(getWorld().resolvePointcut(rpc, rpc.getPointcut()));
            }
        }
    }
    
    @Override
    public boolean isAspect() {
        return getWorld().isAspect(klass);
    }

    @Override
    public boolean isAnnotationStyleAspect() {
        return isAspect();
    }

    @Override
    public boolean isInterface() {
        return klass.isInterface();
    }

    @Override
    public boolean isEnum() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isAnnotation() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getRetentionPolicy() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean canAnnotationTargetType() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public AnnotationTargetKind[] getAnnotationTargetKinds() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isAnnotationWithRuntimeRetention() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isGeneric() {
        return false;
    }

    @Override
    public boolean isAnonymous() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNested() {
        return klass.getEnclosingClassMirror() != null;
    }

    @Override
    public boolean hasAnnotation(UnresolvedType ofType) {
        return Reflection.getAnnotation(klass.getAnnotations(), getWorld().mirrorForType(getWorld().resolve(ofType))) != null;
    }

    @Override
    public AnnotationAJ[] getAnnotations() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResolvedType[] getAnnotationTypes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResolvedMember[] getDeclaredFields() {
        resolveMembers();
        return declaredFields;
    }

    @Override
    public ResolvedType[] getDeclaredInterfaces() {
        List<ClassMirror> interfaceMirrors = klass.getInterfaceMirrors();
        ResolvedType[] result = new ResolvedType[interfaceMirrors.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = forClassMirror(interfaceMirrors.get(i));
        }
        return result;
    }

    @Override
    public ResolvedMember[] getDeclaredMethods() {
        resolveMembers();
        return declaredMethods;
    }

    @Override
    public ResolvedMember[] getDeclaredPointcuts() {
        resolveMembers();
        return declaredPointcuts;
    }

    @Override
    public TypeVariable[] getTypeVariables() {
        return null;
    }

    @Override
    public int getModifiers() {
        return klass.getModifiers();
    }

    @Override
    public PerClause getPerClause() {
        // TODO-RS
        return new PerSingleton();
    }

    @Override
    public Collection<Declare> getDeclares() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ConcreteTypeMunger> getTypeMungers() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ResolvedMember> getPrivilegedAccesses() {
        return Collections.emptyList();
    }

    @Override
    public ResolvedType getSuperclass() {
        ClassMirror superClassMirror = klass.getSuperClassMirror();
        return superClassMirror != null ? forClassMirror(superClassMirror) : null;
    }

    @Override
    public WeaverStateInfo getWeaverState() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDeclaredGenericSignature() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResolvedType getOuterClass() {
        return forClassMirror(klass.getEnclosingClassMirror());
    }

    
    private ResolvedType forClassMirror(ClassMirror klass) {
        return getWorld().resolve(klass);
    }
}
