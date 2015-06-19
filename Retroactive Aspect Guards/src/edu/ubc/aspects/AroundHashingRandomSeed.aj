package edu.ubc.aspects;

public aspect AroundHashingRandomSeed {

    int around() : execution(int sun.misc.Hashing.randomHashSeed(Object)) {
        // LOL hashing efficiency
        return 47;
    }
    
}
