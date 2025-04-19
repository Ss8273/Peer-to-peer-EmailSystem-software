package com.app.emailsystem;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();  // 获取全局 Context
    }

    public static Context getAppContext() {
        return context;
    }
}
