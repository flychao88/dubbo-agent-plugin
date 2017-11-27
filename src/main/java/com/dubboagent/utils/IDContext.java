package com.dubboagent.utils;

import java.util.Random;

/**
 * Date:2017/11/23
 *
 * @author:chao.cheng
 **/
public  class IDContext {

    private long lastTimestamp;
    private short threadSeq;

    private long runRandomTimestamp;
    private int lastRandomValue;
    private Random random;

    public IDContext(long lastTimestamp, short threadSeq) {
        this.lastTimestamp = lastTimestamp;
        this.threadSeq = threadSeq;
    }

    public long nextSeq() {
        return timestamp() * 10000 + nextThreadSeq();
    }

    private long timestamp() {
        long currentTimeMillis = System.currentTimeMillis();

        if (currentTimeMillis < lastTimestamp) {
            // Just for considering time-shift-back by Ops or OS. @hanahmily 's suggestion.
            if (random == null) {
                random = new Random();
            }
            if (runRandomTimestamp != currentTimeMillis) {
                lastRandomValue = random.nextInt();
                runRandomTimestamp = currentTimeMillis;
            }
            return lastRandomValue;
        } else {
            lastTimestamp = currentTimeMillis;
            return lastTimestamp;
        }
    }

    public short nextThreadSeq() {
        if (threadSeq == 10000) {
            threadSeq = 0;
        }
        return threadSeq++;
    }
}
