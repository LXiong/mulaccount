package io.virtualapp.util;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.content.PermissionChecker;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import io.virtualapp.R;
import io.virtualapp.enter.EnterActivity;
import io.virtualapp.home.LoadingActivity;

import com.lody.virtual.client.env.Constants;

public class Utility {
    private static final String THIS_DIR = "AGG";
    private static Gson gson = new Gson();

    public static String getLogTag() {
        return Utility.class.getSimpleName();
    }

    public static boolean hasSdcard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 格式化浮点数
     *
     * @param doubleString 浮点数字符串
     * @param accuracy     保留几位小数
     * @return 格式化后的字符串
     */
    public static String formatDouble(String doubleString, int accuracy) {
        double value = 0f;
        try {
            value = Double.parseDouble(doubleString);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return formatDouble(value, accuracy);
    }

    /**
     * 格式化浮点数
     *
     * @param doubleValue 浮点数值
     * @param accuracy    保留几位小数
     * @return 格式化后的字符串
     */
    public static String formatDouble(double doubleValue, int accuracy) {
        String format = "0";
        if (accuracy > 0) {
            StringBuilder decimalPlaceHolder = new StringBuilder();
            for (int i = 1; i <= accuracy; i++) {
                decimalPlaceHolder.append("0");
            }
            format = "0." + decimalPlaceHolder.toString();
        }
        DecimalFormat decimalFormat = new DecimalFormat(format);
        String formatString = decimalFormat.format(doubleValue);
        return formatString;
    }

    public static boolean restrictPasswordInput(Context context, Editable s) {
        String inputString = s.toString();
        if (inputString.endsWith(" ")) {
            s.delete(inputString.length() - 1, inputString.length());
            return true;
        } else {
            return false;
        }
    }

    /**
     * 限制价格输入
     *
     * @param s
     * @return true表示对Editable处理过了
     */
    public static boolean restrictPriceInput(Editable s) {
        String inputString = s.toString().trim();
        // 限制第一个数字不能输入0，只能输入0.
        if (inputString.startsWith("0") && inputString.length() >= 2) {
            if ('.' != inputString.charAt(1)) {
                s.delete(0, 1);
                return true;
            }
        }
        // 限制小数点后只能输入两位
        if (inputString.contains(".")
                && inputString.length() - 1 - inputString.indexOf(".") > 2) {
            s.delete(inputString.length() - 1, inputString.length());
            return true;
        }
        return false;
    }

    public static Gson getGson() {
        return gson;
    }


    public static String getStrFromByte(byte[] responseBody) {
        String response = null;
        if (responseBody != null) {
            try {
                response = new String(responseBody, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    public static boolean isNetworkConnected(Context context) {
        boolean result = false;
        int ansPermission = context
                .checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE);
        int internetPermission = context
                .checkCallingOrSelfPermission(Manifest.permission.INTERNET);
        if (ansPermission == PackageManager.PERMISSION_GRANTED
                && internetPermission == PackageManager.PERMISSION_GRANTED) {
            if (context != null) {
                ConnectivityManager cm = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = cm.getActiveNetworkInfo();
                if (networkInfo != null) {
                    int type = networkInfo.getType();
                    switch (type) {
                        case ConnectivityManager.TYPE_MOBILE:
                        case ConnectivityManager.TYPE_WIFI:
                            if (networkInfo.isAvailable()
                                    && networkInfo.isConnected()) {
                                result = true;
                            }
                            break;
                    }
                }
            }
        }
        return result;
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static final boolean isGpsOpen(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = false;
        boolean network = false;
        int locatePermission = context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
        int internetPermission = context.checkCallingOrSelfPermission(Manifest.permission.INTERNET);
        if (locatePermission == PackageManager.PERMISSION_GRANTED) {
            // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
            try {
                gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (internetPermission == PackageManager.PERMISSION_GRANTED) {
            // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
            try {
                network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return gps || network;
    }

    public static void setGPS(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // The Android SDK doc says that the location settings activity
            // may not be found. In that case show the general settings.

            // General settings activity
            intent.setAction(Settings.ACTION_SETTINGS);
            try {
                context.startActivity(intent);
            } catch (Exception e) {
            }
        }
    }

    public static void goToApplicationInfoPage(Context context, String packageName) {
        boolean isActivityContext = context instanceof Activity;
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + packageName));
            if (isActivityContext) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
            if (isActivityContext) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(intent);
        }
    }

    public static int getTargetSDKVersion(Context context) {
        int targetSdkVersion = -1;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            targetSdkVersion = packageInfo.applicationInfo.targetSdkVersion;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return targetSdkVersion;
    }

    public static boolean selfPermissionGranted(Context context, String permission) {
        // For Android < Android M, self permissions are always granted.
        boolean result = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int targetSdkVersion = getTargetSDKVersion(context);
            if (targetSdkVersion >= Build.VERSION_CODES.M) {
                // targetSdkVersion >= Android M, we can
                // use Context#checkSelfPermission
                result = context.checkSelfPermission(permission)
                        == PackageManager.PERMISSION_GRANTED;
            } else {
                // targetSdkVersion < Android M, we have to use PermissionChecker
                result = PermissionChecker.checkSelfPermission(context, permission)
                        == PermissionChecker.PERMISSION_GRANTED;
            }
        }
        return result;
    }

    public static String readStringFromApplicationFile(Context context, String fileName) {
        try {
            FileInputStream inputStream = context.openFileInput(fileName);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readStringFromAssetFile(Context context, String assetPath) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(context.getAssets().open(assetPath), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            inputStreamReader.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean wirteJsonToFile(Context context, String jsonData, String filePath) {
        try {
            FileOutputStream fileOutputStream = context.openFileOutput(filePath, Context.MODE_PRIVATE);
            fileOutputStream.write(jsonData.getBytes());
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 获取屏幕尺寸与密度.
     *
     * @param context the context
     * @return mDisplayMetrics
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        Resources mResources;
        if (context == null) {
            mResources = Resources.getSystem();

        } else {
            mResources = context.getResources();
        }
        //DisplayMetrics{density=1.5, width=480, height=854, scaledDensity=1.5, xdpi=160.421, ydpi=159.497}
        //DisplayMetrics{density=2.0, width=720, height=1280, scaledDensity=2.0, xdpi=160.42105, ydpi=160.15764}
        DisplayMetrics mDisplayMetrics = mResources.getDisplayMetrics();
        return mDisplayMetrics;
    }

    public static String getVersionName(Context mContext) {
        String version = "";
        PackageInfo pInfo;
        try {
            pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            version = pInfo.versionName + "(" + pInfo.versionCode + ")";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (version != null && !version.contains("v")) {
            version = "v" + version;
        }
        return version;
    }

    public static final int getVersionCode(Context context) {
        int verCode = 0;
        try {
            PackageInfo appInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            verCode = appInfo.versionCode;
        } catch (Exception e) {
        }
        return verCode;
    }

    /**
     * bitmap转为base64
     *
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] arr = baos.toByteArray();
        String result = Base64.encodeToString(arr, Base64.DEFAULT);
        return result;
    }

    /**
     * 距离数值转换为字符串
     *
     * @param distance 距离，单位米
     * @return 距离字符串
     */
    public static String formatDistance(long distance) {
        String result = null;
        if (distance < 100) {
            result = "<100m";
        } else if (distance < 500) {
            result = "<500m";
        } else {
            result = String.format("%.1fkm", distance * 1.0 / 1000);
        }
        return result;
    }

    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static String getRawVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        String versionName = "";
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }


    public static void createAppShortcut(Application appContext) {
        //创建快捷方式的Intent
        Intent shortcutIntent = new Intent(Constants.ACTION_INSTALL_SHORTCUT);
        //不允许重复创建
        shortcutIntent.putExtra("duplicate", false);
        //需要现实的名称
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appContext.getString(R.string.ivy_privacy_space_add_app));
        //快捷图片
        Parcelable icon = Intent.ShortcutIconResource.fromContext(appContext, R.drawable.ivy_privacy_space_launcher);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

        //点击快捷图片，运行的程序主入口
        Intent launcherIntent = new Intent(appContext, EnterActivity.class);
        launcherIntent.setAction(Intent.ACTION_MAIN);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);
        //发送广播
        appContext.sendBroadcast(shortcutIntent);
    }


    public static int parseInt(String intString, int defaultValue) {
        int value = defaultValue;
        try {
            value = Integer.parseInt(intString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static long parseLong(String longString, long defaultValue) {
        long value = defaultValue;
        try {
            value = Long.parseLong(longString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static float parseFloat(String floatString, float defaultValue) {
        float value = defaultValue;
        try {
            value = Float.parseFloat(floatString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static double parseDouble(String floatString, double defaultValue) {
        double value = defaultValue;
        try {
            value = Double.parseDouble(floatString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String formatInterval(final long intervalInMills) {
        final long hr = TimeUnit.MILLISECONDS.toHours(intervalInMills);
        final long min = TimeUnit.MILLISECONDS.toMinutes(intervalInMills - TimeUnit.HOURS.toMillis(hr));
        final long sec = TimeUnit.MILLISECONDS.toSeconds(intervalInMills - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
        final long ms = TimeUnit.MILLISECONDS.toMillis(intervalInMills - TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min) - TimeUnit.SECONDS.toMillis(sec));
        return String.format("%02d:%02d:%02d.%03d", hr, min, sec, ms);
    }

    public static String formatMinuteAndSeconds(final long intervalInMills) {
        final long min = TimeUnit.MILLISECONDS.toMinutes(intervalInMills);
        final long sec = TimeUnit.MILLISECONDS.toSeconds(intervalInMills - TimeUnit.MINUTES.toMillis(min));
        return String.format("%02d:%02d", min, sec);
    }

    public static String filterPhoneNumber(String phone) {
        if (!TextUtils.isEmpty(phone)) {
            phone = phone.replaceAll(" ", "");
            phone = phone.replaceAll("-", "");
            phone = phone.replaceAll("\\+86", "");
        }
        return phone;
    }


    public static Bitmap getCircleBitmap(Bitmap bmp, int destWidth, int destHeight) {
        // 切割成圆形
        Bitmap bitmap = Bitmap.createScaledBitmap(bmp, destWidth, destHeight, false);
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.TRANSPARENT);
        paint.setColor(0xff424242);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static void loadImg(Context context, String url, ImageView view, int defaultImg) {
        loadImg(context, url, defaultImg, view);
    }

    public static void loadImg(Context context, String url, int placeholder, ImageView view) {
        loadImg(context, url, placeholder, view, null);
    }

    public static void loadImg(Context context, String url, int placeholder, ImageView view, com.squareup.picasso.Callback callback) {
        if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(url.trim())) {
            Picasso.with(context).load(url).placeholder(placeholder).fit().config(Bitmap.Config.RGB_565).into(view, callback);
        } else {
            view.setImageResource(placeholder);
        }
    }
}