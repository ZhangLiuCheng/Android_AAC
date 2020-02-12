//
// Created by zhangliucheng on 2019-08-21.
//
//

#include <android/log.h>
#define LOG_TAG "AAC"

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" {
#include "ffmpeg/include/libavcodec/avcodec.h"
#include "ffmpeg/include/libavformat/avformat.h"
#include "ffmpeg/include/libavutil/avutil.h"
#include "ffmpeg/include/libavutil/frame.h"
#include "ffmpeg/include/libavutil/imgutils.h"
#include "ffmpeg/include/libavutil/opt.h"
#include "ffmpeg/include/libswresample/swresample.h"
}

#include <jni.h>

AVCodecContext *aacCodecCtx = NULL;
AVCodec *aacCodec;
AVFrame *aacFrame;
AVPacket aacPacket;
SwrContext *au_convert_ctx;

extern "C"
JNIEXPORT jint JNICALL
Java_com_playin_aac_FFmpegAAC_init(JNIEnv *env, jobject thiz, jint sample_rate, jint channel,
                                   jint bit) {
    av_register_all();
    avformat_network_init();
    av_init_packet(&aacPacket);
    aacCodec = avcodec_find_decoder(AV_CODEC_ID_AAC);

    if (aacCodec == NULL) {
        return -1;
    }

    aacCodecCtx = avcodec_alloc_context3(aacCodec);
    aacCodecCtx->codec_type = AVMEDIA_TYPE_AUDIO;
    aacCodecCtx->sample_rate = sample_rate;
    aacCodecCtx->channels = channel;
    aacCodecCtx->bit_rate = bit;
    aacCodecCtx->channel_layout = AV_CH_LAYOUT_STEREO;

    if (avcodec_open2(aacCodecCtx, aacCodec, NULL) < 0) {
        return -1;
    }
    aacFrame = av_frame_alloc();
    LOGE("%s", "方法初始化成功");

    au_convert_ctx = swr_alloc();
    au_convert_ctx = swr_alloc_set_opts(au_convert_ctx, AV_CH_LAYOUT_STEREO, AV_SAMPLE_FMT_S16,
                                        sample_rate,
                                        aacCodecCtx->channel_layout, aacCodecCtx->sample_fmt,
                                        aacCodecCtx->sample_rate, 0, NULL);
    swr_init(au_convert_ctx);
    return 0;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_playin_aac_FFmpegAAC_decoding(JNIEnv *env, jobject thiz, jbyteArray aac_buf, jint offset,
                                       jint length) {
    jsize len = env->GetArrayLength(aac_buf);
    jbyte *jbarray = env->GetByteArrayElements(aac_buf, 0);
    av_new_packet(&aacPacket, len);
    memcpy(aacPacket.data, jbarray, len);

    avcodec_send_packet(aacCodecCtx, &aacPacket);
    int result = avcodec_receive_frame(aacCodecCtx, aacFrame);

    if (result == 0) {
        int out_linesize;
        int out_buffer_size = av_samples_get_buffer_size(&out_linesize, aacCodecCtx->channels,
                                                         aacCodecCtx->frame_size,
                                                         aacCodecCtx->sample_fmt, 1);
        uint8_t *out_buffer = (uint8_t *) av_malloc(out_buffer_size);
        swr_convert(au_convert_ctx, &out_buffer, out_linesize, (const uint8_t **) aacFrame->data,
                    aacFrame->nb_samples);

        jbyteArray jarray = env->NewByteArray(out_linesize);
        env->SetByteArrayRegion(jarray, 0, out_linesize, reinterpret_cast<const jbyte *>(out_buffer));

        av_free(out_buffer);
        return jarray;
    }

    return NULL;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_playin_aac_FFmpegAAC_close(JNIEnv *env, jobject thiz) {
    if (NULL != aacCodecCtx) {
        avcodec_close(aacCodecCtx);
        avcodec_free_context(&aacCodecCtx);
        aacCodecCtx = NULL;
    }
    if (aacFrame) {
        av_frame_free(&aacFrame);
        aacFrame = NULL;
    }

    if (NULL != au_convert_ctx) {
        swr_free(&au_convert_ctx);
        au_convert_ctx = NULL;
    }
}