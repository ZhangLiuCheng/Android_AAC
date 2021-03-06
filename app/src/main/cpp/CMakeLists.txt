
cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_BUILD_TYPE "Release")
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -s")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -s")

add_library(
        ffmpegAAC
        SHARED
        FFmpegAAC.cpp)

find_library(
        log-lib
        log)

include_directories(${CMAKE_SOURCE_DIR}/ffmpeg/include)

target_link_libraries(
        ffmpegAAC
        -lm -lz -landroid
        ${CMAKE_SOURCE_DIR}/ffmpeg/libs/${ANDROID_ABI}/libavformat.a
        ${CMAKE_SOURCE_DIR}/ffmpeg/libs/${ANDROID_ABI}/libavcodec.a
        ${CMAKE_SOURCE_DIR}/ffmpeg/libs/${ANDROID_ABI}/libavutil.a
        ${CMAKE_SOURCE_DIR}/ffmpeg/libs/${ANDROID_ABI}/libswscale.a
        ${CMAKE_SOURCE_DIR}/ffmpeg/libs/${ANDROID_ABI}/libswresample.a
        ${log-lib}
)
