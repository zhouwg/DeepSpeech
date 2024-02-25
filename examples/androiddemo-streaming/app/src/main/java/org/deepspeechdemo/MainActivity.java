/*

Copyright (c) Project KanTV(www.cdeos.com). 2019-. All rights reserved.

*/

package org.deepspeechdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.os.Build;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.deepspeech.libdeepspeech.CDEAssetLoader;
import org.deepspeech.libdeepspeech.CDELog;
import org.deepspeech.libdeepspeech.CDEUtils;
import org.deepspeech.libdeepspeech.DeepSpeechModel;
import org.deepspeech.libdeepspeech.DeepSpeechStreamingState;
import org.deepspeechdemo.R;
import org.deepspeechdemo.R.id;
import org.w3c.dom.Text;


public final class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    private DeepSpeechModel model = null;
    private DeepSpeechStreamingState streamContext = null;
    private AtomicBoolean isRecording = new AtomicBoolean(false);
    private AtomicBoolean reCreateStream = new AtomicBoolean(false);
    private final String TFLITE_MODEL_FILENAME = "deepspeech-0.9.3-models.tflite";
    private final String SCORER_FILENAME = "deepspeech-0.9.3-models.scorer";
    private MainActivity mActivity;
    String decoded;
    String lastDecoded;
    int repeatCount = 0;
    long decodedCount = 0;
    private static final int SDK_PERMISSION_REQUEST = 4;

    TextView txtStatus;
    TextView txtTranscription;
    TextView txtTotalCounts;
    Button btnStartInference;


    private final void checkAudioPermission() {
        if (VERSION.SDK_INT >= 23) {
            String permission = "android.permission.RECORD_AUDIO";
            if (this.checkSelfPermission(permission) != 0) {
                ActivityCompat.requestPermissions((Activity) this, new String[]{permission}, 3);
            }
        }

    }


    private void transcribe(MainActivity mainActivity) {
        int audioBufferSize = 2048;
        short[] audioData = new short[audioBufferSize];

        txtTranscription.setText("");
        btnStartInference.setText((CharSequence) "Stop");


        if (model != null) {
            streamContext = model.createStream();
            CDELog.j(TAG, "sample rate:" + model.sampleRate());
            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, model.sampleRate(), AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, audioBufferSize);
            int audioSessionId = recorder.getAudioSessionId();
            AcousticEchoCanceler acousticEchoCanceler = AcousticEchoCanceler.create(audioSessionId);
            if (acousticEchoCanceler == null) {
                CDELog.j(TAG, "initAEC: ----->AcousticEchoCanceler create fail.");
            } else {
                //acousticEchoCanceler.setEnabled(true);
            }
            recorder.startRecording();

            while (this.isRecording.get()) {
                recorder.read(audioData, 0, audioBufferSize);
                if (!reCreateStream.get()) {
                    //CDELog.j(TAG, "feed audio and decode");
                    model.feedAudioContent(streamContext, audioData, audioData.length);
                    decoded = model.intermediateDecode(streamContext);
                }


                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //CDELog.j(TAG, "decoded content:" + decoded);
                        if (!decoded.isEmpty()
                                && !decoded.equals("none")
                                && !decoded.equals("etiennette")
                        ) {
                            txtTotalCounts.setText("Total decoded:" + Long.toString(decodedCount));
                            txtTranscription.setText(decoded);
                            int offset = txtTranscription.getLineCount() * txtTranscription.getLineHeight();
                            int line = txtTranscription.getLineCount();
                            if (offset > txtTranscription.getHeight()) {
                                txtTranscription.scrollTo(0, offset - txtTranscription.getHeight() - txtTranscription.getLineHeight() / 2 + 15);
                            }
                        }
                    }
                });

                if (!decoded.isEmpty()
                        && !decoded.equals("none")) {
                    if (!lastDecoded.equals(decoded)) {
                        decodedCount += decoded.length() - lastDecoded.length();
                    }
                }

                if ((lastDecoded != null) && (lastDecoded.equals(decoded))) {

                    repeatCount++;
                    CDELog.j(TAG, "last decoded is equal current(repeat count:" + repeatCount + ", decode:length:" + decoded.length());
                    if ((decoded.length() > 1024)
                            || ((repeatCount > 200) && (decoded.length() > 512))
                            || ((repeatCount > 50) && (decoded.length() == 0)) //电视背景音,没有人讲话
                            || (repeatCount > 200)

                    ) {
                        txtTranscription.setText("");
                        CDELog.j(TAG, "recreate stream");
                        CDELog.j(TAG, "total decode counts:" + decodedCount);
                        CDELog.j(TAG, "decode:length:" + decoded.length() + "  content:" + decoded);
                        decoded = model.finishStream(streamContext);
                        recorder.stop();
                        recorder.release();
                        streamContext = model.createStream();
                        recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, model.sampleRate(), 16, 2, audioBufferSize);
                        recorder.startRecording();
                        repeatCount = 0;
                    }
                }

                lastDecoded = decoded;


            }


            decoded = model.finishStream(streamContext);
            CDELog.j(TAG, "total decode counts:" + decodedCount);
            btnStartInference.setText((CharSequence) "Start");
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtTranscription.setText(decoded);
                    txtTranscription.setText("");
                }
            });

            recorder.stop();
            recorder.release();
            //txtTranscription.setText("");
        }

    }

    private final boolean createModel() {

        String scorerPath = CDEUtils.getDataPath() + '/' + SCORER_FILENAME;
        String modelPath = CDEUtils.getDataPath() + '/' + TFLITE_MODEL_FILENAME;

        CDELog.j(TAG, "model path: " + modelPath);
        CDELog.j(TAG, "score path:" + scorerPath);

        File modelFile = new File(modelPath);
        if (modelFile.exists()) {
            CDELog.j(TAG, "model file found");
        } else {

            CDELog.j(TAG, "model file not found");
            return false;
        }

        File file = new File(scorerPath);
        if (file.exists()) {
            CDELog.j(TAG, "scorefile found");
        } else {

            CDELog.j(TAG, "scorefile not found");
            return false;
        }
        model = new DeepSpeechModel(modelPath);
        if (model != null) {
            model.enableExternalScorer(scorerPath);
        } else {
            return false;
        }

        return true;
    }


    private final void startListening() {
        if (this.isRecording.compareAndSet(false, true)) {
            Thread workThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    transcribe(mActivity);

                }
            });
            workThread.start();
        }
    }


    private final void stopListening() {
        this.isRecording.set(false);
        txtTranscription.setText("");
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtStatus = (TextView) findViewById(id.status);
        txtTotalCounts = (TextView) findViewById(id.txtTotalCounts);
        txtTranscription = (TextView) findViewById(id.transcription);
        btnStartInference = (Button) findViewById(id.btnStartInference);

        txtTranscription.setMovementMethod(ScrollingMovementMethod.getInstance());

        btnStartInference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording.get()) {
                    CDELog.j(TAG, "stop listening");
                    stopListening();
                } else {
                    CDELog.j(TAG, "start listening");
                    startListening();
                }
            }
        });
        mActivity = this;

        checkAudioPermission();
        requestPermissions();

        if (!createModel()) {
            txtStatus.setText("voice engine initialization failed, please upload TF module files " + TFLITE_MODEL_FILENAME + " and " + SCORER_FILENAME + " to /sdcard/");
            CDELog.j(TAG, "load module failed");
            Toast.makeText(this, "load module failed", Toast.LENGTH_LONG);
            return;
        }

        txtStatus.setText("voice engine version:" + model.getVersion());
    }


    @TargetApi(23)
    private void requestPermissions() {
        String permissionInfo = " ";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.RECORD_AUDIO
                        },
                        SDK_PERMISSION_REQUEST);
            }

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SDK_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CDELog.j(TAG, "PERMISSIONS was granted");
            } else {
                CDELog.j(TAG, "PERMISSIONS was denied");
                CDEUtils.exitAPK(this);
            }
        }
    }


    protected void onDestroy() {
        super.onDestroy();
        if (this.model != null) {
            DeepSpeechModel var10000 = this.model;
            if (var10000 != null) {
                var10000.freeModel();
            }
        }

    }

}


