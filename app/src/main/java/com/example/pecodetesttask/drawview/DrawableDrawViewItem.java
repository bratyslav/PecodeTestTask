package com.example.pecodetesttask.drawview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import com.example.pecodetesttask.drawview.DrawView;

public class DrawableDrawViewItem implements DrawView.DrawViewItem {

    public final Drawable drawable;

    public DrawableDrawViewItem(Drawable drawable) {
        this.drawable = drawable;
    }

    @Override
    public void drawTo(Canvas canvas, Paint paint) {
        drawable.draw(canvas);
    }
}
