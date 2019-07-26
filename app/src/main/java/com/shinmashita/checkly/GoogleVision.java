package com.shinmashita.checkly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.shinmashita.checkly.checktool.Check;
import com.shinmashita.checkly.keyeditor.db.KeysHandler;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GoogleVision extends AppCompatActivity {

    private static final String TAG= GoogleVision.class.getSimpleName();

    Uri image_uri;
    ImageView vision_imageView;


    //Implement ViewPager here
    TextView score_textView;
    TextView mistakes_textView;
    TextView percentage_textView;
    ImageView rating_imageView;

    private RequestPermissionsTool requestTool;

    private static final String DATA_PATH= Environment.getExternalStorageDirectory().toString() + "/mobileVision/";
    private static final String VISION_DATA= "vision_data";

    private static final int GALLERY_CODE=1;
    private static final int CAMERA_CODE=2;

    String table;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_vision);

        vision_imageView=findViewById(R.id.vision_imageView);

        score_textView=findViewById(R.id.score_textView);
        mistakes_textView=findViewById(R.id.mistakes_textView);
        percentage_textView=findViewById(R.id.percentage_textView);
        rating_imageView=findViewById(R.id.rating_imageView);

        ModeDialog();

        KeysHandler handler=new KeysHandler(this, null, null, 1);
        table=handler.getActiveKey();
        Log.v(TAG, "Extra gathered from main is "+table);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions();
        }
    }

    private void pickGallery() {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_CODE);
    }

    private void pickCamera(){
        try {
            String IMGS_PATH = Environment.getExternalStorageDirectory().toString() + "/TesseractSample/imgs";
            prepareDirectory(IMGS_PATH);

            String img_path = IMGS_PATH + "/ocr.jpg";

            image_uri = Uri.fromFile(new File(img_path));

            final Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, CAMERA_CODE);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

    }

    private void pickCameraSource(){
        Intent intent=new Intent(this, OcrCaptureActivity.class);
        intent.putExtra("tableFromGVision", table);
        startActivityForResult(intent, 3);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_CODE) {
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
            if(requestCode == CAMERA_CODE){
                CropImage.activity(image_uri).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult cropResult = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri UResult = cropResult.getUri();
                vision_imageView.setImageURI(UResult);

                BitmapDrawable drawable=(BitmapDrawable)vision_imageView.getDrawable();
                Bitmap bitmap=drawable.getBitmap();

                doOCR(bitmap);
            }
            else if (resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception e= cropResult.getError();
                Toast.makeText(this, ""+e, Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode==3&&resultCode==RESULT_OK){
            Check check=new Check();
            try {
                int score = getIntent().getIntExtra("score",0);
                int items = getIntent().getIntExtra("items",0);
                Log.v(TAG, "Extras gathered are scores: "+score+" and items: "+items);

                check.getRating(score, items, rating_imageView);
                score_textView.setText(Integer.toString(score));
                mistakes_textView.setText(Integer.toString(check.getMisses(score, items)));
                percentage_textView.setText(Integer.toString(check.getPercentage(score, items)));

            }catch (NullPointerException e){
                Log.e(TAG, "No scores or items are passed in the intent.");
            }
        }
    }

    private void prepareDirectory(String path){
        File dir= new File(path);
        if(!dir.exists()&&!dir.mkdirs()){
            Log.e(TAG, "ERROR: "+ DATA_PATH+VISION_DATA+" is not created");
        }
        else{
            Log.i(TAG, "Directory created");
        }
    }

    private void ModeDialog(){
        String[] items={" Capture Image", " Import from Gallery", " Hover through Image"};
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Select Image");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    pickCamera();
                }
                if(which==1){
                    pickGallery();
                }
                if(which==2){
                    pickCameraSource();
                }
            }
        });
        builder.create().show();
    }

    private void doOCR(Bitmap bitmap){

        TextRecognizer recognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        Check check=new Check();

        if (!recognizer.isOperational()) {
            Toast.makeText(this, "Open WiFi to install detector dependencies.", Toast.LENGTH_LONG).show();
        }
        else {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> sparseArray = recognizer.detect(frame);
            ArrayList<String> ans = new ArrayList<String>();


            for (int i = 0; i < sparseArray.size(); i++) {
                TextBlock block= sparseArray.valueAt(i);
                List<? extends Text> lines=block.getComponents();

                for(int j= 0; j< lines.size(); j++){
                    List<? extends Text> elements = lines.get(j).getComponents();

                    for(int k=0; k<elements.size(); k++){
                        if(check.checkifABCD(elements.get(k).getValue())&&elements.get(k).getValue()!=null){
                            //ans[j][k] = elements.get(k).getValue();
                            ans.add(elements.get(k).getValue());

                        }
                    }
                }
            }
            int score=check.getScore(ans, this, table);
            int items=check.getItems(ans);

            check.getRating(score, items, rating_imageView);
            score_textView.setText(Integer.toString(score));
            mistakes_textView.setText(Integer.toString(check.getMisses(score, items)));
            percentage_textView.setText(Integer.toString(check.getPercentage(score, items)));

        }
    }

    public void backClick(View view){
        finish();
    }

    private void requestPermissions() {
        String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestTool = new RequestPermissionsToolImpl();
        requestTool.requestPermissions(this, permissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean grantedAllPermissions = true;
        for (int grantResult : grantResults) {
            if (grantResult != PackageManager.PERMISSION_GRANTED) {
                grantedAllPermissions = false;
            }
        }

        if (grantResults.length != permissions.length || (!grantedAllPermissions)) {

            requestTool.onPermissionDenied();
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }


    }
}
