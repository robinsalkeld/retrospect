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
import java.util.Hashtable;

import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Driver extends Configured implements IApplication, Tool, BundleActivator {
    
    @Override
    public void start(BundleContext context) throws Exception {
        context.registerService(Tool.class.getName(), this, new Hashtable<String, String>());
    }

    @Override
    public void stop(BundleContext frameworkConfig) throws Exception {
    }
    
    public int run(String[] args) throws Exception {
        JobConf job = new JobConf(getConf());
        job.setClassLoader(Driver.class.getClassLoader());
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
        return 0;
    }
    
    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Driver(), args);
        System.exit(res);
    }
    
    public Object start(IApplicationContext context) throws Exception {
        String[] args = (String[])context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        main(args);
        return null;
    }

    public void stop() {
        
    }
}
