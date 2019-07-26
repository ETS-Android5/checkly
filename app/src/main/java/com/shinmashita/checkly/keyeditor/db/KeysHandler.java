package com.shinmashita.checkly.keyeditor.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.shinmashita.checkly.settings.PreferencesHandler;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class KeysHandler extends SQLiteOpenHelper {

    private static final String TAG=KeysHandler.class.getSimpleName();
    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="keys.db";
    private static final String TABLE_KEYS="keys";
    private static final String COLUMN_ID="_id";
    private static final String COLUMN_KEYS="key_name";



    public KeysHandler(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query="CREATE TABLE " + TABLE_KEYS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+ COLUMN_KEYS +" TEXT NOT NULL);";
        db.execSQL(query);
}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_KEYS);
        onCreate(db);
    }

    public void addKey(keys key, String table){
        ContentValues values= new ContentValues();
        values.put(COLUMN_KEYS, key.get_keyValue());
        SQLiteDatabase db= getWritableDatabase();
        db.insert(table, null, values);
        db.close();
    }

    public String findKeyName(){
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_KEYS + " WHERE 1";
        Cursor c= db.rawQuery(query, null);
        c.moveToLast();
        String key_name =c.getString(c.getColumnIndex("key_name"));
        return key_name;
    }

    public String findKeyId(){
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_KEYS + " WHERE 1";
        Cursor c= db.rawQuery(query, null);
        c.moveToLast();
        int keyIdsub=c.getColumnIndex("key_name");
        String keyId=Integer.toString(keyIdsub);
        return keyId;
    }

    public void clearKey (String table){
            SQLiteDatabase db = getWritableDatabase();
            db.delete(table, null, null);
            db.close();
    }

    public String databaseToString(String table){
        String dbString="";
        SQLiteDatabase db=getWritableDatabase();
        String query = "SELECT * FROM " + table + " WHERE 1";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        while(c.moveToNext()){
            if(c.getString(c.getColumnIndex("key_name"))!= null){
                dbString+=c.getString(c.getColumnIndex("key_name"));
                dbString+="\n";
            }
        }
        db.close();
        return dbString;
    }

    public String getLastTableKey(String table){
        SQLiteDatabase db= getReadableDatabase();
        String query = "SELECT * FROM " + table + " WHERE 1";

        Cursor c = db.rawQuery(query, null);
        c.moveToLast();

        String lastKey=c.getString(c.getColumnIndex("key_name"));
        db.close();
        return lastKey;

    }

    public ArrayList<String> databaseToArray(String table){
        ArrayList<String> keys=new ArrayList<>();
        SQLiteDatabase db=getReadableDatabase();
        String query= "SELECT * FROM "+table +" WHERE 1";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        do {
            if(c.getString(c.getColumnIndex("key_name"))!= null){
               keys.add(c.getString(c.getColumnIndex("key_name")));
            }
        }while (c.moveToNext());

        if(keys.isEmpty()){
            Log.e(TAG, "No key is added to keys ArrayList");
        }

        return keys;
    }

    public void generateTable(String name){
        SQLiteDatabase db=getWritableDatabase();
        String query="CREATE TABLE " + name + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+ COLUMN_KEYS +" TEXT NOT NULL);";
        db.execSQL(query);
    }

    public ArrayList<String> getAllTableNames(){
        ArrayList<String> names=new ArrayList<>();
        SQLiteDatabase db=getReadableDatabase();
        Cursor c= db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        if(c.move(3)){
            while (!c.isAfterLast()){
                names.add(c.getString(c.getColumnIndex("name")));

                c.moveToNext();
            }
        }
        names.remove("sqlite_sequence");
        names.remove("activeKey");
        return names;
    }

    public void deleteAllTables(){
        SQLiteDatabase db=getWritableDatabase();
        ArrayList<String> names=getAllTableNames();
        for(int i=0; i<names.size(); i++){
            db.execSQL("DROP TABLE IF EXISTS " + names.get(i));
        }
        db.close();
    }

    public void deleteTable(String table){
        SQLiteDatabase db=getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+table);
        db.close();
    }

    public void renameTable(String in, String out){
        SQLiteDatabase db=getWritableDatabase();
        db.execSQL("ALTER TABLE "+in+" RENAME TO "+out);
    }

    public int getKeyCount(String table){
        SQLiteDatabase db=getReadableDatabase();
        String query="SELECT * FROM "+table+" WHERE 1";
        Cursor c=db.rawQuery(query, null);
        return c.getCount();
    }

    public void generateTempDb(){
        SQLiteDatabase db= getWritableDatabase();
        String query="CREATE TABLE tempTable (id INTEGER PRIMARY KEY AUTOINCREMENT, tempAns TEXT NOT NULL);";
        db.execSQL(query);
        db.close();
    }

    public void addToTempDb(String string){
        ContentValues values=new ContentValues();
        values.put("tempAns", string);
        SQLiteDatabase db= getWritableDatabase();
        db.insert("tempTable", null, values);
        db.close();
    }

    public void clearTempDb(){
        SQLiteDatabase db=getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS tempTable");
        db.close();
    }

    public void setActiveKey(String table){
        String activeKey="activeKey";
        try{
            deleteTable(activeKey);
        }catch (NullPointerException e){
            Log.e(TAG, "Table activeKey is not yet created");
        }
        generateTable(activeKey);
        ContentValues values=new ContentValues();
        values.put(COLUMN_KEYS, table);
        SQLiteDatabase db=getWritableDatabase();
        db.insert(activeKey, null, values);
        db.close();
    }

    public String getActiveKey(){
        SQLiteDatabase db=getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM activeKey WHERE 1", null);

        c.moveToFirst();

        String activeKey=null;

        do {
            if(c.getString(c.getColumnIndex("key_name"))!= null){
                activeKey=c.getString(c.getColumnIndex("key_name"));
            }
        }while (c.moveToNext());

        if(activeKey.isEmpty()){
            Log.e(TAG, "No key is added to keys ArrayList");
        }

        return activeKey;
    }

    public int getActiveCount(){
        return databaseToArray(getActiveKey()).size();
    }
}
