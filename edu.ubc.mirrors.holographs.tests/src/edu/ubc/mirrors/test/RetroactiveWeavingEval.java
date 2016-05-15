package edu.ubc.mirrors.test;

import java.io.File;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import com.sun.jdi.VirtualMachine;

public class RetroactiveWeavingEval implements IApplication {
    
    public static void main(String[] args) throws Exception {
        String type = args[0];
        String benchmark = args[1];
        String casestudy = (args.length > 2 ? args[2] : null);
        
        String mainClass = "spec.harness.Launch";
        String programArgs = "-ikv -ict -wt 5 -ops 1 " + benchmark;
        String classPath = EvalConstants.SpecJVMJar + ":" + EvalConstants.SpecJVMLib + "/*' ";
        String vmArgs = "-cp '" + classPath + " -Dspecjvm.home.dir=" + EvalConstants.SpecJVMRoot;
        String aspectPath = EvalConstants.casestudyAspectPaths.get(casestudy);
        File hologramClassPath = new File(EvalConstants.DataRoot, type + "/SpecJVM2008/" + benchmark + "/hologram_classes");
        
        if (aspectPath == null) {
            VirtualMachine vm = JDIUtils.commandLineLaunch(mainClass + " " + programArgs, vmArgs, false, System.out, System.err);
            while (vm.eventQueue().remove() != null) {}
        } else {
            JDIMirrorWeavingLauncher.launch(mainClass, programArgs, vmArgs, aspectPath, hologramClassPath);
        }
    }

    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        try {
            main(args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public void stop() {
        
    }
}
