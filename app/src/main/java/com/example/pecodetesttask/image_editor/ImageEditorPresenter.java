package com.example.pecodetesttask.image_editor;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

public class ImageEditorPresenter implements View.OnTouchListener {

    private final ImageEditorPresenter.View view;
    private final ImageEditorPresenter.Painter painter;

    public ImageEditorPresenter(ImageEditorPresenter.View view, ImageEditorPresenter.Painter painter) {
        this.view = view;
        this.painter = painter;
        painter.setOnTouchListener(this);
    }

    public void onUndoButtonClick() {
        painter.undo();
    }

    public void onInsertButtonClick() {
        painter.setModeInsert();
        view.makeDrawingColorSelectorInvisible();
        view.makeInsertImagesSelectorVisible();
    }

    public void setDrawingColor(int color) {
        painter.setColor(color);
        painter.setModeDraw();
    }

    public void insertImage(Drawable image) {
        painter.insertNewImage(image);
        view.makeInsertImagesSelectorInvisible();
        view.makeDrawingColorSelectorVisible();
    }

    @Override
    public boolean onTouch(android.view.View touchView, MotionEvent motionEvent) {
        view.makeInsertImagesSelectorInvisible();
        view.makeDrawingColorSelectorVisible();
        return true;
    }

    public interface View {

        void makeDrawingColorSelectorVisible();

        void makeDrawingColorSelectorInvisible();

        void makeInsertImagesSelectorVisible();

        void makeInsertImagesSelectorInvisible();

    }

    public interface Painter {

        void setOnTouchListener(android.view.View.OnTouchListener listener);

        void insertNewImage(Drawable image);

        void setModeInsert();

        void setModeDraw();

        void setColor(int color);

        void undo();

    }

}
