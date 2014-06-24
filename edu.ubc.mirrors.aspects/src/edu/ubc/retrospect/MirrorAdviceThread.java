package edu.ubc.retrospect;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.aspectj.bridge.IMessage;
import org.aspectj.weaver.ShadowMunger;

public class MirrorAdviceThread extends Thread {

    private final MirrorWorld world;
    boolean stop = false;
    
    public MirrorAdviceThread(MirrorWorld world) {
        this.world = world;
    }
    
    BlockingQueue<MirrorEventShadow> q = new LinkedBlockingQueue<MirrorEventShadow>();
    
    @Override
    public void run() {
        while (!stop) {
            MirrorEventShadow shadow;
            try {
                shadow = q.take();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            world.showMessage(IMessage.DEBUG, shadow.toString(), null, null);
            for (ShadowMunger munger : world.getCrosscuttingMembersSet().getShadowMungers()) {
                if (munger.match(shadow, world)) {
                    shadow.addMunger(munger);
                }
            }
            shadow.implement();
        }
    }
    
    public void addShadow(MirrorEventShadow shadow) {
        q.add(shadow);
    }
    
}
