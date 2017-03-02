package com.davidhs.fmtest;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cozendey.opl3.OPL3;
import com.leff.midi.MidiFile;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.PitchBend;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.util.MidiEventListener;
import com.leff.midi.util.MidiProcessor;
import com.leff.midi.util.MidiUtil;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements MidiEventListener {

    OPL3 opl3;
    AudioTrack oplAudioTrack;
    short [] oplBuffer;
    boolean isPlaying = true;

    private Button button_sound;
    private Handler updateHandler;
    private MidiProcessor midiProcessor;

    private double [] noteTable;
    private double [] octTable;
    private int [] channelRegisterOffset = {0, 1, 2, 8, 9, 10, 16, 17, 18};
    private NativeFMSynth synth = new NativeFMSynth();
    private SynthChannelManager channelManager = new SynthChannelManager(synth);
    private Gmtimbre timbre = new Gmtimbre(synth);

    private Runnable audioWriteRunnable = new Runnable() {
        @Override
        public void run() {
//            setupAudio();

            // Fill buffer
            while (isPlaying) {
                enqueueBuffer(512);
//                try {
//                    Thread.sleep(1);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }
    };
    private int minBufferSize;
    private int lastFNum;
    private int lastBlock;
    private int curTempo = 500000;
    private MidiFile midiFile;
    private long samplesPerMs = 0;
    private int curMs;
    private long lastMs;

    private void prequeueBuffer() {
//        for (int i = 0; i < oplBuffer.length; i++) {
//            oplBuffer[i] = opl3.read()[0];
//        }
        synth.getsamples(oplBuffer, oplBuffer.length/2);

        oplAudioTrack.write(oplBuffer, 0, oplBuffer.length);
//        oplAudioTrack.setNotificationMarkerPosition(oplAudioTrack.getPlaybackHeadPosition() + minBufferSize*2);
    }

    private void enqueueBuffer(int samples) {
        synchronized (this) {
            oplBuffer = new short[samples*2];
//            for (int i = 0; i < oplBuffer.length; i++) {
//                oplBuffer[i] = opl3.read()[0];
//            }
            synth.getsamples(oplBuffer, samples);

            oplAudioTrack.write(oplBuffer, 0, samples*2);
        }
//        oplAudioTrack.setNotificationMarkerPosition(oplAudioTrack.getPlaybackHeadPosition() + minBufferSize*2);
    }

    private Thread audioThread;

    @Override
    protected void onDestroy() {
        isPlaying = false;
        oplAudioTrack.stop();

        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button_sound = (Button) findViewById(R.id.button_sound);
        button_sound.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    case KeyEvent.ACTION_DOWN:
                        opl3.write(0, 0xA0, 0x1C);
                        opl3.write(0, 0xB0, 0x32);
                        break;

                    case KeyEvent.ACTION_UP:
                        opl3.write(0, 0xA0, 0x1C);
                        opl3.write(0, 0xB0, 0x12);
                        break;
                }

                return false;
            }
        });

        opl3 = new OPL3();
        synth.init();
        fillTable();
        try {
            midiFile = new MidiFile(getResources().openRawResource(R.raw.gmstri00));
            midiProcessor = new MidiProcessor(midiFile);
            midiProcessor.registerEventListener(this, MidiEvent.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Test audio parameter
        opl3.write(0, 0x01, 1 << 5);
        synth.write(0x105, (byte) 1);

        for (int c = 0; c < 18; c++) {
            opl3.write(0, 0x20 + channelRegisterOffset[c%9], 0x1);
            opl3.write(0, 0x40 + channelRegisterOffset[c%9], 0x10);
            opl3.write(0, 0x60 + channelRegisterOffset[c%9], 0xF0);
            opl3.write(0, 0x80 + channelRegisterOffset[c%9], 0x77);

            synth.write(0x20 + channelRegisterOffset[c%9] + ((c / 9) << 8), (byte) 0x1);
            synth.write(0x40 + channelRegisterOffset[c%9] + ((c / 9) << 8), (byte) 0x10);
            synth.write(0x60 + channelRegisterOffset[c%9] + ((c / 9) << 8), (byte) 0xF0);
            synth.write(0x80 + channelRegisterOffset[c%9] + ((c / 9) << 8), (byte) 0x77);

//        opl3.write(0, 0xA0, 0x98);

            opl3.write(0, 0x23 + channelRegisterOffset[c%9], 0x1);
            opl3.write(0, 0x43 + channelRegisterOffset[c%9], 0x00);
            opl3.write(0, 0x63 + channelRegisterOffset[c%9], 0xF0);
            opl3.write(0, 0x83 + channelRegisterOffset[c%9], 0x77);

            synth.write(0x23 + channelRegisterOffset[c%9] + ((c / 9) << 8), (byte) 0x1);
            synth.write(0x43 + channelRegisterOffset[c%9] + ((c / 9) << 8), (byte) 0x00);
            synth.write(0x63 + channelRegisterOffset[c%9] + ((c / 9) << 8), (byte) 0xF0);
            synth.write(0x83 + channelRegisterOffset[c%9] + ((c / 9) << 8), (byte) 0x77);
//        opl3.write(0, 0xB0, 0x31);
        }

        setupAudio();
//        midiProcessor.start();
        Toast.makeText(this, ""+timbre.opl_timbres[3].mult[0], Toast.LENGTH_SHORT).show();

        audioThread = new Thread(audioWriteRunnable);
        audioThread.start(); // start audio playback
}

    private void fillTable() {
        noteTable = new double[12];
        octTable = new double[12];

        for (int n = 0; n < 12; n++) {
//            noteTable[n] = Math.pow(2.0, (n-9)/12.0);
            octTable[n] = Math.pow(2.0, (20-n));
            noteTable[n] = Math.pow(2.0, (n-9)/12.0) * 440.0;
        }
    }

    private void setupAudio() {
        minBufferSize = AudioTrack.getMinBufferSize(49700, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
//        minBufferSize = 24850;
        Log.d("setupAudio", "minBufferSize = "+minBufferSize);
        oplAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 49700, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
        samplesPerMs = 49700 * 2 / 1000;
        oplBuffer = new short[128];
        updateHandler = new Handler();
        oplAudioTrack.setPositionNotificationPeriod(minBufferSize/8);
        oplAudioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioTrack track) {

            }

            @Override
            public void onPeriodicNotification(AudioTrack track) {
//                Log.d("Update", "Update");
//                prequeueBuffer();
//                try {
//                    Thread.sleep(1);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }, updateHandler);
        prequeueBuffer();
        oplAudioTrack.play();
    }

    @Override
    public void onStart(boolean fromBeginning) {

    }

    @Override
    public void onEvent(MidiEvent event, long ms) {
        if (event instanceof Tempo) {
            Tempo tempoEvent = (Tempo) event;
            curTempo = tempoEvent.getMpqn();
        } else if (event instanceof NoteOn) {
            NoteOn midiEvent = (NoteOn) event;

            if (midiEvent.getChannel() != 0x9) {

                channelManager.noteOn(midiEvent.getChannel(), midiEvent.getNoteValue(), midiEvent.getNoteValue());
//                if (midiEvent.getVelocity() == 0) {
//                    int channel = channelManager.noteOff(midiEvent.getNoteValue());
//                    if (channel == -1) return;
//                    opl3.write(0, 0xA0 + channel, lastFNum & 0xFF);
//                    opl3.write(0, 0xB0 + channel, lastBlock << 2 | lastFNum >> 8);
//                    return;
//                }

//                int curNote = midiEvent.getNoteValue() % 12;
//                int curOct = midiEvent.getNoteValue() / 12;
//                if (curOct < 1) curOct = 1;
//
////            int fNum = (int) ((double)noteTable[curNote]*octTable[curOct]*440.0/49716.0);
////            int fNum = (int) (noteTable[curNote] * octTable[curOct-1] / 49716.0);
//                double fNum = (Math.pow(2.0, (midiEvent.getNoteValue() - 69) / 12.0) * octTable[curOct - 1] * 440.0 / 49716.0);
//                lastFNum = (int) fNum;
//                lastBlock = curOct - 1;
//                int channel = channelManager.noteOn(midiEvent.getNoteValue());
//                opl3.write(0, 0xA0 + channel, lastFNum & 0xFF);
//                opl3.write(0, 0xB0 + channel, lastBlock << 2 | lastFNum >> 8);
//                synth.write(0xA0 + channel, (byte) (lastFNum & 0xFF));
////                Log.d("Noteon", "" + channel + " " + fNum + " " + curOct);
//                opl3.write(0, 0xA0 + channel, lastFNum & 0xFF);
//                opl3.write(0, 0xB0 + channel, 0x20 | (curOct - 1) << 2 | (lastFNum >> 8) & 3);
//                synth.write(0xB0 + channel, (byte) (0x20 | (curOct - 1) << 2 | (lastFNum >> 8) & 3));
            }
        } else if (event instanceof NoteOff) {
            NoteOff midiEvent = (NoteOff) event;

//            int channel = channelManager.noteOff(midiEvent.getNoteValue());
//            if (channel == -1) return;
//            opl3.write(0, 0xA0 + channel, lastFNum & 0xFF);
//            opl3.write(0, 0xB0 + channel, lastBlock << 2 | lastFNum >> 8);
//            synth.write(0xA0 + channel, (byte) (lastFNum & 0xFF));
//            synth.write(0xB0 + channel, (byte) (lastBlock << 2 | lastFNum >> 8));
            channelManager.noteOff(midiEvent.getChannel(), midiEvent.getNoteValue());
        } else if (event instanceof PitchBend) {
            PitchBend midiEvent = (PitchBend) event;

            channelManager.pitchBend(midiEvent.getChannel(), midiEvent.getBendAmount());
        }

        final long samples = (ms - lastMs) * samplesPerMs;
//        opl3.read();
//        Log.d("delta", ""+event.getDelta());
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                enqueueBuffer((int) samples);
//            }
//        }).start();
//        enqueueBuffer((int) samples);
        lastMs = ms;
    }

    @Override
    public void onStop(boolean finished) {

    }
}
