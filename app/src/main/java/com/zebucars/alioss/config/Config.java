package com.zebucars.alioss.config;

/**
 * @author 罗富清
 * @date 2019/4/29
 */
public class Config {


    /**
     * 访问的endpoint地址
     */
    public static final String OSS_ENDPOINT = "http://oss-cn-beijing.aliyuncs.com";
    /**
     * callback 测试地址
     */
    public static final String OSS_CALLBACK_URL = "http://oss-demo.aliyuncs.com:23450";
    /**
     * STS 鉴权服务器地址。
     * 或者根据工程sts_local_server目录中本地鉴权服务脚本代码启动本地STS鉴权服务器。
     * STS 地址
     */
    public static final String STS_SERVER_URL = "http://****/sts/getsts";

    /**
     * 存储空间（Bucket）
     */
    public static final String BUCKET_NAME = "abauto";
    /**
     *
     */
    public static final String OSS_ACCESS_KEY_ID = "xxx";
    /**
     *
     */
    public static final String OSS_ACCESS_KEY_SECRET = "xxx";

    public static final int DOWNLOAD_SUC = 1;
    public static final int DOWNLOAD_FAIL = 2;
    public static final int UPLOAD_SUC = 3;
    public static final int UPLOAD_FAIL = 4;
    public static final int UPLOAD_PROGRESS = 5;
    public static final int LIST_SUC = 6;
    public static final int HEAD_SUC = 7;
    public static final int RESUMABLE_SUC = 8;
    public static final int SIGN_SUC = 9;
    public static final int BUCKET_SUC = 10;
    public static final int GET_STS_SUC = 11;
    public static final int MULTIPART_SUC = 12;
    public static final int STS_TOKEN_SUC = 13;
    public static final int FAIL = 9999;
    public static final int REQUESTCODE_AUTH = 10111;
    public static final int REQUESTCODE_LOCALPHOTOS = 10112;
}
