//
// Created by David Silaban on 3/10/2017.
//

#include "com_davidhs_fmtest_NativeMIDIFile.h"
#include "com_davidhs_fmtest_NativeMIDIFile_MidiEventList.h"
#include "MidiFile.h"

JNIEXPORT jlong JNICALL Java_com_davidhs_fmtest_NativeMIDIFile_midifile_1open
        (JNIEnv *env, jobject thisObject, jstring filename)
{
    return reinterpret_cast<jlong>(new MidiFile(env->GetStringUTFChars(filename, NULL)));
}

JNIEXPORT void JNICALL Java_com_davidhs_fmtest_NativeMIDIFile_midifile_1close
        (JNIEnv *, jobject, jlong handle)
{
    MidiFile *midiFile = reinterpret_cast<MidiFile*>(handle);
    delete midiFile;
}

JNIEXPORT void JNICALL Java_com_davidhs_fmtest_NativeMIDIFile_midifile_1read
  (JNIEnv *env, jobject thisObject, jlong handle, jstring filename)
{
    MidiFile *midiFile = reinterpret_cast<MidiFile*>(handle);
    midiFile->read(env->GetStringUTFChars(filename, 0));
}

JNIEXPORT jint JNICALL Java_com_davidhs_fmtest_NativeMIDIFile_midifile_1getTrackCount
        (JNIEnv *env, jobject thisObject, jlong handle)
{
    MidiFile *midiFile = reinterpret_cast<MidiFile*>(handle);
    return midiFile->getTrackCount();
}

JNIEXPORT jobject JNICALL Java_com_davidhs_fmtest_NativeMIDIFile_midifile_1getTrack
        (JNIEnv *env, jobject thisObject, jlong handle, jint track)
{
    MidiFile *midiFile = reinterpret_cast<MidiFile*>(handle);

    MidiEventList eventList = (*midiFile)[track];
    jclass midieventlist_class = env->FindClass("com/davidhs/fmtest/NativeMIDIFile$MidiEventList");
    jmethodID midieventlist_init_method_id = env->GetMethodID(midieventlist_class, "<init>", "(Lcom/davidhs/fmtest/NativeMIDIFile;)V")
    jfieldID midieventlist_track_field_id = env->GetFieldID(midieventlist_class, "track", "I");
    jobject midieventlist_object = env->NewObject(midieventlist_class, midieventlist_init_method_id, thisObject);
    env->SetIntField(midieventlist_object, midieventlist_track_field_id, track);
    return midieventlist_object;
}

JNIEXPORT jobject JNICALL Java_com_davidhs_fmtest_NativeMIDIFile_00024MidiEventList_midifile_1getTrack_1get
        (JNIEnv *env, jobject thisObject, jlong handle, jint track, jint index)
{
    MidiFile *midiFile = reinterpret_cast<MidiFile*>(handle);

    MidiEvent event = (*midiFile)[track][index];
}