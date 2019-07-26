/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shinmashita.checkly;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.widget.ListView;

import com.google.android.gms.vision.text.Text;
import com.shinmashita.checkly.checktool.Check;
import com.shinmashita.checkly.keyeditor.db.KeysHandler;
import com.shinmashita.checkly.settings.PreferencesHandler;
import com.shinmashita.checkly.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.text.TextBlock;

import java.util.List;


public class OcrGraphic extends GraphicOverlay.Graphic {

    private int mId;

    private static final int TEXT_COLOR = Color.WHITE;
    private static final int HIGHLIGHTED_COLOR=Color.rgb(0,176,240);

    private static Paint sRectPaint;
    private static Paint sTextPaint;
    private static Paint sHighlightedRectPaint;
    private static Paint sHighlightedRectFillPaint;
    private static Paint sHiddenPaint;
    private final TextBlock mText;

    OcrGraphic(GraphicOverlay overlay, TextBlock text) {
        super(overlay);

        mText = text;

        if (sRectPaint == null) {
            sRectPaint = new Paint();
            sRectPaint.setColor(TEXT_COLOR);
            sRectPaint.setStyle(Paint.Style.STROKE);
            sRectPaint.setStrokeWidth(4.0f);
        }

        if (sTextPaint == null) {
            sTextPaint = new Paint();
            sTextPaint.setColor(TEXT_COLOR);
            sTextPaint.setTextSize(22.0f);
        }

        if (sHighlightedRectPaint==null){
            sHighlightedRectPaint=new Paint();
            sHighlightedRectPaint.setColor(HIGHLIGHTED_COLOR);
            sHighlightedRectPaint.setStyle(Paint.Style.STROKE);
            sHighlightedRectPaint.setStrokeWidth(4.0f);
        }

        if (sHighlightedRectFillPaint==null){
            sHighlightedRectFillPaint=new Paint();
            sHighlightedRectFillPaint.setColor(HIGHLIGHTED_COLOR);
            sHighlightedRectFillPaint.setStyle(Paint.Style.FILL);
            sHighlightedRectFillPaint.setAlpha(40);
        }

        if (sHiddenPaint==null){
            sHiddenPaint=new Paint();
            sHiddenPaint.setColor(HIGHLIGHTED_COLOR);
            sHiddenPaint.setStyle(Paint.Style.FILL);
        }
        postInvalidate();
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public TextBlock getTextBlock() {
        return mText;
    }

    public boolean contains(float x, float y) {
        TextBlock text = mText;
        if (text == null) {
            return false;
        }
        RectF rect = new RectF(text.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        return (rect.left < x && rect.right > x && rect.top < y && rect.bottom > y);
    }

    @Override
    public void draw(Canvas canvas) {


        TextBlock text = mText;
        int mKeyCount=20;
        Check check=new Check();

        if (text == null) {
            return;
        }
        int itemCount=check.getItemCount(text);

        if(itemCount==mKeyCount){
            RectF rect = new RectF(text.getBoundingBox());
            rect.left = translateX(rect.left);
            rect.top = translateY(rect.top);
            rect.right = translateX(rect.right);
            rect.bottom = translateY(rect.bottom);
            canvas.drawRect(rect, sHighlightedRectPaint);

            RectF rectFill=new RectF(text.getBoundingBox());
            rectFill.left=translateX(rectFill.left);
            rectFill.top=translateY(rectFill.top);
            rectFill.right=translateX(rectFill.right);
            rectFill.bottom=translateY(rectFill.bottom);
            canvas.drawRect(rectFill, sHighlightedRectFillPaint);

            Rect rectangle=new Rect(1,1,1,1);
            canvas.drawRect(rectangle, sHiddenPaint);

        }

        //start of itemCount for debug
        /**else {
            RectF rect = new RectF(text.getBoundingBox());
            rect.left = translateX(rect.left);
            rect.top = translateY(rect.top);
            rect.right = translateX(rect.right);
            rect.bottom = translateY(rect.bottom);
            canvas.drawRect(rect, sRectPaint);
        }**/


        //float left = translateX(text.getBoundingBox().left);
        // bottom = translateY(text.getBoundingBox().bottom);
        //canvas.drawText("Item count: "+Integer.toString(itemCount), left, bottom, sTextPaint);

        //end of itemCount



        //List<? extends Text> textComponents = text.getComponents();
        //for(Text currentText : textComponents) {
          //  float left = translateX(currentText.getBoundingBox().left);
            //float bottom = translateY(currentText.getBoundingBox().bottom);
            //canvas.drawText(currentText.getValue(), left, bottom, sTextPaint);
        //}
    }
}
