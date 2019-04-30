package com.zebucars.alioss.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * @author 罗富清
 * @date 2019/4/29
 */
public class FileUtils {


    public static String getPath(Context context, Intent data) {
        if (data == null || data.getData() == null) {
            return null;
        }
        String photoPath = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        // 获取选中图片的路径
        Cursor cursor = context.getContentResolver().query(data.getData(), proj, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            photoPath = cursor.getString(columnIndex);
            if (photoPath == null) {
                photoPath = Utils.getPath(context.getApplicationContext(), data.getData());
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return photoPath;
    }


    public static Intent getIntent(Uri mCutUri,Uri uri, int aspectX, int aspectY, int outputX, int outputY) {



        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCutUri);
        return intent;
    }


    /**
     * 获取图片角度
     * @param filepath 文件路径
     * @return 角度
     */
    public int getExif(String filepath) {
        int digree = 0;
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(filepath);
            // 读取图片中相机方向信息
            int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            // 计算旋转角度
            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    digree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    digree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    digree = 270;
                    break;
                default:
                    digree = 0;
                    break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return digree;
    }


    public void luBan(final Context context, final File file) {

        Luban.with(context)
                .load(file)
                .ignoreBy(100).setTargetDir("xxx")
                .filter(new CompressionPredicate() {
                    @Override
                    public boolean apply(String path) {
                        return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                    }
                })
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        // TODO 压缩开始前调用，可以在方法内启动 loading UI
                    }

                    @Override
                    public void onSuccess(File file) {
                        // TODO 压缩成功后调用，返回压缩后的图片文件
                    }

                    @Override
                    public void onError(Throwable e) {
                        // TODO 当压缩过程出现问题时调用
                    }
                }).launch();


    }
}
