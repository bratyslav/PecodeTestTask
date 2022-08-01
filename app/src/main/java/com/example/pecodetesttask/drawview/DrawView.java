package com.example.pecodetesttask.drawview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.example.pecodetesttask.image_editor.ImageEditorPresenter;

import java.util.LinkedList;

public class DrawView extends View implements ImageEditorPresenter.Painter {

    private enum Mode {
        DRAW,
        INSERT
    }

    private static final float TOUCH_TOLERANCE = 4;
    private static final int INSERT_IMAGE_WIDTH = 300;
    private static final int INSERT_IMAGE_HEIGHT = 300;
    private static final int INITIAL_IMAGE_OFFSET_X = 300;
    private static final int INITIAL_IMAGE_OFFSET_Y = 300;
    private static final int STROKE_WIDTH = 20;
    private float touchX, touchY;
    private int currentDrawingColor;
    private Mode mode = Mode.DRAW;
    private Path drawingPath;
    private Bitmap bitmap;
    private Canvas canvas;
    private OnTouchListener onTouchListener = null;
    private final Paint paint;
    private final LinkedList<DrawViewItem> drawingItems = new LinkedList<>();
    private final Paint bitmapPaint = new Paint(Paint.DITHER_FLAG);

    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAlpha(0xff);
    }

    public void init(int height, int width) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        canvas = new Canvas(bitmap);
        currentDrawingColor = Color.WHITE;
    }

    @Override
    public void setOnTouchListener(android.view.View.OnTouchListener listener) {
        onTouchListener = listener;
    }

    @Override
    public void insertNewImage(Drawable image) {
        // deep copy
        Drawable imageCopy = image.getConstantState().newDrawable().mutate();
        imageCopy.setBounds(
                INITIAL_IMAGE_OFFSET_X,
                INITIAL_IMAGE_OFFSET_Y,
                INITIAL_IMAGE_OFFSET_X + INSERT_IMAGE_WIDTH,
                INITIAL_IMAGE_OFFSET_Y + INSERT_IMAGE_HEIGHT
        );
        DrawViewItem drawingItem = new DrawableDrawViewItem(imageCopy);
        drawingItems.add(drawingItem);
        invalidate();
    }

    @Override
    public void setModeInsert() {
        mode = Mode.INSERT;
    }

    @Override
    public void setModeDraw() {
        mode = Mode.DRAW;
    }

    @Override
    public void setColor(int color) {
        currentDrawingColor = color;
    }

    @Override
    public void undo() {
        if (drawingItems.size() != 0) {
            drawingItems.remove(drawingItems.size() - 1);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (this.canvas == null) {
            return;
        }

        // clear previous frame
        this.canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        for (DrawViewItem item : drawingItems) {
            item.drawTo(canvas, paint);
        }

        canvas.drawBitmap(bitmap, 0, 0, bitmapPaint);
    }

    private void touchStart(float x, float y) {
        if (mode == Mode.DRAW) {
            drawingPath = new Path();
            DrawViewItem strokeDrawViewItem = new StrokeDrawViewItem(currentDrawingColor, STROKE_WIDTH, drawingPath);
            drawingItems.add(strokeDrawViewItem);
            drawingPath.reset();
            drawingPath.moveTo(x, y);
            touchX = x;
            touchY = y;
        }
    }

    private void touchMove(float x, float y) {
        if (mode == Mode.DRAW) {
            float dx = Math.abs(x - touchX);
            float dy = Math.abs(y - touchY);

            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                drawingPath.quadTo(touchX, touchY, (x + touchX) / 2, (y + touchY) / 2);
                touchX = x;
                touchY = y;
            }
        } else {
            // looking for the last inserted image
            DrawableDrawViewItem lastImage = null;
            for (int i = drawingItems.size(); i-- > 0;) {
                DrawViewItem item = drawingItems.get(i);
                if (item instanceof DrawableDrawViewItem) {
                    lastImage = (DrawableDrawViewItem) item;
                    break;
                }
            }
            if (lastImage != null) {
                lastImage.drawable.setBounds(
                        Math.round(x - INSERT_IMAGE_WIDTH / 2),
                        Math.round(y - INSERT_IMAGE_WIDTH / 2),
                        Math.round(x + INSERT_IMAGE_WIDTH / 2),
                        Math.round(y + INSERT_IMAGE_HEIGHT / 2)
                );
            }
        }
    }

    private void touchUp() {
        if (mode == Mode.DRAW) {
            drawingPath.lineTo(touchX, touchY);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        performClick();
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }


        if (onTouchListener != null) {
            onTouchListener.onTouch(this, event);
        }

        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public interface DrawViewItem {

        void drawTo(Canvas canvas, Paint paint);

    }

}