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
    private int lastOplChannel = -1;

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

    public void sendMIDI(int message, int param1, int param2) {
        int channel = message & 0x0F;
        if (channel == 9) return;
        switch (message & 0xF0) {
            case 0x80:
                noteOff(channel, param1);
                break;

            case 0x90:
                noteOn(channel, param1, param2);
                break;

            case 0xE0:
                pitchBend(channel, param2 << 7 | param1);
                break;

            case 0xC0:
                programChange(channel, param1);
                break;
        }
    }

    public void noteOn(int midiChannel, int midiNote, int velocity) {

        if (velocity == 0) {
            noteOff(midiChannel, midiNote);
            return;
        }

        int c = lastOplChannel;
        if(lastOplChannel > -1) Log.d("OPLChannelOn1", ""+c+" "+lastOplChannel+" "+oplChannelStatusList.get(c).active+" "+oplChannelStatusList.get(lastOplChannel).active);
        do {
            c = (c+1) % oplChannelStatusList.size();
            if(lastOplChannel > -1) Log.d("OPLChannelOn2", ""+c+" "+lastOplChannel+" "+oplChannelStatusList.get(c).active+" "+oplChannelStatusList.get(lastOplChannel).active);
//            if (oplChannelStatusList.get(c).active) {
////                noteOff(oplChannelStatusList.get(c).midiChannel, oplChannelStatusList.get(c).midiNote);
//                channelOff(c);
//                oplChannelStatusList.get(c).active = false;
//                midiChannelStatusList.get(oplChannelStatusList.get(c).midiChannel).noteToOplChannelMap.remove(oplChannelStatusList.get(c).midiNote);
////                noteOff(oplChannelStatusList.get(c).midiChannel, oplChannelStatusList.get(c).midiNote);
//            }
            if (oplChannelStatusList.get(c).active == false) {
                oplChannelStatusList.get(c).active = true;
                oplChannelStatusList.get(c).midiChannel = midiChannel;
                oplChannelStatusList.get(c).midiNote = midiNote;
                oplChannelStatusList.get(c).midiPatchNumber = midiChannel != 9 ? midiChannelStatusList.get(midiChannel).patchNumber : (midiNote+128);
                midiChannelStatusList.get(midiChannel).noteToOplChannelMap.put(midiNote, c);

                channelOff(c);
//                channelOn(c);
                channelChangeInstrument(c);
                pitchBend(midiChannel, midiChannelStatusList.get(midiChannel).pitchBendValue);
                lastOplChannel = c;
                printOPLChannelsStatus();
                return;
            }
        } while (c != lastOplChannel);

        printOPLChannelsStatus();
    }

    private void printOPLChannelsStatus() {
        StringBuilder sb = new StringBuilder("|");
        for (OplChannelStatus ch :
                oplChannelStatusList) {
            if (ch.active) {
                sb.append(String.format("%2s|", ch.midiChannel));
            } else {
                sb.append("..|");
            }
        }
        Log.d("OPLChannels", sb.toString());
    }

    public void noteOff(int midiChannel, int midiNote) {
        Integer c = midiChannelStatusList.get(midiChannel).noteToOplChannelMap.get(midiNote);
        if (c != null) {
            oplChannelStatusList.get(c).active = false;
            channelOff(c);
        }
        midiChannelStatusList.get(midiChannel).noteToOplChannelMap.remove(midiNote);
//        for (int c = 0; c < oplChannelStatusList.size(); c++) {
//            if (oplChannelStatusList.get(c).midiNote == midiNote && oplChannelStatusList.get(c).midiChannel == midiChannel) {
//                oplChannelStatusList.get(c).active = false;
//                channelOff(c);
//            }
//        }
    }

    public void pitchBend(int midiChannel, int bendAmount) {
        midiChannelStatusList.get(midiChannel).pitchBendValue = bendAmount;

        for (Integer oplChannel :
                midiChannelStatusList.get(midiChannel).noteToOplChannelMap.values()) {
            if (oplChannelStatusList.get(oplChannel).midiChannel == midiChannel) {
                channelTune(oplChannel, bendAmount);
            }
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

//        int curNote = oplChannelStatusList.get(oplChannel).midiNote % 12;
        int curNote = oplChannelStatusList.get(oplChannel).midiChannel != 9 ? oplChannelStatusList.get(oplChannel).midiNote : mTimbre.opl_drum_maps[oplChannelStatusList.get(oplChannel).midiNote].note;
        int curOct = oplChannelStatusList.get(oplChannel).midiNote / 12;
        if (curOct < 1) curOct = 1;

        double fNum = (Math.pow(2.0, (curNote - 69) / 12.0) * Math.pow(2.0, 20 - (curOct - 1)) * oplChannelStatusList.get(oplChannel).tune / 49716.0);
        int curFNum = (int) fNum;
        mSynth.write(0xA0 + (oplChannel%9) + ((oplChannel / 9) << 8), (byte) (curFNum & 0xFF));
        mSynth.write(0xB0 + (oplChannel%9) + ((oplChannel / 9) << 8), (byte) ((oplChannelStatusList.get(oplChannel).active ? 0x20 : 0x0) | (curOct - 1) << 2 | (curFNum >> 8) & 3));
    }

    private void channelOn(int oplChannel) {
        oplChannelStatusList.get(oplChannel).active = true;
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
//        int lastNote = channelStatus.midiNote % 12;
        int lastNote = channelStatus.midiChannel != 9 ? channelStatus.midiNote : mTimbre.opl_drum_maps[channelStatus.midiNote].note;
        int lastOct = channelStatus.midiNote / 12;
        if (lastOct < 1) lastOct = 1;

        double fNum = (Math.pow(2.0, (lastNote - 69) / 12.0) * Math.pow(2.0, 20 - (lastOct - 1)) * channelStatus.tune / 49716.0);
        int lastFNum = (int) fNum;

        mSynth.write(0xA0 + (oplChannel%9) + ((oplChannel / 9) << 8), (byte) (lastFNum & 0xFF));
        mSynth.write(0xB0 + (oplChannel%9) + ((oplChannel / 9) << 8), (byte) ((lastOct - 1) << 2 | (lastFNum >> 8) & 3));
//        channelStatus.active = false;
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
