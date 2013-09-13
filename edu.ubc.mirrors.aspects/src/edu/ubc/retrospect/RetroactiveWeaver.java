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

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.ClassMirrorLoader;
import edu.ubc.mirrors.MirrorInvocationTargetException;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.holographs.ThreadHolograph;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;
import edu.ubc.retrospect.AspectJMirrors.AspectMirror;

public class RetroactiveWeaver {
    
    public static void weave(ClassMirror aspect, ThreadMirror thread) throws ClassNotFoundException, NoSuchMethodException, InterruptedException, MirrorInvocationTargetException {
        VirtualMachineHolograph vm = (VirtualMachineHolograph)aspect.getVM();
        ThreadHolograph threadHolograph = (ThreadHolograph)thread;
        threadHolograph.enterHologramExecution();
        try {
            ThreadHolograph.raiseMetalevel();
            ClassMirrorLoader loader = aspect.getLoader();
            AspectJMirrors mirrors = new AspectJMirrors(vm, loader, thread);
            AspectMirror aspectMirror = mirrors.getAspectMirror(aspect);
            mirrors.resolve();
            
            aspectMirror.installRequests();
            ThreadHolograph.lowerMetalevel();
            vm.dispatch().start();
        } finally {
            threadHolograph.exitHologramExecution();
        }
    }
}

