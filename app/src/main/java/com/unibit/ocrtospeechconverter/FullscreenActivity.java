package com.unibit.ocrtospeechconverter;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.Locale;

import static android.Manifest.permission.CAMERA;
import static android.os.SystemClock.sleep;

public class FullscreenActivity extends AppCompatActivity {

    private SurfaceView surfaceView;

    private CameraSource cameraSource;

    private TextToSpeech textToSpeech;

    private String stringResult = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        ActivityCompat.requestPermissions(this, new String[]{CAMERA}, PackageManager.PERMISSION_GRANTED);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                textToSpeech.setLanguage(Locale.US);

            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSource.release();
    }

    private void textRecognizer() {

        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setRequestedPreviewSize(800, 1024).build();

        surfaceView = findViewById(R.id.surfaceView);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(surfaceView.getHolder());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();

            }
        });

        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {

                SparseArray<TextBlock> sparseArray = detections.getDetectedItems();
                StringBuilder stringBuilder = new StringBuilder();

                sleep(2000);

                for (int i = 0; i < sparseArray.size(); ++i) {
                    TextBlock textBlock = sparseArray.valueAt(i);

                    if (textBlock != null && textBlock.getValue() != null) {
                        stringBuilder.append(textBlock.getValue()).append(" ");
                    }
                }

                final String stringText = stringBuilder.toString();

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        stringResult = stringText;
                        resultObtained();
                    }
                });

            }
        });

    }

    private void resultObtained() {
        setContentView(R.layout.activity_fullscreen);
        TextView textView = findViewById(R.id.fullscreen_content);
        textView.setText(stringResult);
        textToSpeech.speak(stringResult, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    public void buttonStart(View view) {

        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }

        setContentView(R.layout.surfaceview);
        textRecognizer();
    }
}
