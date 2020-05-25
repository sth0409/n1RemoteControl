package com.n1.RemoteControl;

import android.app.Application;

import com.zhouyou.http.EasyHttp;

/**
 * 版权：易金 版权所有
 * <p>
 * 作者：suntinghui
 * <p>
 * 创建日期：2020/5/25 0025 10:54
 * <p>
 * 描述：
 * <p>
 * 修订历史：
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        EasyHttp.init(this);//默认初始化
    }
}
