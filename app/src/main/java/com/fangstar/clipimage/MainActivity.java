package com.fangstar.clipimage;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @ClassName: MainActivity
 */
public class MainActivity extends Activity implements OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.albumBtn).setOnClickListener(this);
        findViewById(R.id.captureBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.getId() == R.id.albumBtn) {
            ClipImageActivity.startActivity(this,1, ClipImageActivity.ACTION_ALBUM);
        } else if (v.getId() == R.id.captureBtn) {
            ClipImageActivity.startActivity(this,1, ClipImageActivity.ACTION_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String path = data.getStringExtra(ClipImageActivity.TEMP_IMAGE_PATH);
            Log.e("ImageByte_MAIN", path);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            Log.e("ImageByte_MAIN_Size", String.valueOf(bitmap.getByteCount() / 1024));
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(bitmap);

            File file = new File(ClipImageActivity.TEMP_IMAGE_PATH);
            if (file.exists()) {
                file.delete();
            }
            Log.e("ImageFile_MAIN_Size", String.valueOf(file.length()));
        }
    }
}
