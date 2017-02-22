package com.davidhs.fmtest;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.cozendey.opl3.OPL3;

public class MainActivity extends AppCompatActivity {

    OPL3 opl3;
    AudioTrack oplAudioTrack;
    short [] oplBuffer;
    boolean isPlaying = true;

    private Button button_sound;

    private Runnable audioWriteRunnable = new Runnable() {
        @Override
        public void run() {
            setupAudio();

            // Fill buffer
            while (isPlaying) {
                for (int i = 0; i < oplBuffer.length; i++) {
                    oplBuffer[i] = opl3.read()[0];
                }

                oplAudioTrack.write(oplBuffer, 0, oplBuffer.length);
            }
        }
    };
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
                        opl3.write(0, 0xA0, 0x98);
                        opl3.write(0, 0xB0, 0x31);
                        break;

                    case KeyEvent.ACTION_UP:
                        opl3.write(0, 0xA0, 0x98);
                        opl3.write(0, 0xB0, 0x11);
                        break;
                }

                return false;
            }
        });

        opl3 = new OPL3();
        audioThread = new Thread(audioWriteRunnable);
        audioThread.start(); // start audio playback

        // Test audio parameter
        opl3.write(0, 0x01, 1 << 5);

        opl3.write(0, 0x20, 0x1);
        opl3.write(0, 0x40, 0x10);
        opl3.write(0, 0x60, 0xF0);
        opl3.write(0, 0x80, 0x77);
//        opl3.write(0, 0xA0, 0x98);

        opl3.write(0, 0x23, 0x1);
        opl3.write(0, 0x43, 0x00);
        opl3.write(0, 0x63, 0xF0);
        opl3.write(0, 0x83, 0x77);
//        opl3.write(0, 0xB0, 0x31);
    }

    private void setupAudio() {
        int minBufferSize = AudioTrack.getMinBufferSize(49700, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
        Log.d("setupAudio", "minBufferSize = "+minBufferSize);
        oplAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 49700, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
        oplBuffer = new short[minBufferSize/2];
        oplAudioTrack.play();
    }
}
