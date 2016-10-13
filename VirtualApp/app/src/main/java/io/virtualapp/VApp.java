package io.virtualapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.ivy.module.locks.manager.LockWidgetManager;
import com.lody.virtual.client.core.VirtualCore;

import java.util.List;

import io.virtualapp.entity.DefaultJson;
import jonathanfinerty.once.Once;

/**
 * @author Lody
 */
public class VApp extends Application {

    private static final String TAG = "VApp";

    private static VApp gDefault;

    public static List<DefaultJson.AdBean> ad;

    public static VApp getApp() {
        return gDefault;
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            VirtualCore.get().startup(base);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        gDefault = this;
        super.onCreate();

        Log.d(TAG, "VApp oncreate");
        if (VirtualCore.get().isMainProcess()) {
            Log.d(TAG, "VApp oncreate ismain process");
            LockWidgetManager.Instance().init(this);
            Once.initialise(this);
        }
    }

}
