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
import java.io.IOException;
import java.util.Collection;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.internal.acquire.HeapDumpProviderDescriptor;
import org.eclipse.mat.internal.acquire.HeapDumpProviderRegistry;
import org.eclipse.mat.snapshot.acquire.IHeapDumpProvider;
import org.eclipse.mat.snapshot.acquire.VmInfo;
import org.eclipse.mat.util.IProgressListener;

public class HeapDumper {

    public static File dumpHeap(int pid, IProgressListener listener) throws SnapshotException, IOException, InterruptedException {
        Collection<HeapDumpProviderDescriptor> dumpProviders = HeapDumpProviderRegistry.instance().getHeapDumpProviders();
        HeapDumpProviderDescriptor descriptor = dumpProviders.iterator().next();
        final IHeapDumpProvider provider = descriptor.getHeapDumpProvider();
        
        VmInfo info = null;
        for (VmInfo v : provider.getAvailableVMs(null)) {
            if (v.getPid() == pid) {
                info = v;
                break;
            }
        }
        if (info == null) {
            throw new InternalError("Invalid pid: " + pid);
        }
        final VmInfo theInfo = info;
        
        final File preferredLocation = new File(System.getProperty("user.home") + "/snapshots/" + pid + ".hprof/");
        preferredLocation.delete();
        
//        Process p = Runtime.getRuntime().exec("jmap -dump:format=b,file=" + preferredLocation.getAbsolutePath() + " " + pid);
//        System.out.println(p.waitFor());
//        return preferredLocation;
        
        return provider.acquireDump(theInfo, preferredLocation, listener);
    }
}
