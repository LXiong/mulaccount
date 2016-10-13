package io.virtualapp.home;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.client.AndroidSdk;
import com.ivy.module.locks.manager.IWidgetEvent;
import com.ivy.module.locks.manager.LockWidgetManager;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.helper.proto.AppSetting;
import com.lody.virtual.helper.utils.BitmapUtils;
import com.lody.virtual.os.VUserHandle;

import java.util.ArrayList;
import java.util.List;

import io.virtualapp.MyVCommends;
import io.virtualapp.R;
import io.virtualapp.VApp;
import io.virtualapp.VCommends;
import io.virtualapp.abs.ui.VActivity;
import io.virtualapp.abs.ui.VUiKit;
import io.virtualapp.effects.ExplosionField;
import io.virtualapp.enter.EnterActivity;
import io.virtualapp.entity.DefaultJson;
import io.virtualapp.home.models.AppModel;
import io.virtualapp.util.FiveRateWidget;
import io.virtualapp.util.LaunchpadAdapter;
import io.virtualapp.util.LogUtils;
import io.virtualapp.util.ShowDialogPresenter;
import io.virtualapp.widgets.PagerView;
import jonathanfinerty.once.Once;

/*import android.widget.ListView;
import android.widget.PopupWindow;*/

/**
 * @author Lody
 */
public class HomeActivity extends VActivity implements HomeContract.HomeView {

    private int[] location1;
    private static final String TAG = "HomeActivity";
    private HomeContract.HomePresenter mPresenter;
    private ProgressBar mLoadingBar;
    private PagerView mPagerView;
    private ImageView mAppFab;
    private ImageView mCrashFab;
    private ImageView mCreateShortCutFab;
    private ExplosionField mExplosionField;
    private LaunchpadAdapter mAdapter;
    private View layout_home_fab;
    private LinearLayout tianzhuan;

    /*private PopupWindow popupWindow;
    private ListView lv_group;*/
    private boolean isOpenAd;

    private InstallerReceiver installerReceiver = new InstallerReceiver();

    private final BroadcastReceiver homeReceiver = new BroadcastReceiver() {
        final String SYS_KEY = "reason"; //标注下这里必须是这么一个字符串值

        final String SYS_HOME_KEY = "homekey";//标注下这里必须是这么一个字符串值

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYS_KEY);
                LogUtils.d("rqy", "reason =" + reason);
                if (TextUtils.equals(reason, SYS_HOME_KEY)) {
                    LogUtils.d("rqy", "home key pressed");
                    finish();
                }
            }
        }
    };

    public static void goHome(Context context) {
        if (context == null)
            return;
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ivy_privacy_space_activity_home);
        AndroidSdk.onCreate(this);
        mLoadingBar = (ProgressBar) findViewById(R.id.pb_loading_app);
        mPagerView = (PagerView) findViewById(R.id.home_launcher);
        View bottom_layout = findViewById(R.id.bottom_layout);
        mAppFab = (ImageView) findViewById(R.id.home_fab);
        mCrashFab = (ImageView) findViewById(R.id.ivy_privacy_space_home_del);
        layout_home_fab = findViewById(R.id.layout_home_fab);
        mCreateShortCutFab = (ImageView) findViewById(R.id.home_create_shortcut);
        tianzhuan = (LinearLayout) findViewById(R.id.tiaozhuan);
        mAdapter = new LaunchpadAdapter(this);
        mPagerView.setAdapter(mAdapter);
        new HomePresenterImpl(this, this);
        mPresenter.start();
        mAppFab.setOnClickListener(v -> mPresenter.wantAddApp());
        mExplosionField = ExplosionField.attachToWindow(this);
        mPagerView.setOnDragChangeListener(mPresenter::dragChange);
        mPagerView.setOnEnterCrashListener(mPresenter::dragNearCrash);
        mPagerView.setOnEnterCreateShortListener(mPresenter::dragNearCreateShortCut);
        mPagerView.setOnCrashItemListener(position -> {
            AppModel model = mAdapter.getItem(position);
            View v = mPagerView.getChildAt(position);
            mExplosionField.explode(v, null);
            mPresenter.deleteApp(model);
        });

        mPagerView.setOnCreateShortListener(position -> {
            AppModel model = mAdapter.getItem(position);
            createShortcut(model);
        });
      /*  mPagerView.setOnItemClickListener((item, pos) -> {

            new AlertDialog.Builder(this)
                    .setTitle("Choose an User")
                    .setItems(getUsers(), (dialog, userId)
                            -> mPresenter.launchApp((AppModel) item, userId))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();

        });*/

        mCrashFab.post(() -> {
            int[] location = new int[2];
            bottom_layout.getLocationInWindow(location);
            LogUtils.d(TAG, "location[0]=" + location[0] + "--location[1]=" + location[1]);
            mPagerView.setBottomLine(location[1]);
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int widthPixels = dm.widthPixels;
            float density = dm.density;
            int screenWidth = (int) (widthPixels * density);
            mPagerView.setScreenWidth(screenWidth);
            LogUtils.d(TAG, "widthPixels=" + widthPixels + "--screenWidth=" + screenWidth);
            location1 = new int[2];
            mAppFab.getLocationInWindow(location1);
            LogUtils.d(TAG, "location[1]=" + location1[0] + "--location[1]=" + location1[1]);
        });

        mPagerView.setOnItemClickListener((item, pos) ->

                mPresenter.launchApp((AppModel) item, 0)

        );
        findViewById(R.id.user_icon).setOnClickListener(v -> LockWidgetManager.Instance().setPasswordMode(LockWidgetManager.PasswordMode.PATTERN)
                .launchAccountPasswordSetting(getApplicationContext(), "admin").addEventListener(IWidgetEvent.EventType.SET_LOCK_SUCCESS, new IWidgetEvent.SetLockSuccessEvent() {
                    @Override
                    public void onPasswordSuccess(android.app.Activity activity) {

                    }

                    @Override
                    public void onPatternSuccess(android.app.Activity activity) {
                        activity.finish();
                    }
                }));


        if (!Once.beenDone(MyVCommends.TAG_SHOULD_COMMENTS)) {
            View view = findViewById(R.id.layout_comments);
            view.setVisibility(View.VISIBLE);
            initHeadView(view);
        }

        registerInstallerReceiver();

        if (!Once.beenDone(MyVCommends.FIRST_RUN_HOMEACTIVITY)) {
            Once.markDone(MyVCommends.FIRST_RUN_HOMEACTIVITY);
        } else if (!Once.beenDone(MyVCommends.SECOND_RUN_HOMEACTIVITY)) {
            Once.markDone(MyVCommends.SECOND_RUN_HOMEACTIVITY);
            ShowDialogPresenter.showDialog(this);
        }

        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(homeReceiver, homeFilter);
    }

    private void createShortcut(AppModel model) {
        Intent shortcut = new Intent(Constants.ACTION_INSTALL_SHORTCUT);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, model.name + "+");
        shortcut.putExtra("duplicate", false);//设置是否重复创建
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.putExtra("pkg", model.packageName);
        intent.setClassName(getPackageName(), EnterActivity.class.getName());
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);

        Bitmap bitmap = BitmapUtils.drawableToBitMap(getResources(), model.icon);
        if (bitmap != null) {
            shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
        }
        sendBroadcast(shortcut);
    }

    public boolean isViewVisible(View view) {
        return view.getVisibility() == View.VISIBLE;
    }

    public int getCommentsLayoutWidth() {
        View view = findViewById(R.id.layout_comments);
        return isViewVisible(view) ? view.getHeight() : 0;
    }

    private void initHeadView(final View headerView) {

        headerView.findViewById(R.id.rate_bad).setOnClickListener(v -> {
            Once.markDone(MyVCommends.TAG_SHOULD_COMMENTS);
            ShowDialogPresenter.showComplainDialog(HomeActivity.this);
            headerView.setVisibility(View.GONE);
        });

        headerView.findViewById(R.id.rate_good).setOnClickListener(v -> {
            /*share.setFiveRate(true);
            RiseSdk.track(TrackEvent.CATEGORY_APP,
                    TrackEvent.CATEGORY_RATE_GOOD, "", 1);
            App.getSharedPreferences().edit().putBoolean("five_r", true).apply();*/
            if (com.privacy.common.Utils.hasPlayStore(HomeActivity.this)) {
                com.privacy.common.Utils.rateUs(v.getContext());
            }
            Once.markDone(MyVCommends.TAG_SHOULD_COMMENTS);
            View alertDialogView = View.inflate(v.getContext(), R.layout.scan_result_rate_five, null);

            final FiveRateWidget w = new FiveRateWidget(v.getContext(), FiveRateWidget.MATCH_PARENT, FiveRateWidget.MATCH_PARENT, FiveRateWidget.PORTRAIT);
            w.addView(alertDialogView);
            w.addToWindow();

            w.setOnClickListener(v1 -> {
                w.removeAllViews();
                w.removeFromWindow();

            });
            headerView.setVisibility(View.GONE);
        });

        headerView.findViewById(R.id.rate_close).setOnClickListener(v -> {
            Once.markDone(MyVCommends.TAG_SHOULD_COMMENTS);
            headerView.setVisibility(View.GONE);
        });
    }

    /*private void showWindow(View v) {
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(R.layout.ivy_privacy_space_group_list, null);

            lv_group = (ListView) view.findViewById(R.id.lvGroup);
            lv_group.setVerticalScrollBarEnabled(false);
            // 加载数据
            ArrayList<String> groups = new ArrayList<>();
            groups.add(getString(R.string.ivy_privacy_space_add_password));
            //groups.add(getString(R.string.add_password));
            GroupAdapter groupAdapter = new GroupAdapter(this, groups);
            lv_group.setAdapter(groupAdapter);
            // 创建一个PopuWidow对象
            popupWindow = new PopupWindow(view, 400, 120 * (groups.size()));
        }
        // 使其聚集
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(true);

        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        // WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        int xPos = -popupWindow.getWidth() + v.getWidth() / 2;
        popupWindow.showAsDropDown(v, xPos, 45);
        lv_group.setOnItemClickListener((adapterView, view, i, l) -> {
            popupWindow.dismiss();
            if (i == 0) {

            }
        });
    }*/

    public void registerInstallerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_PACKAGE_ADDED);
        filter.addAction(Constants.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        registerReceiver(installerReceiver, filter);
    }

    public void unregisterInstallerReceiver() {
        unregisterReceiver(installerReceiver);
    }

    @Override
    protected void onResume() {
        LogUtils.d("rqy", "onResume");
        super.onResume();
        AndroidSdk.onResume(this);
        mAdapter.onResume();
        if (!isOpenAd) {
            AndroidSdk.showFullAd(AndroidSdk.FULL_TAG_START);
            isOpenAd = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        AndroidSdk.onPause();
        mAdapter.onPause();
    }

    @Override
    public void setPresenter(HomeContract.HomePresenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showLoading() {
        mLoadingBar.setVisibility(View.VISIBLE);
        mPagerView.setVisibility(View.GONE);
    }

    @Override
    public void loadFinish(List<AppModel> appModels) {
        //add by renqingyou for ad start
        appModels = reBuildAppModels(appModels);
        //add by renqingyou for ad end
        mAdapter.setModels(appModels);
        mPagerView.refreshView();
        hideLoading();
        int mPage = mPagerView.getTotalPage();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = 6;
        layoutParams.rightMargin = 6;
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        tianzhuan.removeAllViews();
        for (int i = 0; i < mPage; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(layoutParams);
            imageView.setImageResource(i == 0 ? R.drawable.ivy_privacy_space_tiao_zhuan_selected : R.drawable.ivy_privacy_space_tiao_zhuan);
            tianzhuan.addView(imageView);
        }
        mPagerView.setOnPageChangedListener((oldPage, newPage) -> {
            LogUtils.d("rqy", "oldPage--" + oldPage + "--new Page=" + newPage);
            ImageView imageView = (ImageView) tianzhuan.getChildAt(newPage);
            imageView.setImageResource(R.drawable.ivy_privacy_space_tiao_zhuan_selected);
            if (newPage != 0) {
                ImageView oldImageView = (ImageView) tianzhuan.getChildAt(0);
                oldImageView.setImageResource(R.drawable.ivy_privacy_space_tiao_zhuan);
            }
            if (oldPage != 0 && oldPage < tianzhuan.getChildCount()) {
                ImageView oldImageView = (ImageView) tianzhuan.getChildAt(oldPage);
                oldImageView.setImageResource(R.drawable.ivy_privacy_space_tiao_zhuan);
            }
        });
    }

    private List<AppModel> reBuildAppModels(List<AppModel> appModels) {
        List<AppModel> adList = new ArrayList<>();
        if (VApp.ad != null) {
            for (DefaultJson.AdBean adBean : VApp.ad) {
                AppModel adModel = new AppModel();
                adModel.isAd = true;
                //adModel.icon = getResources().getDrawable(R.drawable.ivy_privacy_space_launcher);
                adModel.AdType = adBean.type;
                adModel.imgUrl = adBean.img;
                adModel.name = adBean.title;
                adModel.crossAdData = adBean.data;
                adModel.interval_time = adBean.interval_time;
                adList.add(adModel);
            }
        }
        if (adList.isEmpty()) {
            return appModels;
        }
        List<AppModel> subList1 = new ArrayList<>();
        List<AppModel> subList2 = new ArrayList<>();

        int maxCountInFirstPage = 9 - adList.size();
        if (maxCountInFirstPage < 0) {
            // TODO: 2016/10/9  
            LogUtils.e(TAG, "adList size is >9");
            maxCountInFirstPage = 0;
        }
        if (appModels.size() > maxCountInFirstPage) {
            for (int i = 0; i < maxCountInFirstPage; i++) {
                subList1.add(appModels.get(i));
            }
            for (int i = maxCountInFirstPage; i < appModels.size(); i++) {
                subList2.add(appModels.get(i));
            }
            subList1.addAll(adList);
            subList1.addAll(subList2);
        } else {
            subList1 = appModels;
            subList1.addAll(adList);
        }
        return subList1;
    }

    @Override
    public void loadError(Throwable err) {
        hideLoading();
    }

    @Override
    public void showGuide() {
        /*new MaterialShowcaseView.Builder(this).setTarget(mAppFab).setDelay(700)
                .setContentText("Click this button to add an App ~").setDismissText("Got it")
                .setDismissTextColor(Color.parseColor("#03a9f4")).show();*/
    }

    @Override
    public void showFab() {
       /* mAppFab.show();
        mCrashFab.hide();*/
        AnimationSet animationSet = new AnimationSet(true);
        Animation mCrashFabAnimation = new TranslateAnimation(0, -location1[0] / 2, 0, 0);
        mCrashFabAnimation.setFillAfter(false);
        mCrashFabAnimation.setDuration(300);
        mCrashFabAnimation.setInterpolator(new DecelerateInterpolator());
        mCrashFabAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCrashFab.clearAnimation();
                mCrashFab.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        Animation anim = new AlphaAnimation(1.0f, 0.1f);
        anim.setDuration(200);

        animationSet.addAnimation(mCrashFabAnimation);
        animationSet.addAnimation(anim);


        AnimationSet animationSet1 = new AnimationSet(true);
        Animation mCreateShortCutAnimation = new TranslateAnimation(0, location1[0] / 2, 0, 0);
        mCreateShortCutAnimation.setFillAfter(false);
        mCreateShortCutAnimation.setDuration(300);
        mCreateShortCutAnimation.setInterpolator(new DecelerateInterpolator());
        mCreateShortCutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mCreateShortCutFab.clearAnimation();
                mCreateShortCutFab.setVisibility(View.GONE);

                Animation anim = new AlphaAnimation(0.1f, 1.0f);
                anim.setDuration(500);
                layout_home_fab.setVisibility(View.VISIBLE);
                layout_home_fab.startAnimation(anim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        Animation anim1 = new AlphaAnimation(1.0f, 0.1f);
        anim.setDuration(200);

        animationSet1.addAnimation(mCreateShortCutAnimation);
        animationSet1.addAnimation(anim1);

        mCrashFab.startAnimation(animationSet);
        mCreateShortCutFab.startAnimation(animationSet1);
    }

    @Override
    public void hideFab() {
        /*mAppFab.hide();
        mCrashFab.setVisibility(View.VISIBLE);
        mCrashFab.show();*/
        layout_home_fab.setVisibility(View.GONE);

        AnimationSet animationSet = new AnimationSet(true);
        Animation mCreateShortCutAnimation = new TranslateAnimation(location1[0] / 2, 0, 0, 0);

        mCreateShortCutAnimation.setFillAfter(false);
        mCreateShortCutAnimation.setInterpolator(new DecelerateInterpolator());
        mCreateShortCutAnimation.setDuration(300);
        animationSet.addAnimation(mCreateShortCutAnimation);

        Animation anim = new AlphaAnimation(0.1f, 1.0f);
        anim.setDuration(200);
        animationSet.addAnimation(anim);

        mCreateShortCutFab.setVisibility(View.VISIBLE);
        mCreateShortCutFab.startAnimation(animationSet);


        AnimationSet animationSet1 = new AnimationSet(true);
        Animation mCrashFabAnimation = new TranslateAnimation(-location1[0] / 2, 0, 0, 0);

        mCrashFabAnimation.setFillAfter(false);
        mCrashFabAnimation.setDuration(300);
        mCrashFabAnimation.setInterpolator(new DecelerateInterpolator());
        animationSet1.addAnimation(mCrashFabAnimation);
        animationSet1.addAnimation(anim);

        mCrashFab.setVisibility(View.VISIBLE);
        mCrashFab.startAnimation(animationSet1);
    }

    @Override
    public void setCrashShadow(boolean isShow) {
        //mCrashFab.setShadow(isShow);
        if (isShow) {
            mCrashFab.setImageResource(R.drawable.ivy_privacy_space_crash_selected);
        } else {
            mCrashFab.setImageResource(R.drawable.ivy_privacy_space_crash);
        }
    }

    @Override
    public void setCreateShortCutShadow(boolean isCreateShortCut) {
        // mCreateShortCutFab.setShadow(isCreateShortCut);
        if (isCreateShortCut) {
            mCreateShortCutFab.setImageResource(R.drawable.ivy_privacy_space_create_shortcut_selected);
            //mCreateShortCutFab.setColorNormalResId(R.color.ivy_privacy_space_colorPrimaryRed);
        } else {
            mCreateShortCutFab.setImageResource(R.drawable.ivy_privacy_space_create_shortcut);
            //mCreateShortCutFab.setColorNormalResId(R.color.ivy_privacy_space_colorPrimary);
        }
    }

    @Override
    public void waitingAppOpen() {
        ProgressDialog.show(this, getString(R.string.ivy_privacy_space_wait), getString(R.string.ivy_privacy_space_preparing));
    }


    @Override
    public void refreshPagerView() {
        mPagerView.refreshView();
    }

    @Override
    public void addAppToLauncher(AppModel model) {
        mAdapter.add(model);
        mPagerView.itemAdded();
    }

    private void hideLoading() {
        mLoadingBar.setVisibility(View.GONE);
        mPagerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == VCommends.REQUEST_SELECT_APP && data != null) {
            AppModel model = data.getParcelableExtra(VCommends.EXTRA_APP_MODEL);
            mPresenter.addApp(model);
            AppSetting info = VirtualCore.get().findApp(model.packageName);
            if (info != null) {
                if (info.dependSystem) {
                    mPresenter.dataChanged();
                    return;
                }
                model.context = this;
                ProgressDialog dialog = ProgressDialog.show(this, getString(R.string.ivy_privacy_space_wait), getString(R.string.ivy_privacy_space_optimize_app));
                VUiKit.defer().when(() -> {
                    try {
                        model.loadData(info.getApplicationInfo(VUserHandle.USER_OWNER));
                        VirtualCore.get().preOpt(info.packageName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).done((res) -> {
                    dialog.dismiss();
                    mPresenter.dataChanged();
                });
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterInstallerReceiver();
        if (homeReceiver != null) {
            try {
                unregisterReceiver(homeReceiver);
            } catch (Exception e) {
                LogUtils.e("rqy", "unregisterReceiver homeReceiver failure :" + e.getCause());
            }
        }
    }

  /*  public String[] getUsers() {
        List<VUserInfo> userList = VUserManager.get().getUsers(false);
        List<String> users = new ArrayList<>(userList.size());
        for (VUserInfo info : userList) {
            users.add(info.name);
        }
        return users.toArray(new String[users.size()]);
    }*/

    public class InstallerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (HomeActivity.this.mPresenter == null)
                return;

            HomeActivity.this.mPresenter.dataChanged();
        }
    }
}
