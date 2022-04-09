package com.example.virtualassistant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Locale;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    private SpeechRecognizer speechRecognizer;
    public TextToSpeech textToSpeech;
    private TextView textView;
    private Intent intent;
    private String string = "";
    private ImageView micButton, capturedImage;
    private int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE=1;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            checkPermission();
        }
        textView = findViewById(R.id.textView);
        micButton=findViewById(R.id.button);
        capturedImage=findViewById(R.id.picture);
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
            }
        });

        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                textView.setText("Please speak your command");
            }
            @Override
            public void onBeginningOfSpeech() {
            }
            @Override
            public void onRmsChanged(float rmsdB) {
            }
            @Override
            public void onBufferReceived(byte[] buffer) {
            }
            @Override
            public void onEndOfSpeech() {
                textView.setText("Processing...");
            }
            @Override
            public void onError(int error) {
            }
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
                //textView.setText(matches.get(0));
                if (matches != null){
                    string=matches.get(0);
                    string=string.toLowerCase();
                    textView.setText(string);

                    if (string.indexOf("timer")>0){
                            createTimer(string, textToSpeech);
                        }
                    else if(string.indexOf("picture")>0){
                        openCamera(textToSpeech);
                    }
                    else if(string.indexOf("search")>-1){
                        googleSearch(string, textToSpeech);
                    }
                    else if(string.indexOf("create")>-1){
                        createMethod(textToSpeech);
                    }
                    else{
                        textToSpeech.speak("I did not understand the command", TextToSpeech.QUEUE_FLUSH, null, null);
                    }

                    }
                }

            @Override
            public void onPartialResults(Bundle partialResults) {
            }
            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });

        micButton.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                    micButton.setBackground(getDrawable(R.drawable.button_states));
                    speechRecognizer.stopListening();
                }
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    capturedImage.setImageResource(0);
                    textView.setText("");
                    string="";
                    textToSpeech.speak("Hello, How can I help you?", TextToSpeech.QUEUE_FLUSH, null, null);
                    micButton.setBackground(getDrawable(R.drawable.button_state_not_pressed));
                    speechRecognizer.startListening(intent);
                }
                return true;
            }
        });
    }


    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,new String[]{RECORD_AUDIO, READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},1);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this,"Permission Granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        speechRecognizer.destroy();
    }

    private void createTimer(String value, TextToSpeech textToSpeech) {

        String[] input = value.split(" ");
        int minutes;
        final int[] counter = new int[1];
        minutes =Integer.parseInt(input[input.length -2]);
        counter[0] =minutes;
        textToSpeech.speak("Timer starting now.", TextToSpeech.QUEUE_FLUSH, null, null);
        new CountDownTimer(minutes*1000, 1000){
            public void onTick(long millisUntilFinished){
                textView.setText(String.valueOf(counter[0]));
                counter[0]--;
            }
            public  void onFinish(){
                textView.setText("FINISH!!");
            }
        }.start();
    }


    private void openCamera(TextToSpeech textToSpeech){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        textToSpeech.speak("Opening Camera", TextToSpeech.QUEUE_FLUSH, null, null);
        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            ImageView imageview = (ImageView) findViewById(R.id.picture); //sets imageview as the bitmap
            imageview.setImageBitmap(image);
        }
    }


    private void googleSearch(String string, TextToSpeech textToSpeech){
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        String term = string.substring(7);
        String speech= "This is what I found for "+term;
        textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null, null);
        intent.putExtra(SearchManager.QUERY, term);
        startActivity(intent);
    }

    private void createMethod(TextToSpeech textToSpeech){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        textToSpeech.speak("What do I write in the file?", TextToSpeech.QUEUE_FLUSH, null, null);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "filetest.txt");
        try {
            if (!file.exists()){
                file.createNewFile();
            }

            FileWriter fileWriter = new FileWriter(file);
            fileWriter.append("My 1st voice assistance App");
            String path = file.getPath();
            Log.e("FILE PATH", path);
            fileWriter.flush();
            fileWriter.close();
        }
        catch (Exception e){
            e.printStackTrace();
            return;
        }
        textToSpeech.speak("The file has been created", TextToSpeech.QUEUE_FLUSH, null, null);
    }
}