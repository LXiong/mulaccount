package io.virtualapp.util;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.privacy.common.Utils;

/**
 * Created by huale on 2015/2/3.
 */
public class FiveRateWidget extends FrameLayout implements View.OnClickListener {
    public static final int MATCH_PARENT = WindowManager.LayoutParams.MATCH_PARENT;
    public static final int WRAP_CONTENT = WindowManager.LayoutParams.WRAP_CONTENT;

    public static final int LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    public static final int PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    public static final int BEHIND = ActivityInfo.SCREEN_ORIENTATION_BEHIND;


    WindowManager.LayoutParams lp;
    boolean movable;
    int screenHeight;

    public FiveRateWidget(Context context, int width, int height, int orientation) {
        this(context, Gravity.NO_GRAVITY, orientation, width, height, false);
    }

    public FiveRateWidget(Context context, int gravity, int width, int height, boolean movable) {
        this(context, gravity, ActivityInfo.SCREEN_ORIENTATION_BEHIND, width, height, movable);
    }

    public FiveRateWidget(Context context, int gravity, int orientation, int width, int height, boolean movable) {
        super(context);
        this.movable = movable;
        int type = Build.VERSION.SDK_INT >= 19 ? WindowManager.LayoutParams.TYPE_TOAST : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        int flag = Build.VERSION.SDK_INT >= 19 ? (WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR) : 0;
        lp = new WindowManager.LayoutParams(
                width, height, type, flag,
                PixelFormat.TRANSLUCENT);
        lp.gravity = gravity;
        lp.screenOrientation = orientation;

        wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point size = Utils.getScreenSize(context);
        screenHeight = size.y;

        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (listener != null) {
            listener.onClick();
        }
    }

    public interface IWidgetListener {
        boolean onBackPressed();

        boolean onMenuPressed();

        void onClick();
    }

    public boolean isShowing() {
        return added;
    }

    private boolean added = false;

    public void addToWindow() {
        if (!added && wm != null) {
            try {
                wm.addView(this, lp);
                added = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeFromWindow() {
        if (added && wm != null) {
            try {
                added = false;
                wm.removeView(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private IWidgetListener listener;

    public void setWidgetListener(IWidgetListener listener) {
        this.listener = listener;
    }

    private WindowManager wm;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (this.listener != null) {
                        if (this.listener.onBackPressed()) return true;
                    }
                }
                break;

            case KeyEvent.KEYCODE_MENU:
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (this.listener != null) {
                        if (this.listener.onMenuPressed()) return true;
                    }
                }
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return movable;
    }

    private float x, y;
    private boolean moving = false;

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (this.movable) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP) {
                if (!moving && this.listener != null) {
                    listener.onClick();
                    return true;
                }
            } else if (action == MotionEvent.ACTION_DOWN) {
                x = event.getRawX();
                y = event.getRawY();
                moving = false;
                return true;
            } else if (action == MotionEvent.ACTION_MOVE) {
                float dx = event.getRawX() - x;
                float dy = event.getRawY() - y;
                if (Math.abs(dx) > 20 || Math.abs(dy) > 20) {
                    moving = true;
                    x = event.getRawX();
                    y = event.getRawY();
                    lp.x = (int) x - getWidth() / 2;
                    lp.y = screenHeight - (int) y - getHeight() / 2;
                    wm.updateViewLayout(this, lp);
                }
            }
            return super.onTouchEvent(event);
        } else {
            return super.onTouchEvent(event);
        }
    }
}
