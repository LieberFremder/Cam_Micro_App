package com.example.microcamapp;

import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.camerakit.CameraKit;
import com.camerakit.CameraKitView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Locale;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    //Recorder Variables
    private MediaRecorder recorder;
    public static final int REQUEST_AUDIO_PERMISSION_CODE = 1;
    private boolean flagRecording;
    private ImageView recordBtn;
    private TextView audioText;

    //TextToSpeech Variables
    private TextToSpeech speechMachine;

    ////Handler Variables
    private Handler mainHandler = new Handler();

    //Camera Variables
    private CameraKitView cameraKitView;
    private boolean flagTakePhoto;
    private int photoCounter;
    Thread photos_thread = new Thread() {
        @Override
        public void run()
        {
            photoCounter = 0;
            while(speechMachine.isSpeaking())
            {
                //wait
            };
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    //mostrar view de camara
                    cameraKitView.setVisibility(View.VISIBLE);
                }
            },400);
            while(photoCounter < 4)//aqui se define el numero de fotos a tomar
            {
                if(flagTakePhoto)
                {
                    photoCounter++;
                    flagTakePhoto = false;
                    //call runnable/method
                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run()
                        {
                            takePhoto();
                        }
                    },500);
                }
            }
            cameraKitView.setVisibility(View.INVISIBLE);
            mainHandler.post(new Runnable() {
                @Override
                public void run()
                {
                    //avisar que ya se realizo el registro de la cara
                    speechMachine.speak("El registro fue realizado exitosamente, gracias", TextToSpeech.QUEUE_ADD, null, null);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialization
        flagRecording = false;
        flagTakePhoto = true;
        recordBtn = findViewById(R.id.i_record);
        audioText = findViewById(R.id.t_audio);

        //camera initialization
        cameraKitView = findViewById(R.id.camera);
        cameraKitView.setFacing(CameraKit.FACING_FRONT);
        cameraKitView.setVisibility(View.INVISIBLE);

        //TextToSpeech initialization
        speechMachine = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status)
            {
                Locale locSpanish = new Locale("spa", "COL");
                speechMachine.setLanguage(locSpanish);
            }
        });
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
        audioText.setText(R.string.text_recording);
    }

    public void actionRecording(View view)
    {
        if(!flagRecording)
        {
            //comenzar a grabar audio
            flagRecording = true;
            recordBtn.setImageResource(R.drawable.recording);
            recordAudio();
        }
        else
        {
            //finalizar la grabacion audio
            flagRecording = false;
            audioText.setVisibility(View.INVISIBLE);
            recordBtn.setImageResource(R.drawable.norecording);
            recordBtn.setEnabled(false);
            recorder.stop();
            recorder.release();
            recorder = null;
            showToast("Fin de la grabacion");
            audioText.setVisibility(View.INVISIBLE);
            speechMachine.speak("El audio ha sido almacenado, ahora se tomara registro de su cara, mire a la camara por favor", TextToSpeech.QUEUE_ADD, null, null);
            //start taking photos
            photos_thread.start();
        }
    }
    private void takePhoto()
    {
        cameraKitView.captureImage(new CameraKitView.ImageCallback() {
            //this callback is async
            @Override
            public void onImage(CameraKitView cameraKitView, final byte[] capturedImage) {
                File savedPhoto = new File(Environment.getExternalStorageDirectory(), "registro" + String.valueOf(photoCounter) + ".jpg");
                try {
                    FileOutputStream outputStream = new FileOutputStream(savedPhoto.getPath());
                    outputStream.write(capturedImage);
                    outputStream.close();
                    flagTakePhoto = true;
                } catch (java.io.IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
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
    public void exitApp(View view)
    {
        flagTakePhoto = false;
        if(photos_thread.isAlive())
        {
            //terminar el hilo
            photos_thread.interrupt();
        }
        finish();
    }
}
