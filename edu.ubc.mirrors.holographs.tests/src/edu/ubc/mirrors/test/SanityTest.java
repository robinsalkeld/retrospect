package edu.ubc.mirrors.test;

import com.sun.jdi.VirtualMachine;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;
import junit.framework.TestCase;

public class SanityTest extends TestCase {

    public void testJRubyStackTrace() throws Exception {
        HeapDumpTest2.main(new String[] {"/Users/robinsalkeld/java_pid41658.0001.jrubyirb.hprof"});
    }
    
    public void testPrintOSGiBundles() throws Exception {
        EclipseHeapDumpTest.main(new String[] {"/Users/robinsalkeld/java_pid2675.0001.subeclipseonjava7.hprof"});
    }
    
    public void testCDTBugSetup() throws Exception {
        MethodMirror method = CDTBugTest.getNameKeyMethod("/Users/robinsalkeld/Documents/UBC/Code/snapshots/cdt_oom_bug/java_pid7720.hprof");
        ClassMirror nameClass = method.getDeclaringClass().getVM().findAllClasses(CDTBugTest.CPPASTName, false).get(0);
        VirtualMachineMirror vm = nameClass.getVM();
        ThreadMirror thread = vm.getThreads().get(0);
        ObjectMirror firstInstance = nameClass.getInstances().get(0);
        
        String result = Reflection.getRealStringForMirror((InstanceMirror)method.invoke(thread, null, firstInstance));
        assertEquals("File: _Bidit - []", result);
    }
    
    public void testLiveToStringer() throws Exception {
        VirtualMachine jdiVM = JDIVirtualMachineMirror.commandLineLaunch(
                "edu.ubc.mirrors.test.JREOnly", 
                "-cp \"/Users/robinsalkeld/Documents/UBC/Code/Retrospect/edu.ubc.mirrors.holographs.tests/bin\"",
                false);
        // Ignore the VMStartEvent
        jdiVM.eventQueue().remove();
        LiveVersusDeadToStringEvaluation.run(jdiVM);
    }
}
