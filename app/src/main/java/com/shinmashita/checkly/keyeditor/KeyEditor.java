package com.shinmashita.checkly.keyeditor;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import com.shinmashita.checkly.OcrCaptureActivity;
import com.shinmashita.checkly.R;
import com.shinmashita.checkly.keyeditor.db.KeysHandler;
import com.shinmashita.checkly.keyeditor.db.keys;
import com.shinmashita.checkly.settings.PreferencesHandler;

import java.util.ArrayList;

public class KeyEditor extends AppCompatActivity {

    private static final String TAG=KeyEditor.class.getSimpleName();

    ListView key_list;
    KeysHandler handler;
    ArrayList<String> items;
    ArrayList<String> itemList;
    ArrayAdapter<String>adapter;
    ArrayAdapter<String>itemListAdapter;
    Intent data;
    int extra;
    String extraTableName;
    PreferencesHandler preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_editor);

        getSupportActionBar().setTitle("Key Editor");
        getSupportActionBar().setElevation(20);

        itemList = new ArrayList<>();
        key_list=findViewById(R.id.keys_listView);
        handler=new KeysHandler(this, null, null, 1);
        preferences= new PreferencesHandler(this, null, null, 1);



        try{
            itemList.addAll(handler.getAllTableNames());
        }catch (NullPointerException e){
            Log.e(TAG, "No itemList yet");
        }

        if(itemList!=null) {
            itemListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, itemList);
            key_list.setAdapter(itemListAdapter);
            key_list.setLongClickable(true);

            extraTableName=getIntent().getStringExtra("KeysFromMain");
            if(extraTableName!=null) {
                try {
                    extra = getIntent().getIntExtra("PositionFromMain", 0);
                    Log.v(TAG, " Successfully got extra: " + extraTableName +" and "+extra);
                    Toast.makeText(KeyEditor.this, "Active key: " + key_list.getItemAtPosition(extra), Toast.LENGTH_SHORT).show();

                } catch (NullPointerException e) {
                    Log.e(TAG, "No selected key table yet");
                }
            }

            key_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    editDlg(key_list.getItemAtPosition(position).toString());
                    return true;
                }
            });

            key_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    for(int i=0; i<key_list.getCount(); i++){
                        key_list.getChildAt(i).setBackgroundColor(getResources().getColor(R.color.colorWhite));
                    }

                    key_list.getChildAt(position).setBackgroundColor(getResources().getColor(R.color.colorEmpWhite));
                    preferences.setTargetCount(handler.getKeyCount(key_list.getItemAtPosition(position).toString()));

                    //data=new Intent(KeyEditor.this, MainActivity.class);
                    //data.putExtra("keys", key_list.getItemAtPosition(position).toString());
                    //data.putExtra("position", position);

                    handler.setActiveKey(key_list.getItemAtPosition(position).toString());

                    try {
                        setResult(KeyEditor.RESULT_OK, data);
                        Log.v(TAG, "Extras to be sent are "+handler.getActiveKey());
                    }catch (NullPointerException e) {
                        Log.e(TAG, "No data put into extras");
                    }

                    Toast.makeText(KeyEditor.this, "Active key: " + key_list.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {

        handler=new KeysHandler(this, null, null, 1);
        try {
            Log.v(TAG, "All added items are: " + handler.databaseToArray(data.getStringExtra("keys")));
        }catch (NullPointerException e) {
           Log.e(TAG, "Missing keys on databaseToArray method");
        }
        finish();
        return true;
    }

    public void addKeyDlg(View view){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View mView=getLayoutInflater().inflate(R.layout.keyedit_dlg, null);

        final ListView keyList=mView.findViewById(R.id.keyEditDlg_listView);
        Button a_btn=mView.findViewById(R.id.keyEditDlg_aBtn);
        Button b_btn=mView.findViewById(R.id.keyEditDlg_bBtn);
        Button c_btn=mView.findViewById(R.id.keyEditDlg_cBtn);
        Button d_btn=mView.findViewById(R.id.keyEditDlg_dBtn);
        Button save_btn=mView.findViewById(R.id.keyEditDlg_saveBtn);


        items=new ArrayList<String>();

        if(items!=null){
            adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
            keyList.setAdapter(adapter);
        }


        builder.setView(mView);
        final AlertDialog dlg= builder.create();
        dlg.show();

        a_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.add("A");
                adapter.notifyDataSetChanged();
                keyList.smoothScrollToPosition(adapter.getCount()); }
        });

        b_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.add("B");
                adapter.notifyDataSetChanged();
                keyList.smoothScrollToPosition(adapter.getCount());}
        });

        c_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.add("C");
                adapter.notifyDataSetChanged();
                keyList.smoothScrollToPosition(adapter.getCount());
            }
        });

        d_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                items.add("D");
                adapter.notifyDataSetChanged();
                keyList.smoothScrollToPosition(adapter.getCount());}
        });

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nameDlg();
                dlg.dismiss();
            }
        });


    }

    public void nameDlg(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        View view=getLayoutInflater().inflate(R.layout.keyedit_name_dlg, null);

        final EditText name=view.findViewById(R.id.keyEdit_name);
        final EditText desc=view.findViewById(R.id.keyEdit_desc);
        Button save_btn=view.findViewById(R.id.keyEdit_nameDlg_saveBtn);

        builder.setView(view);
        final AlertDialog dlg= builder.create();
        dlg.show();

        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!name.getText().toString().isEmpty()){
                    createDB(name.getText().toString());
                    itemList.add(name.getText().toString());
                    itemListAdapter.notifyDataSetChanged();
                    dlg.dismiss();
                }
                else {
                    Toast.makeText(KeyEditor.this, "Key input is required.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createDB(String name) {
        handler=new KeysHandler(this, null, null, 1);

        try {
        handler.generateTable(name);
        } catch (android.database.sqlite.SQLiteException e){
            Toast.makeText(KeyEditor.this, "The key name already exists. Try a new one.", Toast.LENGTH_LONG).show();
        }

        for(int i=0; i<items.size(); i++){
            keys key=new keys(items.get(i));
            handler.addKey(key, name);

            Log.v(TAG, key.get_keyValue()+" is added into key database");
        }

        Log.v(TAG, "Table name: "+name+" ;Item count: "+handler.databaseToArray(name).size());
    }

    private void updateDB(String name){
        handler=new KeysHandler(this, null, null, 1);
        handler.deleteTable(name);
        handler.generateTable(name);

        for(int i=0; i<items.size(); i++){
            if(items.get(i)!=null) {
                keys key = new keys(items.get(i));
                handler.addKey(key, name);

                Log.v(TAG, key.get_keyValue() + " is added into key database");
            }
        }
        Log.v(TAG, "Table name: "+name+" ;Item count: "+handler.databaseToArray(name).size());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.keyedit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.keyEdit_menu_doOCR:
                Intent intent=new Intent(this, OcrCaptureActivity.class);
                startActivity(intent);
                return true;
            case R.id.keyEdit_menu_clear:
                handler=new KeysHandler(KeyEditor.this, null, null, 1);
                handler.deleteAllTables();
                itemList.clear();
                itemListAdapter.notifyDataSetChanged();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void editDlg(final String str){
        String[] titles={" Rename key", " Edit key", " Delete"};
        AlertDialog.Builder builder=new AlertDialog.Builder(KeyEditor.this);
        builder.setItems(titles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        AlertDialog.Builder builder1=new AlertDialog.Builder(KeyEditor.this);
                        View view=getLayoutInflater().inflate(R.layout.keyedit_name_dlg, null);

                        final EditText name=view.findViewById(R.id.keyEdit_name);
                        final EditText desc=view.findViewById(R.id.keyEdit_desc);
                        Button save_btn=view.findViewById(R.id.keyEdit_nameDlg_saveBtn);

                        name.setText(str);
                        builder1.setView(view);
                        final AlertDialog dlg= builder1.create();
                        dlg.show();

                        save_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(!name.getText().toString().isEmpty()){
                                    handler=new KeysHandler(KeyEditor.this, null, null, 1);
                                    handler.renameTable(str, name.getText().toString());

                                    //replace method
                                    int index=itemList.indexOf(str);
                                    itemList.remove(index);
                                    itemList.add(index, name.getText().toString());

                                    itemListAdapter.notifyDataSetChanged();
                                    dlg.dismiss();
                                }
                                else {
                                    Toast.makeText(KeyEditor.this, "Key input is required.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        break;

                    case 1:
                        AlertDialog.Builder bldr=new AlertDialog.Builder(KeyEditor.this);
                        View mView=getLayoutInflater().inflate(R.layout.keyedit_dlg, null);

                        ListView keyList=mView.findViewById(R.id.keyEditDlg_listView);
                        Button a_btn=mView.findViewById(R.id.keyEditDlg_aBtn);
                        Button b_btn=mView.findViewById(R.id.keyEditDlg_bBtn);
                        Button c_btn=mView.findViewById(R.id.keyEditDlg_cBtn);
                        Button d_btn=mView.findViewById(R.id.keyEditDlg_dBtn);
                        Button saveBtn=mView.findViewById(R.id.keyEditDlg_saveBtn);

                        handler=new KeysHandler(KeyEditor.this, null, null, 1);

                        items=new ArrayList<String>();
                        items.addAll(handler.databaseToArray(str));

                        if(items!=null){
                            adapter=new ArrayAdapter<String>(KeyEditor.this, android.R.layout.simple_list_item_1, items);
                            keyList.setAdapter(adapter);
                        }


                        bldr.setView(mView);
                        final AlertDialog dialog1= bldr.create();
                        dialog1.show();

                        keyList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                items.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                        });

                        a_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                items.add("A");
                                adapter.notifyDataSetChanged(); }
                        });

                        b_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                items.add("B");
                                adapter.notifyDataSetChanged();}
                        });

                        c_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                items.add("C");
                                adapter.notifyDataSetChanged();
                            }
                        });

                        d_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                items.add("D");
                                adapter.notifyDataSetChanged();          }
                        });

                        saveBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateDB(str);
                                dialog1.dismiss();
                            }
                        });
                        break;

                    case 2:
                        handler=new KeysHandler(KeyEditor.this, null, null, 1);
                        itemList.remove(str);
                        itemListAdapter.notifyDataSetChanged();
                        handler.deleteTable(str);
                        break;
                }
            }
        });

        builder.create().show();
    }
}
