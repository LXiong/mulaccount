package com.lody.virtual.client.hook.patchs.am;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.TypedValue;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.local.ActivityClientRecord;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.helper.compat.ObjectsCompat;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class StartActivity extends BaseStartActivity {

    @Override
    public String getName() {
        return "startActivity";
    }


    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        super.call(who, method, args);
        VLog.d("rqy", "who=%s,method.getName=%s", who, method.getName());
        for (int i = 0; i < args.length; i++) {
            VLog.d("rqy", "args[%d]=%s", i, args[i]);
        }
        int intentIndex = ArrayUtils.indexOfFirst(args, Intent.class);
        VLog.d("rqy", "intentIndex=%d", intentIndex);
        if (intentIndex == -1) {
            return method.invoke(who, args);
        }
        int resultToIndex = ArrayUtils.indexOfObject(args, IBinder.class, 2);

        VLog.d("rqy", "resultToIndex=%d", resultToIndex);
        String resolvedType = (String) args[intentIndex + 1];
        Intent intent = (Intent) args[intentIndex];
        intent.setDataAndType(intent.getData(), resolvedType);
        IBinder resultTo = resultToIndex >= 0 ? (IBinder) args[resultToIndex] : null;
        int userId = VUserHandle.myUserId();

        if (ComponentUtils.isStubComponent(intent)) {
            return method.invoke(who, args);
        }
        ActivityInfo activityInfo = VirtualCore.get().resolveActivityInfo(intent, userId);
        if (activityInfo == null) {
            return method.invoke(who, args);
        }
        String resultWho = null;
        int requestCode = 0;
        Bundle options = ArrayUtils.getFirst(args, Bundle.class);
        if (resultTo != null) {
            resultWho = (String) args[resultToIndex + 1];
            requestCode = (int) args[resultToIndex + 2];
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            args[intentIndex - 1] = getHostPkg();
        }

        int res = VActivityManager.get().startActivity(intent, activityInfo, resultTo, options, resultWho, requestCode, VUserHandle.myUserId());
        if (res != 0 && resultTo != null && requestCode > 0) {
            VActivityManager.get().sendActivityResult(resultTo, resultWho, requestCode);
        }
        if (resultTo != null) {
            ActivityClientRecord r = VActivityManager.get().getActivityRecord(resultTo);
            if (r != null && r.activity != null) {
                try {
                    TypedValue out = new TypedValue();
                    Resources.Theme theme = r.activity.getResources().newTheme();
                    theme.applyStyle(activityInfo.getThemeResource(), true);
                    if (theme.resolveAttribute(android.R.attr.windowAnimationStyle, out, true)) {

                        TypedArray array = theme.obtainStyledAttributes(out.data,
                                new int[]{
                                        android.R.attr.activityOpenEnterAnimation,
                                        android.R.attr.activityOpenExitAnimation
                                });

                        r.activity.overridePendingTransition(array.getResourceId(0, 0), array.getResourceId(1, 0));
                        array.recycle();
                    }
                } catch (Throwable e) {
                    // Ignore
                }
            }
        }
        return res;
    }

}
