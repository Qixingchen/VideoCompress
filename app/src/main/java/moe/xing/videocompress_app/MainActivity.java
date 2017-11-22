package moe.xing.videocompress_app;

import android.databinding.DataBindingUtil;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;

import com.jakewharton.rxbinding.view.RxView;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import moe.xing.baseutils.Init;
import moe.xing.baseutils.utils.FileUtils;
import moe.xing.baseutils.utils.LogHelper;
import moe.xing.rxfilepicker.RxGetFile;
import moe.xing.videocompress.compress.RxCompress;
import moe.xing.videocompress_app.databinding.ActivityMainBinding;
import moe.xing.videoplayer.PlayerActivity;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init.init(getApplication(), true, "", "");
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        RxView.clicks(mBinding.selectFile).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                getVideo();

            }
        });
    }

    private void getVideo() {

        final File out;
        try {
            out = FileUtils.getCacheFile("out.mp4");
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        RxGetFile.newBuilder().isSingle(true).type("video/*").build()
                .flatMap(new Func1<File, Observable<Double>>() {
                    @Override
                    public Observable<Double> call(File file) {
                        int rate;
                        try {
                            rate = Integer.parseInt(mBinding.rate.getText().toString());
                        } catch (NumberFormatException ignore) {
                            rate = 3000;
                        }
                        if (rate < 1) {
                            rate = 3000;
                        }
                        try {
                            int[] outSize = RxCompress.getSizeWithMaxSideSize(Uri.fromFile(file).toString(), 1280);
                            return RxCompress.Compress(Uri.fromFile(file).toString(), out, outSize, rate);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return RxCompress.Compress(Uri.fromFile(file).toString(), out, 0, 0, rate);
                        }


                    }
                })
                .throttleLast(1, TimeUnit.SECONDS)
                .subscribe(new Subscriber<Double>() {
                    @Override
                    public void onCompleted() {
                        LogHelper.w("onCompleted");
                        //获取视频信息
                        MediaPlayer mp = MediaPlayer.create(MainActivity.this, Uri.fromFile(out));
                        int duration = mp.getDuration();
                        LogHelper.i("video duration:" + TimeUnit.MILLISECONDS.toSeconds(duration));
                        LogHelper.i("video height&width:" + mp.getVideoHeight() + " " + mp.getVideoWidth());
                        LogHelper.i("fileSize:" + Formatter.formatFileSize(MainActivity.this, out.length()));
                        LogHelper.i("kibit rate :" + out.length() * 8 / TimeUnit.MILLISECONDS.toSeconds(duration) / 1024);


                        startActivity(PlayerActivity.getStartIntent(MainActivity.this, out.getAbsolutePath(), true));
                    }

                    @Override
                    public void onError(Throwable e) {
                        LogHelper.e(e);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(Double aDouble) {
                        LogHelper.i(String.valueOf(aDouble));
                    }
                });
    }

}

