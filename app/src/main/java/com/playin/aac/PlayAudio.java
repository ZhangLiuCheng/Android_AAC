package com.playin.aac;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import static com.playin.aac.Constant.AUDIO_FORMAT;
import static com.playin.aac.Constant.CHANNEL_CONFIG;
import static com.playin.aac.Constant.SAMPLE_RATE;

public class PlayAudio {

    private AudioTrack mAudioTrack;

    public PlayAudio() {
        init();
    }

    public void init() {
        if (mAudioTrack != null) {
            release();
        }
        int minBufSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                CHANNEL_CONFIG, AUDIO_FORMAT);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, minBufSize, AudioTrack.MODE_STREAM);
        mAudioTrack.play();
    }

    public void release() {
        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack.release();
        }
    }

    public void playAudioTrack(byte[] data, int offset, int length) {
        if (data == null || data.length == 0) {
            return;
        }
        try {
            mAudioTrack.write(data, offset, length);
        } catch (Exception e) {
            Log.e("PlayAudio", "AudioTrack Exception : " + e.toString());
        }
    }
}