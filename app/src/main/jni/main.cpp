//
// Created by David Silaban on 2/23/2017.
//

#include <jni.h>
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
                                                 jint numsamples) {
    if (env->IsSameObject(sndptr_, NULL)) {
        sndptr_ = env->NewShortArray(numsamples);
    }

    jshort *sndptr = env->GetShortArrayElements(sndptr_, NULL);

    // TODO

    if (env->GetArrayLength(sndptr_) < numsamples) {
        env->ThrowNew(env->FindClass("Ljava/lang/IllegalArgumentException"), "Invalid sndptr array length");
        env->ReleaseShortArrayElements(sndptr_, sndptr, 0);
        return;
    }

    // Get OPL3 samples to buffer
    OPL3::adlib_getsample(sndptr, numsamples);

    env->ReleaseShortArrayElements(sndptr_, sndptr, 0);
}