//
// Created by David Silaban on 2/23/2017.
//

#include <jni.h>
#include "gmtimbre.h"
#include "com_davidhs_fmtest_NativeFMSynth.h"

namespace OPL3 {
    #define OPLTYPE_IS_OPL3
    #include "opl.cpp"
}

JNIEXPORT void JNICALL
Java_com_davidhs_fmtest_NativeFMSynth_init( JNIEnv* env,
                                            jobject thisObject )
{
    OPL3::adlib_init(49700);
}

JNIEXPORT void JNICALL
Java_com_davidhs_fmtest_NativeFMSynth_write(JNIEnv *, jobject, jint reg, jbyte val)
{
    OPL3::adlib_write(reg, val);
}

JNIEXPORT void JNICALL
Java_com_davidhs_fmtest_NativeFMSynth_getsamples(JNIEnv *env, jobject thisObject, jshortArray sndptr_,
                                                 jint numsamples)
{
    if (env->IsSameObject(sndptr_, NULL)) {
        env->ThrowNew(env->FindClass("java/lang/NullPointerException"), "Null sndptr object");
        return;
    }

    jshort *sndptr = env->GetShortArrayElements(sndptr_, NULL);

    // TODO

    if (numsamples*2 > env->GetArrayLength(sndptr_)) {
        env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), "Invalid sndptr array length");
        env->ReleaseShortArrayElements(sndptr_, sndptr, 0);
        return;
    }

    // Get OPL3 samples to buffer
    OPL3::adlib_getsample(sndptr, numsamples);

    env->ReleaseShortArrayElements(sndptr_, sndptr, 0);
}

JNIEXPORT void JNICALL Java_com_davidhs_fmtest_NativeFMSynth_getpatches(JNIEnv *env, jobject thisObject,
        jobjectArray opl_timbres_, jobjectArray opl_drum_maps_)
{
    if (env->IsSameObject(opl_timbres_, NULL)) {
        env->ThrowNew(env->FindClass("java/lang/NullPointerException"), "Null opl_timbres object");
        return;
    }

    if (env->IsSameObject(opl_drum_maps_, NULL)) {
        env->ThrowNew(env->FindClass("java/lang/NullPointerException"), "Null opl_drum_maps object");
        return;
    }

    if (env->GetArrayLength(opl_timbres_) != 256) {
        env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), "opl_timbres array length not 256");
        return;
    }

    if (env->GetArrayLength(opl_drum_maps_) != 128) {
        env->ThrowNew(env->FindClass("java/lang/IllegalArgumentException"), "opl_drum_maps array length not 128");
        return;
    }

    jclass opl_timbre_class = env->FindClass("com/davidhs/fmtest/Gmtimbre$opl_timbre");
    jfieldID opl_timbre_mult_id = env->GetFieldID(opl_timbre_class, "mult", "[B");
    jfieldID opl_timbre_tl_id = env->GetFieldID(opl_timbre_class, "tl", "[B");
    jfieldID opl_timbre_ad_id = env->GetFieldID(opl_timbre_class, "ad", "[B");
    jfieldID opl_timbre_sr_id = env->GetFieldID(opl_timbre_class, "sr", "[B");
    jfieldID opl_timbre_wf_id = env->GetFieldID(opl_timbre_class, "wf", "[B");
    jfieldID opl_timbre_fb_id = env->GetFieldID(opl_timbre_class, "fb", "B");
    jfieldID opl_timbre_note_id = env->GetFieldID(opl_timbre_class, "note", "B");
    jfieldID opl_timbre_octave_id = env->GetFieldID(opl_timbre_class, "octave", "B");
    for (int i = 0; i < env->GetArrayLength(opl_timbres_); i++) {
        jobject opl_timbre_object = env->GetObjectArrayElement(opl_timbres_, i);
        jbyteArray opl_timbre_mult_jarray = (jbyteArray)env->GetObjectField(opl_timbre_object, opl_timbre_mult_id);
        jbyte *opl_timbre_mult_array = env->GetByteArrayElements(opl_timbre_mult_jarray, NULL);
//        opl_timbre_mult_array = (jbyte*)opl_timbres[i].mult;
        memcpy(opl_timbre_mult_array, opl_timbres[i].mult);
        env->ReleaseByteArrayElements(opl_timbre_mult_jarray, opl_timbre_mult_array, 0);
    }
}