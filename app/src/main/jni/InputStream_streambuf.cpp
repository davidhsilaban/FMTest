//
// Created by David Silaban on 11/03/2017.
//

#include "InputStream_streambuf.h"

std::streambuf::traits_type::int_type InputStream_streambuf::underflow() {
    jclass inputstream_class = env->GetObjectClass(j_input_stream);
    jmethodID inputstream_read_method_id = env->GetMethodID(inputstream_class, "read", "()I");

    if (gptr() == NULL || gptr() >= egptr()) {
        int value = env->CallIntMethod(j_input_stream, inputstream_read_method_id);
        env->DeleteLocalRef(inputstream_class);
        if (value == -1) {
            return traits_type::eof();
        } else {
            *input_buffer = value;
            setg(input_buffer, input_buffer, input_buffer+1);
            return traits_type::to_int_type(*input_buffer);
        }
    } else {
        return traits_type::to_int_type(*input_buffer);
    }
}

InputStream_streambuf::InputStream_streambuf(JNIEnv *env, jobject j_input_stream) : env(env), j_input_stream(j_input_stream) {

}

std::streambuf::traits_type::int_type InputStream_streambuf::sync() {
    return 0;
}

InputStream_streambuf::~InputStream_streambuf() {
    jclass inputstream_class = env->GetObjectClass(j_input_stream);
    jmethodID inputstream_close_method_id = env->GetMethodID(inputstream_class, "close", "()V");
    env->CallVoidMethod(j_input_stream, inputstream_close_method_id);
    env->DeleteLocalRef(j_input_stream);
}

std::streamsize InputStream_streambuf::showmanyc() {
    jclass inputstream_class = env->GetObjectClass(j_input_stream);
    jmethodID inputstream_available_method_id = env->GetMethodID(inputstream_class, "available", "()I");
    return env->CallIntMethod(j_input_stream, inputstream_available_method_id);
}
