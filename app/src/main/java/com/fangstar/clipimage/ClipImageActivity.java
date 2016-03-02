package com.fangstar.clipimage;
//package com.fangstar.broker.activities.clipimage;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 裁剪图片的Activity
 * Created by G
 */
public class ClipImageActivity extends Activity implements OnClickListener {
    public static final int ACTION_ALBUM = 1;
    public static final int ACTION_CAPTURE = 2;
    public static final String TEMP_IMAGE_PATH = "tempImagePath";
    private static final String KEY = "actionCode";
    private final String TEMP_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/tempimage.jpg";
    private ClipImageLayout mClipImageLayout = null;
    private String PHOTO_PATH;
    private int mImageTagWidth, mImageTagHeight;


    public static void startActivity(Activity activity, int requestCode, int actionCode) {
        Intent intent = new Intent(activity, ClipImageActivity.class);
        intent.putExtra(KEY, actionCode);
        activity.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clipimage);

        mImageTagWidth = 4096;
        mImageTagHeight = 4096;
        mClipImageLayout = (ClipImageLayout) findViewById(R.id.clipImageLayout);
        findViewById(R.id.okBtn).setOnClickListener(this);
        findViewById(R.id.cancleBtn).setOnClickListener(this);

        int actionCode = getIntent().getIntExtra(KEY, ACTION_ALBUM);
        switch (actionCode) {
            case ACTION_ALBUM:
                startAlbum();
                break;
            case ACTION_CAPTURE:
                startCapture();
                break;
            default:
                startAlbum();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.getId() == R.id.okBtn) {
            Bitmap bitmap = mClipImageLayout.clip();
            saveTempBitmap(bitmap);
            Intent intent = new Intent();
            intent.putExtra(TEMP_IMAGE_PATH, TEMP_PATH);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    private void saveTempBitmap(Bitmap bitmap) {
        File f = new File(TEMP_PATH);
        if (f.exists()) {
            f.delete();
        }
        FileOutputStream fOut = null;
        try {
            if (f.createNewFile()) {
                fOut = new FileOutputStream(f);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
                fOut.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fOut != null)
                    fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            setResult(resultCode, data);
            finish();
        }
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        String imagePath = null;
        switch (requestCode) {
            case ACTION_ALBUM:
                try {
                    Uri datauri = data.getData();
                    BitmapFactory.decodeStream(this.getContentResolver().openInputStream(datauri), null, options);
                    String ftype = options.outMimeType;
                    if ("image/jpeg".equals(ftype) || "image/png".equals(ftype)) {
                        Log.e("ImageTypeTest", options.outMimeType);
                        // 使用获取到的inSampleSize值再次解析图片
                        options.inSampleSize = calculateInSampleSize(options);
                        options.inJustDecodeBounds = false;
                        bitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(datauri), null, options);
                        imagePath = getFilePathByUri(datauri);
                    }else{
                        Toast.makeText(this, "格式错误，请使用jpg/png格式的图片", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("Exception", e.getMessage(), e);
                    finish();
                }
                break;
            case ACTION_CAPTURE:
                BitmapFactory.decodeFile(PHOTO_PATH, options);
                options.inSampleSize = calculateInSampleSize(options);
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeFile(PHOTO_PATH, options);
                imagePath = PHOTO_PATH;
                break;
        }
        if (bitmap != null) {
            if (imagePath != null) {
                int degrees = readBitmapDegree(imagePath);
                if (degrees == 0) {
                    mClipImageLayout.setImageBitmap(bitmap);
                } else {
                    mClipImageLayout.setImageBitmap(rotateBitmap(degrees, bitmap));
                }
            } else {
                mClipImageLayout.setImageBitmap(bitmap);
            }
        } else {
            finish();
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        while ((height / inSampleSize) > mImageTagHeight || (width / inSampleSize) > mImageTagWidth) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    private void startAlbum() {
        try {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            startActivityForResult(intent, ACTION_ALBUM);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            try {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, ACTION_ALBUM);
            } catch (Exception ex) {
                // TODO: handle exception
                e.printStackTrace();
            }
        }
    }

    private void startCapture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        PHOTO_PATH = Environment.getExternalStorageDirectory() + "/DCIM/Camera/" + getPhotoFileName();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(PHOTO_PATH)));
        startActivityForResult(intent, ACTION_CAPTURE);
    }

    /**
     * 用时间戳生成照片名称
     */
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        return "IMG_" + dateFormat.format(date) + ".jpg";
    }

    /**
     * 读取图像的旋转度
     */
    private int readBitmapDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * 旋转图片
     */
    private Bitmap rotateBitmap(int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        //创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        //释放原图
        bitmap.recycle();
        return resizedBitmap;
    }

    /**
     * 通过uri获取文件路径
     */
    public String getFilePathByUri(Uri mUri) {
        try {
            if (mUri.getScheme().equals("file")) {
                return mUri.getPath();
            } else {
                Cursor cursor = getContentResolver().query(mUri, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    String path = cursor.getString(1);
                    cursor.close();
                    return path;
                } else {
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
