package com.example.pecodetesttask.image_editor;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.transition.TransitionManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import androidx.transition.Slide;
import androidx.transition.Transition;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.example.pecodetesttask.AppCompatActivityWithoutSystemUI;
import com.example.pecodetesttask.R;
import com.example.pecodetesttask.databinding.ActivityImageEditorBinding;
import com.example.pecodetesttask.drawview.DrawView;

public class ImageEditorActivity extends AppCompatActivityWithoutSystemUI implements ImageEditorPresenter.View {

    public static byte[] imageBytes; // used to pass image bytes from camera activity
    private static final int IMAGES_SELECTOR_APPEARANCE_TIME = 200;
    private LinearLayoutCompat drawingColorSelectorView;
    private GridView insertImageSelectorView;
    private ActivityImageEditorBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // connect to content view
        binding = ActivityImageEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // show passed image
        Bitmap baseImage = getBaseImage();
        if (baseImage != null) {
            binding.imageView.setImageBitmap(baseImage);
        }

        // create and show painter
        DrawView painter = binding.drawView;
        setPainterView(painter);

        // launch presenter
        ImageEditorPresenter presenter = new ImageEditorPresenter(this, painter);

        // bind buttons to presenter
        binding.insertButton.setOnClickListener(view -> presenter.onInsertButtonClick());
        binding.undoButton.setOnClickListener(view -> presenter.onUndoButtonClick());

        // create selectors and bind them to presenter
        setColorSelectorView(presenter);
        setImagesSelectorView(presenter);

        insertImageSelectorView.setVisibility(View.GONE);
    }

    @Override
    public void makeDrawingColorSelectorVisible() {
        drawingColorSelectorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void makeDrawingColorSelectorInvisible() {
        drawingColorSelectorView.setVisibility(View.GONE);
    }

    @Override
    public void makeInsertImagesSelectorVisible() {
        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(IMAGES_SELECTOR_APPEARANCE_TIME);
        transition.addTarget(insertImageSelectorView);
        TransitionManager.beginDelayedTransition(binding.getRoot(), transition);
        insertImageSelectorView.setVisibility(View.VISIBLE);
    }

    @Override
    public void makeInsertImagesSelectorInvisible() {
        Transition transition = new Slide(Gravity.BOTTOM);
        transition.setDuration(IMAGES_SELECTOR_APPEARANCE_TIME);
        transition.addTarget(insertImageSelectorView);
        TransitionManager.beginDelayedTransition(binding.getRoot(), transition);
        insertImageSelectorView.setVisibility(View.GONE);
    }



    /** Secondary Functions */

    private Bitmap getBaseImage() {
        if (imageBytes != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            return Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true
            );
        } else {
            return null;
        }
    }

    private void setPainterView(DrawView painter) {
        ViewTreeObserver paintViewTreeObserver = painter.getViewTreeObserver();
        paintViewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                painter.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = painter.getMeasuredWidth();
                int height = painter.getMeasuredHeight();
                painter.init(height, width);
            }
        });
    }

    private void setImagesSelectorView(ImageEditorPresenter presenter) {
        insertImageSelectorView = binding.imageSelector;
        Drawable emoji_cool = ResourcesCompat.getDrawable(
                getResources(),
                R.drawable.emoji_cool,
                getTheme()
        );
        Drawable emoji_laugh = ResourcesCompat.getDrawable(
                getResources(),
                R.drawable.emoji_laugh,
                getTheme()
        );
        Drawable[] insertImages = new Drawable[] { emoji_cool, emoji_laugh, emoji_cool, emoji_cool, emoji_cool, emoji_cool, emoji_cool };
        SelectImageAdapter adapter = new SelectImageAdapter(this, insertImages);
        insertImageSelectorView.setAdapter(adapter);

        insertImageSelectorView.setOnItemClickListener(
            (parent, view, position, id) -> presenter.insertImage(insertImages[position])
        );
    }

    private void setColorSelectorView(ImageEditorPresenter presenter) {
        SelectColorButton[] colorButtons = new SelectColorButton[] {
            new SelectColorButton(this, Color.WHITE, presenter),
            new SelectColorButton(this, Color.RED, presenter),
            new SelectColorButton(this, Color.GREEN, presenter),
            new SelectColorButton(this, Color.BLUE, presenter)
        };
        drawingColorSelectorView = binding.colorSelectorLinear;

        for (SelectColorButton colorButton : colorButtons) {
            drawingColorSelectorView.addView(colorButton);
        }
    }



    /** Private Classes */

    private static class SelectColorButton extends androidx.appcompat.widget.AppCompatButton {

        private static final int EDGE_SIZE = 100;
        private static final int MARGIN = 20;

        SelectColorButton(Context context, int color, ImageEditorPresenter presenter) {
            super(context);
            LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(EDGE_SIZE, EDGE_SIZE);
            params.setMargins(MARGIN, 0, MARGIN, 0);
            setLayoutParams(params);
            GradientDrawable shape = new GradientDrawable();
            shape.setCornerRadius(EDGE_SIZE / 2);
            shape.setColor(color);
            setBackground(shape);
            setTag(color);
            setOnClickListener(view -> presenter.setDrawingColor(color));
        }

    }

    private static class SelectImageAdapter extends BaseAdapter {

        private final Context context;
        private final Drawable[] items;

        public SelectImageAdapter(Context context, Drawable[] items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View gridView;

            if (convertView == null) {
                gridView = inflater.inflate(R.layout.image_selection_item, parent, false);
                ImageView imageView = gridView.findViewById(R.id.image_selection_item);
                imageView.setImageDrawable(items[position]);
            } else {
                gridView = convertView;
            }

            return gridView;
        }

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

    }

}