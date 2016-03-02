package com.fangstar.clipimage;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.base.BaseApplication;
import com.fangstar.multipart.MultipartRequest;
import com.fangstar.multipart.MultipartRequestParams;
import com.tools.Utils;

import java.io.File;

/**
 * @author G
 */
public class MainActivity extends Activity implements OnClickListener {
    private Dialog mImagePickerDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.chooseAvatar).setOnClickListener(this);

        //选择头像相关
        View imagePickerContent = this.getLayoutInflater().inflate(R.layout.dialog_clipimage_picker, null);
        mImagePickerDialog = Utils.createBottomSlideDialog(this, imagePickerContent);
        imagePickerContent.findViewById(R.id.albumBtn).setOnClickListener(this);
        imagePickerContent.findViewById(R.id.captureBtn).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chooseAvatar:
                mImagePickerDialog.show();
                break;
            case R.id.albumBtn:
                ClipImageActivity.startActivity(this, 1, ClipImageActivity.ACTION_ALBUM);
                break;
            case R.id.captureBtn:
                ClipImageActivity.startActivity(this, 1, ClipImageActivity.ACTION_CAPTURE);
                break;
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

            File file = new File(path);
            Log.e("ImageFile_MAIN_Size", String.format("%s,%s,%d", file.getName(), file.getPath(), file.length()));
//            if (file.exists()) {
//                file.delete();
//            }

            MultipartRequestParams params = new MultipartRequestParams();
            params.put("u_id", "4359");
            params.put("contact_id", "906819");
            params.put("avatar_type", "2");
            params.put("avatar_photo", file);

//            String SERVER = "http://192.168.22.9/FSERP/probe/admin/fangstarbrokerapi/public";
//            String changeCustomerAvatar = SERVER + "/app/probe/contact/update/modify-customer-avatar";
//
//            MultipartRequest a = new MultipartRequest(Request.Method.POST, params, changeCustomerAvatar, new Response.Listener<String>() {
//                @Override
//                public void onResponse(String response) {
//                    Log.e("MainActivity", response);
//                }
//            }, new Response.ErrorListener() {
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    error.printStackTrace();
//                }
//            });
//            RequestQueue que = BaseApplication.requestQueue;
//            que.add(a);
        }
    }
}
