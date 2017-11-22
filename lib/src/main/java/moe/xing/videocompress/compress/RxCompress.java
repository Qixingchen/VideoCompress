package moe.xing.videocompress.compress;

import android.support.annotation.NonNull;

import org.m4m.Uri;

import java.io.File;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by Qi Xingchen on 2017/10/31.
 */

public class RxCompress {

    @NonNull
    public static Observable<Double> Compress(@NonNull Uri from, @NonNull File dest, int outHeight, int outWidth, int videoBitRate) {
        return Observable.create(new Compress(from, dest, outHeight, outWidth, videoBitRate));
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
