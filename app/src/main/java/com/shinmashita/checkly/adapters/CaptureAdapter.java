package com.shinmashita.checkly.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.shinmashita.checkly.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class CaptureAdapter extends ArrayAdapter<Sheet> {

    private Context mContext;
    int mResource;

    public CaptureAdapter(@NonNull Context context, int resource, ArrayList<Sheet> sheetName) {
        super(context, resource, sheetName);
        mContext=context;
        mResource=resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        String tag=getItem(position).getSheetName();
        int score=getItem(position).getScore();
        int items=getItem(position).getItems();
        int icResource=getItem(position).getImageResource();



        Sheet sheet=new Sheet(tag, score, items, icResource);

        LayoutInflater inflater=LayoutInflater.from(mContext);
        convertView=inflater.inflate(mResource, parent, false);

        TextView tagView=convertView.findViewById(R.id.capture_adapter_tag);
        TextView scoreView=convertView.findViewById(R.id.capture_adapter_score);
        TextView itemsView=convertView.findViewById(R.id.capture_adapter_items);
        ImageView ic =convertView.findViewById(R.id.capture_adapter_ic);

        tagView.setText(tag);
        scoreView.setText(Integer.toString(score));
        itemsView.setText(Integer.toString(items));
        ic.setImageResource(icResource);

        return convertView;
    }
}
