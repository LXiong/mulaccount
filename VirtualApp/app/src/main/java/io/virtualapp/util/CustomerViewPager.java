package io.virtualapp.util;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by lixi on 2015/9/23.
 */
public class CustomerViewPager extends ViewPager {

    private boolean noScroll = false;

    public void setNoScroll(boolean noScroll) {
        this.noScroll = noScroll;
    }

    public CustomerViewPager(Context context) {
        super(context);
    }

    public CustomerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (noScroll) {
            return false;
        } else {
            return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (noScroll) {
            return false;
        } else {
            return super.onInterceptTouchEvent(event);
        }
    }
}
