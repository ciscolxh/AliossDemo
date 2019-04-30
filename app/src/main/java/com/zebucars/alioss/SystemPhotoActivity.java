package com.zebucars.alioss;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.bumptech.glide.Glide;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.zebucars.alioss.config.Config;
import com.zebucars.alioss.utils.BitmapUtil;
import com.zebucars.alioss.utils.FileUtils;
import com.zebucars.alioss.utils.MD5Utils;
import com.zebucars.alioss.utils.OSSUtils;
import com.zebucars.alioss.utils.getPhotoFromPhotoAlbum;

import java.io.File;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import rx.Observer;

/**
 * @author 罗富清
 */
public class SystemPhotoActivity extends AppCompatActivity implements View.OnClickListener, OSSCompletedCallback<PutObjectRequest, PutObjectResult> {

    final private int REQUEST_IMAGE = 124;
    final private int REQUEST_CAMERA = 125;
    final private int REQUEST_CLIP = 126;
    private ImageView img;
    /**
     * 拍照照片路径
     */
    private File cameraSavePath;
    /**
     * 照片uri
     */
    private Uri uri;
    private Context context;

    private File file;
    private String path;
    private OSSAsyncTask<PutObjectResult> task;
    private Uri fileUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_photo);
        context = this;
        initFileUrl();
    }

    /**
     * 不调用此方法 访问图库会出错
     */
    private void initFileUrl() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        initView();
    }

    private void initView() {
        findViewById(R.id.album).setOnClickListener(this);
        findViewById(R.id.photo).setOnClickListener(this);
        findViewById(R.id.upload).setOnClickListener(this);

        img = findViewById(R.id.img);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.album:
                intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, REQUEST_IMAGE);
                break;
            case R.id.photo:
                getPermissions();
                break;
            case R.id.upload:

                break;
            default:
                break;
        }
    }

    /**
     * 打开相机
     */
    private void openCamera() {
        Intent intent;
        cameraSavePath = new File(Environment.getExternalStorageDirectory().getPath() + "/" + System.currentTimeMillis() + ".png");
        intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String authority = getPackageName() + ".provider";
            uri = FileProvider.getUriForFile(SystemPhotoActivity.this, authority, cameraSavePath);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(cameraSavePath);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, REQUEST_CAMERA);
    }


    public void getPermissions() {
        //动态申请内存存储权限
        RxPermissions rxPermissions = new RxPermissions((Activity) context);
        rxPermissions
                .request(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            openCamera();
                        } else {
                            Toast.makeText(context, "没有得到权限", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(context, "没有权限", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            switch (requestCode) {
                // 相册返回图片
                case REQUEST_IMAGE:
                    if (data != null && data.getData() != null) {
                       String filePath = getPhotoFromPhotoAlbum.getRealPathFromUri(this, data.getData());
                        File file = BitmapUtil.compressImage(filePath);
                        photoClip(Uri.fromFile(file));
//                        Glide.with(context).load(file.getPath()).into(img);
//                        photoClip(data.getData());
                    }

                    break;
                // 相机返回图片
                case REQUEST_CAMERA:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        photoClip(Uri.fromFile(cameraSavePath));
                    } else {
//                        photoClip(uri);
                    }
//                    File files = BitmapUtils.getimage(cameraSavePath.getPath(),context);

                    File files = BitmapUtil.compressImage(cameraSavePath.getPath());
                    photoClip(Uri.fromFile(files));

//                    Glide.with(context).load(path).into(img);
                    break;
                // 裁剪图片
                case REQUEST_CLIP:
//                    path = FileUtils.getPath(context, data);
//                    if (path == null) {
//                        File file = null;
//                        try {
//                            file = new File(new URI(String.valueOf(data.getData())));
//                        } catch (URISyntaxException e) {
//                            e.printStackTrace();
//                        }
//                        //照片路径
//                        path = Objects.requireNonNull(file).getPath();
//                    }
                    Glide.with(context).load(fileUri).into(img);

//                    Bitmap bitmap = PhotoUtils.getBitmapFromUri(data.getData(), this);
//                    if (bitmap != null) {
//                        img.setImageBitmap(bitmap);
//                    }

                    // todo 删除文件
//                    ossUpload();
//                    File file = new File(path);
//                    file.delete();
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void photoClip(Uri uri) {
        File mCutFile = new File(context.getNoBackupFilesDir(), System.currentTimeMillis() + ".png");
        if (!mCutFile.getParentFile().exists()) {
            mCutFile.getParentFile().mkdirs();
        }
       fileUri = Uri.fromFile(mCutFile);
        int aspectX = 1;
        int aspectY = 1;
        int outputX = 300;
        int outputY = 300;
        // 调用系统中自带的图片剪裁
        Intent intent = FileUtils.getIntent(fileUri,uri, aspectX, aspectY, outputX, outputY);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, "");
        startActivityForResult(intent, REQUEST_CLIP);
    }


    void ossUpload() {
        Single.create(new SingleOnSubscribe<OssLoadBean>() {
            @Override
            public void subscribe(SingleEmitter<OssLoadBean> emitter) {
                File file = new File(path);
                OSS oss = OSSUtils.getOSS(context);
                emitter.onSuccess(new OssLoadBean(oss, file));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).
                subscribe(new SingleObserver<OssLoadBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(OssLoadBean ossLoadBean) {
                        upLoadFile(ossLoadBean.getFile(), ossLoadBean.getOss());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });

    }

    void upLoadFile(File file, OSS oss) {
        // 构造上传请求。
        PutObjectRequest put = new PutObjectRequest(
                Config.BUCKET_NAME,
                "user/" + MD5Utils.calculationFileMD5(file.getPath()) + ".png",
                file.getPath()
        );
        // 异步上传时可以设置进度回调。
        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                Log.d("PutObject", "currentSize: " + currentSize + " totalSize: " + totalSize);
            }
        });
        task = oss.asyncPutObject(put, this);
        // task.cancel(); // 可以取消任务。
        // 等待任务完成。
        task.waitUntilFinished();
    }


    @Override
    public void onSuccess(PutObjectRequest request, PutObjectResult result) {
        Toast.makeText(context, "上传成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
        Toast.makeText(context, "上传失败", Toast.LENGTH_SHORT).show();
    }
}
