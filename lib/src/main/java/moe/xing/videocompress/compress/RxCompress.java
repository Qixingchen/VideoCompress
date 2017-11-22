package moe.xing.videocompress.compress;

import android.support.annotation.NonNull;
import android.support.annotation.Size;

import org.m4m.MediaFileInfo;
import org.m4m.Uri;
import org.m4m.android.AndroidMediaObjectFactory;

import java.io.File;
import java.io.IOException;

import moe.xing.baseutils.Init;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by Qi Xingchen on 2017/10/31.
 */

@SuppressWarnings("SameParameterValue")
public class RxCompress {

    /**
     * compress Video
     *
     * @param fromUri      origin video Uri
     * @param dest         dest file path
     * @param outHeight    out height
     * @param outWidth     out width
     * @param videoBitRate out video bit rate in Kbps
     * @return Observable<Double> with progress
     */
    @NonNull
    public static Observable<Double> Compress(@NonNull String fromUri, @NonNull File dest, int outHeight, int outWidth, int videoBitRate) {
        return Observable.create(new Compress(new Uri(fromUri), dest, outHeight, outWidth, videoBitRate));
    }

    /**
     * compress Video
     *
     * @param fromUri      origin video Uri
     * @param dest         dest file path
     * @param outSize      out size first is height,second is width
     * @param videoBitRate out video bit rate in Kbps
     * @return Observable<Double> with progress
     */
    @NonNull
    public static Observable<Double> Compress(@NonNull String fromUri, @NonNull File dest, @NonNull @Size(2) int[] outSize, int videoBitRate) {
        return Observable.create(new Compress(new Uri(fromUri), dest, outSize[0], outSize[1], videoBitRate));
    }

    /**
     * get video out size with max side size
     *
     * @param originHeight video origin height
     * @param originWidth  video origin width
     * @param maxSideSize  output max side
     * @return int[2], first is height,second is width
     */
    @Size(value = 2)
    @NonNull
    public static int[] getSizeWithMaxSideSize(int originHeight, int originWidth, int maxSideSize) {
        // is origin Height is large
        boolean isHeightLarge = originHeight > originWidth;
        int originLargeSize = isHeightLarge ? originHeight : originWidth;
        int originSmallSize = isHeightLarge ? originWidth : originHeight;

        int outLargeSide, outSmallSide;
        if (originLargeSize < maxSideSize) {
            outLargeSide = originLargeSize;
            outSmallSide = originSmallSize;
        } else {
            double div = 1.0 * originLargeSize / maxSideSize;
            outLargeSide = maxSideSize;
            outSmallSide = (int) Math.round(1.0 * originSmallSize / div);
        }

        if (isHeightLarge) {
            return new int[]{outLargeSide, outSmallSide};
        } else {
            return new int[]{outSmallSide, outLargeSide};
        }
    }

    /**
     * get video out size with max side size
     *
     * @param videoUri    video Uri
     * @param maxSideSize output max side
     * @return int[2], first is height,second is width
     * @throws IOException video not found or not support
     */
    @Size(value = 2)
    @NonNull
    public static int[] getSizeWithMaxSideSize(@NonNull String videoUri, int maxSideSize) throws IOException {

        MediaFileInfo mediaFileInfo = new org.m4m.MediaFileInfo(new AndroidMediaObjectFactory(Init.getApplication()));
        mediaFileInfo.setUri(new Uri(videoUri));
        org.m4m.VideoFormat videoFormat = (org.m4m.VideoFormat) mediaFileInfo.getVideoFormat();
        if (videoFormat == null) {
            throw new IOException("video format not support");
        }
        int videoWidthIn = videoFormat.getVideoFrameSize().width();
        int videoHeightIn = videoFormat.getVideoFrameSize().height();
        return getSizeWithMaxSideSize(videoHeightIn, videoWidthIn, maxSideSize);
    }

    private final static class Compress implements Observable.OnSubscribe<Double> {

        @NonNull
        private Uri from;
        private File dest;
        private int outHeight, outWidth, videoBitRate;

        Compress(@NonNull Uri from, File dest, int outHeight, int outWidth, int videoBitRate) {
            this.from = from;
            this.dest = dest;
            this.outHeight = outHeight;
            this.outWidth = outWidth;
            this.videoBitRate = videoBitRate;
        }

        @Override
        public void call(Subscriber<? super Double> subscriber) {
            new CompressCore(subscriber).transCode(from, dest, outHeight, outWidth, videoBitRate);
        }
    }

}
