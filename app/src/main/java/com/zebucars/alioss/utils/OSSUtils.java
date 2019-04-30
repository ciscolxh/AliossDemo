package com.zebucars.alioss.utils;

import android.content.Context;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.zebucars.alioss.config.Config;

/**
 * @author 罗富清
 * @date 2019/4/29
 */
public class OSSUtils {

    /**
     * 获取 OSS连接器
     * @param context 上下文
     * @return oss
     */
    public static OSS getOSS(Context context) {
        String endpoint = Config.OSS_ENDPOINT;
        // TODO 推介使用服务器授权
        String stsServer = Config.OSS_ENDPOINT;
        OSSCredentialProvider credentialProvider = new OSSAuthCredentialsProvider(stsServer);
        // TODO 将本地授权删除改为服务器授权
        OSSCustomSignerCredentialProvider provider = new OSSCustomSignerCredentialProvider() {
            @Override
            public String signContent(String content) {
                return com.alibaba.sdk.android.oss.common.utils.OSSUtils.sign(Config.OSS_ACCESS_KEY_ID, Config.OSS_ACCESS_KEY_SECRET, content);
            }
        };
        // 配置类如果不设置，会有默认配置。
        ClientConfiguration conf = new ClientConfiguration();
        //连接超时，默认15秒。
        conf.setConnectionTimeout(15 * 1000);
        //socket超时，默认15秒。
        conf.setSocketTimeout(15 * 1000);
        //最大并发请求数，默认5个
        conf.setMaxConcurrentRequest(5);
        //失败后最大重试次数，默认2次。
        conf.setMaxErrorRetry(2);
        return new OSSClient(context, endpoint, provider);
    }
}
