package com.example.microcamapp;

import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;

import java.io.File;
import java.io.FileOutputStream;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    //Recorder Variables
    private MediaRecorder recorder;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    private boolean flagRecording;
    private ImageView recordBtn;

    //Camera Variables
    private CameraKitView cameraKitView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flagRecording = false;
        recordBtn = findViewById(R.id.i_record);

        //camera init
        cameraKitView = findViewById(R.id.camera);
        cameraKitView.setFacing(CameraKit.FACING_FRONT);
        cameraKitView.setVisibility(View.INVISIBLE);
    }
    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.onResume();
    }

    @Override
    protected void onPause() {
        cameraKitView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.onStop();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        cameraKitView.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(!checkPermissions())//consultar los permisos para grabar audio del dispositivo
        {
            requestPermissions();//habilitar los permisos para grabar audio
        }
    }
    public void recordAudio()
    {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/InnovatiAudio.mp4");//ruta y nombre del archivo del audio grabado
        try
        {
            recorder.prepare();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        recorder.start();
        showToast("Grabando audio");
    }

    public void actionRecording(View view)
    {
        if(!flagRecording)
        {
            cameraKitView.setVisibility(View.VISIBLE);
            //comenzar a grabar audio
            flagRecording = true;
            recordBtn.setImageResource(R.drawable.recording);
            recordAudio();
        }
        else
        {
            //finalizar la grabacion audio
            flagRecording = false;
            recordBtn.setImageResource(R.drawable.norecording);
            recorder.stop();
            recorder.release();
            recorder = null;
            showToast("Fin de la grabacion");
            cameraKitView.captureImage(new CameraKitView.ImageCallback() {
                @Override
                public void onImage(CameraKitView cameraKitView, final byte[] capturedImage) {
                    File savedPhoto = new File(Environment.getExternalStorageDirectory(), "tiger.jpg");
                    try {
                        FileOutputStream outputStream = new FileOutputStream(savedPhoto.getPath());
                        outputStream.write(capturedImage);
                        outputStream.close();
                        cameraKitView.setVisibility(View.INVISIBLE);
                    } catch (java.io.IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    private void takePicture()
    {

    }
    private void requestPermissions()
    {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, REQUEST_AUDIO_PERMISSION_CODE);
    }
    public boolean checkPermissions()
    {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }
    public void showToast(final String msg)
    {
        MainActivity.this.runOnUiThread(new Runnable()
        {
            public void run()
            {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
