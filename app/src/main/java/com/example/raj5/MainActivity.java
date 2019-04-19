package com.example.raj5;

import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.File;

import ai.picovoice.porcupinemanager.KeywordCallback;
import ai.picovoice.porcupinemanager.PorcupineManager;
import ai.picovoice.porcupinemanager.PorcupineManagerException;

public class MainActivity extends AppCompatActivity {
  private PorcupineManager porcupineManager = null;
  private MediaPlayer notificationPlayer;
  private RelativeLayout layout;
  private ToggleButton recordButton;
  TextView tv_raj;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    if (Utils.hasRecordPermission(this)) {
      try {
        porcupineManager = initPorcupine();
      } catch (PorcupineManagerException e) {
        e.printStackTrace();
      }
      try {
        porcupineManager.start();
      } catch (PorcupineManagerException e) {
        e.printStackTrace();
      }

    } else {
      Utils.showRecordPermission(this);
      Toast.makeText(this, "Please give permission for voice command.",
              Toast.LENGTH_LONG).show();
    }


    tv_raj=(TextView)findViewById(R.id.my_text);

    Utils.configurePorcupine(this);
    notificationPlayer = MediaPlayer.create(this, R.raw.notification);
    layout = findViewById(R.id.layout);
    recordButton = findViewById(R.id.record_button);

    // Make the footer linkable.
    // create the keyword spinner.
    //configureKeywordSpinner();







//
//    try {
//      porcupineManager = initPorcupine();
//      porcupineManager.start();
//    }catch (Exception e){}



  }


  /**
   * Handler for the record button. Processes the audio and uses Porcupine library to detect the
   * keyword. It increments a counter to indicate the occurrence of a keyword.
   * @param view ToggleButton used for recording audio.
   */
  public void process(View view) {
    try {
      if (recordButton.isChecked()) {
        // check if record permission was given.
        if (Utils.hasRecordPermission(this)) {
          porcupineManager = initPorcupine();
          porcupineManager.start();

        } else {
          Utils.showRecordPermission(this);
        }
      } else {
        porcupineManager.stop();
      }
    } catch (PorcupineManagerException e) {
      Utils.showErrorToast(this);
    }
  }

  /**
   * Initialize the porcupineManager library.
   * @return Porcupine instance.
   */
  private PorcupineManager initPorcupine() throws PorcupineManagerException {
    Log.e("PorcupineManager","yes");
    Spinner mySpinner= findViewById(R.id.keyword_spinner);
    String kwd = "Pineapple";
    // It is assumed that the file name is all lower-case and spaces are replaced with "_".
    String filename = kwd.toLowerCase().replaceAll("\\s+", "_");
    // get the keyword file and model parameter file from internal storage.
    String keywordFilePath = new File(this.getFilesDir(), filename + ".ppn")
            .getAbsolutePath();
    String modelFilePath = new File(this.getFilesDir(), "params.pv").getAbsolutePath();
    final int detectedBackgroundColor = getResources()
            .getColor(R.color.colorAccent);
    return new PorcupineManager(modelFilePath, keywordFilePath, 0.5f, new KeywordCallback() {
      @Override
      public void run(int keyword_index) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            if (!notificationPlayer.isPlaying()) {
              notificationPlayer.start();
            }
            // change the background color for 1 second.
            layout.setBackgroundColor(detectedBackgroundColor);
            new CountDownTimer(1000, 100) {

              @Override
              public void onTick(long millisUntilFinished) {
                if (!notificationPlayer.isPlaying()) {
                  notificationPlayer.start();

                }
              }

              @Override
              public void onFinish() {
                layout.setBackgroundColor(Color.TRANSPARENT);
              }
            }.start();
          }
        });
      }
    });
  }

  /**
   * Check the result of the record permission request.
   * @param requestCode request code of the permission request.
   * @param permissions requested permissions.
   * @param grantResults results of the permission requests.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode,
                                         @NonNull String permissions[],
                                         @NonNull int[] grantResults) {
    // We only ask for record permission.
    if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
      ToggleButton tbtn = findViewById(R.id.record_button);
      tbtn.toggle();
    } else {
      try {
        porcupineManager = initPorcupine();
        porcupineManager.start();
      } catch (PorcupineManagerException e) {
        Utils.showErrorToast(this);
      }
    }
  }

  /**
   * Configure the style and behaviour of the keyword spinner.
   */
  private void configureKeywordSpinner(){
    Spinner spinner = findViewById(R.id.keyword_spinner);

    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
            this, R.array.keywords, R.layout.keyword_spinner_item);
    adapter.setDropDownViewResource(R.layout.keyword_spinner_item);
    spinner.setAdapter(adapter);


    // Make sure user stopped recording before changing the keyword.
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                 int position, long id) {
        // if (recordButton.isChecked()) {
        // if (porcupineManager != null) {
        Log.e("spinner selected:", "111");

        try {
          // check if record permission was given.
          //if (Utils.hasRecordPermission(this)) {
          Log.e("spinner selected:", "yes");
          porcupineManager = initPorcupine();
          porcupineManager.start();

              /*} else {
                Utils.showRecordPermission(this);
              }*/
          //porcupineManager.stop();
        } catch (PorcupineManagerException e) {
          Log.e("spinner selected:", "yes");

          Utils.showErrorToast(getApplicationContext());
        }
        //}
        recordButton.toggle();
        // }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parentView) {
        // Do nothing.
        Log.e("spinner selected:", "yes");

      }
    });
  }
}
