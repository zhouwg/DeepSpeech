#!/usr/bin/env bash

# Copyright (c) Project KanTV(www.cde-os.com). 2021-. All rights reserved.

set -e

TARGET=libdeepspeech-jni.so
LIB_TYPE=SHARED
LIB_BUILD_TYPE=Release

if [ "${PROJECT_BUILD_TYPE}" == "release" ]; then
    LIB_BUILD_TYPE=Release
fi

if [ "${PROJECT_BUILD_TYPE}" == "debug" ]; then
    LIB_BUILD_TYPE=Debug
fi


if [ "x${PROJECT_ROOT_PATH}" == "x" ]; then
    echo "pwd is `pwd`"
    echo "pls run . build/envsetup in project's toplevel directory firstly"
    exit 1
fi

if [ "x${ANDROID_NDK}" == "x" ]; then
        echo "pwd is `pwd`"
        echo "pls run . build/envsetup in project's toplevel directory firstly"
        exit 1
fi

if [ -d out ]; then
    echo "remove out directory in `pwd`"
    rm -rf out
fi

function build_arm64
{
LIB_TYPE=SHARED
cmake -H. -B./out/arm64-v8a -DPRJ_ROOT_PATH=${PROJECT_ROOT_PATH} -DTARGET_NAME=${TARGET} -DCMAKE_BUILD_TYPE=${LIB_BUILD_TYPE} -DBUILD_TARGET=${BUILD_TARGET} -DBULD_LIB_TYPE=${LIB_TYPE} -DANDROID_ABI=arm64-v8a -DANDROID_PLATFORM=android-21 -DANDROID_NDK=${ANDROID_NDK}  -DCMAKE_TOOLCHAIN_FILE=${ANDROID_NDK}/build/cmake/android.toolchain.cmake
cd ./out/arm64-v8a
make
if [ "${PROJECT_BUILD_TYPE}" == "release" ]; then
    echo "${ANDROID_NDK}/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android-strip -s ${TARGET}"
    echo "pwd `pwd`"
    ${ANDROID_NDK}/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android-strip -s ${TARGET}
fi

cd -
}


function build_armv7a
{
LIB_TYPE=SHARED
cmake -H. -B./out/armeabi-v7a -DPRJ_ROOT_PATH=${PROJECT_ROOT_PATH} -DTARGET_NAME=${TARGET} -DCMAKE_BUILD_TYPE=${LIB_BUILD_TYPE}  -DENABLE_OPTIMIZE_BY_MEMPOOL_AND_NEON=${ENABLE_OPTIMIZE_BY_MEMPOOL_AND_NEON} -DBUILD_TARGET=${BUILD_TARGET} -DBULD_LIB_TYPE=${LIB_TYPE} -DANDROID_ABI=armeabi-v7a -DANDROID_PLATFORM=android-21 -DANDROID_NDK=${ANDROID_NDK}  -DCMAKE_TOOLCHAIN_FILE=${ANDROID_NDK}/build/cmake/android.toolchain.cmake
cd ./out/armeabi-v7a
make
if [ "${PROJECT_BUILD_TYPE}" == "release" ]; then
    echo "${ANDROID_NDK}/toolchains/llvm/prebuilt/linux-x86_64/bin/arm-linux-androideabi-strip -s ${TARGET}"
    echo "pwd `pwd`"
    ${ANDROID_NDK}/toolchains/llvm/prebuilt/linux-x86_64/bin/arm-linux-androideabi-strip -s ${TARGET}
fi


cd -

}


build_arm64
build_armv7a
