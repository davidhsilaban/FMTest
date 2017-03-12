package com.davidhs.fmtest;

import java.io.InputStream;

/**
 * Created by David Silaban on 3/10/2017.
 */

public class NativeMIDIFile {

    private long nativemidifile_handle;

    static {
        System.loadLibrary("synth");
    }

    public class MidiEvent {
        private int internal_track, index;
        public int tick, track, seq;
        public double seconds;

        public int isTempo() {
            return midifile_getTrack_get_isTempo(NativeMIDIFile.this, nativemidifile_handle, internal_track, index);
        }

        public int getTempoMicro() {
            return midifile_getTrack_get_getTempoMicro(NativeMIDIFile.this, nativemidifile_handle, internal_track, index);
        }

        public int getCommandByte() {
            return midifile_getTrack_get_getCommandByte(NativeMIDIFile.this, nativemidifile_handle, internal_track, index);
        }

        public int getP1() {
            return midifile_getTrack_get_getP1(NativeMIDIFile.this, nativemidifile_handle, internal_track, index);
        }

        public int getP2() {
            return midifile_getTrack_get_getP2(NativeMIDIFile.this, nativemidifile_handle, internal_track, index);
        }

        private native int midifile_getTrack_get_getTempoMicro(NativeMIDIFile thisObject, long handle, int track, int index);
        private native int midifile_getTrack_get_isTempo(NativeMIDIFile thisObject, long handle, int track, int index);
        private native int midifile_getTrack_get_getCommandByte(NativeMIDIFile thisObject, long handle, int track, int index);
        private native int midifile_getTrack_get_getP1(NativeMIDIFile thisObject, long handle, int track, int index);
        private native int midifile_getTrack_get_getP2(NativeMIDIFile thisObject, long handle, int track, int index);
    }

    public class MidiEventList {
        private int track;

        public MidiEvent get(int index) {
            return midifile_getTrack_get(NativeMIDIFile.this, nativemidifile_handle, track, index);
        }

        public int size() {
            return midifile_getTrack_size(NativeMIDIFile.this, nativemidifile_handle, track);
        }

        private native MidiEvent midifile_getTrack_get(NativeMIDIFile thisObject, long handle, int track, int index);
        private native int midifile_getTrack_size(NativeMIDIFile thisObject, long handle, int track);
    }

    public NativeMIDIFile(String filename) {
        nativemidifile_handle = midifile_open(filename);
    }

    public NativeMIDIFile(InputStream stream) {
        nativemidifile_handle = midifile_open(stream);
    }

    public int getTrackCount() {
        return midifile_getTrackCount(nativemidifile_handle);
    }

    public MidiEventList getTrack(int track) {
        return midifile_getTrack(nativemidifile_handle, track);
    }

    public int getTicksPerQuarterNote() {
        return midifile_getTicksPerQuarterNote(nativemidifile_handle);
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
    private native long midifile_open(InputStream stream);
    private native void midifile_read(long handle, String filename);
    private native void midifile_close(long handle);

    private native int midifile_getTrackCount(long handle);
    private native MidiEventList midifile_getTrack(long handle, int track);

    private native int midifile_getTicksPerQuarterNote(long handle);
}
