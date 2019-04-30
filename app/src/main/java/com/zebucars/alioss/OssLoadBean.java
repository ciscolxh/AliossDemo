package com.zebucars.alioss;

import com.alibaba.sdk.android.oss.OSS;

import java.io.File;

/**
 * @author 罗富清
 * @date 2019/4/29
 */
public class OssLoadBean {
    private OSS oss;
    private File file;

    public OssLoadBean(OSS oss, File file) {
        this.oss = oss;
        this.file = file;
    }

    public OSS getOss() {
        return oss;
    }

    public void setOss(OSS oss) {
        this.oss = oss;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
