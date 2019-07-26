package com.shinmashita.checkly.checktool;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.shinmashita.checkly.R;
import com.shinmashita.checkly.answers.AnswersHandler;
import com.shinmashita.checkly.keyeditor.db.KeysHandler;

import java.util.ArrayList;
import java.util.List;


public class Check {

    private static final String TAG=Check.class.getSimpleName();
    KeysHandler db;
    AnswersHandler answersHandler;



    public boolean checkifABCD(String element){

        //This applies for google vision. Set whitelist for Tesseract.
        ArrayList<String> CharacterSet = new ArrayList<String>();
        CharacterSet.add("A");
        CharacterSet.add("B");
        CharacterSet.add("C");
        CharacterSet.add("D");

        int l=0;
        for(int k=0; k<CharacterSet.size(); k++){
            if (element.equalsIgnoreCase(CharacterSet.get(k))){
                l++;
            }
        }
        if (l>0){
            return true;
        }
        else {
            return false;
        }
    }

    public ArrayList<String> AnsArray(){
        ArrayList<String> CharacterSet=new ArrayList<>();
        CharacterSet.add("A");
        CharacterSet.add("4");

        CharacterSet.add("B");
        CharacterSet.add("&");
        CharacterSet.add("8");
        CharacterSet.add("G");

        CharacterSet.add("C");
        CharacterSet.add("6");
        CharacterSet.add("L");

        CharacterSet.add("D");
        CharacterSet.add("P");



        return CharacterSet;
    }

    public boolean checkifABCDX(String element){

        //This applies for google vision. Set whitelist for Tesseract.
        ArrayList<String> CharacterSet = new ArrayList<String>();

        CharacterSet.add("A");
        CharacterSet.add("4");

        CharacterSet.add("B");
        CharacterSet.add("&");
        CharacterSet.add("8");
        CharacterSet.add("G");

        CharacterSet.add("C");
        CharacterSet.add("6");
        CharacterSet.add("L");

        CharacterSet.add("D");
        CharacterSet.add("P");

        CharacterSet.add("X");

        int l=0;
        for(int k=0; k<CharacterSet.size(); k++){
            if (element.equalsIgnoreCase(CharacterSet.get(k))){
                l++;
            }
        }
        if (l>0){
            return true;
        }
        else {
            return false;
        }
    }

    public boolean onBlockCheckifABCD(TextBlock block){

        List<? extends Text> lines=block.getComponents();

        for(int j= 0; j< lines.size(); j++) {
            if(lines.get(j).getValue().toString().contains("a")){
                return true;
            }
            if(lines.get(j).getValue().toString().contains("A")){
                return true;
            }
            if(lines.get(j).getValue().toString().contains("b")){
                return true;
            }
            if(lines.get(j).getValue().toString().contains("B")){
                return true;
            }
            if(lines.get(j).getValue().toString().contains("c")){
                return true;
            }
            if(lines.get(j).getValue().toString().contains("C")){
                return true;
            }
            if(lines.get(j).getValue().toString().contains("d")){
                return true;
            }
            if(lines.get(j).getValue().toString().contains("D")){
                return true;
            }
        }
        return false;
    }

    public int getScore(ArrayList<String> item, Context context, String table){
        db= new KeysHandler(context, null, null, 1);
        int score=0;

        Log.v(TAG, "Table value on getScore parameter is "+table);
        try {
            for (int i = 0; i < db.databaseToArray(table).size(); i++) {
                Log.v(TAG, item.get(i)+" compare to "+ db.databaseToArray(table).get(i));
                if (item.get(i).equalsIgnoreCase(db.databaseToArray(table).get(i))) {
                    score++;
                }
            }
        }catch (NullPointerException e){
            Toast.makeText(context, "There's no text WTH?!", Toast.LENGTH_SHORT).show();
        }catch (IndexOutOfBoundsException e){
            Toast.makeText(context,"Size is not equal", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Detected values size: "+item.size());
        }

        Log.v(TAG, "Score: "+score);
        return score;
    }

    public int getItems(ArrayList<String> items){
        Log.v(TAG, "Items: "+items.size());
        return items.size();
    }

    public void getRating(int score, int items, ImageView imageView){
        try {


            long percentage = (100 * score) / items;

            Log.v(TAG, "Percentage in float is: "+percentage);

            if(percentage<75){
                imageView.setImageResource(R.drawable.f_icon);
            }
            if(percentage>=75&&percentage<80){
                imageView.setImageResource(R.drawable.d_icon);
            }
            if(percentage>=80&&percentage<85){
                imageView.setImageResource(R.drawable.c_icon);
            }
            if(percentage>=85&&percentage<90){
                imageView.setImageResource(R.drawable.b_icon);
            }
            if(percentage>=90){
                imageView.setImageResource(R.drawable.a_icon);
            }


        }catch (ArithmeticException e){
            Log.e(TAG, "Items detected might be zero");
        }
    }

    public int getMisses(int score, int items){
        Log.v(TAG, "Misses: "+(items-score));
        return items-score;
    }

    public int getPercentage(int score, int items){
        try {
            Log.v(TAG,"Percentage: "+((100*score)/items));
            return (100*score)/items;
        }catch (ArithmeticException e){
            return 0;
        }

    }

    //Below methods Mobile Vision API classes
    public int getItemCount(TextBlock block){
        int itemCount=0;
        int Xcount=0;
        List<? extends Text> lines=block.getComponents();

        for(int j= 0; j< lines.size(); j++) {
            char [] chars=lines.get(j).getValue().toCharArray();
            ArrayList<String> charList=new ArrayList<>();

            for (int k=0; k<chars.length; k++){
                charList.add(Character.valueOf(chars[k]).toString());
            }

            ArrayList<String> sheet=toSheetFormat(charList);

            for (int i=0; i<sheet.size(); i++){
                if(AnsArray().contains(sheet.get(i))){
                    itemCount++;
                    Xcount=0;
                }
                if(sheet.get(i).equalsIgnoreCase("X")){
                    Xcount++;
                    if(Xcount>2){
                        Xcount=1;
                        itemCount++;
                    }
                }
                if(Xcount>2){
                    itemCount++;
                    Xcount=1;
                }
            }
        }
        return itemCount;
    }

    public ArrayList<String> toSheetFormat(ArrayList<String> elements){
        //remove stray values
        ArrayList <String> newElements=new ArrayList<>();

        for (int i=0; i<elements.size(); i++){
            if (checkifABCDX(elements.get(i))){
                if(elements.get(i).equalsIgnoreCase("P")){
                    newElements.add("D");
                }
                if(elements.get(i).equalsIgnoreCase("&")){
                    newElements.add("B");
                }
                if(elements.get(i).equalsIgnoreCase("8")){
                    newElements.add("B");
                }
                if(elements.get(i).equalsIgnoreCase("G")){
                    newElements.add("B");
                }
                if(elements.get(i).equalsIgnoreCase("L")){
                    newElements.add("C");
                }
                if(elements.get(i).equalsIgnoreCase("6")){
                    newElements.add("C");
                }
                if(elements.get(i).equalsIgnoreCase("4")){
                    newElements.add("A");
                }
                else {
                    newElements.add(elements.get(i).toUpperCase());
                }
            }
        }

        return newElements;
    }

    public ArrayList<String> getArrayElementsFromBlock(TextBlock block){
        ArrayList<String> ans=new ArrayList<>();
        ArrayList<String> elements=new ArrayList<>();

        List<? extends Text> lines=block.getComponents();

        int Xcount=0;

        for(int i= 0; i< lines.size(); i++){
            char[] chars=lines.get(i).getValue().toCharArray();
            for(int k=0; k<chars.length; k++){
                elements.add(Character.valueOf(chars[k]).toString()); }

            Log.v(TAG, "Elements up to line "+(i+1)+" are: "+elements);
            }

        for (int j=0; j<toSheetFormat(elements).size(); j++){
            if (Xcount>2){
                ans.add("?");
                Xcount=1;
            }
            if (toSheetFormat(elements).get(j).equalsIgnoreCase("X")) {
                Xcount++;

                if (Xcount>2){
                    ans.add("?");
                    Xcount=1;
                }
            }
            if(checkifABCD(toSheetFormat(elements).get(j))){
                ans.add(toSheetFormat(elements).get(j).toUpperCase());
                Xcount=0;
            }
        }
        Log.v(TAG, "Elements after removal of stray values: "+toSheetFormat(elements));
        Log.v(TAG, "Elements from text block: "+ans);
        return ans;
    }

    public ArrayList<String> getSheetFormatFromBlock(TextBlock block){
        ArrayList<String> ans=new ArrayList<>();
        ArrayList<String> elements=new ArrayList<>();

        List<? extends Text> lines=block.getComponents();

        for(int i= 0; i< lines.size(); i++){
            char[] chars=lines.get(i).getValue().toCharArray();
            for(int k=0; k<chars.length; k++){
                elements.add(Character.valueOf(chars[k]).toString()); }

            Log.v(TAG, "Elements up to line "+(i+1)+" are: "+elements);
        }

        for (int j=0; j<elements.size(); j++){
            if(checkifABCD(elements.get(j))){
                ans.add(elements.get(j).toUpperCase());
            }
        }

        Log.v(TAG, "Elements from text block: "+ans);
        return ans;
    }

    public int getMaterialIc(int score, int items){
        try {
            long percentage = (100 * score) / items;

            Log.v(TAG, "Percentage in float is: "+percentage);

            if(percentage<75){
                return R.drawable.f_material_ic;
            }
            if(percentage>=75&&percentage<80){
                return R.drawable.d_material_ic;
            }
            if(percentage>=80&&percentage<85){
                return R.drawable.c_material_ic;
            }
            if(percentage>=85&&percentage<90){
                return R.drawable.b_material_ic;
            }
            if(percentage>=90){
                return R.drawable.a_material_ic;
            }


        }catch (ArithmeticException e){
            Log.e(TAG, "Items detected might be zero");
        }
        return R.drawable.f_material_ic;
    }

    public int getMaterialRating(String a, String b){
            if(a.equalsIgnoreCase(b)){
                return R.drawable.ic_check_black_24dp;
            }
            else {
                return R.drawable.ic_clear_black_24dp;
            }
    }

}
