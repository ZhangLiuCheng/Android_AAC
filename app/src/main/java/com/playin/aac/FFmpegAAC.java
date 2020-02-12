package com.playin.aac;

public class FFmpegAAC {

    static {
        System.loadLibrary("ffmpegAAC");
    }

    public native int init(int sampleRate, int channel, int bit);

    public native byte[] decoding(byte[] aacBuf, int offset, int length);

    public native void close();
}
