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
package edu.ubc.mirrors.eclipse.mat;

import java.util.AbstractList;

import org.eclipse.mat.SnapshotException;
import org.eclipse.mat.snapshot.model.IInstance;

import edu.ubc.mirrors.ObjectMirror;

public class HeapDumpLazyInstanceList extends AbstractList<ObjectMirror> {

    public HeapDumpLazyInstanceList(HeapDumpVirtualMachineMirror vm, int[] objectIDs) {
        this.vm = vm;
        this.objectIDs = objectIDs;
    }

    private final HeapDumpVirtualMachineMirror vm;
    private final int[] objectIDs;

    @Override
    public ObjectMirror get(int index) {
        try {
            IInstance object = (IInstance)vm.getSnapshot().getObject(objectIDs[index]);
            // Don't call vm.makeMirror() to avoid caching.
            return new HeapDumpInstanceMirror(vm, object);
        } catch (SnapshotException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int size() {
        return objectIDs.length;
    }
    
}
