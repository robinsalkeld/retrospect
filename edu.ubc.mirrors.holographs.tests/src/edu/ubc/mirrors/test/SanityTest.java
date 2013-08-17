package edu.ubc.mirrors.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.util.ConsoleProgressListener;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.eclipse.mat.plugins.ExpressionQuery;
import edu.ubc.mirrors.jdi.JDIVirtualMachineMirror;

public class SanityTest extends TestCase {

    public void testJRubyStackTrace() throws Exception {
        HeapDumpTest2.main(new String[] {"/Users/robinsalkeld/Documents/UBC/Code/RetrospectData/snapshots/jruby_irb/java_pid41658.0001.jrubyirb.hprof"});
    }
    
    public void testPrintOSGiBundles() throws Exception {
        EclipseHeapDumpTest.main(new String[] {"/Users/robinsalkeld/Documents/UBC/Code/RetrospectData/snapshots/eclipse_for_osgi_dump/java_pid2675.0001.subeclipseonjava7.hprof"});
    }
    
    public void testCDTBugSetup() throws Exception {
        MethodMirror method = CDTBugTest.getNameKeyMethod("/Users/robinsalkeld/Documents/UBC/Code/RetrospectData/snapshots/cdt_oom_bug/java_pid7720.hprof");
        ClassMirror nameClass = method.getDeclaringClass().getVM().findAllClasses(CDTBugTest.CPPASTName, false).get(0);
        VirtualMachineMirror vm = nameClass.getVM();
        ThreadMirror thread = vm.getThreads().get(0);
        ObjectMirror firstInstance = nameClass.getInstances().get(0);
        
        String result = Reflection.getRealStringForMirror((InstanceMirror)method.invoke(thread, null, firstInstance));
        assertEquals("File: _Bidit - []", result);
    }
    
    public void testExpressionQuery() throws Exception {
        ConsoleProgressListener listener = new ConsoleProgressListener(System.out);
        ISnapshot snapshot = SnapshotFactory.openSnapshot(new File("/Users/robinsalkeld/Documents/UBC/Code/snapshots/cdt_oom_bug/java_pid7720.hprof"), 
                listener);
        int objectID = snapshot.getClassesByName(CDTBugTest.CPPASTName, false).iterator().next().getObjectIds()[0];
        
        // Need to generate a JavaProject for this to work
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("dummy");
        IProgressMonitor monitor = new NullProgressMonitor();
        if (!project.exists()) {
            project.create(monitor);
            project.open(monitor);
        }
        IJavaProject javaProject = JavaCore.create(project);
        
        IProjectDescription description = project.getDescription();
        description.setNatureIds(new String[] {JavaCore.NATURE_ID});
        project.setDescription(description, monitor);
        
        List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
        entries.add(JavaCore.newLibraryEntry(new Path("/Library/Java/JavaVirtualMachines/1.7.0.jdk/Contents/Home/jre/lib/rt.jar"), null, null));
        javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
        
        ExpressionQuery query = new ExpressionQuery();
        query.expression = "toString()";
        query.snapshot = snapshot;
        query.objectIds = new int[] {objectID};
        query.execute(listener);
    }
    
    public void testLiveToStringer() throws Exception {
        final VirtualMachine jdiVM = JDIUtils.commandLineLaunch(
                "edu.ubc.mirrors.test.JREOnly", 
                "-cp \"/Users/robinsalkeld/Documents/UBC/Code/Retrospect/edu.ubc.mirrors.holographs.tests/bin\"",
                false);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    jdiVM.exit(0);
                } catch (VMDisconnectedException e) {
                    // Ignore
                }
            }
        });
        
        // Ignore the VMStartEvent
        jdiVM.eventQueue().remove();
        LiveVersusDeadToStringEvaluation.run(jdiVM);
        jdiVM.exit(0);
    }
    
    // TODO-RS: DebuggingTest
}
