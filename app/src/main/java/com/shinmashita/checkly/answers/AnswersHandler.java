package com.shinmashita.checkly.answers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import androidx.annotation.Nullable;

public class AnswersHandler extends SQLiteOpenHelper {

    private static final String TAG=AnswersHandler.class.getSimpleName();
    private static final String ANSWERS_DB_NAME="answers.db";
    private static final int ANSWERS_DB_VERSION=1;
    private static final String CHAR_ANS_ID="_answer_id";
    private static final String CHAR_ANS_RAW="_answer_value";
    private static final String CHAR_KEY_ID="_answerKey_id";
    private static final String CHAR_KEY_RAW="_answerKey_value";


    public AnswersHandler(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, ANSWERS_DB_NAME, factory, ANSWERS_DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query="CREATE TABLE rawSheets (sheetsId INTEGER PRIMARY KEY AUTOINCREMENT, sheetsName TEXT NOT NULL, scores INTEGER NOT NULL, items INTEGER NOT NULL, misses INTEGER NOT NULL, percentage INTEGER NOT NULL);";
        db.execSQL(query);
        db.execSQL("CREATE TABLE sheetKeys (sheetsId INTEGER PRIMARY KEY AUTOINCREMENT, keyName TEXT NOT NULL);");
        db.execSQL("CREATE TABLE scoreTable (sheetsId INTEGER PRIMARY KEY AUTOINCREMENT, scoreValue INTEGER NOT NULL);");
        db.execSQL("CREATE TABLE itemsTable (sheetsId INTEGER PRIMARY KEY AUTOINCREMENT, itemsValue INTEGER NOT NULL);");
        db.execSQL("CREATE TABLE missTable (sheetsId INTEGER PRIMARY KEY AUTOINCREMENT, missValue INTEGER NOT NULL);");
        db.execSQL("CREATE TABLE percentTable (sheetsId INTEGER PRIMARY KEY AUTOINCREMENT, percentValue INTEGER NOT NULL);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + "rawSheets");
        db.execSQL("DROP TABLE IF EXISTS " + "sheetKeys");
        db.execSQL("DROP TABLE IF EXISTS " + "scoreTable");
        db.execSQL("DROP TABLE IF EXISTS " + "itemsTable");
        db.execSQL("DROP TABLE IF EXISTS " + "missTable");
        db.execSQL("DROP TABLE IF EXISTS " + "percentTable");
        onCreate(db);
    }

    public void generateSheet(String name, int scores, int items, int misses, int percentage, String keyName){
        SQLiteDatabase db=getWritableDatabase();
        String query="CREATE TABLE " + name + "(" +
                CHAR_ANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+ CHAR_ANS_RAW +" TEXT NOT NULL);";
        db.execSQL(query);
        db.execSQL("CREATE TABLE "+name+"_key ("+CHAR_KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+CHAR_KEY_RAW+" TEXT NOT NULL);");
        ContentValues values=new ContentValues();
        values.put("sheetsName", name);
        values.put("scores", scores);
        values.put("items", items);
        values.put("misses", misses);
        values.put("percentage", percentage);
        db.insert("rawSheets", null, values);

        ContentValues values1=new ContentValues();
        values1.put("keyName", keyName);
        db.insert("sheetKeys",null, values1);

        db.close();
    }

    public void addAnswer(answers answer, String sheet){
        ContentValues values=new ContentValues();
        values.put(CHAR_ANS_RAW, answer.get_answer_value());
        SQLiteDatabase db=getWritableDatabase();
        db.insert(sheet, null, values);
        db.close();
    }

    public void addSavedKey(answers answer, String sheet){
        ContentValues values=new ContentValues();
        values.put(CHAR_KEY_RAW, answer.get_answer_value());
        SQLiteDatabase db=getWritableDatabase();
        db.insert(sheet+"_key", null, values);
        db.close();
    }

    public ArrayList<String> getAllSheetNames(){
        ArrayList<String> names=new ArrayList<>();
        SQLiteDatabase db=getReadableDatabase();
        Cursor c= db.rawQuery("SELECT sheetsName FROM rawSheets", null);

        if(c.moveToFirst()){
            while (!c.isAfterLast()){
                names.add(c.getString(c.getColumnIndex("sheetsName")));

                c.moveToNext();
            }
        }

        c.close();
        db.close();

        return names;
    }

    public ArrayList<String> getAllKeyNames(){
        ArrayList<String> names=new ArrayList<>();
        SQLiteDatabase db=getReadableDatabase();
        Cursor c= db.rawQuery("SELECT keyName FROM sheetKeys", null);

        if(c.moveToFirst()){
            while (!c.isAfterLast()){
                names.add(c.getString(c.getColumnIndex("keyName")));

                c.moveToNext();
            }
        }

        c.close();
        db.close();

        return names;
    }


    public ArrayList<Integer> getAllScores(){
        ArrayList<Integer> scores=new ArrayList<>();
        SQLiteDatabase db=getReadableDatabase();
        Cursor c= db.rawQuery("SELECT scoreValue FROM scoreTable", null);

        if(c.moveToFirst()){
            while (!c.isAfterLast()){
                scores.add(c.getInt(c.getColumnIndex("scoreValue")));

                c.moveToNext();
            }
        }

        c.close();
        db.close();

        return scores;
    }

    public void recordScore(Integer score){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("scoreValue", score);
        db.insert("scoreTable", null, values);
        Log.v(TAG, "Score recorded on scoreTable: "+score);
        db.close();
    }

    public ArrayList<Integer> getAllItems(){
        ArrayList<Integer> items=new ArrayList<>();
        SQLiteDatabase db=getReadableDatabase();
        Cursor c= db.rawQuery("SELECT itemsValue FROM itemsTable", null);

        if(c.moveToFirst()){
            while (!c.isAfterLast()){
                items.add(c.getInt(c.getColumnIndex("itemsValue")));

                c.moveToNext();
            }
        }

        c.close();
        db.close();

        return items;
    }

    public void recordItems(Integer items){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("itemsValue", items);
        db.insert("itemsTable", null, values);
        Log.v(TAG, "Items recorded on itemsTable: "+items);
        db.close();
    }

    public ArrayList<Integer> getAllMiss(){
        ArrayList<Integer> miss=new ArrayList<>();
        SQLiteDatabase db=getReadableDatabase();
        Cursor c= db.rawQuery("SELECT missValue FROM missTable", null);

        if(c.moveToFirst()){
            while (!c.isAfterLast()){
                miss.add(c.getInt(c.getColumnIndex("missValue")));

                c.moveToNext();
            }
        }

        c.close();
        db.close();

        return miss;
    }

    public void recordMiss(Integer miss){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("missValue", miss);
        db.insert("missTable", null, values);
        Log.v(TAG, "Misses recorded on missTable: "+miss);
        db.close();
    }

    public ArrayList<Integer> getAllPercent(){
        ArrayList<Integer> percent=new ArrayList<>();
        SQLiteDatabase db=getReadableDatabase();
        Cursor c= db.rawQuery("SELECT percentValue FROM percentTable", null);

        if(c.moveToFirst()){
            while (!c.isAfterLast()){
                percent.add(c.getInt(c.getColumnIndex("percentValue")));

                c.moveToNext();
            }
        }

        c.close();
        db.close();

        return percent;
    }

    public void recordPercent(Integer percent){
        SQLiteDatabase db=getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put("percentValue", percent);
        db.insert("percentTable", null, values);
        Log.v(TAG, "Percentage recorded on percentTable: "+percent);
        db.close();
    }

    public int getSheetsCount(){
        int count=0;
        SQLiteDatabase db=getReadableDatabase();
        Cursor c= db.rawQuery("SELECT sheetsName FROM rawSheets", null);

        if(c.moveToFirst()){
            while (!c.isAfterLast()){
                count++;
                c.moveToNext();
            }
        }

        Log.v(TAG, "Sheet count is "+count);
        c.close();
        db.close();

        return count;
    }

    public int getScore(String sheetName){
        SQLiteDatabase db=getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM rawSheets", null);
        c.moveToFirst();
        int score=0;

        while (!c.isAfterLast()){
            if (c.getString(c.getColumnIndex("sheetsName"))==sheetName){
                score=c.getInt(c.getColumnIndex("scores"));
            }
        }

        Log.v(TAG, "Score gathered from getScore: "+score);

        try {
            return score;
        }catch (NullPointerException e){
            Log.e(TAG, "No int score gathered");
            return 0;
        }
    }

    public int getItems(String sheetName){
        SQLiteDatabase db=getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM rawSheets", null);
        c.moveToFirst();
        int items=0;

        while (!c.isAfterLast()){
            if (c.getString(c.getColumnIndex("sheetsName"))==sheetName){
                items=c.getInt(c.getColumnIndex("items"));
            }
        }

        try {
            return items;
        }catch (NullPointerException e){
            Log.e(TAG, "No int items gathered");
            return 0;
        }
    }

    public int getMisses(String sheetName){
        SQLiteDatabase db=getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM rawSheets", null);
        c.moveToFirst();
        int misses=0;

        while (!c.isAfterLast()){
            if (c.getString(c.getColumnIndex("sheetsName"))==sheetName){
                misses=c.getInt(c.getColumnIndex("misses"));
            }
        }

        try {
            return misses;
        }catch (NullPointerException e){
            Log.e(TAG, "No int misses gathered");
            return 0;
        }
    }

    public int getPercentage(String sheetName){
        SQLiteDatabase db=getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM rawSheets", null);
        c.moveToFirst();
        int percentage=0;

        while (!c.isAfterLast()){
            if (c.getString(c.getColumnIndex("sheetsName"))==sheetName){
                percentage=c.getInt(c.getColumnIndex("percentage"));
            }
        }

        try {
            return percentage;
        }catch (NullPointerException e){
            Log.e(TAG, "No int percentage gathered");
            return 0;
        }
    }

    public ArrayList<String> charAnsToArray(String name){
        ArrayList<String> ans=new ArrayList<>();
        SQLiteDatabase db=getReadableDatabase();
        String query= "SELECT "+CHAR_ANS_RAW+" FROM "+name+" WHERE 1";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        do {
            if(c.getString(c.getColumnIndex(CHAR_ANS_RAW))!= null){
                ans.add(c.getString(c.getColumnIndex(CHAR_ANS_RAW)));
            }
        }while (c.moveToNext());

        if(ans.isEmpty()){
            Log.e(TAG, "No sheets is added to sheets ArrayList");
        }

        return ans;
    }

    public ArrayList<String> charKeysToArray(String name){
        ArrayList<String> ans=new ArrayList<>();
        SQLiteDatabase db=getReadableDatabase();
        String query= "SELECT "+CHAR_KEY_RAW+" FROM "+name +"_key WHERE 1";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        do {
            if(c.getString(c.getColumnIndex(CHAR_KEY_RAW))!= null){
                ans.add(c.getString(c.getColumnIndex(CHAR_KEY_RAW)));
            }
        }while (c.moveToNext());

        if(ans.isEmpty()){
            Log.e(TAG, "No sheetKeys is added to sheets ArrayList");
        }

        return ans;
    }

    public ArrayList<String> sheetsToArray(){
        ArrayList<String> sheetNames=new ArrayList<>();
        SQLiteDatabase db=getReadableDatabase();
        String query= "SELECT * FROM rawSheets WHERE 1";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();

        do {
            if(c.getString(c.getColumnIndex("sheetsName"))!= null){
                sheetNames.add(c.getString(c.getColumnIndex("sheetsName")));
            }
        }while (c.moveToNext());

        if(sheetNames.isEmpty()){
            Log.e(TAG, "No sheets is added to sheets ArrayList");
        }

        return sheetNames;
    }

    public void deleteSheet(String name){
        SQLiteDatabase db=getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+name);
        db.close();
    }

    public void deleteAllSheets(){
        ArrayList<String> names=getAllSheetNames();
        ArrayList<String> keyNames=getAllKeyNames();

        SQLiteDatabase db=getWritableDatabase();
        for(int i=0; i<names.size(); i++){
            db.execSQL("DROP TABLE IF EXISTS " + names.get(i));
        }

        for(int i=0; i<keyNames.size(); i++){
            db.execSQL("DROP TABLE IF EXISTS " + keyNames.get(i));
        }

        db.execSQL("DROP TABLE IF EXISTS " + "rawSheets");
        db.execSQL("DROP TABLE IF EXISTS " + "sheetKeys");
        db.execSQL("DROP TABLE IF EXISTS " + "scoreTable");
        db.execSQL("DROP TABLE IF EXISTS " + "itemsTable");
        db.execSQL("DROP TABLE IF EXISTS " + "missTable");
        db.execSQL("DROP TABLE IF EXISTS " + "percentTable");
        onCreate(db);
        db.close();
    }

    public void renameSheet(String in, String out){
        SQLiteDatabase db=getWritableDatabase();
        db.execSQL("ALTER TABLE "+in+" RENAME TO "+out);
    }



}
