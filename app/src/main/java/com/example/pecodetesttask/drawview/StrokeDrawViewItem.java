package com.example.pecodetesttask.drawview;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.example.pecodetesttask.drawview.DrawView;

public class StrokeDrawViewItem implements DrawView.DrawViewItem {

    public int color;
    public int strokeWidth;
    public Path path;

    public StrokeDrawViewItem(int color, int strokeWidth, Path path) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }

    @Override
    public void drawTo(Canvas canvas, Paint paint) {
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        canvas.drawPath(path, paint);
    }
}
