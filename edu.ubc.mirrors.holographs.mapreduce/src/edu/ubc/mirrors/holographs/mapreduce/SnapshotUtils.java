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
