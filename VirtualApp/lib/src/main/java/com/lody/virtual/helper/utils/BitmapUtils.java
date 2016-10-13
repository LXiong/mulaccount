package com.lody.virtual.helper.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import android.graphics.drawable.Drawable;

import com.lody.virtual.R;

/**
 * @author Lody
 */
public class BitmapUtils {

 /*   public static Bitmap drawableToBitMap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = ((BitmapDrawable) drawable);
            return bitmapDrawable.getBitmap();
        } else {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);

            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }*/

    public static Bitmap drawableToBitMap(Resources resources, Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.ivy_privacy_space_shortcut_bg);
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight());
        Bitmap bitmap2 = bitmap1.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap2);
        drawable.setBounds(12, 12, bitmap.getWidth() - 12, bitmap.getHeight() - 12);
        drawable.draw(canvas);
        Bitmap shortcut_left_img = BitmapFactory.decodeResource(resources, R.drawable.ivy_privacy_space_shortcut_left_img);
        canvas.drawBitmap(shortcut_left_img, 3, bitmap2.getHeight() - shortcut_left_img.getHeight(), new Paint());
        return bitmap2;
    }

}
