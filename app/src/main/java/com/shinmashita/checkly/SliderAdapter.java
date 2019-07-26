package com.shinmashita.checkly;

import android.content.Context;
import android.transition.Slide;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public SliderAdapter(Context context){
        this.context=context;
    }

    public int[] slide_images={R.drawable.gvision_icon, R.drawable.a_icon};

    public String [] slide_headings={"FORMATTED CORRECTING","EDIT KEYS"};

    public String [] slide_contents={
            "Uses Mobile Vision Text API and formatted sheet for answers. Results are more accurate.",
            "Edit your keys here."
    };

    @Override
    public int getCount() {
        return slide_headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==(RelativeLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        layoutInflater=(LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view =layoutInflater.inflate(R.layout.slide_layout, container, false);

        ImageView slide_image=(ImageView) view.findViewById(R.id.slide_image);
        TextView slide_heading=(TextView) view.findViewById(R.id.slide_heading);
        TextView slide_content=(TextView) view.findViewById(R.id.slide_content);

        slide_image.setImageResource(slide_images[position]);
        slide_heading.setText(slide_headings[position]);
        slide_content.setText(slide_contents[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((RelativeLayout)object);
    }
}
