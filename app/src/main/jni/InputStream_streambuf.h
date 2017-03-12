//
// Created by David Silaban on 11/03/2017.
//

#ifndef FMTEST_INPUTSTREAM_STREAMBUF_H
#define FMTEST_INPUTSTREAM_STREAMBUF_H


#include <streambuf>
#include <jni.h>

class InputStream_streambuf : public std::streambuf {

public:
    InputStream_streambuf(JNIEnv *, jobject);

protected:
    virtual std::streamsize showmanyc() override;

public:

    ~InputStream_streambuf();

protected:
    std::streambuf::traits_type::int_type underflow() override;

    traits_type::int_type sync() override;

private:
    JNIEnv *env;
    jobject j_input_stream;
    char input_buffer[1];
};


#endif //FMTEST_INPUTSTREAM_STREAMBUF_H
