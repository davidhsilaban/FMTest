package com.davidhs.fmtest;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by David Silaban on 2/22/2017.
 */

public class SynthChannelManager {

    public static final int OPL_CHANNELS = 18;
    private Gmtimbre mTimbre;
    private NativeFMSynth mSynth;

    private int [] channelRegisterOffset = {0, 1, 2, 8, 9, 10, 16, 17, 18};

    private class OplChannelStatus {
        public boolean active = false;
        public int midiPatchNumber;
        public int midiChannel;
        public int midiNote;
        public double tune = 440.0;
    }

    private class MidiChannelStatus {
        public int patchNumber;
        public int pitchBendValue;
        public boolean sustain;
        public HashMap<Integer, Integer> noteToOplChannelMap = new HashMap<>();
    }

    ArrayList<OplChannelStatus> oplChannelStatusList;
    ArrayList<MidiChannelStatus> midiChannelStatusList;

    public SynthChannelManager(NativeFMSynth synth, Gmtimbre timbre) {
        mSynth = synth;
        mTimbre = timbre;

        oplChannelStatusList = new ArrayList<>();
        midiChannelStatusList = new ArrayList<>();
        for (int c = 0; c < OPL_CHANNELS; c++) {
            oplChannelStatusList.add(new OplChannelStatus());
        }

        for (int m = 0; m < 16; m++) {
            midiChannelStatusList.add(new MidiChannelStatus());
        }
    }

    public void noteOn(int midiChannel, int midiNote, int velocity) {

        if (velocity == 0) {
            noteOff(midiChannel, midiNote);
            return;
        }

        for (int c = 0; c < oplChannelStatusList.size(); c++) {
            if (oplChannelStatusList.get(c).active == false) {
                oplChannelStatusList.get(c).active = true;
                oplChannelStatusList.get(c).midiChannel = midiChannel;
                oplChannelStatusList.get(c).midiNote = midiNote;
                oplChannelStatusList.get(c).midiPatchNumber = midiChannelStatusList.get(midiChannel).patchNumber;
                midiChannelStatusList.get(midiChannel).noteToOplChannelMap.put(midiNote, c);

                channelOff(c);
                channelOn(c);
                pitchBend(midiChannel, midiChannelStatusList.get(midiChannel).pitchBendValue);
                return;
            }
        }

        noteOff(oplChannelStatusList.get(0).midiChannel, oplChannelStatusList.get(0).midiNote);
    }

    public void noteOff(int midiChannel, int midiNote) {
        for (int c = 0; c < oplChannelStatusList.size(); c++) {
            if (oplChannelStatusList.get(c).midiNote == midiNote && oplChannelStatusList.get(c).midiChannel == midiChannel) {
                oplChannelStatusList.get(c).active = false;
                midiChannelStatusList.get(midiChannel).noteToOplChannelMap.remove(midiNote);

                channelOff(c);
            }
        }
    }

    public void pitchBend(int midiChannel, int bendAmount) {
        midiChannelStatusList.get(midiChannel).pitchBendValue = bendAmount;

        for (Integer oplChannel :
                midiChannelStatusList.get(midiChannel).noteToOplChannelMap.values()) {
            channelTune(oplChannel, bendAmount);
        }
    }

    public void programChange(int midiChannel, int programNumber) {
        midiChannelStatusList.get(midiChannel).patchNumber = programNumber;
    }

    private void channelTune(int oplChannel, int bendAmount) {
//        Log.d("Bend", ""+bendAmount);
        double result = 440.0;

        if (bendAmount >= 8192) {
            result = ((bendAmount-8192) * 0.00001370 * 440.0) + 440.0;
        } else {
            result = ((bendAmount-8192) * 0.00001370 * 440.0) + 440.0;
        }

        oplChannelStatusList.get(oplChannel).tune = result;

        int curNote = oplChannelStatusList.get(oplChannel).midiNote % 12;
        int curOct = oplChannelStatusList.get(oplChannel).midiNote / 12;
        if (curOct < 1) curOct = 1;

        double fNum = (Math.pow(2.0, (oplChannelStatusList.get(oplChannel).midiNote - 69) / 12.0) * Math.pow(2.0, 20 - (curOct - 1)) * oplChannelStatusList.get(oplChannel).tune / 49716.0);
        int curFNum = (int) fNum;
        mSynth.write(0xA0 + (oplChannel%9) + ((oplChannel / 9) << 8), (byte) (curFNum & 0xFF));
        mSynth.write(0xB0 + (oplChannel%9) + ((oplChannel / 9) << 8), (byte) ((oplChannelStatusList.get(oplChannel).active ? 0x20 : 0x0) | (curOct - 1) << 2 | (curFNum >> 8) & 3));
    }

    private void channelOn(int oplChannel) {
        int curNote = oplChannelStatusList.get(oplChannel).midiNote % 12;
        int curOct = oplChannelStatusList.get(oplChannel).midiNote / 12;
        if (curOct < 1) curOct = 1;

//            int fNum = (int) ((double)noteTable[curNote]*octTable[curOct]*440.0/49716.0);
//            int fNum = (int) (noteTable[curNote] * octTable[curOct-1] / 49716.0);
        double fNum = (Math.pow(2.0, (oplChannelStatusList.get(oplChannel).midiNote - 69) / 12.0) * Math.pow(2.0, 20 - (curOct - 1)) * oplChannelStatusList.get(oplChannel).tune / 49716.0);
        int curFNum = (int) fNum;
        channelChangeInstrument(oplChannel);
        mSynth.write(0xA0 + (oplChannel%9) + ((oplChannel / 9) << 8), (byte) (curFNum & 0xFF));
        mSynth.write(0xB0 + (oplChannel%9) + ((oplChannel / 9) << 8), (byte) (0x20 | (curOct - 1) << 2 | (curFNum >> 8) & 3));
    }

    private void channelOff(int oplChannel) {
        OplChannelStatus channelStatus = oplChannelStatusList.get(oplChannel);
        int lastNote = channelStatus.midiNote % 12;
        int lastOct = channelStatus.midiNote / 12;
        if (lastOct < 1) lastOct = 1;

        double fNum = (Math.pow(2.0, (channelStatus.midiNote - 69) / 12.0) * Math.pow(2.0, 20 - (lastOct - 1)) * channelStatus.tune / 49716.0);
        int lastFNum = (int) fNum;

        mSynth.write(0xA0 + (oplChannel%9) + ((oplChannel / 9) << 8), (byte) (lastFNum & 0xFF));
        mSynth.write(0xB0 + (oplChannel%9) + ((oplChannel / 9) << 8), (byte) ((lastOct - 1) << 2 | (lastFNum >> 8) & 3));
    }

    private void channelChangeInstrument(int oplChannel) {
        Gmtimbre.opl_timbre opl_timbre_data = mTimbre.opl_timbres[oplChannelStatusList.get(oplChannel).midiPatchNumber];
        mSynth.write(0x20 + channelRegisterOffset[oplChannel%9] + ((oplChannel / 9) << 8), opl_timbre_data.mult[0]);
        mSynth.write(0x40 + channelRegisterOffset[oplChannel%9] + ((oplChannel / 9) << 8), opl_timbre_data.tl[0]);
        mSynth.write(0x60 + channelRegisterOffset[oplChannel%9] + ((oplChannel / 9) << 8), opl_timbre_data.ad[0]);
        mSynth.write(0x80 + channelRegisterOffset[oplChannel%9] + ((oplChannel / 9) << 8), opl_timbre_data.sr[0]);

        mSynth.write(0x23 + channelRegisterOffset[oplChannel%9] + ((oplChannel / 9) << 8), opl_timbre_data.mult[1]);
        mSynth.write(0x43 + channelRegisterOffset[oplChannel%9] + ((oplChannel / 9) << 8), opl_timbre_data.tl[1]);
        mSynth.write(0x63 + channelRegisterOffset[oplChannel%9] + ((oplChannel / 9) << 8), opl_timbre_data.ad[1]);
        mSynth.write(0x83 + channelRegisterOffset[oplChannel%9] + ((oplChannel / 9) << 8), opl_timbre_data.sr[1]);

        mSynth.write(0xC0 + oplChannel, (byte) (opl_timbre_data.fb | (3 << 4)));

        mSynth.write(0xE0 + channelRegisterOffset[oplChannel%9] + ((oplChannel / 9) << 8), opl_timbre_data.wf[0]);
        mSynth.write(0xE3 + channelRegisterOffset[oplChannel%9] + ((oplChannel / 9) << 8), opl_timbre_data.wf[1]);
    }
}
