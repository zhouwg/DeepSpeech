/**
 *
 * Copyright (c) Project KanTV(www.cde-os.com). 2021-. All rights reserved.
 *
 */
package com.cdeos.deepspeechdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.tbruyelle.rxpermissions2.RxPermissions;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.deepspeech.libdeepspeech.CDEAssetLoader;
import org.deepspeech.libdeepspeech.CDELog;
import org.deepspeech.libdeepspeech.CDEUtils;
import org.deepspeech.libdeepspeech.DeepSpeechModel;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getName();
    DeepSpeechModel _m = null;
    EditText _tfliteModel;
    EditText _audioFile;
    EditText _appInfo;

    TextView _decodedString;
    TextView _tfliteStatus;
    public static final int RequestPermissionCode = 1;
    Button _startInference;

    final int BEAM_WIDTH = 50;

    private String modelFileName = "deepspeech-0.9.3-models.tflite";
    private String audioFileName = "audio.wav";

    private char readLEChar(RandomAccessFile f) throws IOException {
        byte b1 = f.readByte();
        byte b2 = f.readByte();
        return (char)((b2 << 8) | b1);
    }

    private int readLEInt(RandomAccessFile f) throws IOException {
        byte b1 = f.readByte();
        byte b2 = f.readByte();
        byte b3 = f.readByte();
        byte b4 = f.readByte();
        return (int)((b1 & 0xFF) | (b2 & 0xFF) << 8 | (b3 & 0xFF) << 16 | (b4 & 0xFF) << 24);
    }

    private void newModel(String tfliteModel) {
        this._tfliteStatus.setText("Creating model");
        if (this._m == null) {
            // sphinx-doc: java_ref_model_start
            this._m = new DeepSpeechModel(tfliteModel);
            this._m.setBeamWidth(BEAM_WIDTH);
            // sphinx-doc: java_ref_model_stop
        }
    }

    private void doInference(String audioFile) {
        long inferenceExecTime = 0;

        this._startInference.setEnabled(false);

        //this.newModel(this._tfliteModel.getText().toString());

        this._tfliteStatus.setText("Extracting audio features ...");

        try {
            RandomAccessFile wave = new RandomAccessFile(audioFile, "r");

            wave.seek(20); char audioFormat = this.readLEChar(wave);
            assert (audioFormat == 1); // 1 is PCM
            // tv_audioFormat.setText("audioFormat=" + (audioFormat == 1 ? "PCM" : "!PCM"));

            wave.seek(22); char numChannels = this.readLEChar(wave);
            assert (numChannels == 1); // MONO
            // tv_numChannels.setText("numChannels=" + (numChannels == 1 ? "MONO" : "!MONO"));

            wave.seek(24); int sampleRate = this.readLEInt(wave);
            assert (sampleRate == this._m.sampleRate()); // desired sample rate
            // tv_sampleRate.setText("sampleRate=" + (sampleRate == 16000 ? "16kHz" : "!16kHz"));

            wave.seek(34); char bitsPerSample = this.readLEChar(wave);
            assert (bitsPerSample == 16); // 16 bits per sample
            // tv_bitsPerSample.setText("bitsPerSample=" + (bitsPerSample == 16 ? "16-bits" : "!16-bits" ));

            wave.seek(40); int bufferSize = this.readLEInt(wave);
            assert (bufferSize > 0);
            // tv_bufferSize.setText("bufferSize=" + bufferSize);

            wave.seek(44);
            byte[] bytes = new byte[bufferSize];
            wave.readFully(bytes);

            short[] shorts = new short[bytes.length/2];
            // to turn bytes to shorts as either big endian or little endian.
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);

            this._tfliteStatus.setText("Running inference ...");

            long inferenceStartTime = System.currentTimeMillis();

            // sphinx-doc: java_ref_inference_start
            String decoded = this._m.stt(shorts, shorts.length);
            // sphinx-doc: java_ref_inference_stop

            inferenceExecTime = System.currentTimeMillis() - inferenceStartTime;

            this._decodedString.setText("ASR result:" + decoded);

        } catch (FileNotFoundException ex) {

        } catch (IOException ex) {

        } finally {

        }
        this._tfliteStatus.setText("Finished! ASR Took " + inferenceExecTime + "ms");

        this._startInference.setEnabled(true);
    }

    public void playAudioFile() {
        try {
            MediaPlayer mediaPlayer = new  MediaPlayer();
            mediaPlayer.setDataSource(this._audioFile.getText().toString());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException ex) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (this._m != null) {
            this._m.freeModel();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this._decodedString = (TextView) findViewById(R.id.decodedString);
        this._tfliteStatus = (TextView) findViewById(R.id.tfliteStatus);
        this._tfliteModel   = (EditText) findViewById(R.id.tfliteModel);
        this._audioFile     = (EditText) findViewById(R.id.audioFile);
        this._tfliteModel.setText(CDEUtils.getDataPath(this.getApplicationContext()) + modelFileName);
        this._tfliteStatus.setText("Ready, waiting ...");
        this._audioFile.setText(CDEUtils.getDataPath(this.getApplicationContext()) + audioFileName);
        this._startInference = (Button) findViewById(R.id.btnStartInference);
        this._startInference.setOnClickListener(v -> {
            //if (checkPermission())
            {
                CDELog.j(TAG, "start ASR");
                this._tfliteStatus.setText("");
                this._tfliteStatus.setText("Ready, waiting ...");
                this.playAudioFile();
                this.doInference(this._audioFile.getText().toString());
            }
            //else{
               // requestPermission();
            //}
        });

        _appInfo = ((EditText)findViewById(R.id.appinfo));

        CDEAssetLoader.copyAssetFile(this.getApplicationContext(), modelFileName, CDEUtils.getDataPath(this.getApplicationContext()) + modelFileName);
        CDEAssetLoader.copyAssetFile(this.getApplicationContext(), audioFileName, CDEUtils.getDataPath(this.getApplicationContext()) + audioFileName);

        requestPermission();

        this.newModel(this._tfliteModel.getText().toString());
        if (this._m != null) {
            _appInfo.setText("DeepSpeech version:" + this._m.getVersion());
        } else {
            CDELog.j(TAG, "deep module init failed");
            _appInfo.setText("DeepSpeech initialization failed");
        }
    }
    public boolean checkPermission() {
        //int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        //return result == PackageManager.PERMISSION_GRANTED;
        boolean result =(
                ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ||  ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ||  ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                ||  ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ||  ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
        );

        return result;
    }
    private void requestPermission() {
        //ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, RequestPermissionCode);
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            //检查是否已经有该权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    ||  ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||  ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    ||  ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    ||  ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
            ) {
                //权限没有开启，请求权限
                ActivityCompat.requestPermissions(this,
                        new String[] {
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                        },
                        RequestPermissionCode);
            } else {
                //权限已经开启

            }

        }
    }


}
