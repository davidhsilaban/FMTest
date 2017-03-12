//
// Created by David Silaban on 3/10/2017.
//

#include "com_davidhs_fmtest_NativeMIDIFile.h"
#include "com_davidhs_fmtest_NativeMIDIFile_MidiEvent.h"
#include "com_davidhs_fmtest_NativeMIDIFile_MidiEventList.h"
#include "InputStream_streambuf.h"
#include "MidiFile.h"

JNIEXPORT jlong JNICALL Java_com_davidhs_fmtest_NativeMIDIFile_midifile_1open__Ljava_lang_String_2
        (JNIEnv *env, jobject thisObject, jstring filename)
{
    return reinterpret_cast<jlong>(new MidiFile(env->GetStringUTFChars(filename, NULL)));
}

JNIEXPORT jlong JNICALL Java_com_davidhs_fmtest_NativeMIDIFile_midifile_1open__Ljava_io_InputStream_2
        (JNIEnv *env, jobject thisObject, jobject j_inputstream)
{
    InputStream_streambuf sbuf(env, j_inputstream);
    istream stream(&sbuf);
    MidiFile *mfile = new MidiFile(stream);
    mfile->joinTracks();
    mfile->deltaTicks();
    return reinterpret_cast<jlong>(mfile);
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
    jmethodID midieventlist_init_method_id = env->GetMethodID(midieventlist_class, "<init>", "(Lcom/davidhs/fmtest/NativeMIDIFile;)V");
    jfieldID midieventlist_track_field_id = env->GetFieldID(midieventlist_class, "track", "I");
    jobject midieventlist_object = env->NewObject(midieventlist_class, midieventlist_init_method_id, thisObject);
    env->SetIntField(midieventlist_object, midieventlist_track_field_id, track);
    return midieventlist_object;
}

JNIEXPORT jint JNICALL Java_com_davidhs_fmtest_NativeMIDIFile_00024MidiEventList_midifile_1getTrack_1size
        (JNIEnv *env, jobject thisObject, jobject parentThis, jlong handle, jint track)
{
    MidiFile *midiFile = reinterpret_cast<MidiFile*>(handle);

    return (*midiFile)[track].size();
}

JNIEXPORT jobject JNICALL Java_com_davidhs_fmtest_NativeMIDIFile_00024MidiEventList_midifile_1getTrack_1get
        (JNIEnv *env, jobject thisObject, jobject parentThis, jlong handle, jint track, jint index)
{
    MidiFile *midiFile = reinterpret_cast<MidiFile*>(handle);

    MidiEvent event = (*midiFile)[track][index];
    jclass midievent_class = env->FindClass("com/davidhs/fmtest/NativeMIDIFile$MidiEvent");
    jmethodID  midievent_init_method_id = env->GetMethodID(midievent_class, "<init>", "(Lcom/davidhs/fmtest/NativeMIDIFile;)V");
    jfieldID midievent_tick_field_id = env->GetFieldID(midievent_class, "tick", "I");
    jfieldID midievent_track_field_id = env->GetFieldID(midievent_class, "track", "I");
    jfieldID midievent_seq_field_id = env->GetFieldID(midievent_class, "seq", "I");
    jfieldID midievent_seconds_field_id = env->GetFieldID(midievent_class, "seconds", "D");
    jfieldID midievent_internal_track_field_id = env->GetFieldID(midievent_class, "internal_track", "I");
    jfieldID midievent_index_field_id = env->GetFieldID(midievent_class, "index", "I");
    jobject midievent_object = env->NewObject(midievent_class, midievent_init_method_id, parentThis);
    env->SetIntField(midievent_object, midievent_tick_field_id, event.tick);
    env->SetIntField(midievent_object, midievent_track_field_id, event.track);
    env->SetIntField(midievent_object, midievent_seq_field_id, event.seq);
    env->SetDoubleField(midievent_object, midievent_seconds_field_id, event.seconds);
    env->SetIntField(midievent_object, midievent_internal_track_field_id, track);
    env->SetIntField(midievent_object, midievent_index_field_id, index);
    return midievent_object;
}

JNIEXPORT jint JNICALL Java_com_davidhs_fmtest_NativeMIDIFile_midifile_1getTicksPerQuarterNote
        (JNIEnv *env, jobject thisObject, jlong handle)
{
    MidiFile *midiFile = reinterpret_cast<MidiFile*>(handle);
    return midiFile->getTicksPerQuarterNote();
}

JNIEXPORT jint JNICALL Java_com_davidhs_fmtest_NativeMIDIFile_00024MidiEvent_midifile_1getTrack_1get_1getTempoMicro
        (JNIEnv *env, jobject thisObject, jobject parentThis, jlong handle, jint track, jint index)
{
    MidiFile *midiFile = reinterpret_cast<MidiFile*>(handle);

    MidiEvent event = (*midiFile)[track][index];
    return event.getTempoMicro();
}

JNIEXPORT jint JNICALL Java_com_davidhs_fmtest_NativeMIDIFile_00024MidiEvent_midifile_1getTrack_1get_1isTempo
(JNIEnv *env, jobject thisObject, jobject parentThis, jlong handle, jint track, jint index)
{
    MidiFile *midiFile = reinterpret_cast<MidiFile*>(handle);

    MidiEvent event = (*midiFile)[track][index];
    return event.isTempo();
}

JNIEXPORT jint JNICALL Java_com_davidhs_fmtest_NativeMIDIFile_00024MidiEvent_midifile_1getTrack_1get_1getCommandByte
        (JNIEnv *env, jobject thisObject, jobject parentThis, jlong handle, jint track, jint index)
{
    MidiFile *midiFile = reinterpret_cast<MidiFile*>(handle);

    MidiEvent event = (*midiFile)[track][index];
    return event.getCommandByte();
}

JNIEXPORT jint JNICALL Java_com_davidhs_fmtest_NativeMIDIFile_00024MidiEvent_midifile_1getTrack_1get_1getP1
        (JNIEnv *env, jobject thisObject, jobject parentThis, jlong handle, jint track, jint index)
{
    MidiFile *midiFile = reinterpret_cast<MidiFile*>(handle);

    MidiEvent event = (*midiFile)[track][index];
    return event.getP1();
}

JNIEXPORT jint JNICALL Java_com_davidhs_fmtest_NativeMIDIFile_00024MidiEvent_midifile_1getTrack_1get_1getP2
        (JNIEnv *env, jobject thisObject, jobject parentThis, jlong handle, jint track, jint index)
{
    MidiFile *midiFile = reinterpret_cast<MidiFile*>(handle);

    MidiEvent event = (*midiFile)[track][index];
    return event.getP2();
}