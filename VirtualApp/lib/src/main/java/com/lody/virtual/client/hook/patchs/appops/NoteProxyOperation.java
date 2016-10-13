package com.lody.virtual.client.hook.patchs.appops;

import android.app.AppOpsManager;

import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * Created by IVY on 2016/9/26.
 */

public class NoteProxyOperation extends ReplaceCallingPkgHook {
    public NoteProxyOperation(String name) {
        super(name);
    }

    @Override
    public boolean beforeCall(Object who, Method method, Object... args) {
        HookUtils.replaceFirstAppPkg(args);
        return super.beforeCall(who, method, args);
    }

    @Override
    public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
        return AppOpsManager.MODE_ALLOWED;
    }
}
