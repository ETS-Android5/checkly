package com.shinmashita.checkly;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.shinmashita.checkly.adapters.CaptureResAdapter;
import com.shinmashita.checkly.adapters.RawSheet;
import com.shinmashita.checkly.answers.AnswersHandler;
import com.shinmashita.checkly.checktool.Check;

import java.util.ArrayList;

public class SheetView extends AppCompatActivity {

    private static final String TAG=SheetView.class.getSimpleName();

    public static final String SHEET_NAME="sheetName";
    public static final String SHEET_SCORE="sheetScore";
    public static final String SHEET_MISSES="sheetMiss";
    public static final String SHEET_PERCENT="sheetPercent";


    TextView scoreView, missesView, percentView;
    ImageView icResource;
    ListView mCharList;
    ArrayList<RawSheet> mRawSheetArray;
    ArrayList<String> charAns, charKeys;
    CaptureResAdapter adapter;
    AnswersHandler mAnswersHandler;
    Check check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet_view);

        scoreView=findViewById(R.id.sheetView_score);
        missesView=findViewById(R.id.sheetView_misses);
        percentView=findViewById(R.id.sheetView_percent);
        mCharList=findViewById(R.id.sheetView_ListView);
        icResource=findViewById(R.id.sheetView_ic);

        String sheetName=getIntent().getStringExtra(SHEET_NAME);
        getSupportActionBar().setTitle(sheetName);
        getSupportActionBar().setElevation(20);

        int score=getIntent().getIntExtra(SHEET_SCORE, 0);
        int misses=getIntent().getIntExtra(SHEET_MISSES,0);
        int percent=getIntent().getIntExtra(SHEET_PERCENT,0);

        Log.v(TAG, "Intents collected: "+sheetName+", "+score+", "+misses+", "+percent);

        scoreView.setText(Integer.toString(score));
        missesView.setText(Integer.toString(misses));
        percentView.setText(Integer.toString(percent));

        check=new Check();
        check.getRating(score, score+misses, icResource);

        mAnswersHandler=new AnswersHandler(SheetView.this, null, null, 1);
        charAns=mAnswersHandler.charAnsToArray(sheetName);
        charKeys=mAnswersHandler.charKeysToArray(sheetName);

        mRawSheetArray=new ArrayList<RawSheet>();

        Log.v(TAG, "CharAns: "+charAns);
        Log.v(TAG, "CharKeys: "+charKeys);

        try {
            for(int i=0; i<charAns.size(); i++){
                RawSheet rawSheet=new RawSheet(charAns.get(i), charKeys.get(i), check.getMaterialRating(charAns.get(i), charKeys.get(i)));
                mRawSheetArray.add(rawSheet);
            }

        }catch (NullPointerException e){
            Log.e(TAG, "Raw sheets added may be null");
        }

        try{
            adapter=new CaptureResAdapter(SheetView.this, R.layout.capture_res_adapter, mRawSheetArray);
            mCharList.setAdapter(adapter);
        }catch (NullPointerException e){
            Log.e(TAG, "No raw sheets added to arrayList in adapter");
        }

    }
}
