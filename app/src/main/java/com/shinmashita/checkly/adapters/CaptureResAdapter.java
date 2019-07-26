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

public class CaptureResAdapter extends ArrayAdapter<RawSheet> {

    private Context mContext;
    private int mResource;

    public CaptureResAdapter(@NonNull Context context, int resource, ArrayList<RawSheet> rawSheets) {
        super(context, resource, rawSheets);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        String charAns=getItem(position).getCharAns();
        String charKeys=getItem(position).getCharKeys();
        int src=getItem(position).getImageSrc();

        RawSheet rawSheet=new RawSheet(charAns, charKeys, src);

        LayoutInflater inflater=LayoutInflater.from(mContext);
        convertView=inflater.inflate(mResource, parent, false);

        TextView charAnsView=convertView.findViewById(R.id.ocr_capture_charAns);
        TextView charKeysVIew=convertView.findViewById(R.id.ocr_capture_charKeys);
        ImageView rating=convertView.findViewById(R.id.ocr_capture_rating);

        charAnsView.setText(charAns);
        charKeysVIew.setText(charKeys);
        rating.setImageResource(src);

        return convertView;
    }
}
