package edu.ubc.mirrors.holographs;

public class HolographDebuggingThread extends Thread {
    HolographDebuggingThread(String name) {
        super(name);
    }

    @Override
    public void run() {
        while (true);
    }
}