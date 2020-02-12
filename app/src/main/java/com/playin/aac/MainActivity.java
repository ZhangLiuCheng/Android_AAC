package com.playin.aac;

import android.Manifest;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Arrays;

import static com.playin.aac.Constant.AUDIO_FORMAT;
import static com.playin.aac.Constant.BIT_RATE;
import static com.playin.aac.Constant.CHANNEL_CONFIG;
import static com.playin.aac.Constant.CHANNEL_COUNT;
import static com.playin.aac.Constant.SAMPLE_RATE;

public class MainActivity extends AppCompatActivity implements AacEncoder.EncoderListener,
        AacDecoder.DecoderListener {

    private final RxPermissions rxPermissions = new RxPermissions(this);

    private AudioRecord audioRecord;

    private AacFileUtil fileUtil;
    private AacEncoder aacEncoder;
    private AacDecoder aacDecoder;
    private PlayAudio playAudio;
    private FFmpegAAC fFmpegAAC;

    private boolean recording;
    private boolean playing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fileUtil = new AacFileUtil(this);
        aacEncoder = new AacEncoder(this);
        aacDecoder = new AacDecoder(this);
        playAudio = new PlayAudio();
        fFmpegAAC = new FFmpegAAC();
        fFmpegAAC.init(SAMPLE_RATE, CHANNEL_COUNT, BIT_RATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRecord(null);
    }

    public void startRecord(View view) {
        rxPermissions.request(Manifest.permission.RECORD_AUDIO)
                .subscribe(granted -> {
                    if (granted) {
                        record();
                    } else {
                    }
                });
    }

    public void stopRecord(View view) {
        recording = false;
        if (null != audioRecord) {
            audioRecord.stop();
            audioRecord.release();
        }
    }

    public void playAudioWithMediaCodec(View view) {
        playing = true;
        fileUtil.prepareRead();
        new Thread(() -> {
            while (playing) {
                // 读取文件aac数据
                byte[] buf = fileUtil.readAac();
                if (buf == null) {
                    break;
                } else {
                    // aac数据通过AacDecoder类解码成pcm,通过pcmData方法回调
                    aacDecoder.decodeAAc(buf, 0, buf.length);
                }
            }
        }).start();
    }


    public void playAudioWithFFmpeg(View view) {
        playing = true;
        fileUtil.prepareRead();
        new Thread(() -> {
            while (playing) {
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 读取文件aac数据
                byte[] buf = fileUtil.readAac();
                if (buf == null) {
                    break;
                } else {
                    byte[] pcmBuf = fFmpegAAC.decoding(buf, 0, buf.length);

                    Log.e("TAG", " pcmBuf  ---->  " + Arrays.toString(pcmBuf));
                    if (null != pcmBuf) {
                        playAudio.playAudioTrack(pcmBuf, 0, pcmBuf.length);
                    }
                }
            }
        }).start();
    }

    public void record() {
        int minBufferSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT);
        AudioRecord audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                minBufferSize);

        audioRecord.startRecording();
        recording = true;
        final byte[] buffer = new byte[minBufferSize / 3];
        Log.e("TAG", "minBufferSize  " + minBufferSize + "   ====  " + buffer.length);

        new Thread(() -> {
            while (recording) {
                int len = audioRecord.read(buffer, 0, buffer.length);
                if (0 < len) {
                    // pcm 通过AacEncoder类编码成aac,通过aacData方法回调
                    aacEncoder.encoderAAC(buffer);
                }
            }
        }).start();
    }

    @Override
    public void aacData(byte[] buf) {
        Log.e("TAG", "aacData  ---> " + Arrays.toString(buf));
        fileUtil.writeAac(buf);
    }

    @Override
    public void pcmData(byte[] buf, int offset, int length) {
        Log.e("TAG", "pcmData  ---> " + Arrays.toString(buf));
        playAudio.playAudioTrack(buf, 0, length);
    }
}
