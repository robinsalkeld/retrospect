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

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapred.InputFormat;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.eclipse.mat.snapshot.ISnapshot;
import org.eclipse.mat.snapshot.SnapshotFactory;

public class SnapshotObjectsOfTypeInputFormat implements InputFormat<IntWritable, IntWritable> {

    @Override
    public InputSplit[] getSplits(JobConf job, int numSplits) throws IOException {
        ISnapshot snapshot = SnapshotUtils.openSnapshot(job);
        int[] objectIDs = SnapshotUtils.getInputObjectIDs(job, snapshot);
        SnapshotFactory.dispose(snapshot);
        
        int maxNumObjects = job.getInt("maxNumObjects", objectIDs.length);
        int numObjects = Math.min(objectIDs.length, maxNumObjects);
        
        int approxSplitSize = job.getInt("splitSize", -1);
        if (approxSplitSize == -1) {
            approxSplitSize = Math.max(1, (int)(((float)numObjects) / numSplits));
        } else {
            numSplits = Math.max(1, (int)(((float)numObjects) / approxSplitSize));
        }
                
        InputSplit[] splits = new InputSplit[numSplits];
        int offset = 0;
        for (int index = 0; index < numSplits; index++) {
            int splitSize = Math.min(numObjects - offset, approxSplitSize);
            if (index == numSplits - 1) {
                splitSize = numObjects - offset;
            }
            int[] slice = new int[splitSize];
            System.arraycopy(objectIDs, offset, slice, 0, splitSize);
            splits[index] = new ObjectIDArraySplit(slice);
            
            offset += splitSize;
        }
        return splits;
    }

    @Override
    public RecordReader<IntWritable, IntWritable> getRecordReader(
            InputSplit genericSplit, JobConf job, Reporter reporter)
            throws IOException {
        
        ObjectIDArraySplit split = (ObjectIDArraySplit)genericSplit;
        return new ObjectIDArrayReader(split.objectIDs);
    }

    private static class ObjectIDArraySplit implements InputSplit {

        private int[] objectIDs;
        
        @SuppressWarnings("unused")
        public ObjectIDArraySplit() {
            // For serialization
        }
        
        public ObjectIDArraySplit(int[] objectIDs) {
            this.objectIDs = objectIDs;
        }

        @Override
        public void write(DataOutput out) throws IOException {
            out.writeInt(objectIDs.length);
            for (int i = 0; i < objectIDs.length; i++) {
                out.writeInt(objectIDs[i]);
            }
        }

        @Override
        public void readFields(DataInput in) throws IOException {
            objectIDs = new int[in.readInt()];
            for (int i = 0; i < objectIDs.length; i++) {
                objectIDs[i] = in.readInt();
            }
        }

        @Override
        public long getLength() throws IOException {
            return objectIDs.length;
        }

        @Override
        public String[] getLocations() throws IOException {
            return new String[0];
        }
        
    }
    
    private static class ObjectIDArrayReader implements RecordReader<IntWritable, IntWritable> {
        
        public ObjectIDArrayReader(int[] objectIDs) {
            this.objectIDs = objectIDs;
            System.out.println("ObjectIDArrayReader for " + objectIDs.length + " objects");
        }

        private final int[] objectIDs;
        private int index = 0;
        
        @Override
        public boolean next(IntWritable key, IntWritable value) throws IOException {
            if (index < objectIDs.length) {
                key.set(index);
                value.set(objectIDs[index++]);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public IntWritable createKey() {
            return new IntWritable();
        }

        @Override
        public IntWritable createValue() {
            return new IntWritable();
        }

        @Override
        public long getPos() throws IOException {
            return index;
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public float getProgress() throws IOException {
            return index / ((float)objectIDs.length);
        }
        
    }
}
