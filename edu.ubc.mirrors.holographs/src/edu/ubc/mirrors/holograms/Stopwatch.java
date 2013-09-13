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
package edu.ubc.mirrors.holograms;

public class Stopwatch {

    private long lastStarted = -1;
    private long total = 0;
    
    public boolean isRunning() {
        return lastStarted != -1;
    }
    
    public void start() {
        if (isRunning()) {
            throw new IllegalStateException();
        }
        lastStarted = System.currentTimeMillis();
    }
    
    public long stop() {
        if (!isRunning()) {
            throw new IllegalStateException();
        }
        total += (System.currentTimeMillis() - lastStarted);
        lastStarted = -1;
        return total;
    }
    
    public long lap() {
        if (!isRunning()) {
            throw new IllegalStateException();
        }
        long now = System.currentTimeMillis();
        long lap = now - lastStarted; 
        total += lap;
        lastStarted = now;
        return lap;
    }
    
    public void reset() {
        lastStarted = -1;
        total = 0;
    }
    
    public long total() {
        return total;
    }
}
