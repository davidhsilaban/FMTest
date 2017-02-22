package com.davidhs.fmtest;

import java.util.ArrayList;

/**
 * Created by David Silaban on 2/22/2017.
 */

public class SynthChannelManager {

    private class ChannelStatus {
        public boolean active = false;
        public int midiNote;
    }

    ArrayList<ChannelStatus> channelStatusList;

    public SynthChannelManager() {
        channelStatusList = new ArrayList<>();
        for (int c = 0; c < 9; c++) {
            channelStatusList.add(new ChannelStatus());
        }
    }

    public int noteOn(int midiNote) {
        for (int c = 0; c < channelStatusList.size(); c++) {
            if (channelStatusList.get(c).active == false) {
                channelStatusList.get(c).active = true;
                channelStatusList.get(c).midiNote = midiNote;
                return c;
            }
        }

        noteOff(channelStatusList.get(0).midiNote);
        return 0;
    }

    public int noteOff(int midiNote) {
        for (int c = 0; c <channelStatusList.size(); c++) {
            if (channelStatusList.get(c).midiNote == midiNote) {
                channelStatusList.get(c).active = false;
                return c;
            }
        }

        return -1;
    }
}
