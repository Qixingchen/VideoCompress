package moe.xing.videocompress.compress;

import android.media.MediaCodecInfo;
import android.support.annotation.NonNull;

import org.m4m.AudioFormat;
import org.m4m.Uri;
import org.m4m.VideoFormat;
import org.m4m.android.AndroidMediaObjectFactory;
import org.m4m.android.AudioFormatAndroid;
import org.m4m.android.VideoFormatAndroid;
import org.m4m.domain.Resolution;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import moe.xing.baseutils.Init;
import moe.xing.baseutils.utils.LogHelper;
import rx.Subscriber;

/**
 * Created by Qi Xingchen on 2017/10/31.
 */

@SuppressWarnings("WeakerAccess")
class CompressCore {


    protected final String videoMimeType = "video/avc";
    protected final String audioMimeType = "audio/mp4a-latm";
    protected int mVideoFrameRate = 30;
    protected int videoIFrameInterval = 1;
    protected int mAudioBitRate = 96 * 1024;
    protected org.m4m.MediaFileInfo mediaFileInfo = null;
    protected org.m4m.AudioFormat audioFormat = null;
    protected org.m4m.VideoFormat videoFormat = null;
    protected long duration = 0;
    protected int videoWidthIn = 0;
    protected int videoHeightIn = 0;
    @NonNull
    private Subscriber<? super Double> mSubscriber;
    public org.m4m.IProgressListener progressListener = new org.m4m.IProgressListener() {
        @Override
        public void onMediaStart() {

        }

        @Override
        public void onMediaProgress(float progress) {


            mSubscriber.onNext((double) progress);
        }


        @Override
        public void onMediaDone() {
            if (!mSubscriber.isUnsubscribed()) {
                LogHelper.i("onCompleted");
                mSubscriber.onCompleted();
            }
        }


        @Override
        public void onMediaPause() {
        }

        @Override
        public void onMediaStop() {
        }

        @Override
        public void onError(Exception exception) {

            mSubscriber.onError(exception);

        }
    };

    public CompressCore(@NonNull Subscriber<? super Double> subscriber) {
        mSubscriber = subscriber;
    }

    public void transCode(@NonNull Uri from, @NonNull File dest, int outHeight, int outWidth, int videoBitRate) {
        getFileInfo(from);
        if (outHeight == 0) {
            outHeight = videoHeightIn;
        }
        if (outWidth == 0) {
            outWidth = videoWidthIn;
        }
        AndroidMediaObjectFactory factory = new AndroidMediaObjectFactory(Init.getApplication());
        org.m4m.MediaComposer mediaComposer = new org.m4m.MediaComposer(factory, progressListener);
        try {
            mediaComposer.addSourceFile(from);
            int orientation = mediaFileInfo.getRotation();
            //m4m 将在压制时强制更换方向为 0 ,90/270 视频需要交换宽高
//            if (orientation == 90 || orientation == 270) {
//                int temp = outWidth;
//                //noinspection SuspiciousNameCombination
//                outWidth = outHeight;
//                outHeight = temp;
//            }

            mediaComposer.setTargetFile(dest.getAbsolutePath(), orientation);

            // set video encoder
            VideoFormatAndroid videoFormat = new VideoFormatAndroid(videoMimeType, outWidth, outHeight);

            videoFormat.setVideoBitRateInKBytes(videoBitRate);
            videoFormat.setVideoFrameRate(mVideoFrameRate);
            videoFormat.setVideoIFrameInterval(videoIFrameInterval);

            mediaComposer.setTargetVideoFormat(videoFormat);

            // set audio encoder

            /*
             *  Audio resampling is unsupported by current m4m release
             * Output sample rate and channel count are the same as for input.
             */
            AudioFormatAndroid aFormat = new AudioFormatAndroid(audioMimeType, audioFormat.getAudioSampleRateInHz(), audioFormat.getAudioChannelCount());

            aFormat.setAudioBitrateInBytes(mAudioBitRate);
            aFormat.setAudioProfile(MediaCodecInfo.CodecProfileLevel.AACObjectLC);

            mediaComposer.setTargetAudioFormat(aFormat);

            mediaComposer.start();
        } catch (IOException e) {
            if (!mSubscriber.isUnsubscribed()) {
                mSubscriber.onError(e);
            }
        }


    }

    protected void getFileInfo(@NonNull Uri from) {
        try {
            mediaFileInfo = new org.m4m.MediaFileInfo(new AndroidMediaObjectFactory(Init.getApplication()));
            mediaFileInfo.setUri(from);

            duration = mediaFileInfo.getDurationInMicroSec();

            audioFormat = (org.m4m.AudioFormat) mediaFileInfo.getAudioFormat();
            if (audioFormat == null) {

                mSubscriber.onError(new Throwable("音频格式不可用"));
            }

            videoFormat = (org.m4m.VideoFormat) mediaFileInfo.getVideoFormat();
            if (videoFormat == null) {
                mSubscriber.onError(new Throwable("视频格式不可用"));
            } else {
                videoWidthIn = videoFormat.getVideoFrameSize().width();
                videoHeightIn = videoFormat.getVideoFrameSize().height();
            }
            LogHelper.i(String.format(Locale.getDefault(), "Duration = %d sec \n", TimeUnit.MICROSECONDS.toSeconds(duration)));
            LogHelper.i(String.format(Locale.getDefault(), "videoHeightIn = %d ,videoWidthIn = %d \n", videoHeightIn, videoWidthIn));

            printVideoInfo(videoFormat);
            printAudioInfo(audioFormat);

        } catch (Exception e) {
            String message = (e.getMessage() != null) ? e.getMessage() : e.toString();


            mSubscriber.onError(new Throwable(message));

        }
    }

    private void printVideoInfo(VideoFormat videoFormat) {

        String noInfo = "No info";
        String videoCodec = noInfo;
        String videoMimeType = noInfo;
        String videoFrameRate = noInfo;
        String videoIFrameInterval = noInfo;
        String videoBitRateInKBytes = noInfo;

        String videoWidth = noInfo;
        String videoHeight = noInfo;

        //video format
        try {
            videoCodec = videoFormat.getVideoCodec();
        } catch (RuntimeException ignore) {
        }
        LogHelper.i("videoCodec: " + videoCodec);

        try {
            videoMimeType = videoFormat.getMimeType();
        } catch (RuntimeException ignore) {
        }
        LogHelper.i("videoMimeType: " + videoMimeType);

        try {
            Resolution resolution = videoFormat.getVideoFrameSize();
            videoWidth = String.format(Locale.getDefault(), "%d", resolution.width());
            videoHeight = String.format(Locale.getDefault(), "%d", resolution.height());
        } catch (RuntimeException ignore) {
        }
        LogHelper.i("videoWidth: " + videoWidth);
        LogHelper.i("videoHeight: " + videoHeight);


        try {
            videoFrameRate = String.format(Locale.getDefault(), "%d", videoFormat.getVideoFrameRate());
            mVideoFrameRate = videoFormat.getVideoFrameRate();
        } catch (RuntimeException ignore) {
        }
        LogHelper.i("mVideoFrameRate: " + videoFrameRate);

        try {
            videoIFrameInterval = String.format(Locale.getDefault(), "%d", videoFormat.getVideoIFrameInterval());
        } catch (RuntimeException ignore) {
        }
        LogHelper.i("videoIFrameInterval: " + videoIFrameInterval);

        try {
            videoBitRateInKBytes = String.format(Locale.getDefault(), "%d", videoFormat.getVideoBitRateInKBytes());
        } catch (RuntimeException ignore) {
        }
        LogHelper.i("videoBitRateInKBytes: " + videoBitRateInKBytes);
    }

    private void printAudioInfo(AudioFormat audioFormat) {
        String noInfo = "No info";
        String audioCodec = noInfo;
        String audioProfile = noInfo;
        String audioMimeType = noInfo;
        String audioChannelCount = noInfo;
        String audioBitrateInBytes = noInfo;
        String audioSampleRateHz = noInfo;

        //audio format
        try {
            audioCodec = String.format(Locale.getDefault(), "%s", audioFormat.getAudioCodec());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        LogHelper.i("audioCodec: " + audioCodec);

        try {
            audioProfile = String.format(Locale.getDefault(), "%d", audioFormat.getAudioProfile());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        LogHelper.i("audioProfile: " + audioProfile);

        try {
            audioMimeType = String.format(Locale.getDefault(), "%s", audioFormat.getMimeType());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        LogHelper.i("audioMimeType: " + audioMimeType);

        try {
            audioChannelCount = String.format(Locale.getDefault(), "%d", audioFormat.getAudioChannelCount());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        LogHelper.i("audioChannelCount: " + audioChannelCount);

        try {
            audioSampleRateHz = String.format(Locale.getDefault(), "%d", audioFormat.getAudioSampleRateInHz());
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        LogHelper.i("audioSampleRateHz: " + audioSampleRateHz);

        try {
            audioBitrateInBytes = String.format(Locale.getDefault(), "%d", audioFormat.getAudioBitrateInBytes());
            mAudioBitRate = audioFormat.getAudioBitrateInBytes();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        LogHelper.i("audioBitrateInBytes: " + audioBitrateInBytes);
    }
}
