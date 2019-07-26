package com.shinmashita.checkly.settings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class PreferencesHandler extends SQLiteOpenHelper {

    private static final String TAG=PreferencesHandler.class.getSimpleName();
    private static final String DATABASE_NAME="preference.db";
    private static final String PREFERENCE_VALUE ="value" ;
    private static final String PREFERENCE_ID ="preference_id";
    private static int DATABASE_VERSION=1;

    public static final int DEFAULT_MODE=0;
    public static final int ITEM_COUNT_MODE=1;
    public static final int TEXT_MODE=2;


    public PreferencesHandler(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS OcrCaptureMode");
        db.execSQL("DROP TABLE IF EXISTS TargetCount");
        onCreate(db);
    }

    public void setOcrCaptureMode(int mode){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues values=new ContentValues();


        switch (mode){
            case DEFAULT_MODE:
                values.put("OcrCaptureMode", mode);
                db.replace("Preferences", null, values);
                break;

            case ITEM_COUNT_MODE:
                values.put("OcrCaptureMode", mode);
                db.replace("Preferences", null, values);
                break;

            case TEXT_MODE:
                values.put("OcrCaptureMode", mode);
                db.replace("Preferences", null, values);
                break;
        }

        Log.v(TAG, "OcrCaptureMode: "+mode);
        db.close();
    }

    public int getOcrCaptureMode(){
        SQLiteDatabase db=getReadableDatabase();
        String query= "SELECT * FROM Preferences WHERE 1";
        Cursor c=db.rawQuery(query, null);
        c.moveToFirst();
        int mode=0;
        try {
            mode=c.getInt(c.getColumnIndex("OcrCaptureMode"));
        }catch (NullPointerException e){
            Log.e(TAG, "No OcrCaptureMode gathered from preferences database.");
        }
        Log.v(TAG, "Mode selected: "+mode);

        return mode;
    }

    public void setTargetCount(int count){
        SQLiteDatabase db=getWritableDatabase();
        String tag="TargetCount";
        deleteTable(tag);
        generateTable(tag);

        ContentValues values=new ContentValues();
        values.put(PREFERENCE_VALUE, count);
        db.insert(tag, null, values);
        Log.v(TAG, "Key count: "+count);
        db.close();
    }

    public int getTargetCount(){
        SQLiteDatabase db=getWritableDatabase();
        int count=0;


        String query="SELECT * FROM TargetCount WHERE 1";
        Cursor c=db.rawQuery(query, null);
        c.moveToLast();

        count=c.getInt(c.getColumnIndex(PREFERENCE_VALUE));
        return count;
    }

    public void generateTable(String name){
        SQLiteDatabase db=getWritableDatabase();
        String query="CREATE TABLE " + name + "(" +
                PREFERENCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+ PREFERENCE_VALUE +" TEXT NOT NULL);";
        db.execSQL(query);
    }

    public void deleteTable(String table){
        SQLiteDatabase db=getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+table);
    }
}
