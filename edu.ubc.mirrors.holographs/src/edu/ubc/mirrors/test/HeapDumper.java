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
        
        final File preferredLocation = new File("/Users/robinsalkeld/Documents/UBC/Code/snapshots/" + pid + ".hprof/");
        preferredLocation.delete();
        
//        Process p = Runtime.getRuntime().exec("jmap -dump:format=b,file=" + preferredLocation.getAbsolutePath() + " " + pid);
//        System.out.println(p.waitFor());
//        return preferredLocation;
        
        return provider.acquireDump(theInfo, preferredLocation, listener);
    }
}
