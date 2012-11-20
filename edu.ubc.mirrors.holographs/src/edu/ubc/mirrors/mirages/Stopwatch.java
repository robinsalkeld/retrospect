package edu.ubc.mirrors.mirages;

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
