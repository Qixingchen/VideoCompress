VideoCompress

[![Release](https://jitpack.io/v/Qixingchen/VideoCompress.svg?style=flat-square)](https://jitpack.io/#Qixingchen/VideoCompress)
[![Build Status](https://travis-ci.org/Qixingchen/VideoCompress.svg?branch=master)](https://travis-ci.org/Qixingchen/VideoCompress)
[![Coverage Status](https://coveralls.io/repos/github/Qixingchen/VideoCompress/badge.svg)](https://coveralls.io/github/Qixingchen/VideoCompress)

---
### download

 use [jitpack](https://jitpack.io/#Qixingchen/VideoCompress)

### how to use
``` java

RxCompress.Compress(FileUri, outFile, outHeight, outWidth, rateInKib)
.subscribe(new Subscriber<Double>() {
        @Override
        public void onCompleted() {
             // todo
        }

        @Override
        public void onError(Throwable e) {
            // todo
        }

        @Override
        public void onNext(Double progress) {
             // todo
        }
```


read javadoc in [jitpack](https://jitpack.io/com/github/Qixingchen/VideoCompress/-SNAPSHOT/javadoc/)
