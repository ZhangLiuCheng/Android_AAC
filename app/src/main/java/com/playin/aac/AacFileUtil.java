package com.playin.aac;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class AacFileUtil {

    private final static String TAG = "AacFileUtil";

    private RandomAccessFile rafile;

    public AacFileUtil(Context context) {

        File file = new File(context.getExternalFilesDir(null), "test.aac");
//        File file = new File(context.getExternalFilesDir(null), "accdump-ffmpeg.apv");
        Log.e(TAG, "------>  " + file.getAbsolutePath());
        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            rafile = new RandomAccessFile(file, "rw");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeAac(byte[] aac) {
        try {
            int aacLen = aac.length;
            byte[] wrap = new byte[4 + aacLen];
            byte[] lenBuf = intToBytes(aacLen);
            System.arraycopy(lenBuf, 0, wrap, 0, lenBuf.length);
            System.arraycopy(aac, 0, wrap, 4, aac.length);
            rafile.write(wrap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void prepareRead() {
        try {
            rafile.seek(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] readAac() {
        try {
            byte[] lenBuf = new byte[4];
            rafile.read(lenBuf);
            int aacLen = bytesToInt(lenBuf);
            if (aacLen <= 0) return null;

            // 丢弃4个字节
//            rafile.read(lenBuf);

            byte[] aacBuf = new byte[aacLen];
            rafile.read(aacBuf);

            Log.e(TAG, "---->  " + Arrays.toString(aacBuf));
            return aacBuf;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] intToBytes(int num){
        return ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(num).array();
    }

    private int bytesToInt(byte[] bytes){
        return ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN).getInt();
    }
}
