package com.cfbrownweb.whatgenre;

import android.media.AudioFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "cfbrownweb";

    private MediaPlayer player;
    private TextView instruction;
    private ImageButton recBtn;

    private final int RECORDING = 0;
    private final int STOPRECORDING = 1;

    private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case RECORDING:
                    instruction.setText("Listening...");
                    recBtn.setEnabled(false);
                    break;
                case STOPRECORDING:
                    instruction.setText(getString(R.string.instruction));
                    recBtn.setEnabled(true);
                    break;
                default:
                    //nothing
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        instruction = (TextView) findViewById(R.id.instruction);
        recBtn = (ImageButton) findViewById(R.id.record_btn);
    }

    public void record(View view){

        //Listen for audio
        Runnable r = new Runnable() {
            @Override
            public void run() {
                WavSoundRecorder recorder = new WavSoundRecorder(getApplicationContext().getCacheDir().getAbsolutePath());
                Log.i(TAG, "Recorder object made");
                recorder.startRecording();
                long endTime = System.currentTimeMillis() + 10000;
                handler.sendEmptyMessage(RECORDING);

                while(System.currentTimeMillis() <= endTime){}
                recorder.stopRecording();
                handler.sendEmptyMessage(STOPRECORDING);
            }
        };

        Thread thread = new Thread(r);
        thread.start();
    }

    public void playback(View view){
        //Playback recording
        String fileName = getApplicationContext().getCacheDir().getAbsolutePath() + "/recording.wav";
        player = new MediaPlayer();
        try{
            player.setDataSource(fileName);
            player.prepare();
            player.start();
        }
        catch(IOException e){
            //TODO handle exception
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                stopPlaying();
            }
        });
    }

    private void stopPlaying() {
        player.release();
        player = null;
    }

    public void anotherRecord(View view){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                ExtAudioRecorder recorder = new ExtAudioRecorder(true, MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
                recorder.setOutputFile(getApplicationContext().getCacheDir().getAbsolutePath() + "/recording.wav");
                recorder.prepare();
                recorder.start();
                long endTime = System.currentTimeMillis() + 10000;
                handler.sendEmptyMessage(RECORDING);

                while(System.currentTimeMillis() <= endTime){}
                recorder.stop();
                handler.sendEmptyMessage(STOPRECORDING);
            }
        };

        Thread thread = new Thread(r);
        thread.start();
    }

    public void yetAnotherRecord(View view){
        Runnable r = new Runnable() {
            @Override
            public void run() {
                MediaRecorder recorder = new MediaRecorder();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFile(getApplicationContext().getCacheDir().getAbsolutePath() + "/recording.amr");
                recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                try{
                    recorder.prepare();
                }
                catch (IOException e){
                    //TODO handle exception
                }

                recorder.start();
                long endTime = System.currentTimeMillis() + 10000;
                handler.sendEmptyMessage(RECORDING);

                while(System.currentTimeMillis() <= endTime){}
                recorder.stop();
                handler.sendEmptyMessage(STOPRECORDING);
            }
        };

        Thread thread = new Thread(r);
        thread.start();
    }
}
