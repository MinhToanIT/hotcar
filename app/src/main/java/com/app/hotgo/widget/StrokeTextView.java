package com.app.hotgo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class StrokeTextView extends android.support.v7.widget.AppCompatTextView {

    public static final String ANDROID_SCHEMA = "http://schemas.android.com/apk/res/android";

//    public MyButton(Context context) {
//        super(context);
//        initTypeface();
//    }

    public StrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //initTypeface();
        applyCustomFont(context, attrs);
    }

    public StrokeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //initTypeface();
        applyCustomFont(context, attrs);
    }

//    private void initTypeface() {
//        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Product Sans Regular.ttf");
//        this.setTypeface(tf);
//    }


    private void applyCustomFont(Context context, AttributeSet attrs) {
        int textStyle = attrs.getAttributeIntValue(ANDROID_SCHEMA, "textStyle", Typeface.NORMAL);

        Typeface customFont = selectTypeface(context, textStyle);
        setTypeface(customFont);
    }

    private Typeface selectTypeface(Context context, int textStyle) {
        /*
         * information about the TextView textStyle:
         * http://developer.android.com/reference/android/R.styleable.html#TextView_textStyle
         */
        switch (textStyle) {
            case Typeface.BOLD: // bold
                return FontCache.getTypeface(context,"fonts/MyriadPro-Bold.otf");

            case Typeface.ITALIC: // italic
                return FontCache.getTypeface(context,"fonts/MyriadPro-It.otf");

            case Typeface.BOLD_ITALIC: // bold italic
                return FontCache.getTypeface(context,"fonts/MyriadPro-BoldIt.otf");

            case Typeface.NORMAL: // regular
            default:
                return FontCache.getTypeface(context,"fonts/MyriadPro-Regular.otf");
        }
    }


    @Override
    public void onDraw(Canvas canvas)
    {
        int textColor = getTextColors().getDefaultColor();
        setTextColor(Color.WHITE);
        getPaint().setStrokeWidth(6);
        getPaint().setStyle(Paint.Style.STROKE);
        super.onDraw(canvas);
        setTextColor(textColor);
        getPaint().setStrokeWidth(0);
        getPaint().setStyle(Paint.Style.FILL);
        super.onDraw(canvas);
    }
}
