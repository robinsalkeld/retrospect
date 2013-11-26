/*******************************************************************************
 * Copyright (c) 2013 Robin Salkeld
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package edu.ubc.retrospect;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.aspectj.weaver.ConcreteTypeMunger;
import org.aspectj.weaver.CrosscuttingMembersSet;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.loadtime.definition.Definition;
import org.aspectj.weaver.loadtime.definition.DocumentParser;

import edu.ubc.mirrors.Callback;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.InputStreamMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodHandle;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;

public class MirrorWeaver {
    
    private final MirrorWorld world;
    private final CrosscuttingMembersSet xcutSet;
    
    public MirrorWeaver(VirtualMachineMirror vm, ClassMirrorLoader loader, ThreadMirror thread) throws ClassNotFoundException, NoSuchMethodException, MirrorInvocationTargetException {
        this.world = new MirrorWorld(vm, loader, thread);
        this.xcutSet = new CrosscuttingMembersSet(world);
        
        world.resolve(UnresolvedType.forName(ClassLoader.class.getName()));
    }

    public MirrorWorld getWorld() {
        return world;
    }
    
    public void weave() throws ClassNotFoundException, NoSuchMethodException, InterruptedException, MirrorInvocationTargetException {
        Reflection.withThread(world.thread, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ThreadHolograph.raiseMetalevel();
        
                System.out.println("Loading weaving configuration...");
                String definitionPath = "META-INF/aop-ajc.xml";
                InstanceMirror definitionStream = (InstanceMirror)Reflection.invokeMethodHandle(world.loader, world.thread, new MethodHandle() {
                    protected void methodCall() throws Throwable {
                        ((ClassLoader)null).getResourceAsStream((String)null);
                    }
                }, world.vm.makeString("META-INF/aop-ajc.xml"));
                FakeURLStreamHandler handler = new FakeURLStreamHandler(new InputStreamMirror(world.thread, definitionStream));
                Definition definition;
                try {
                    URL fakeURL = new URL("file", "", 0, definitionPath, handler);
                    definition = DocumentParser.parse(fakeURL);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                
                System.out.println("Loading aspects...");
                for (String aspectClassName : definition.getAspectClassNames()) {
                    ReferenceType aspectType = (ReferenceType)world.resolve(aspectClassName);
                    xcutSet.addOrReplaceAspect(aspectType);
                }
                
                System.out.println("Weaving aspects...");
                for (ConcreteTypeMunger munger : xcutSet.getTypeMungers()) {
                    MirrorTypeMunger mirrorMunger = (MirrorTypeMunger)munger;
                    mirrorMunger.munge(MirrorWeaver.this);
                }
                
                final Set<MirrorEventShadow> joinpointShadows = new HashSet<MirrorEventShadow>();
                for (ShadowMunger munger : xcutSet.getShadowMungers()) {
                    AdviceMirror advice = (AdviceMirror)munger;
                    System.out.println("Installing event requests for advice: " + advice);
                    PointcutMirrorRequestExtractor.installCallback(world, advice, new Callback<MirrorEventShadow>() {
                        public void handle(MirrorEventShadow shadow) {
                            if (shadow.getDeclaringClass().getLoader() != null) {
                                joinpointShadows.add(shadow);
                            }
                        }
                    });
                    System.out.println("Done.");
                }
                
                world.vm.dispatch().addSetCallback(new Runnable() {
                    public void run() {
                        for (MirrorEventShadow shadow : joinpointShadows) {
                            for (ShadowMunger munger : xcutSet.getShadowMungers()) {
                                if (munger.match(shadow, world)) {
                                    shadow.addMunger(munger);
                                }
                            }
                            shadow.implement();
                        }
                        joinpointShadows.clear();
                    }
                });
                
                ThreadHolograph.lowerMetalevel();
                
                return null;
            }
        });
    }
}

