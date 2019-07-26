package com.shinmashita.checkly;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.shinmashita.checkly.adapters.CaptureAdapter;
import com.shinmashita.checkly.adapters.Sheet;
import com.shinmashita.checkly.answers.AnswersHandler;
import com.shinmashita.checkly.checktool.Check;

import java.util.ArrayList;

public class SheetList extends AppCompatActivity {

    public static final String TAG=SheetList.class.getSimpleName();
    public static final String SHEET_NAME="sheetName";
    public static final String SHEET_SCORE="sheetScore";
    public static final String SHEET_MISSES="sheetMiss";
    public static final String SHEET_PERCENT="sheetPercent";


    ListView sheetListView;
    AnswersHandler mAnswersHandler;
    ArrayList<Sheet> sheetArrayList;
    CaptureAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet_list);

        getSupportActionBar().setTitle("SheetList");
        getSupportActionBar().setElevation(20);


        //Define listView here
        sheetListView=findViewById(R.id.sheetList_listView);
        TextView nullHint=findViewById(R.id.sheetList_nullHint);



        mAnswersHandler=new AnswersHandler(this, null, null, 1);
        ArrayList<String> sheetNameArray=mAnswersHandler.getAllSheetNames();

        sheetArrayList=new ArrayList<Sheet>();
        Check check=new Check();

        for (int i=0; i<mAnswersHandler.getSheetsCount(); i++){

            String sheetName=sheetNameArray.get(i);
            int resScore=mAnswersHandler.getAllScores().get(i);
            int resItems=mAnswersHandler.getAllItems().get(i);
            int ic=check.getMaterialIc(resScore, resItems);

            Sheet sheet=new Sheet(sheetName, resScore, resItems, ic);
            sheetArrayList.add(sheet);
        }

        if(sheetArrayList==null){
            nullHint.setVisibility(View.VISIBLE);
        }else {
            nullHint.setVisibility(View.INVISIBLE);
        }

        try {
            adapter=new CaptureAdapter(SheetList.this, R.layout.capture_adapter, sheetArrayList);
            sheetListView.setAdapter(adapter);
        }catch (NullPointerException e){
            Log.e(TAG, "No names for generated sheets available");
        }

        sheetListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(SheetList.this, SheetView.class);
                intent.putExtra(SHEET_NAME, mAnswersHandler.getAllSheetNames().get(position));
                intent.putExtra(SHEET_SCORE, mAnswersHandler.getAllScores().get(position));
                intent.putExtra(SHEET_MISSES, mAnswersHandler.getAllMiss().get(position));
                intent.putExtra(SHEET_PERCENT, mAnswersHandler.getAllPercent().get(position));

                Log.v(TAG, "Intents to be sent to SheetView are: "+intent.getStringExtra(SHEET_NAME)+", "+intent.getIntExtra(SHEET_SCORE, 0)+", "+intent.getIntExtra(SHEET_MISSES, 0)+", "+intent.getIntExtra(SHEET_PERCENT, 0));

                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sheetlist_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.sheetList_clearBtn){
            AlertDialog.Builder builder=new AlertDialog.Builder(SheetList.this);
            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mAnswersHandler=new AnswersHandler(SheetList.this, null, null, 1);
                    mAnswersHandler.deleteAllSheets();
                    sheetArrayList.clear();
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.setTitle("Delete all sheets?");
            AlertDialog dialog=builder.create();
            dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {

        finish();
        return true;
    }
}
