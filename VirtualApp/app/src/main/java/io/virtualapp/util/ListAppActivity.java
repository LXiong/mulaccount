package io.virtualapp.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;

import java.util.ArrayList;

import io.virtualapp.R;
import io.virtualapp.VCommends;
import io.virtualapp.abs.ui.VActivity;
import io.virtualapp.home.ListAppContract;
import io.virtualapp.home.ListAppFragment;

/**
 * @author Lody
 */
public class ListAppActivity extends VActivity {

    private CustomerViewPager mViewPager;
    private PagerSlidingTabStrip mPagerTabStrip;

    public static void gotoListApp(Activity activity) {
        Intent intent = new Intent(activity, ListAppActivity.class);
        activity.startActivityForResult(intent, VCommends.REQUEST_SELECT_APP);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ivy_privacy_space_activity_list_app);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        final ArrayList<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(ListAppFragment.newInstance(ListAppContract.SELECT_APP_FROM_SYSTEM));
        // fragmentList.add(ListAppFragment.newInstance(ListAppContract.SELECT_APP_FROM_SD_CARD));

        String[] tabTitles = {"system"};
        mViewPager = (CustomerViewPager) findViewById(R.id.app_list_pager);
        mPagerTabStrip = (PagerSlidingTabStrip) findViewById(R.id.app_pager_tap_strip);
        mPagerTabStrip.setTextColor(Color.WHITE);
        mPagerTabStrip.setVisibility(View.GONE);

        AppPagerAdapter appPagerAdapter = new AppPagerAdapter(getSupportFragmentManager(), fragmentList, tabTitles);
        mViewPager.setAdapter(appPagerAdapter);
        mViewPager.setOffscreenPageLimit(appPagerAdapter.getCount());

        mPagerTabStrip.setViewPager(mViewPager);
        mPagerTabStrip.setTypeface(null, Typeface.NORMAL);
    }

}
