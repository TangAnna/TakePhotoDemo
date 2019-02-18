package com.example.tang.androidavatardemo;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.example.tang.androidavatardemo.permission.MPermission;
import com.example.tang.androidavatardemo.permission.OnMPermissionGranted;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private ImageView mImageView;
    //请求相机权限
    private static final int REQUEST_CODE_PERMISSION = 100;
    private static final int CEMERA_REQUEST_CODE = 101;//相机
    private static final int XIANGCE_REQUEST_CODE = 102;//相册

    private static final String IMAGE_FILE_NAME = "user_head_icon.jpg";
    //需要申请的权限的数组
    private String[] mPermissions = {Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.tv_main_camera).setOnClickListener(this);
        findViewById(R.id.tv_main_xiangce).setOnClickListener(this);
        mImageView = findViewById(R.id.iv_main_show);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_main_camera://拍照
                requestBasicPermission(mPermissions, REQUEST_CODE_PERMISSION);
                break;
            case R.id.tv_main_xiangce://相册
                requestBasicPermission(mPermissions, REQUEST_CODE_PERMISSION);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        MPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    //同意
    @RequiresApi(api = Build.VERSION_CODES.M)
    @OnMPermissionGranted(REQUEST_CODE_PERMISSION)
    public void onBasicPermissionSuccess() {
        camear();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ===" + resultCode);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CEMERA_REQUEST_CODE:
                    File pictureFile = new File(PictureUtil.getMyPetRootDirectory(), IMAGE_FILE_NAME);
                    Uri pictureUri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        pictureUri = FileProvider.getUriForFile(this,
                                "com.example.mypet.fileprovider", pictureFile);
                    } else {
                        pictureUri = Uri.fromFile(pictureFile);
                    }
                    Log.d(TAG, "picURI=" + pictureUri.toString());
                    if (pictureUri != null) {
                        try {
                            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(pictureUri));
                            mImageView.setImageBitmap(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 相机拍照
     */
    public void camear() {
        Intent intent;
        Uri pictureUri;
        //getMyPetRootDirectory()得到的是Environment.getExternalStorageDirectory() + File.separator+"MyPet"
        //也就是我之前创建的存放头像的文件夹（目录）
        File pictureFile = new File(PictureUtil.getMyPetRootDirectory(), IMAGE_FILE_NAME);
        File file = new File(PictureUtil.getMyPetRootDirectory());
        if (!file.exists()) {
            pictureFile.mkdirs();
        }

        // 判断当前系统
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //这一句非常重要
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //""中的内容是随意的，但最好用package名.provider名的形式，清晰明了
            pictureUri = FileProvider.getUriForFile(this,
                    "com.example.mypet.fileprovider", pictureFile);
        } else {
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            pictureUri = Uri.fromFile(pictureFile);
        }
        // 去拍照,拍照的结果存到oictureUri对应的路径中
        intent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri);
        Log.e(TAG, "before take photo" + pictureUri.toString());
        startActivityForResult(intent, CEMERA_REQUEST_CODE);
    }

    /**
     * 相册里选择
     */
    public void chooseXiangCe() {

    }

    /**
     * 基本权限管理
     */
    private void requestBasicPermission(String[] permission, int requestCode) {
        MPermission.with(this)
                .addRequestCode(requestCode)
                .permissions(
                        permission
                ).request();
    }

    public void setPicToView(Uri uri) {
        if (uri != null) {
            Bitmap photo = null;
            try {
                photo = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // 创建 smallIcon 文件夹
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                //String storage = Environment.getExternalStorageDirectory().getPath();
                File dirFile = new File(PictureUtil.getMyPetRootDirectory(), "Icon");
                if (!dirFile.exists()) {
                    if (!dirFile.mkdirs()) {
                        Log.d(TAG, "in setPicToView->文件夹创建失败");
                    } else {
                        Log.d(TAG, "in setPicToView->文件夹创建成功");
                    }
                }
                File file = new File(dirFile, IMAGE_FILE_NAME);
//                InfoPrefs.setData(Constants.UserInfo.HEAD_IMAGE,file.getPath());
                //Log.d("result",file.getPath());
                // Log.d("result",file.getAbsolutePath());
                // 保存图片
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(file);
                    photo.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.flush();
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            // 在视图中显示图片
//            showHeadImage();
            //circleImageView_user_head.setImageBitmap(InfoPrefs.getData(Constants.UserInfo.GEAD_IMAGE));
        }
    }
}
