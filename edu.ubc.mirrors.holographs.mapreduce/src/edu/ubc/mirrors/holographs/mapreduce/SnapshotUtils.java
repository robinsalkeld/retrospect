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
package edu.ubc.mirrors.holographs.mapreduce;

import java.io.File;

import org.apache.hadoop.mapred.JobConf;
import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;
import org.eclipse.mat.snapshot.model.IClass;
import org.eclipse.mat.util.VoidProgressListener;

import edu.ubc.mirrors.ClassMirror;
import edu.ubc.mirrors.holographs.VirtualMachineHolograph;

public class SnapshotUtils {

    public static ISnapshot openSnapshot(JobConf job) {
     // TODO-RS: Accept arbitrary path, copy locally
//      Path snapshotPath = FileInputFormat.getInputPaths(job)[0];
      File snapshotPath = new File(job.get("snapshotPath"));
      try {
          return SnapshotFactory.openSnapshot(snapshotPath, new VoidProgressListener());
      } catch (SnapshotException e) {
          throw new RuntimeException(e);
      }
    }
    
    public static int[] getInputObjectIDs(JobConf job, ISnapshot snapshot) {
        String targetClassName = job.get("targetClassName");
        try {
            IClass klass = snapshot.getClassesByName(targetClassName, false).iterator().next();
            return klass.getObjectIds();
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
        
    }
    
}
