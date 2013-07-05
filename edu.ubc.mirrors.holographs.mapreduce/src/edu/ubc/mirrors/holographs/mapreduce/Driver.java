package edu.ubc.mirrors.holographs.mapreduce;

import java.io.File;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

public class Driver implements IApplication {
    public static void main(String[] args) throws Exception {
        JobConf job = new JobConf("method invoking");
        job.setInputFormat(SnapshotObjectsOfTypeInputFormat.class);
        job.setMapperClass(InvokeMethodMapper.class);
        job.setCombinerClass(TextCountSumReducer.class);
        job.setReducerClass(TextCountSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        
        job.set("snapshotPath", args[0]);
        job.set("targetClassName", "org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName");
        job.setInt("splitSize", 10000);
        job.setInt("maxNumObjects", 100000);
        
        FileInputFormat.addInputPath(job, new Path(args[0]));
        
        String outputPath = args[1];
        int suffix = 2;
        while (new File(outputPath).exists()) {
            outputPath = args[1] + suffix++;
        }
        FileOutputFormat.setOutputPath(job, new Path(outputPath));
        
        JobClient.runJob(job);
    }
    
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
}
