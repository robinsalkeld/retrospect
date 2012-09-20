package edu.ubc.retrospect;

import java.lang.reflect.Method;

import org.aspectj.lang.annotation.Pointcut;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.ObjectArrayMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.mirages.MethodHandle;
import edu.ubc.mirrors.mirages.Reflection;

public class RetroactiveWeaver {
    
    private static PointcutParser pointcutParser = new PointcutParser();
    
    public static void weave(ClassMirror aspect) throws ClassNotFoundException {
        VirtualMachineMirror vm = aspect.getVM();
        ThreadHolograph threadHolograph = (ThreadHolograph)vm.getThreads().get(0);
        threadHolograph.enterHologramExecution();
        ClassMirrorLoader loader = aspect.getLoader();
        
        ObjectArrayMirror methods = (ObjectArrayMirror)Reflection.invokeMethodHandle(aspect, new MethodHandle() {
            protected void methodCall() throws Throwable {
                ((Class<?>)null).getDeclaredMethods();
            }
        });
        ObjectMirror pointcutClass = Reflection.classMirrorForName(vm, threadHolograph, Pointcut.class.getName(), true, loader);
        int n = methods.length();
        for (int i = 0; i < n; i++) {
            ObjectMirror method = methods.get(i);
            ObjectMirror annot = (ObjectMirror)Reflection.invokeMethodHandle(method, new MethodHandle() {
                protected void methodCall() throws Throwable {
                    ((Method)null).getAnnotation(null);
                }
            }, pointcutClass);
            if (annot != null) {
                String pointcut = Reflection.getRealStringForMirror((InstanceMirror)Reflection.invokeMethodHandle(annot, new MethodHandle() {
                    protected void methodCall() throws Throwable {
                        ((Pointcut)null).value();
                    }
                }));
                abc.aspectj.ast.Pointcut pc = pointcutParser.parse(pointcut);
                pc.makeAIPointcut();
                System.out.println(pc);
            }
        }
        threadHolograph.exitHologramExecution();
    }
}
