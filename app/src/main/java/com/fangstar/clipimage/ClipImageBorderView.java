package com.fangstar.clipimage;
//package com.fangstar.broker.activities.clipimage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * 剪裁框遮罩层
 * Created by G
 */
public class ClipImageBorderView extends View {
    /**
     * 边框的宽:dp
     */
    private int mBorderWidth = 1;

    private Paint mPaint;
    private Rect mClipRect;
    private int mClipW = 640, mClipH = 640;

    public ClipImageBorderView(Context context) {
        this(context, null);
    }

    public ClipImageBorderView(Context context, int clipWidth, int clipHeight) {
        this(context);
        mClipW = clipWidth;
        mClipH = clipHeight;
    }

    public ClipImageBorderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                int width = getMeasuredWidth();
                int height = getMeasuredHeight();
                if (mClipW > width) {
                    mClipW = width;
                }
                if (mClipH > height) {
                    mClipH = height;
                }
                mClipRect = new Rect((width - mClipW) / 2, (height - mClipH) / 2, (width - mClipW) / 2 + mClipW, (height - mClipH) / 2 + mClipH);
                return true;
            }
        });

        mBorderWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBorderWidth, getResources().getDisplayMetrics());
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(Color.parseColor("#AA000000"));
        mPaint.setStyle(Style.FILL);
        canvas.drawRect(0, 0, mClipRect.left, getHeight(), mPaint);
        canvas.drawRect(mClipRect.left, 0, getWidth(), mClipRect.top, mPaint);
        canvas.drawRect(mClipRect.right, mClipRect.top, getWidth(), getHeight(), mPaint);
        canvas.drawRect(mClipRect.left, mClipRect.bottom, mClipRect.right, getHeight(), mPaint);
        // 边框
        mPaint.setColor(Color.parseColor("#FFFFFFFF"));
        mPaint.setStrokeWidth(mBorderWidth);
        mPaint.setStyle(Style.STROKE);
        canvas.drawRect(mClipRect, mPaint);
    }
}
