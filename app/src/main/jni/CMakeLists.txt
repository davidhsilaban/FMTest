cmake_minimum_required(VERSION 3.4.1)

include_directories(include)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -g -Wall -O3 -std=c++11")

add_library(midifile STATIC
            src-library/Binasc.cpp
                src-library/MidiEvent.cpp
                src-library/MidiEventList.cpp
                src-library/MidiFile.cpp
                src-library/MidiMessage.cpp
                src-library/Options.cpp)

add_library(synth SHARED
            synth.cpp
            midifile.cpp
            InputStream_streambuf.cpp)

target_link_libraries(synth
                      midifile
                      log)