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
package edu.ubc.mirrors.test;

import java.io.File;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import com.sun.jdi.VirtualMachine;

public class SnapshotAndPrepare implements IApplication {
    public void main(String[] args) throws Exception {
        int port = Integer.valueOf(args[0]);
        
        final VirtualMachine jdiVM = JDIUtils.connectOnPort(port, null, null);
        System.out.println("Connected.");
        
        File snapshotPath = LiveVersusDeadToStringEvaluation.pauseAndDump(jdiVM);
        
        LiveVersusDeadToStringEvaluation.createPreparedVMHolographForSnapshot(snapshotPath);
    }
    
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
}
