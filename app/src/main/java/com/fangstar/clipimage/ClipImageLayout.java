package com.fangstar.clipimage;
//package com.fangstar.broker.activities.clipimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * @ClassName: ClipImageLayout
 * @Description
 * @author
 * @date
 */
public class ClipImageLayout extends RelativeLayout {
	private ClipZoomImageView mZoomImageView;
	private ClipImageBorderView mClipImageView;
	private int mClipW=640,mClipH=640;

	public ClipImageLayout(Context context) {
		this(context, null);
	}

	public ClipImageLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mZoomImageView = new ClipZoomImageView(context,mClipW,mClipH);
		mClipImageView = new ClipImageBorderView(context,mClipW,mClipH);
		android.view.ViewGroup.LayoutParams lp = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		this.addView(mZoomImageView, lp);
		this.addView(mClipImageView, lp);

//		setImageDrawable(getResources().getDrawable(R.drawable.c));
	}

	public void setImageDrawable(Drawable drawable) {
		mZoomImageView.setImageDrawable(drawable);
	}
	public void setImageBitmap(Bitmap bitmap) {
		mZoomImageView.setImageBitmap(bitmap);
	}

	/**
	 * 裁切图片
	 */
	public Bitmap clip() {
		return mZoomImageView.clip();
	}
}
