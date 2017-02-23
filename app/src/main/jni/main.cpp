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