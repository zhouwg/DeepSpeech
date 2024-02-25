#!/usr/bin/env bash

# Copyright (c) Project KanTV(www.cde-os.com). 2021-. All rights reserved.

function build_init()
{
if [ "x${PROJECT_ROOT_PATH}" == "x" ]; then
    echo "pwd is `pwd`"
    echo "pls run . build/envsetup in project's toplevel directory firstly"
    exit 1
fi
}


function build_tensorflow()
{
cd ${PROJECT_ROOT_PATH}/tensorflow
./build_arm32.sh
./build_arm64.sh

}


function build_jni()
{
cd ${PROJECT_ROOT_PATH}

mkdir -p deepspeech-jni/libs/armeabi-v7a
mkdir -p deepspeech-jni/libs/arm64-v8a
mkdir -p examples/androiddemo/app/src/main/jniLibs/armeabi-v7a
mkdir -p examples/androiddemo/app/src/main/jniLibs/arm64-v8a
mkdir -p examples/androiddemo-streaming/app/src/main/jniLibs/armeabi-v7a
mkdir -p examples/androiddemo-streaming/app/src/main/jniLibs/arm64-v8a

#modify following line to adapto to your local dev envs
LOCAL_BAZEL_PATH=/home/weiguo/.cache/bazel/_bazel_weiguo/d483cd2a2d9204cb5bb4d870c2729238

/bin/cp -fv ${LOCAL_BAZEL_PATH}/execroot/org_tensorflow/bazel-out/armeabi-v7a-opt/bin/native_client/libdeepspeech.so deepspeech-jni/libs/armeabi-v7a/
/bin/cp -fv ${LOCAL_BAZEL_PATH}/execroot/org_tensorflow/bazel-out/arm64-v8a-opt/bin/native_client/libdeepspeech.so   deepspeech-jni/libs/arm64-v8a/

/bin/cp -fv ${LOCAL_BAZEL_PATH}/execroot/org_tensorflow/bazel-out/armeabi-v7a-opt/bin/native_client/libdeepspeech.so examples/androiddemo/app/src/main/jniLibs/armeabi-v7a/
/bin/cp -fv ${LOCAL_BAZEL_PATH}/execroot/org_tensorflow/bazel-out/arm64-v8a-opt/bin/native_client/libdeepspeech.so   examples/androiddemo/app/src/main/jniLibs/arm64-v8a/

/bin/cp -fv ${LOCAL_BAZEL_PATH}/execroot/org_tensorflow/bazel-out/armeabi-v7a-opt/bin/native_client/libdeepspeech.so examples/androiddemo-streaming/app/src/main/jniLibs/armeabi-v7a/
/bin/cp -fv ${LOCAL_BAZEL_PATH}/execroot/org_tensorflow/bazel-out/arm64-v8a-opt/bin/native_client/libdeepspeech.so   examples/androiddemo-streaming/app/src/main/jniLibs/arm64-v8a/


cd ${PROJECT_ROOT_PATH}/deepspeech-jni
./build.sh

}


function build_check()
{
cd ${PROJECT_ROOT_PATH}

ls -l deepspeech-jni/libs/arm64-v8a/
ls -l deepspeech-jni/libs/armeabi-v7a/


/bin/cp -fv deepspeech-jni/libs/armeabi-v7a/lib*jni*.so examples/androiddemo/app/src/main/jniLibs/armeabi-v7a/
/bin/cp -fv deepspeech-jni/libs/arm64-v8a/lib*jni*.so   examples/androiddemo/app/src/main/jniLibs/arm64-v8a/

/bin/cp -fv deepspeech-jni/libs/armeabi-v7a/lib*jni*.so examples/androiddemo-streaming/app/src/main/jniLibs/armeabi-v7a/
/bin/cp -fv deepspeech-jni/libs/arm64-v8a/lib*jni*.so   examples/androiddemo-streaming/app/src/main/jniLibs/arm64-v8a/

ls -l examples/androiddemo/app/src/main/jniLibs/armeabi-v7a/
ls -l examples/androiddemo/app/src/main/jniLibs/arm64-v8a/

ls -l examples/androiddemo-streaming/app/src/main/jniLibs/armeabi-v7a/
ls -l examples/androiddemo-streaming/app/src/main/jniLibs/arm64-v8a/

}

build_init
build_tensorflow
build_jni
build_check
