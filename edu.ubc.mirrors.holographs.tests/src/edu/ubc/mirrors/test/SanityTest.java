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

import static edu.ubc.mirrors.eclipse.mat.HeapDumpVirtualMachineMirror.unhash;

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

import com.sun.jdi.VirtualMachine;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.InstanceMirror;
import edu.ubc.mirrors.MethodMirror;
import edu.ubc.mirrors.ObjectMirror;
import edu.ubc.mirrors.Reflection;
import edu.ubc.mirrors.ThreadMirror;
import edu.ubc.mirrors.VirtualMachineMirror;
import edu.ubc.mirrors.eclipse.mat.plugins.ExpressionQuery;

public class SanityTest extends TestCase {

    public void testJRubyStackTrace() throws Exception {
        HeapDumpTest2.main(new String[] {"/Users/robinsalkeld/Documents/UBC/Code/RetrospectData/snapshots/jruby_irb/java_pid41658.0001.jrubyirb.hprof"});
    }
    
//    public void testPrintOSGiBundles() throws Exception {
//        EclipseHeapDumpTest.main(new String[] {"/Users/robinsalkeld/Documents/UBC/Code/RetrospectData/snapshots/eclipse_for_osgi_dump/java_pid52701.0001.hprof"});
//    }
    
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
        ISnapshot snapshot = SnapshotFactory.openSnapshot(new File("/Users/robinsalkeld/Documents/UBC/Code/RetrospectData/snapshots/cdt_oom_bug/java_pid7720.hprof"), 
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
        query.objects = new int[] {objectID};
        query.execute(listener);
    }
    
    public void testLiveToStringer() throws Exception {
        final VirtualMachine jdiVM = JDIUtils.commandLineLaunch(
                "edu.ubc.mirrors.test.JREOnly", 
                "-cp \"/Users/robinsalkeld/Documents/UBC/Code/Retrospect/edu.ubc.mirrors.holographs.tests/bin\"",
                false, null, null);
        
        // Ignore the VMStartEvent
        jdiVM.eventQueue().remove();
        LiveVersusDeadToStringEvaluation.run(jdiVM);
        jdiVM.exit(0);
    }
    
    private static int hash(int h) {
        // Copied from HashMap$Entry#hash for reference and testing.
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
    private static int hashExpanded(int h, int[] shiftOffsets) {
        int result = h;
        for (int offset : shiftOffsets) {
            result ^= h >>> offset;
        }
        return result;
    }
    public void testUnhash() {
        int h = 1168935528;
        int hashed = hash(h);
        assertEquals(hashed, hashExpanded(h, new int[]{4, 7, 12, 16, 19, 20, 24, 27}));
        
        int unhashed = unhash(hashed, new int[]{4, 7, 12, 16, 19, 20, 24, 27});
        assertEquals(Integer.toBinaryString(h), Integer.toBinaryString(unhashed));
        
        int unhashedFaster = unhash(unhash(hashed, new int[]{4, 7}), new int[]{12, 20});
        assertEquals(Integer.toBinaryString(h), Integer.toBinaryString(unhashedFaster));
    }
    
//    public void testToStringOnEclipse() throws Exception {
//        ToStringer.main(new String[] {"/Users/robinsalkeld/snapshots/3341.hprof"});
//    }
    
//    public void testToStringOnTomcat() throws Exception {
//        ToStringer.main(new String[] {"/Users/robinsalkeld/snapshots/3780.hprof"});
//    }
    
    public void testTracingAspect() throws Exception {
        new TracingExampleTest().testTracingAspect();
    }
    
    public void testRacerAspects() throws Exception {
        new RacerTest().testRacerExample(); 
    }
    
//    public void testTracingAspectTOD() throws Exception {
//        TODMirrorWeavingLauncher.launch("tod-ExampleMain", 
//                "/Users/robinsalkeld/Documents/UBC/Code/Tracing Example Aspects/bin",
//                "/Users/robinsalkeld/Documents/UBC/Code/RetrospectData/tod/TracingTest");
//    }
    
}
