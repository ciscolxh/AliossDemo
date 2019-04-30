package com.zebucars.alioss;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.zebucars.alioss.config.Config;
import com.zebucars.alioss.utils.IoUtils;
import com.zebucars.alioss.utils.MD5Utils;
import com.zebucars.alioss.utils.OSSUtils;

import java.io.File;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleObserver;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author macman
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener, OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
    private final String TAG = "MainActivity";
    private Context context;
    private OSSAsyncTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        findViewById(R.id.btn).setOnClickListener(this);
        findViewById(R.id.select).setOnClickListener(this);
        findViewById(R.id.photo).setOnClickListener(this);
    }


    void upLoadFile(File file, OSS oss) {
        // 构造上传请求。
        PutObjectRequest put = new PutObjectRequest(Config.BUCKET_NAME, "user/" + MD5Utils.calculationFileMD5(file.getPath())+".png", file.getPath());
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn:
                ossUpload();
                break;
            case R.id.select:
                PictureSelector.create(MainActivity.this)
                        .openGallery(PictureMimeType.ofImage())
                        .selectionMode(PictureConfig.SINGLE)
                        .forResult(PictureConfig.CHOOSE_REQUEST);
                break;
            case R.id.photo:
                PictureSelector.create(MainActivity.this)
                        .openCamera(PictureMimeType.ofImage())
                        .forResult(PictureConfig.CHOOSE_REQUEST);
                break;
            default:
                break;
        }
    }


    void ossUpload() {
        Single.create(new SingleOnSubscribe<OssLoadBean>() {
            @Override
            public void subscribe(SingleEmitter<OssLoadBean> emitter) {
                File file = getFile();
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


    File getFile() {
        String json = "{\"name\":\"我是测试RxJava上传文件\"}";
        File file = new File(context.getFilesDir(), "search_data.json");
        boolean status = IoUtils.writeFile(json, file);
        if (status) {
            Log.e(TAG, "写入成功");
        } else {
            Log.e(TAG, "写入失败");
        }
        return file;
    }

    @Override
    public void onSuccess(PutObjectRequest request, PutObjectResult result) {

        Log.d(TAG, "PutObject:" + "UploadSuccess");
        Log.d(TAG, "ETag:" + result.getETag());
        Log.d(TAG, "RequestId:" + result.getRequestId());
        Log.d(TAG, request.getBucketName());
    }

    @Override
    public void onFailure(PutObjectRequest request, ClientException clientException, ServiceException serviceException) {
        // 请求异常。
        if (clientException != null) {
            // 本地异常，如网络异常等。
            clientException.printStackTrace();
        }
        if (serviceException != null) {
            // 服务异常。
            Log.e(TAG, "ErrorCode:" + serviceException.getErrorCode());
            Log.e(TAG, "RequestId:" + serviceException.getRequestId());
            Log.e(TAG, "HostId:" + serviceException.getHostId());
            Log.e(TAG, "RawMessage:" + serviceException.getRawMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (task != null) {
            task.cancel();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片、视频、音频选择结果回调
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    Log.e(TAG,selectList.toString());
                    for (LocalMedia media:selectList){
                        Log.e(TAG,media.getCompressPath());
                        Log.e(TAG,media.getPictureType());
                        Log.e(TAG,media.getPath());

                    }
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true  注意：音视频除外
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true  注意：音视频除外
                    // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                    break;
                default:
                    break;
            }
        }
    }


}
