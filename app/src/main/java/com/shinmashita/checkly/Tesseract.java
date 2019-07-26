package com.shinmashita.checkly;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.leptonica.android.WriteFile;
import com.googlecode.tesseract.android.TessBaseAPI;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;

public class Tesseract extends AppCompatActivity {

    private static final String TAG=Tesseract.class.getSimpleName();

    TextView score;
    ImageView imageView;
    Uri image_uri;


    private TessBaseAPI tessBaseAPI;
    private static final String lang = "eng";
    private static final String DATA_PATH= Environment.getExternalStorageDirectory().toString() + "/Tesseract/";
    private static final String TESSDATA= "tessdata";
    private RequestPermissionsTool requestTool;
    String result="No results";

    private static final int GALLERY_CODE=1;
    private static final int CAMERA_CODE=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tesseract);

        score=findViewById(R.id.score);
        imageView=findViewById(R.id.imageView);

        ModeDialog();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_CODE) {
                CropImage.activity(data.getData()).setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
            if(requestCode == CAMERA_CODE){
                prepareTesseract();
                startOCR(image_uri);
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult cropResult = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri UResult = cropResult.getUri();
                prepareTesseract();
                startOCR(UResult);
            }
            else if (resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                Exception e= cropResult.getError();
                Toast.makeText(this, ""+e, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void prepareDirectory(String path){
        File dir= new File(path);
        if(!dir.exists()&&!dir.mkdirs()){
            Log.e(TAG, "ERROR: "+ DATA_PATH+TESSDATA+" is not created");
        }
        else{
            Log.i(TAG, "Directory created");
        }
    }

    private void prepareTesseract(){
        try{
            prepareDirectory(DATA_PATH+TESSDATA);
        }catch (Exception e){
            e.printStackTrace();
        }
        copyTessData(TESSDATA);
    }

    private void copyTessData(String path) {
        try{
            String fileList[]=getAssets().list(path);
            for(String fileName:fileList){
                String pathToDataFile= DATA_PATH + path + "/"+fileName;
                if(!(new File(pathToDataFile)).exists()){
                    InputStream in= getAssets().open(path+"/"+fileName);
                    OutputStream out = new FileOutputStream(pathToDataFile);

                    byte[] buf=new byte[1024];
                    int len;

                    while((len=in.read(buf))>0){
                        out.write(buf,0,len);
                    }
                    in.close();
                    out.close();

                    Log.d(TAG, "Copied "+fileName+" to tessdata");
                }
            }
        }catch (IOException e){
            Log.e(TAG, "Unable to copy file to tessdata "+ e.toString());
        }
    }

    private void startOCR(Uri image_uri){
        try{
            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize=4;
            Bitmap bitmap=BitmapFactory.decodeFile(image_uri.getPath(), options);

            try {
                ExifInterface exif = new ExifInterface(image_uri.getPath());
                int exifOrientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL);

                Log.v(TAG, "Orient: " + exifOrientation);

                int rotate = 0;

                switch (exifOrientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotate = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotate = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotate = 270;
                        break;
                }

                Log.v(TAG, "Rotation: " + rotate);

                if (rotate != 0) {

                    // Getting width & height of the given image.
                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();

                    // Setting pre rotate
                    Matrix mtx = new Matrix();
                    mtx.postRotate(rotate);

                    // Rotating Bitmap
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
                }
            }catch (Exception e){
                Log.e(TAG, "Exception on rotation: "+e.getMessage());
            }

            result=extractText(bitmap);
            score.setText(result);
        }catch (Exception e){
            Log.e(TAG, "startOCR: "+e.getMessage());
        }
    }

    private String extractText(Bitmap bitmap) {
        try{
            tessBaseAPI=new TessBaseAPI();
        }
        catch (Exception e){
            Log.e(TAG, "extractText: " + e.getMessage());
            if(tessBaseAPI==null){
                Log.e(TAG, "TessBaseAPI is null");
            }
        }

        tessBaseAPI.init("/storage/emulated/0/Tesseract", lang);
        //tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);
        //tessBaseAPI.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "AaBbCcDd ");
        Log.d(TAG, "Training file loaded");

        tessBaseAPI.setImage(bitmap);
        Bitmap thresholdedImg= WriteFile.writeBitmap(tessBaseAPI.getThresholdedImage());
        imageView.setImageBitmap(thresholdedImg);

        String resText="No text yet";
        try{
            resText=tessBaseAPI.getUTF8Text();
        }catch (Exception e){
            Log.e(TAG, "Text not recognized");
        }

        Log.v(TAG, "Average confidence: "+ tessBaseAPI.meanConfidence());

        tessBaseAPI.end();
        return resText;
    }

    private void ModeDialog(){
        String[] items={" Capture Image", " Import from Gallery"};
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
            }
        });
        builder.create().show();
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
