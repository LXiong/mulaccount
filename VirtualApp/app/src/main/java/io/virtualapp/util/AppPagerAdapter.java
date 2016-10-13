package io.virtualapp.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Agg (2014) All Rights Reserved.
 * Created by Herbert Dai on 7/22/15.
 */

public class AppPagerAdapter extends FragmentPagerAdapter {

	ArrayList<Fragment> list;

	public String[] mTitles = null;

	public AppPagerAdapter(FragmentManager fm, ArrayList<Fragment> list) {
		super(fm);
		this.list = list;
	}

	public AppPagerAdapter(FragmentManager fm, ArrayList<Fragment> list, String[] titles) {
		super(fm);
		this.list = list;
		mTitles = titles;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Fragment getItem(int arg0) {
		return list.get(arg0);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		if (mTitles != null) {
			return mTitles[position];
		}
		return super.getPageTitle(position);
	}

	public void setTitles(String[] tabTitles) {
		mTitles = tabTitles;
	}
}

