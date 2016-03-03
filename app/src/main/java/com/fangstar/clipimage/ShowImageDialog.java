package com.fangstar.clipimage;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.volley.toolbox.NetworkImageView;
import com.base.BaseApplication;

/**
 * 显示图片
 * Created by G
 */
public class ShowImageDialog extends Dialog implements DialogInterface.OnDismissListener, View.OnClickListener {
    private final String TAG = this.getClass().getSimpleName();
    private Bitmap image = null;
    private String url = null;

    public ShowImageDialog(Context context, Bitmap image) {
        this(context, R.style.Dialog_ShowImg, image);
    }

    public ShowImageDialog(Context context, int theme, Bitmap image) {
        super(context, theme);
        this.image = image;
    }

    public ShowImageDialog(Context context, String path, boolean isNetWork) {
        this(context, R.style.Dialog_ShowImg, path, isNetWork);
    }

    public ShowImageDialog(Context context, int theme, String path, boolean isNetWork) {
        super(context, theme);
        if (isNetWork) {
            this.url = path;
        } else {
            this.image = BitmapFactory.decodeFile(path);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.dialog_showimage);
        if (this.image != null) {
            this.setOnDismissListener(this);
            ImageView img = (ImageView) findViewById(R.id.iv_dialog_img);
            img.setImageBitmap(this.image);
            img.setOnClickListener(this);
        } else if (this.url != null) {
            NetworkImageView imgNet = (NetworkImageView) findViewById(R.id.iv_dialog_img_net);
            imgNet.setImageUrl(this.url, BaseApplication.imageLoader);
            imgNet.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_dialog_img:
                this.dismiss();
                break;
            case R.id.iv_dialog_img_net:
                this.dismiss();
                break;
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Log.e(TAG, "Dismiss----------Dismiss");
        this.image.recycle();
    }
}
