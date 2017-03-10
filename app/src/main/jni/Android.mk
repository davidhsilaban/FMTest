LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libmidifile

LOCAL_SRC_FILES = \
    src-library/Binasc.cpp \
    src-library/MidiEvent.cpp \
    src-library/MidiEventList.cpp \
    src-library/MidiFile.cpp \
    src-library/MidiMessage.cpp \
    src-library/Options.cpp

LOCAL_C_INCLUDES += $(LOCAL_PATH)/include

LOCAL_CFLAGS += -c -g -Wall -O3 -std=c++11

include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE := synth

LOCAL_SRC_FILES := synth.cpp midifile.cpp

LOCAL_STATIC_LIBRARIES := libmidifile

LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)