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
    public native void getsamples(short[] sndptr, int numsamples);
    public native void getpatches(Gmtimbre.opl_timbre[] opl_timbres, Gmtimbre.opl_drum_map[] opl_drum_maps);
}
