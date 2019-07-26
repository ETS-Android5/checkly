package com.shinmashita.checkly;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.shinmashita.checkly.adapters.CaptureAdapter;
import com.shinmashita.checkly.adapters.CaptureResAdapter;
import com.shinmashita.checkly.adapters.RawSheet;
import com.shinmashita.checkly.adapters.Sheet;
import com.shinmashita.checkly.answers.AnswersHandler;
import com.shinmashita.checkly.answers.answers;
import com.shinmashita.checkly.checktool.Check;
import com.shinmashita.checkly.keyeditor.db.KeysHandler;
import com.shinmashita.checkly.settings.PreferencesHandler;
import com.shinmashita.checkly.ui.camera.CameraSource;
import com.shinmashita.checkly.ui.camera.CameraSourcePreview;
import com.shinmashita.checkly.ui.camera.GraphicOverlay;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


public final class OcrCaptureActivity extends AppCompatActivity {
    private static final String TAG = "OcrCaptureActivity";

    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String TextBlockObject = "String";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    // Helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    private String table;
    private View bottomSheet;
    private BottomSheetBehavior mBottomSheetBehavior;
    private Animation fromBottom, toBottom;
    private TextView score_text, items_text, score_summary, items_summary, misses_summary, percentage_summary;
    private ImageView OcrHoldBtn, RateMaterialIc;
    private AnswersHandler mAnswersHandler;
    private KeysHandler keysHandler;
    private int score, items, misses, percentage;
    private ArrayList<String> charAns, charKeys;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.ocr_capture);


        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);


        boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, false);
        boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);

        keysHandler=new KeysHandler(OcrCaptureActivity.this, null, null, 1);
        table=keysHandler.getActiveKey();
        Log.v(TAG, "Extra gathered from Google Vision is "+table);

        bottomSheet=findViewById(R.id.ocr_capture_bottomSheet);
        mBottomSheetBehavior=BottomSheetBehavior.from(bottomSheet);

        score_text=findViewById(R.id.ocr_capture_score);
        items_text=findViewById(R.id.ocr_capture_items);
        OcrHoldBtn=findViewById(R.id.ocr_capture_btn);
        RateMaterialIc=findViewById(R.id.ocr_capture_materialIc);
        score_summary=findViewById(R.id.capture_summary_scoreView);
        items_summary=findViewById(R.id.capture_summary_itemView);
        misses_summary=findViewById(R.id.capture_summary_missView);
        percentage_summary=findViewById(R.id.capture_summary_percentView);


        fromBottom= AnimationUtils.loadAnimation(this, R.anim.slide_from_bottom);
        toBottom=AnimationUtils.loadAnimation(this, R.anim.slide_to_bottom);

        OcrHoldBtn.setAnimation(fromBottom);

        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                switch (i){
                    case  BottomSheetBehavior.STATE_HIDDEN:

                        OcrHoldBtn.setVisibility(View.VISIBLE);
                        OcrHoldBtn.setAnimation(fromBottom);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                OcrHoldBtn.setAnimation(toBottom);
            }
        });


        //Permission
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

    }


    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }

    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor(mGraphicOverlay));

        OcrGraphic graphic;

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet available.");

            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        mCameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(2.0f)
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null)
                .build();


    }


    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");

            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus,false);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }


    private void startCameraSource() throws SecurityException {
        // Check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }


    private boolean onTap(float rawX, float rawY) {
        OcrGraphic graphic = mGraphicOverlay.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;
        Check check= new Check();
        keysHandler=new KeysHandler(OcrCaptureActivity.this, null, null, 1);

        if (graphic != null) {
            text = graphic.getTextBlock();
            if (text != null && text.getValue() != null && check.onBlockCheckifABCD(text)) {
                score=check.getScore(check.getArrayElementsFromBlock(text), OcrCaptureActivity.this, table);
                items=check.getItems(check.getArrayElementsFromBlock(text));
                misses=check.getMisses(score, items);
                percentage=check.getPercentage(score, items);
                charAns=check.getArrayElementsFromBlock(text);
                charKeys=keysHandler.databaseToArray(table);

                score_text.setText(Integer.toString(score));
                items_text.setText(Integer.toString(items));

                RateMaterialIc.setImageResource(check.getMaterialIc(score, items));

                score_summary.setText(Integer.toString(score));
                items_summary.setText(Integer.toString(items));
                misses_summary.setText(Integer.toString(misses));
                percentage_summary.setText(Integer.toString(percentage));

                /**try {
                    Intent data= new Intent();
                    data.putExtra("score", score);
                    data.putExtra("items", items);
                    setResult(RESULT_OK, data);
                    finish();
                    Log.v(TAG, "Extras to be sent are "+score+" and "+items);
                }catch (NullPointerException e){
                    Log.e(TAG, "No score or items intents detected");
                }**/
                }
            else {
                Log.d(TAG, "text data is null");
            }
        }
        else {
            Log.d(TAG,"no text detected");
        }

        return text != null;
    }

    public void OcrHoldBtnClick(View view){
        PreferencesHandler preference=new PreferencesHandler(this, null, null, 1);
        Log.v(TAG, "Count: "+preference.getTargetCount());

        Rect rect=locateView(this.findViewById(R.id.ocr_crossHair));
        float hairX=rect.left+((rect.right-rect.left)/2);
        float hairY=rect.bottom;
        onTap(hairX, hairY);

        OcrHoldBtn.setAnimation(toBottom);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        OcrHoldBtn.setVisibility(View.INVISIBLE);

        Log.v(TAG, "onTap is called at x= "+ hairX+ " and y= "+ hairY);
    }

    public Rect locateView(View v){
        Rect rect=new Rect();
        int []location=new int[2];
        if(v==null){
            return rect;
        }
        v.getLocationOnScreen(location);

        rect.left=location[0];
        rect.top=location[1];
        rect.right=rect.left+v.getWidth();
        rect.bottom=rect.top+v.getHeight();

        return rect;
    }

    public int getKeyCount(){
        PreferencesHandler preferences=new PreferencesHandler(this, null, null, 1);
        return preferences.getTargetCount();
    }

    public void nameDlg(View view){
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        View v=getLayoutInflater().inflate(R.layout.capture_name_dlg, null);

        final EditText name=v.findViewById(R.id.capture_dlg_name);
        Button saveBtn=v.findViewById(R.id.capture_dlg_saveBtn);

        builder.setView(v);
        final AlertDialog dlg=builder.create();
        dlg.show();

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAnswersHandler=new AnswersHandler(OcrCaptureActivity.this, null, null, 1);

                if(!name.getText().toString().isEmpty()) {
                    try {
                        mAnswersHandler.generateSheet(name.getText().toString(), score, items, misses, percentage, table);
                        Log.v(TAG, "Generated new sheet with name: "+name.getText().toString()+" score: "+score+" items: "+items+" misses: "+misses+ " percentage: "+percentage);

                        for(int i=0; i<charAns.size(); i++){
                            answers answer=new answers(charAns.get(i));
                            mAnswersHandler.addAnswer(answer, name.getText().toString());
                            Log.v(TAG, "Added answer "+answer.get_answer_value()+" to sheet "+name.getText().toString());
                        }

                        for(int i=0; i<charKeys.size(); i++){
                            answers answer=new answers(charKeys.get(i));
                            mAnswersHandler.addSavedKey(answer, name.getText().toString());
                            Log.v(TAG, "Added key "+answer.get_answer_value()+" to sheet "+name.getText().toString()+"_key");
                        }


                        mAnswersHandler.recordScore(score);
                        mAnswersHandler.recordItems(items);
                        mAnswersHandler.recordMiss(misses);
                        mAnswersHandler.recordPercent(percentage);

                        dlg.dismiss();
                    }catch (NullPointerException e){
                        Log.e(TAG, "No scores, items, misses, or percentage saved into answers db");
                    }catch (SQLiteException e){
                        Toast.makeText(OcrCaptureActivity.this, "That name already exists. Try a new one", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "SQLiteException at generating new sheet");
                    }
                }
                else{
                    Toast.makeText(OcrCaptureActivity.this, "Add a name for your sheet", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onClickSheetList(View view){
        Intent intent= new Intent(OcrCaptureActivity.this, SheetList.class);
        startActivity(intent);
    }


    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            //change bottom sheet behavior to collapse only when results are detected
            OcrHoldBtn.setAnimation(toBottom);
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            OcrHoldBtn.setVisibility(View.INVISIBLE);
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }



    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mCameraSource.doZoom(detector.getScaleFactor());
        }
    }
}
