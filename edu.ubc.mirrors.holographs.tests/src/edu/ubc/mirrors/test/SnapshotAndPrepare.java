package edu.ubc.mirrors.test;

import java.io.File;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import com.sun.jdi.VirtualMachine;

import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;

public class SnapshotAndPrepare implements IApplication {
    public void main(String[] args) throws Exception {
        int port = Integer.valueOf(args[0]);
        
        final VirtualMachine jdiVM = JDIUtils.connectOnPort(port);
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
