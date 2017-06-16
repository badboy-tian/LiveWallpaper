package com.i7play.videopapger.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.i7play.videopapger.R;

/**
 * Created by shashankm on 24/03/17.
 */

public class CustomRange extends View implements View.OnTouchListener {
    private static final String TAG = CustomRange.class.getSimpleName();

    /**
     * Default holder view width for the range bar
     */
    private static final float DEFAULT_HOLDER_WIDTH = 16f;

    /**
     * Default min value for the range bar. It's recommended to change this to your needs
     * either through xml or with {@link CustomRange#setMinValue(float)}
     */
    private static final float DEFAULT_MIN_VALUE = 0f;

    /**
     * Default max value for the range bar. It's recommended to change this to your needs
     * either through xml or with {@link CustomRange#setMaxValue(float)}
     */
    private static final float DEFAULT_MAX_VALUE = 100f;

    private enum DragPosition {
        START, END, NOT_DEFINED
    }

    private float startPosition = 0;

    private float endPosition = 100;

    private DragPosition draggingPosition = DragPosition.NOT_DEFINED;

    private Paint progressPaint;

    private int holderColor;

    private float holderWidth;

    private int nonSelectedColor;

    private int selectedColor;

    private float minValue;

    private float maxValue;

    private RangeChangeListener rangeChangeListener;

    interface RangeChangeListener {
        void onRangeChanged(float startValue, float endValue);
    }

    public CustomRange(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs, context);
    }

    public CustomRange(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomRange(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(attrs, context);
    }

    public void setRangeChangeListener(RangeChangeListener rangeChangeListener) {
        this.rangeChangeListener = rangeChangeListener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int height = getHeight();
        int halfHeight = getHeight() / 2;
        int startX = (int) (getWidth() * startPosition / 100);
        int endX = (int) (getWidth() * endPosition / 100);

        // draw the part of the bar that's filled
        // noinspection SuspiciousNameCombination
        progressPaint.setStrokeWidth(height);

        // draw non selected color before start position
        progressPaint.setColor(nonSelectedColor);
        canvas.drawLine(0, halfHeight, startX, halfHeight, progressPaint);

        // draw selected color
        progressPaint.setColor(selectedColor);
        canvas.drawLine(startX + (holderWidth / 2), halfHeight, endX - (holderWidth / 2), halfHeight, progressPaint);

        // draw the unfilled section after end position
        progressPaint.setColor(nonSelectedColor);
        canvas.drawLine(endX, halfHeight, getWidth(), halfHeight, progressPaint);

        // draw holders before start and after end position
        progressPaint.setColor(holderColor);
        progressPaint.setStrokeWidth(holderWidth);
        canvas.drawLine(startX + (holderWidth / 2), height, startX + (holderWidth / 2), 0, progressPaint);
        canvas.drawLine(endX - (holderWidth / 2), height, endX - (holderWidth / 2), 0, progressPaint);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // scale down drag point in terms of 100
        int dragPoint = (int) (100 + (((event.getX() - v.getWidth()) / v.getWidth()) * 100));

        if (dragPoint < 0 || dragPoint > 100) return true;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isCloseToStart(dragPoint)) {
                    startPosition = dragPoint;
                    draggingPosition = DragPosition.START;
                } else {
                    endPosition = dragPoint;
                    draggingPosition = DragPosition.END;
                }

                if (rangeChangeListener != null) {
                    float startValue = startPosition * (maxValue / 100);
                    float endValue = endPosition * (maxValue / 100);
                    rangeChangeListener.onRangeChanged(startValue, endValue);
                }

                invalidate();
                return true;

            case MotionEvent.ACTION_MOVE:
                if (draggingPosition == DragPosition.NOT_DEFINED) return true;

                if (draggingPosition == DragPosition.START && dragPoint < (endPosition - 10)) {
                    startPosition = dragPoint;
                } else if (draggingPosition == DragPosition.END && dragPoint > (startPosition + 10)) {
                    endPosition = dragPoint;
                }

                if (rangeChangeListener != null) {
                    float startValue = startPosition * (maxValue / 100);
                    float endValue = endPosition * (maxValue / 100);
                    rangeChangeListener.onRangeChanged(startValue, endValue);
                }

                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                draggingPosition = DragPosition.NOT_DEFINED;
                return true;
        }
        return false;
    }

    public float getMinValue() {
        return minValue;
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(float maxValue) {
        this.maxValue = maxValue;
    }

    public float getStartValue() {
        return (startPosition / 100f ) * maxValue;
    }

    public float getEndValue() {
        return (endPosition / 100f) * maxValue;
    }

    private boolean isCloseToStart(int percent) {
        return Math.abs(percent - startPosition) < Math.abs(percent - endPosition);
    }

    private void init(AttributeSet attrs, Context context) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomRange);
        try {
            holderColor = typedArray.getColor(R.styleable.CustomRange_holderColor, Color.WHITE);
            holderWidth = typedArray.getDimension(R.styleable.CustomRange_holderWidth, convertDpToPixel(DEFAULT_HOLDER_WIDTH, context));
            nonSelectedColor = typedArray.getColor(R.styleable.CustomRange_nonSelectedColor, Color.GRAY);
            selectedColor = typedArray.getColor(R.styleable.CustomRange_selectedColor, Color.GREEN);
            minValue = typedArray.getFloat(R.styleable.CustomRange_minValue, DEFAULT_MIN_VALUE);
            maxValue = typedArray.getFloat(R.styleable.CustomRange_maxValue, DEFAULT_MAX_VALUE);
        } finally {
            typedArray.recycle();
        }

        progressPaint = new Paint();
        progressPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        setOnTouchListener(this);
    }

    private float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
