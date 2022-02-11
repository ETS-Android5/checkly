package com.shinmashita.checkly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.L;
import com.shinmashita.checkly.answers.AnswersHandler;
import com.shinmashita.checkly.checktool.Check;
import com.shinmashita.checkly.keyeditor.KeyEditor;
import com.shinmashita.checkly.keyeditor.db.KeysHandler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG=MainActivity.class.getSimpleName();
    private Button gvisionBtn;
    private Button keyEditBtn, sheetViewBtn, aboutBtn;
    private TextView strScore, strItems, strMiss, strPercent, strSheetName, strSheetViewItems;
    ImageView icon;
    private boolean checkBool;
    String extra;
    KeysHandler keysHandler;
    AnswersHandler answersHandler;
    int extrasPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //if(!getSplashValue()){
       //     storeSplashValue(true);
        //}

        gvisionBtn=(Button)findViewById(R.id.gvisionBtn);
        keyEditBtn=(Button)findViewById(R.id.keyEditBtn);
        sheetViewBtn=(Button)findViewById(R.id.main_sheetView_btn);
        aboutBtn=(Button)findViewById(R.id.main_aboutBtn);

        strScore=findViewById(R.id.main_score);
        strItems=findViewById(R.id.main_items);
        strMiss=findViewById(R.id.main_miss);
        strPercent=findViewById(R.id.main_percent);
        strSheetName=findViewById(R.id.main_keyName);
        strSheetViewItems=findViewById(R.id.main_key_items);

        icon=findViewById(R.id.main_material);

        keysHandler=new KeysHandler(this, null, null, 1);
        try{
            if(!keysHandler.getActiveKey().isEmpty()){
                strSheetName.setText(keysHandler.getActiveKey());
                strSheetViewItems.setText(Integer.toString(keysHandler.getActiveCount()));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        answersHandler=new AnswersHandler(this, null, null, 1);
        Check check=new Check();

        try {
            strScore.setText(Integer.toString(answersHandler.getAllScores().get(answersHandler.getAllScores().size()-1)));
            strItems.setText(Integer.toString(answersHandler.getAllItems().get(answersHandler.getAllItems().size()-1)));
            strMiss.setText(Integer.toString(answersHandler.getAllMiss().get(answersHandler.getAllMiss().size()-1)));
            strPercent.setText(Integer.toString(answersHandler.getAllPercent().get(answersHandler.getAllPercent().size()-1)));
            icon.setImageResource(check.getMaterialIc(answersHandler.getAllScores().get(answersHandler.getAllScores().size()-1), answersHandler.getAllItems().get(answersHandler.getAllItems().size()-1)));
        }catch (NullPointerException e){
            Log.e(TAG, "No sheets yet");
        }catch (Exception e){
            e.printStackTrace();
        }

        sheetViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_sheetViewActivity();
            }
        });

        aboutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_abtActivity();
            }
        });

        gvisionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String test=keysHandler.getActiveKey();
                    open_ocrActivity();
                } catch (Exception e)
                {
                    Toast.makeText(MainActivity.this, "Add an active answer key first.", Toast.LENGTH_SHORT).show();
                    open_keyEditorActivity();
                }

            }
        });

        gvisionBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
                View view=getLayoutInflater().inflate(R.layout.dev_access_dlg, null);

                Button continueBtn=view.findViewById(R.id.devAccess_continueBtn);
                final EditText devPass=view.findViewById(R.id.devAccess_password);

                builder.setView(view);
                final AlertDialog dlg=builder.create();
                dlg.show();

                continueBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if(devPass.getText().toString().equals("dragonfruit")){
                                open_tesseractActivity();
                            }
                            if(devPass.getText().toString().equals("siopaorice")){
                                open_gvisionActivity();
                            }
                            else {
                                dlg.dismiss();
                                Toast.makeText(MainActivity.this, "Wow, nice guess", Toast.LENGTH_SHORT).show();
                            }
                            Log.v(TAG, "Pass Input: "+devPass.getText().toString());
                        }catch (NullPointerException e){
                            dlg.dismiss();
                            Toast.makeText(MainActivity.this, "Was that an accident?", Toast.LENGTH_SHORT).show();
                        }
                    }
                });



                return true;
            }
        });


        keyEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                open_keyEditorActivity();
            }
        });


    }

    @Override
    protected void onResume() {
        keysHandler=new KeysHandler(this, null, null, 1);
        try{
            if(!keysHandler.getActiveKey().isEmpty()){
                strSheetName.setText(keysHandler.getActiveKey());
                strSheetViewItems.setText(Integer.toString(keysHandler.getActiveCount()));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        answersHandler=new AnswersHandler(this, null, null, 1);
        Check check=new Check();

        try {
            strScore.setText(Integer.toString(answersHandler.getAllScores().get(answersHandler.getAllScores().size()-1)));
            strItems.setText(Integer.toString(answersHandler.getAllItems().get(answersHandler.getAllItems().size()-1)));
            strMiss.setText(Integer.toString(answersHandler.getAllMiss().get(answersHandler.getAllMiss().size()-1)));
            strPercent.setText(Integer.toString(answersHandler.getAllPercent().get(answersHandler.getAllPercent().size()-1)));
            icon.setImageResource(check.getMaterialIc(answersHandler.getAllScores().get(answersHandler.getAllScores().size()-1), answersHandler.getAllItems().get(answersHandler.getAllItems().size()-1)));
        }catch (NullPointerException e){
            Log.e(TAG, "No sheets yet");
        }catch (Exception e){
            e.printStackTrace();
        }
        super.onResume();
    }

    private void open_gvisionActivity() {
        Intent intent= new Intent(this, GoogleVision.class);
        try {
            intent.putExtra("KeysFromMain", extra);
            intent.putExtra("PositionFromMain", extrasPosition);
            Log.v(TAG, "Extras to be sent to GVision are "+extra+" and "+extrasPosition);
        }catch (NullPointerException e){
            Log.e(TAG, "No keys to be passed to googleVision");
        }

        startActivity(intent);
    }

    private void open_ocrActivity() {
        Intent intent= new Intent(this, OcrCaptureActivity.class);
        startActivity(intent);
    }

    private void open_sheetViewActivity() {
        Intent intent= new Intent(this, SheetList.class);
        startActivity(intent);
    }

    private void open_tesseractActivity() {
        Intent intent=new Intent(this, Tesseract.class);
        startActivity(intent);
    }

    private void open_keyEditorActivity() {
        Intent intent=new Intent(this, KeyEditor.class);
        try {
            intent.putExtra("KeysFromMain", extra);
            intent.putExtra("PositionFromMain", extrasPosition);
            Log.v(TAG, "Extras from main are "+extra+" and "+extrasPosition);
        }catch (NullPointerException e){
            Log.e(TAG, "No keys to be passed to keyEditor");
        }

        startActivityForResult(intent, 1);
    }

    private void open_abtActivity(){
        Intent intent= new Intent(this, About.class);
        startActivity(intent);
    }

    private void storeSplashValue(boolean value){
        SharedPreferences preferences=getSharedPreferences("splashValue", MODE_PRIVATE);
        SharedPreferences.Editor editor=preferences.edit();
        editor.putBoolean("boolValue", value);
        editor.apply();
    }

    private boolean getSplashValue(){
        SharedPreferences preferences=getSharedPreferences("splashValue", MODE_PRIVATE);
        boolean value=preferences.getBoolean("splashValue", false);
        return value;
    }
}
