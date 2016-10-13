package io.virtualapp.enter;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.android.client.AndroidSdk;
import com.android.client.SdkResultListener;
import com.ivy.module.locks.manager.IWidgetEvent;
import com.ivy.module.locks.manager.LockWidgetManager;
import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.proto.InstallResult;

import java.util.List;

import io.virtualapp.MyVCommends;
import io.virtualapp.R;
import io.virtualapp.VApp;
import io.virtualapp.abs.ui.VActivity;
import io.virtualapp.home.HomeActivity;
import io.virtualapp.home.LoadingActivity;
import io.virtualapp.home.models.AppModel;
import io.virtualapp.entity.DefaultJson;
import io.virtualapp.util.JsonParser;
import io.virtualapp.util.LogUtils;
import jonathanfinerty.once.Once;

public class EnterActivity extends VActivity {

    private static String TAG = "EnterActivity";

    private String shortCutPkg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.ivy_privacy_space_activity_enter);
        shortCutPkg = getIntent().getStringExtra("pkg");
        LogUtils.d(TAG, "shortCutPkg=" + shortCutPkg);
        if (TextUtils.isEmpty(shortCutPkg)) {
            AndroidSdk.onCreate(this, new AndroidSdk.Builder().setSdkResultListener(
                    new SdkResultListener() {
                        @Override
                        public void onInitialized() {

                        }

                        @Override
                        public void onReceiveServerExtra(String s) {
                            LogUtils.d(TAG, "s=" + s);
                            if ((!VirtualCore.get().isMainProcess())) {
                                return;
                            }
                            if (!TextUtils.isEmpty(s) && !TextUtils.equals("{}", s)) {
                                parseJson(s);
                            } else {
                                parseJson(AndroidSdk.getExtraData());
                            }

                        }

                        @Override
                        public void onReceiveNotificationData(String s) {

                        }

                    }

            ));
        }

        if (!isSetLockPwd()) {
            LockWidgetManager.Instance().setPasswordMode(LockWidgetManager.PasswordMode.PATTERN)
                    .launchAccountPasswordSetting(getApplicationContext(), "admin").addEventListener(IWidgetEvent.EventType.SET_LOCK_SUCCESS, new IWidgetEvent.SetLockSuccessEvent() {
                @Override
                public void onPasswordSuccess(android.app.Activity activity) {

                }

                @Override
                public void onPatternSuccess(android.app.Activity activity) {
                    HomeActivity.goHome(activity);
                    activity.finish();
                }
            });
        } else {
            LockWidgetManager.Instance().launch(LockWidgetManager.ViewType.UNLOCK_VIEW_ACTIVITY).addEventListener(IWidgetEvent.EventType.UN_LOCK_SUCCESS, new IWidgetEvent.UnLockSuccessEvent() {
                @Override
                public void onUnLockSuccess(android.app.Activity activity, String name) {
                    //Toast.makeText(EnterActivity.this, "unlock解锁成功", Toast.LENGTH_SHORT).show();
                    if (TextUtils.isEmpty(shortCutPkg)) {
                        HomeActivity.goHome(activity);
                    } else {
                        AppModel appModel = new AppModel();
                        appModel.packageName = shortCutPkg;
                        LoadingActivity.launch(activity, appModel, 0);
                    }
                    activity.finish();
                }

                @Override
                public void onUnLockSuccess(String s) {

                }

                @Override
                public void onScreenLockSuccess(String name) {

                }
            });
        }
        finish();
    }

    private void parseJson(String defaultJson) {

        // String defaultInstallPackage = Utility.readStringFromAssetFile(this, VCommends.FILE_DEFAULT_INSTALL_PACKAGE);
        LogUtils.d(TAG, "installGms()", defaultJson);
        if (TextUtils.isEmpty(defaultJson)) {
            LogUtils.d(TAG, "defaultInstallPackage is null");
            return;
        }
        DefaultJson dip = JsonParser.getInstance().fromJson(defaultJson, DefaultJson.class);

        if (dip == null) {
            LogUtils.d(TAG, "defaultInstallPackage is null");
            return;
        }

        VApp.ad = dip.ad;

        /*if (VApp.ad != null) {
            for (DefaultJson.AdBean adBean : VApp.ad) {
                if (adBean.data == null || !TextUtils.equals(adBean.type, DefaultJson.TYPE_CROSS)) {
                    continue;
                }
                for (DefaultJson.CrossAd crossAd : adBean.data) {
                    if (!TextUtils.equals(crossAd.type, DefaultJson.TYPE_CROSS) || TextUtils.isEmpty(crossAd.img)) {
                        continue;
                    }
                    RiseSdk.cacheUrl(crossAd.img);
                }
            }
        }*/
        if (!Once.beenDone(MyVCommends.TAG_INSTALL_PACKAGE)) {
            installDefaultPackage(dip.default_pkg);
            Once.markDone(MyVCommends.TAG_INSTALL_PACKAGE);
        }

    }

    private void installDefaultPackage(List<DefaultJson.DefaultPkgBean> dataBeanList) {
        if (dataBeanList == null || dataBeanList.isEmpty()) {
            LogUtils.d(TAG, "defaultInstallPackage is empty");
            return;
        }
        PackageManager pm = VirtualCore.get().getUnHookPackageManager();
        for (DefaultJson.DefaultPkgBean dataBean : dataBeanList) {
            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(dataBean.pkg, 0);
                String apkPath = appInfo.sourceDir;
                InstallResult res = VirtualCore.get().installApp(apkPath,
                        InstallStrategy.DEPEND_SYSTEM_IF_EXIST | InstallStrategy.TERMINATE_IF_EXIST);
                if (!res.isSuccess) {
                    LogUtils.d(TAG, "Unable to install app %s: %s.", appInfo.packageName, res.error);
                }
            } catch (Throwable e) {
                // Ignore
            }
        }
    }

    private boolean isSetLockPwd() {
        return !TextUtils.isEmpty(LockWidgetManager.Instance().getAccountPassword("admin"));
    }

}
