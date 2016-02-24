package com.fangstar.clipimage;
//package com.fangstar.broker.activities.clipimage;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * @ClassName: ClipZoomImageView
 * @Description:
 * @author:
 * @date:
 */
public class ClipZoomImageView extends ImageView implements OnScaleGestureListener, OnTouchListener, ViewTreeObserver.OnGlobalLayoutListener {

	public static float SCALE_MAX = 4.0f;
	private static float SCALE_MID = 2.0f;

	/**
	 * 初始化时的缩放比例，如果图片宽或高大于屏幕，此值将小于0
	 */
	private float initScale = 1.0f;
	private boolean once = true;

	/**
	 * 用于存放矩阵的9个值
	 */
	private final float[] matrixValues = new float[9];

	/**
	 * 缩放的手势检测
	 */
	private ScaleGestureDetector mScaleGestureDetector = null;
	private final Matrix mScaleMatrix = new Matrix();

	/**
	 * 用于双击检测
	 */
	private GestureDetector mGestureDetector;
	private boolean isAutoScale;

	private float mLastX;
	private float mLastY;

	private boolean isCanDrag;
	private int lastPointerCount;

	private Rect mClipRect;
	private int mClipW=640,mClipH=640;

	public ClipZoomImageView(Context context) {
		this(context,null);
	}

	public ClipZoomImageView(Context context,int clipWidth,int clipHeight) {
		this(context);
		mClipW=clipWidth;
		mClipH=clipHeight;
	}

	public ClipZoomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setScaleType(ScaleType.MATRIX);
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
		mGestureDetector = new GestureDetector(context,
				new SimpleOnGestureListener() {
					@Override
					public boolean onDoubleTap(MotionEvent e) {
						if (isAutoScale)
							return true;

						float x = e.getX();
						float y = e.getY();
						if (getScale() < SCALE_MID) {
							ClipZoomImageView.this.postDelayed(new AutoScaleRunnable(SCALE_MID, x, y), 16);
							isAutoScale = true;
						} else {
							ClipZoomImageView.this.postDelayed(new AutoScaleRunnable(initScale, x, y), 16);
							isAutoScale = true;
						}
						return true;
					}
				});
		mScaleGestureDetector = new ScaleGestureDetector(context, this);
		this.setOnTouchListener(this);
	}

	/**
	 * 自动缩放的任务
	 */
	private class AutoScaleRunnable implements Runnable {
		static final float BIGGER = 1.07f;
		static final float SMALLER = 0.93f;
		private float mTargetScale;
		private float tmpScale;

		/**
		 * 缩放的中心
		 */
		private float x;
		private float y;

		/**
		 * 传入目标缩放值，根据目标值与当前值，判断应该放大还是缩小
		 */
		public AutoScaleRunnable(float targetScale, float x, float y) {
			this.mTargetScale = targetScale;
			this.x = x;
			this.y = y;
			if (getScale() < mTargetScale) {
				tmpScale = BIGGER;
			} else {
				tmpScale = SMALLER;
			}
		}

		@Override
		public void run() {
			// 进行缩放
			mScaleMatrix.postScale(tmpScale, tmpScale, x, y);
			checkBorder();
			setImageMatrix(mScaleMatrix);

			final float currentScale = getScale();
			// 如果值在合法范围内，继续缩放
			if (((tmpScale > 1f) && (currentScale < mTargetScale)) || ((tmpScale < 1f) && (mTargetScale < currentScale))) {
				ClipZoomImageView.this.postDelayed(this, 16);
			} else {// 设置为目标的缩放比例
				final float deltaScale = mTargetScale / currentScale;
				mScaleMatrix.postScale(deltaScale, deltaScale, x, y);
				checkBorder();
				setImageMatrix(mScaleMatrix);
				isAutoScale = false;
			}
		}
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		float scale = getScale();
		float scaleFactor = detector.getScaleFactor();
		if (getDrawable() == null)
			return true;

		/**
		 * 缩放的范围控制
		 */
		if ((scale < SCALE_MAX && scaleFactor > 1.0f) || (scale > initScale && scaleFactor < 1.0f)) {
			/**
			 * 最大值最小值判断
			 */
			if (scaleFactor * scale < initScale) {
				scaleFactor = initScale / scale;
			}
			if (scaleFactor * scale > SCALE_MAX) {
				scaleFactor = SCALE_MAX / scale;
			}
			/**
			 * 设置缩放比例
			 */
			mScaleMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
			checkBorder();
			setImageMatrix(mScaleMatrix);
		}
		return true;
	}

	/**
	 * 根据当前图片的Matrix获得图片的范围
	 */
	private RectF getMatrixRect() {
		Matrix matrix = mScaleMatrix;
		RectF rect = new RectF();
		Drawable d = getDrawable();
		if (null != d) {
			rect.set(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
			matrix.mapRect(rect);
		}
		return rect;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		return true;
	}

	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event))
			return true;
		mScaleGestureDetector.onTouchEvent(event);

		float x = 0, y = 0;
		// 拿到触摸点的个数
		final int pointerCount = event.getPointerCount();
		// 得到多个触摸点的x与y平均值
		for (int i = 0; i < pointerCount; i++) {
			x += event.getX(i);
			y += event.getY(i);
		}
		x = x / pointerCount;
		y = y / pointerCount;

		/**
		 * 每当触摸点发生变化时，重置mLasX , mLastY
		 */
		if (pointerCount != lastPointerCount) {
			isCanDrag = false;
			mLastX = x;
			mLastY = y;
		}

		lastPointerCount = pointerCount;
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			float dx = x - mLastX;
			float dy = y - mLastY;
			if (!isCanDrag) {
				isCanDrag = isCanDrag(dx, dy);
			}
			if (isCanDrag) {
				if (getDrawable() != null) {
					RectF rect = getMatrixRect();
					// 如果宽度小于裁剪宽度，则禁止左右移动
					if (rect.width() <= mClipW) {
						dx = 0;
					}
					// 如果高度小于裁剪高度，则禁止上下移动
					if (rect.height() <= mClipH) {
						dy = 0;
					}
					mScaleMatrix.postTranslate(dx, dy);
					checkBorder();
					setImageMatrix(mScaleMatrix);
				}
			}
			mLastX = x;
			mLastY = y;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			lastPointerCount = 0;
			break;
		}
		return true;
	}

	/**
	 * 获得当前的缩放比例
	 */
	public final float getScale() {
		mScaleMatrix.getValues(matrixValues);
		return matrixValues[Matrix.MSCALE_X];
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (Build.VERSION.SDK_INT < 16) {
			getViewTreeObserver().removeGlobalOnLayoutListener(this);
		} else {
			getViewTreeObserver().removeOnGlobalLayoutListener(this);
		}
	}

	@Override
	public void onGlobalLayout() {
		if (once) {
			Drawable d = getDrawable();
			if (d == null)
				return;

			int width = getWidth();
			int height = getHeight();
			// 拿到图片的宽和高
			int drawableW = d.getIntrinsicWidth();
			int drawableH = d.getIntrinsicHeight();
			Log.e("ClipZoomImage_ImageSize", String.valueOf(drawableW) + "," + String.valueOf(drawableH));
			float scale = 1.0f;
			// 图片大于剪裁框
			if (drawableW > mClipW && drawableH < mClipH) {
				scale = 1.0f * mClipH / drawableH;
			} else if (drawableH > mClipH && drawableW < mClipW) {
				scale = 1.0f * mClipW / drawableW;
			} else if (drawableW > mClipW && drawableH > mClipH) {
				float scaleW = mClipW * 1.0f / drawableW;
				float scaleH = mClipH * 1.0f / drawableH;
				scale = Math.max(scaleW, scaleH);
			}
			// 图片小于剪裁框
			if (drawableW < mClipW && drawableH > mClipH) {
				scale = 1.0f * mClipW / drawableW;
			} else if (drawableH < mClipH && drawableW > mClipW) {
				scale = 1.0f * mClipH / drawableH;
			} else if (drawableW < mClipW && drawableH < mClipH) {
				float scaleW = 1.0f * mClipW / drawableW;
				float scaleH = 1.0f * mClipH / drawableH;
				scale = Math.max(scaleW, scaleH);
			}

			initScale = scale;
			SCALE_MID = initScale * 2;
			SCALE_MAX = initScale * 4;
			mScaleMatrix.postTranslate((width - drawableW) / 2, (height - drawableH) / 2);
			//小图放大处理
			if (drawableW < mClipW || drawableH < mClipH){
				mScaleMatrix.postScale(scale, scale, getWidth() / 2, getHeight() / 2);
			}
			// 图片移动至屏幕中心
			setImageMatrix(mScaleMatrix);
			once = false;
		}
	}

	/**
	 * 剪切图片，返回剪切后的bitmap对象
	 */
	public Bitmap clip() {
		Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		draw(canvas);
		return Bitmap.createBitmap(bitmap,mClipRect.left,mClipRect.top,mClipW,mClipH);
	}

	/**
	 * 边界检测
	 */
	private void checkBorder() {
		RectF rect = getMatrixRect();
		float deltaX = 0;
		float deltaY = 0;
		// 如果宽或高大于剪裁框宽高，则控制在范围内(1的误差)
		if (rect.width()+1 >= mClipW) {
			if (rect.left > mClipRect.left) {
				deltaX=mClipRect.left-rect.left;
			}
			if (rect.right <  mClipRect.right) {
				deltaX=mClipRect.right-rect.right;
			}
		}
		if (rect.height()+1 >= mClipH) {
			if (rect.top > mClipRect.top) {
				deltaY=mClipRect.top-rect.top;
			}
			if (rect.bottom < mClipRect.bottom) {
				deltaY=mClipRect.bottom-rect.bottom;
			}
		}
		mScaleMatrix.postTranslate(deltaX, deltaY);
	}
	/**
	 * 是否是拖动行为
	 */
	private boolean isCanDrag(float dx, float dy) {
		return Math.sqrt((dx * dx) + (dy * dy)) >= 0;
	}
}
