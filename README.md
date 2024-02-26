# DeepSpeech

![Mozilla's DeepSpeech](https://github.com/mozilla/DeepSpeech) is an open source embedded (offline, on-device) speech-to-text engine which can run in **real time** on devices, using a model trained by machine learning techniques based on `Baidu's Deep Speech research paper <https://arxiv.org/abs/1412.5567>`_. Project DeepSpeech uses Google's `TensorFlow <https://www.tensorflow.org/>`_ to make the implementation easier.

This is source code of DeepSpeech(my customized DeepSpeech for project KanTV), derived from original Mozilla's DeepSpeech. with a little enhancements(because I'm **NOT** technical expert in AI filed):

- refine the entire log subsystem

- refine the build system

- refine the Android examples and validated them in my various phone(Huawei's phone with hisilicon SoC, Xiaomi 14 with latest qcom SoC------Qualcomm SM8650-AB Snapdragon 8 Gen 3 (4 nm))

The goal of this project is:

- plain C/C++/Java implementation **without dependencies** 
- Android **turn-key project** for AI experts/ASR researchers and software developers

### How to build project for target Android

#### prerequisites

- Host OS information:

```
uname -a

Linux 5.8.0-43-generic #49~20.04.1-Ubuntu SMP Fri Feb 5 09:57:56 UTC 2021 x86_64 x86_64 x86_64 GNU/Linux

cat /etc/issue

Ubuntu 20.04.2 LTS \n \l

```


- tools & utilities
```
sudo apt-get update
sudo apt-get install vim -y
sudo apt-get install net-tools -y
sudo apt-get install build-essential -y
sudo apt-get install cmake -y
sudo apt-get install curl -y
sudo apt-get install python -y
sudo apt-get install tcl expect -y
sudo apt-get install nginx -y
sudo apt-get install git -y
sudo apt-get install spawn-fcgi -y
sudo apt-get install u-boot-tools -y

sudo apt-get install libbrotli-dev -y
sudo apt-get install nasm -y
sudo apt-get install yasm -y
sudo apt-get install libass-dev -y
sudo apt-get install libx11-dev -y
sduo apt-get install libxcb-xinerama0-dev -y
sudo apt-get install libxfixes-dev -y
sudo apt-get install libxcb-xfixes0-dev -y
sudo apt-get install libxinerama-dev -y
sudo apt-get install libxcb-xinput-dev -y
sudo apt-get install libxi-dev -y
sudo apt-get install libasound2-dev -y
sudo apt-get install libxv-dev -y
sudo apt-get install libsdl2-dev -y
sudo apt install openjdk-17-jdk-headless -y

sudo apt-get install -y android-tools-adb android-tools-fastboot autoconf \
        automake bc bison build-essential ccache cscope curl device-tree-compiler \
        expect flex ftp-upload gdisk acpica-tools libattr1-dev libcap-dev \
        libfdt-dev libftdi-dev libglib2.0-dev libhidapi-dev libncurses5-dev \
        libssl-dev libtool make \
        mtools netcat python-crypto python3-crypto python-pyelftools \
        python3-pycryptodome python3-pyelftools python3-serial \
        rsync unzip uuid-dev xdg-utils xterm xz-utils zlib1g-dev

```

- bazel
  
  download ![bazel-3.1.0](https://github.com/bazelbuild/bazel/releases?page=5) and install bazel manually

```
  wget https://github.com/bazelbuild/bazel/releases/download/3.1.0/bazel-3.1.0-linux-x86_64
```
```
  sudo ./bazel-3.1.0-installer-linux-x86_64.sh
```

- Android NDK & Android Studio

  download and install Android Studio and Android NDK-r21e manually
  
  [Android NDK-r21e(LTS)](https://developer.android.com/ndk/downloads)


  [Android Studio 4.2.1](https://developer.android.google.cn/studio)



#### Fetch source codes

```
git clone https://github.com/zhouwg/DeepSpeech
cd DeepSpeech
git checkout kantv
```

#### Pre-Build

modify build script(build/envsetup.sh) to adapt to your local dev envs

https://github.com/zhouwg/DeepSpeech/blob/kantv/build/envsetup.sh#L34



#### Build 

- step1:build all native codes to generated essential libs

```
. build/envsetup.sh
./build-all.sh
```

- step2: build Android examples

build Android examples by latest Android Studio IDE


#### Post-Build

 download model file from ![offcial DeepSpeech](https://github.com/mozilla/DeepSpeech/releases/tag/v0.9.3) and upload model file to real Android phone


 
```
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.9.3/deepspeech-0.9.3-models.scorer
```

```
wget https://github.com/mozilla/DeepSpeech/releases/download/v0.9.3/deepspeech-0.9.3-models.tflite
```

```
adb push deepspeech-0.9.3-models.tflite  /sdcard/
deepspeech-0.9.3-models.tflite: 1 file pushed. 17.4 MB/s (47331784 bytes in 2.598s)

adb push deepspeech-0.9.3-models.scorer  /sdcard/
deepspeech-0.9.3-models.scorer: 1 file pushed. 16.4 MB/s (953363776 bytes in 55.349s)

```

#### Run Android example on real Android phone

![Screenshot_20240225_165453_com cdeos deepspeechdemo](https://github.com/zhouwg/kantv/assets/6889919/c8a2cd0e-59cd-4a39-8b27-265d9c3c5d57)

![Screenshot_20240225_165306_com cdeos deepspeechdemo](https://github.com/zhouwg/kantv/assets/6889919/44e15d5c-1c33-46a9-a90c-92cbb14bfe23)


### Support

- Please do not send e-mail to me(I'm just an experienced Andriod system software developer and interested in device-side AI application but **NOT** AI technical expert). Public technical discussion on github is preferred.
- feel free to submit issues or new features(focus on Android at the moment), volunteer support would be provided if time permits.
  

### Contribution

 If you want to contribute to project DeepSpeech, be sure to review the [opening issues](https://github.com/zhouwg/DeepSpeech/issues?q=is%3Aopen+is%3Aissue).

 We use [GitHub issues](https://github.com/zhouwg/DeepSpeech/issues) for tracking requests and bugs, please see [how to submit issue in this project ](https://github.com/zhouwg/DeepSpeech/issues/1).


### whisper.cpp

![whisper.cpp](https://github.com/ggerganov/whisper.cpp) is a powerful and excellent/amazing/shocking open source ASR project. compare to Mozilla's DeepSpeech on real Android phone:

- the ASR result of whisper.cpp is very very very acurate and much better than Mozilla's DeepSpeech
- the real-time performance of Mozilla's DeepSpeech is good and much better than whisper.cpp



### Acknowledgement

I learnt/got too much from open source community and many/sincerely thanks to all contributors of the great open source community, especially all original authors and all contributors of the great Linux & Android & FFmpeg and other excellent projects. Hope this project is a little useful for open source community.



### License

```
Copyright (c) 2021 maintainer of Mozilla's DeepSpeech

Licensed under MPL-2.0
```

```
Copyright (c) 2021-2023 maintainer of Project KanTV

Licensed under Apachev2.0 or later
```
