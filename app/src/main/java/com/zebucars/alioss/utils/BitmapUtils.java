package com.zebucars.alioss.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

/**
 * @author 罗富清
 * @date 2019/4/30
 */
public class BitmapUtils {

    /**
     * 图片按比例大小压缩方法
     *
     * @param srcPath （根据路径获取图片并压缩）
     * @return file
     */
    public static File getimage(String srcPath, Context context) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了，只读取图片的大小，不分配内存
        newOpts.inJustDecodeBounds = true;
        // 此时返回bm为空
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是1920*1080分辨率，所以高和宽我们设置为
        // 这里设置高度为1920f
        float hh = 1920f;
        // 这里设置宽度为1080f
        float ww = 1080f;
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        // be=1表示不缩放
        int be = 1;
        // 如果宽度大的话根据宽度固定大小缩放
        if (w > h && w > ww) {
            be = (int) (newOpts.outWidth / ww);
            // 如果高度高的话根据宽度固定大小缩放
        } else if (w < h && h > hh) {
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0) {
            be = 1;
        }
        // 设置缩放比例
        newOpts.inSampleSize = be;
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        //检测并旋转图片
        Bitmap bitmap1 = changeImageLocate(srcPath, bitmap);
        // 压缩好比例大小后再进行质量压缩
        return compressImage(bitmap1, context);
    }

    //检测并旋转图片
    public static Bitmap changeImageLocate(String filepath, Bitmap bitmap) {
        //根据图片的filepath获取到一个ExifInterface的对象
        int degree;
        try {
            ExifInterface exif = new ExifInterface(filepath);
            // 读取图片中相机方向信息
            int ori = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Log.e("degree========ori====", ori + "");

            // 计算旋转角度
            switch (ori) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
                default:
                    degree = 0;
                    break;
            }
            Log.e("degree============", degree + "");
            if (degree != 0) {
                Log.e("degree============", "degree != 0");
                // 旋转图片
                Matrix m = new Matrix();
//                    m.setScale(0.5f,0.5f);
                m.postRotate(degree);

                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);

                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 质量压缩
     *
     * @param bitmap
     * @return file
     */
    private static File compressImage(Bitmap bitmap, Context context) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        int options = 90;

        // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
        while (baos.toByteArray().length / 1024 > 300 && options > 0) {
            // 重置baos即清空baos
            baos.reset();
            // 这里压缩options%，把压缩后的数据存放到baos中
            bitmap.compress(Bitmap.CompressFormat.PNG, options, baos);
            // 每次都减少10
            options -= 10;
        }
        File dirFile = new File(context.getExternalCacheDir(), "xxx.png");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        File file = new File(dirFile.getPath(), System.currentTimeMillis() + ".png");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            try {
                fos.write(baos.toByteArray());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        recycleBitmap(bitmap);
        return file;
    }

    /**
     * 回收Bitmap
     *
     * @param bitmaps bitmap
     */
    private static void recycleBitmap(Bitmap... bitmaps) {
        if (bitmaps == null) {
            return;
        }
        for (Bitmap bm : bitmaps) {
            if (null != bm && !bm.isRecycled()) {
                bm.recycle();
            }
        }
    }

    public static void luBan(final Context context , String path, final ImageView view){
        Luban.with(context)
                .load(path)
                .ignoreBy(30)
                .setTargetDir(context.getExternalCacheDir()+"/xxx.png")
                .filter(new CompressionPredicate() {
                    @Override
                    public boolean apply(String path) {
                        return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".png"));
                    }
                })
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onSuccess(File file) {
                        BitmapFactory.Options newOpts = new BitmapFactory.Options();
                        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了，只读取图片的大小，不分配内存
                        newOpts.inJustDecodeBounds = false;
                        // 此时返回bm为空
                        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), newOpts);

                        Bitmap bitmap1 = changeImageLocate(file.getPath(),bitmap);

                        File f = compressImage(bitmap1,context);
                        Glide.with(context).load(f.getPath()).into(view);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                }).launch();
    }

}
