package com.davidhs.fmtest;

/**
 * Created by David Silaban on 3/10/2017.
 */

public class NativeMIDIFile {

    private long nativemidifile_handle;

    static {
        System.loadLibrary("synth");
    }

    public class MidiEvent {
        public int tick, track, seq;
        public double seconds;
    }

    public class MidiEventList {
        private int track;

        public MidiEvent get(int index) {
            return midifile_getTrack_get(nativemidifile_handle, track, index);
        }

        public native MidiEvent midifile_getTrack_get(long handle, int track, int index);
    }

    public NativeMIDIFile(String filename) {
        nativemidifile_handle = midifile_open(filename);
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void close() {
        midifile_close(nativemidifile_handle);
    }

    private native long midifile_open(String filename);
    private native void midifile_read(long handle, String filename);
    private native void midifile_close(long handle);

    private native int midifile_getTrackCount(long handle);
    private native MidiEventList midifile_getTrack(long handle, int track);
}
