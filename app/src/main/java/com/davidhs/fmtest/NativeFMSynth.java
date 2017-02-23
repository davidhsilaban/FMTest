package com.davidhs.fmtest;

/**
 * Created by David Silaban on 2/23/2017.
 */

public class NativeFMSynth {

    static {
        System.loadLibrary("synth");
    }

    public native void init();
    public native void write(int reg, byte val);
}
