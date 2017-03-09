package com.davidhs.fmtest;

//
// Copyright (C) 1994-1995 Apogee Software, Ltd.
// Copyright (C) 2015-2016 Alexey Khokholov (Nuke.YKT)
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//


import android.app.Application;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by David Silaban on 3/2/2017.
 */

public class Gmtimbre {

//    typedef struct
//    {
//        byte mult[2];
//        byte tl[2];
//        byte ad[2];
//        byte sr[2];
//        byte wf[2];
//        byte fb;
//        int8_t note;
//        byte octave;
//    } opl_timbre;

    public class opl_timbre {
        byte[] mult = new byte[2];
        byte[] tl = new byte[2];
        byte[] ad = new byte[2];
        byte[] sr = new byte[2];
        byte[] wf = new byte[2];
        byte fb;
        byte note;
        byte octave;
    }

//    typedef struct
//    {
//        byte base;
//        byte note;
//    } opl_drum_map;

    public class opl_drum_map {
        byte base;
        byte note;
    }

//    #pragma pack()

    public Gmtimbre(NativeFMSynth synth) {
        synth.getpatches(Gmtimbre.this, opl_timbres, opl_drum_maps);
        Log.d("Gmtimbre", ""+opl_timbres[0].mult[0]);
    }

    public opl_timbre [] opl_timbres = new opl_timbre[256];
    public opl_drum_map [] opl_drum_maps = new opl_drum_map[128];
}