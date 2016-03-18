/*Author: Chris Brown
* Date: 16/03/2016
* Description: Launcher activity class. Allows the user to record audio
* and handles posting of file to server*/
package com.cfbrownweb.whatgenre;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "cfbrownweb";

    private MediaPlayer player;
    private static TextView instruction;
    private static ImageButton recBtn;
    private long recordLength = 10000;
    private static String genre = null;
    private static String rec_prompt = null;
    private static String calc_prompt = null;

    private final static int RECORDING = 0;
    private final static int STOPRECORDING = 1;
    private final static int STOPCALCULATING = 2;

    private static int serverResponseCode = 0;

    private final String upLoadServerUri = "http://cfbrownweb.ngrok.io/whatgenre/uploadaudio.php";

    /**********  File Path *************/
    private String uploadFilePath = null;
    private String uploadFileName = null;

    static class RecordHandler extends Handler {
        private String origText = instruction.getText().toString();

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case RECORDING:
                    instruction.setText(rec_prompt);
                    recBtn.setEnabled(false);
                    break;
                case STOPRECORDING:
                    instruction.setText(calc_prompt);
                    break;
                case STOPCALCULATING:
                    instruction.setText(origText);
                    recBtn.setEnabled(true);
                    break;
                default:
                    //nothing
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        instruction = (TextView) findViewById(R.id.instruction);
        recBtn = (ImageButton) findViewById(R.id.record_btn);

        uploadFilePath = getApplicationContext().getCacheDir().getAbsolutePath() + "/";
        uploadFileName = "recording.wav";

        rec_prompt = getString(R.string.listening_prompt);
        calc_prompt = getString(R.string.calculating_prompt);
    }

    public void record(View view){

        final RecordHandler recordHandler = new RecordHandler();

        //Listen for audio
        Runnable r = new Runnable() {
            @Override
            public void run() {
                WavSoundRecorder recorder = new WavSoundRecorder(getApplicationContext().getCacheDir().getAbsolutePath());
                Log.i(TAG, "Recorder object made");
                recorder.startRecording();
                long endTime = System.currentTimeMillis() + recordLength;
                recordHandler.sendEmptyMessage(RECORDING);

                while(System.currentTimeMillis() <= endTime){}
                recorder.stopRecording();
                recordHandler.sendEmptyMessage(STOPRECORDING);
                uploadFile(uploadFilePath + uploadFileName, recordHandler);
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

    private int uploadFile(String sourceFileUri, Handler handler) {

        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024; //TODO set to audio file size
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {
            Log.e("uploadFile", "Source File not exist :"
                    + uploadFilePath + "" + uploadFileName);

            return 0;
        }
        else {
            try {
                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=" + fileName + lineEnd);
                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

//                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bufferSize = bytesAvailable;
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
//                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bufferSize = bytesAvailable;
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){
                    //TODO Success, give the user the response genre
                    InputStream in = conn.getInputStream();
                    String encoding = conn.getContentEncoding();
                    encoding = encoding == null ? "UTF-8" : encoding;

                    genre = IOUtils.toString(in, encoding);
                    in.close();
                    Intent genrePageIntent = new Intent(MainActivity.this, GenreActivity.class);
                    genrePageIntent.putExtra("genre", genre);
                    startActivity(genrePageIntent);
                    handler.sendEmptyMessage(STOPCALCULATING);
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Upload server exception", "Exception : " + e.getMessage(), e);
            }
            return serverResponseCode;
        } // End else block
    }
}
