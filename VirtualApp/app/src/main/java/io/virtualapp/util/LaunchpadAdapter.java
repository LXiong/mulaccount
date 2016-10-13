package io.virtualapp.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.client.AndroidSdk;
import com.ivy.module.clean.CleanManager;
import com.ivy.module.loading.LoadingMaster;
import com.lody.virtual.helper.utils.BitmapUtils;
import com.core.common.SdkEnv;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.virtualapp.R;
import io.virtualapp.entity.DefaultJson;
import io.virtualapp.home.HomeActivity;
import io.virtualapp.home.models.AppModel;
import io.virtualapp.widgets.PagerAdapter;

/**
 * @author Lody
 */
public class LaunchpadAdapter extends PagerAdapter<AppModel> {

    private static final int LOADING_ANIMAL_TIME = 5000;

    private Rotate3dAnimation rotation;

    private Rotate3dAnimation rotation1;

    private FrameLayout parentView;

    private int currentViewTag = 0;

    public static final int ANIMATION_DURATION = 500;

    public HomeActivity context;

    private Handler mHandler;

    private boolean onPause;

    private boolean initSuccess;

    public static final int MSG_START_AD_ANIMAL = 100;

    public static final int DEFAULT_INTERVAL_TIME = 5000;

    public int interval_time = DEFAULT_INTERVAL_TIME;

    public HashMap<Integer, View> nativeAdMap;

    public void addNativeAdToMap(Integer key, View view) {
        if (nativeAdMap == null) {
            nativeAdMap = new HashMap<>();
        }
        nativeAdMap.put(key, view);
    }

    public LaunchpadAdapter(HomeActivity context) {
        // noinspection unchecked
        super(context, new ArrayList<>(6));
        this.context = context;
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_START_AD_ANIMAL:
                        View child = getCurrentAnimalChild();
                        if (child == null) {
                            return;
                        }
                        child.startAnimation(rotation);
                        break;
                    default:
                        break;
                }
                super.handleMessage(msg);
            }
        };

    }

    private void applyRotation(View view, float start, float end) {
        // 计算中心点
        final float centerX = view.getWidth() / 2.0f;
        final float centerY = view.getHeight() / 2.0f;

        rotation = new Rotate3dAnimation(start, end,
                centerX, centerY, 310.0f, true);
        rotation.setDuration(ANIMATION_DURATION);
        rotation.setFillAfter(true);
        rotation.setInterpolator(new AccelerateInterpolator());
        // 设置监听
        rotation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            // 动画结束
            public void onAnimationEnd(Animation animation) {
                int count = parentView.getChildCount();
                View child = getCurrentAnimalChild();
                if (child != null) {
                    child.setAlpha(0);
                }
                if (currentViewTag == count - 1) {
                    currentViewTag = 0;
                } else {
                    currentViewTag++;
                }
                LogUtils.d("rqy", "startAnimal,childCount=" + parentView.getChildCount() + "currentViewTag=" + currentViewTag);
                child = getCurrentAnimalChild();
                if (child != null) {
                    parentView.bringChildToFront(child);
                    //child.postInvalidate();
                    child.setAlpha(1);
                    child.startAnimation(rotation1);
                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        rotation1 = new Rotate3dAnimation(-90, 0, centerX, centerY, 310.0f,
                false);
        rotation1.setDuration(ANIMATION_DURATION);
        rotation1.setFillAfter(true);
        //rotation1.setInterpolator(new DecelerateInterpolator());
        rotation1.setInterpolator(new AccelerateInterpolator());
        rotation1.setAnimationListener(new Animation.AnimationListener() {
                                           @Override
                                           public void onAnimationStart(Animation animation1) {

                                           }

                                           @Override
                                           public void onAnimationEnd(Animation animation1) {
                                               if (onPause) {
                                                   return;
                                               }
                                               View child = getCurrentAnimalChild();
                                               if (child == null) {
                                                   return;
                                               }
                                               parentView.bringChildToFront(child);
                                               //child.postInvalidate();
                                               mHandler.removeMessages(MSG_START_AD_ANIMAL);
                                               mHandler.sendEmptyMessageDelayed(MSG_START_AD_ANIMAL, interval_time);
                                           }

                                           @Override
                                           public void onAnimationRepeat(Animation animation1) {

                                           }
                                       }

        );
    }

    public View getCurrentAnimalChild() {
        if (parentView == null) {
            LogUtils.d("rqy", "parentView is null");
            return null;
        }
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View child = parentView.getChildAt(i);
            if (currentViewTag == (Integer) child.getTag()) {
                return child;
            }
        }
        LogUtils.d("rqy", "child is not exists");
        return null;
    }


    public void setModels(List<AppModel> models) {
        this.mList = models;
    }

    @Override
    public int getItemLayoutId(int position, AppModel appModel) {
        return R.layout.ivy_privacy_space_item_launcher_app;
    }

    @Override
    public void onBindView(View view, AppModel appModel) {
        ImageView iconView = (ImageView) view.findViewById(R.id.item_app_icon);
        TextView nameView = (TextView) view.findViewById(R.id.item_app_name);
        LabelView labelView = (LabelView) view.findViewById(R.id.ad_label);
        View record = view.findViewById(R.id.record);
        if (appModel.isAd) {
            labelView.setVisibility(View.VISIBLE);
            record.setVisibility(View.VISIBLE);
            if (TextUtils.equals(DefaultJson.TYPE_CLEAR, appModel.AdType)) {
                labelView.setVisibility(View.GONE);
                if (isSameDayClickAd(DefaultJson.TYPE_CLEAR)) {
                    record.setVisibility(View.INVISIBLE);
                }
                nameView.setText(appModel.name);
                Utility.loadImg(context, appModel.imgUrl, iconView, R.drawable.ivy_privacy_space_launcher);
                view.setOnClickListener(v -> CleanManager.Instance().runClean(context, () -> {
                    //Toast.makeText(context, R.string.ivy_privacy_space_clear_finish, Toast.LENGTH_SHORT).show();
                    if (record.getVisibility() != View.INVISIBLE) {
                        record.setVisibility(View.INVISIBLE);
                        saveCurrentClickAdTime(DefaultJson.TYPE_CLEAR);
                    }
                }));
            } else if (TextUtils.equals(DefaultJson.TYPE_FULL, appModel.AdType)) {
                Utility.loadImg(view.getContext(), appModel.imgUrl, iconView, R.drawable.ivy_privacy_space_launcher);
                if (isSameDayClickAd(DefaultJson.TYPE_FULL)) {
                    record.setVisibility(View.INVISIBLE);
                }
                view.setOnClickListener(view1 -> LoadingMaster.startLoadingAnim(context, LoadingMaster.AnimType.BOX, R.color.ivy_privacy_space_colorPrimary, LOADING_ANIMAL_TIME, isEnd -> {
                    if (isEnd) {
                        AndroidSdk.showFullAd(AndroidSdk.FULL_TAG_START);
                    }
                    if (record.getVisibility() != View.INVISIBLE) {
                        record.setVisibility(View.INVISIBLE);
                        saveCurrentClickAdTime(DefaultJson.TYPE_FULL);
                    }
                }));
                nameView.setText(appModel.name);
            } else if (TextUtils.equals(DefaultJson.TYPE_CROSS, appModel.AdType)) {
                int size = appModel.crossAdData.size();
                interval_time = appModel.interval_time;
                parentView = (FrameLayout) view;
                parentView.removeAllViews();
                for (int i = 0; i < size; i++) {
                    DefaultJson.CrossAd crossAd = appModel.crossAdData.get(i);
                    if (TextUtils.equals(crossAd.type, DefaultJson.TYPE_NATIVE)) {
                        Integer finalI = i;
                        View adView = AndroidSdk.peekNativeAdViewWithLayout("icon", AndroidSdk.NATIVE_AD_TYPE_THIRD, R.layout.ivy_privacy_space_item_launcher_app,
                                clientNativeAd -> {
                                    LogUtils.d("rqy", "onClick native AD");
                                    if (nativeAdMap == null || !nativeAdMap.containsKey(finalI)) {
                                        return;
                                    }
                                    View view1 = nativeAdMap.get(finalI);
                                    if (view1 == null) {
                                        LogUtils.d("rqy", "onClick native AD view1==null");
                                        return;
                                    }
                                    View adRecord = view1.findViewById(R.id.record);
                                    if (adRecord == null) {
                                        LogUtils.d("rqy", "onClick native AD adRecord==null");
                                        return;
                                    }
                                    if (adRecord.getVisibility() != View.INVISIBLE) {
                                        adRecord.setVisibility(View.INVISIBLE);
                                        saveCurrentClickAdTime(DefaultJson.TYPE_NATIVE);
                                    }
                                });
                        if (adView != null) {
                            addNativeAdToMap(i, adView);
                            parentView.addView(adView);
                            View record1 = adView.findViewById(R.id.record);
                            LabelView labelView1 = (LabelView) adView.findViewById(R.id.ad_label);
                            if (labelView1 != null) {
                                labelView1.setVisibility(View.VISIBLE);
                            }
                            if (record1 != null) {
                                if (isSameDayClickAd(DefaultJson.TYPE_NATIVE)) {
                                    record1.setVisibility(View.INVISIBLE);
                                } else {
                                    record1.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    } else {
                        LayoutInflater layoutInflater = LayoutInflater.from(context);
                        View view1 = layoutInflater.inflate(R.layout.ivy_privacy_space_item_launcher_app, null);
                        ImageView iconView1 = (ImageView) view1.findViewById(R.id.item_app_icon);
                        TextView nameView1 = (TextView) view1.findViewById(R.id.item_app_name);
                        LabelView labelView1 = (LabelView) view1.findViewById(R.id.ad_label);
                        View record1 = view1.findViewById(R.id.record);
                        labelView1.setVisibility(View.VISIBLE);
                        if (isSameDayClickAd(DefaultJson.TYPE_CROSS)) {
                            record1.setVisibility(View.INVISIBLE);
                        } else {
                            record1.setVisibility(View.VISIBLE);
                        }
                        Utility.loadImg(view.getContext(), crossAd.img, iconView1, R.drawable.ivy_privacy_space_launcher);
                        nameView1.setText(crossAd.title);
                        view1.setOnClickListener(v -> {
                            if (record1.getVisibility() != View.INVISIBLE) {
                                record1.setVisibility(View.INVISIBLE);
                                saveCurrentClickAdTime(DefaultJson.TYPE_CROSS);
                            }
                            SdkEnv.openPlayStore(crossAd.pkg +
                                    "&referrer=utm_source%3D双开%26utm_campaign%3D" + context.getPackageName(), null);
                        });
                        parentView.addView(view1);
                    }
                }
                if (parentView.getChildCount() <= 0) {

                } else {
                    for (int i = 0; i < parentView.getChildCount(); i++) {
                        parentView.getChildAt(i).setAlpha(i == 0 ? 1 : 0);
                        parentView.getChildAt(i).setTag(i);
                    }
                    parentView.postDelayed(() -> {
                        applyRotation(parentView.getChildAt(0), 0, 90);
                        parentView.getChildAt(0).startAnimation(rotation);
                    }, DEFAULT_INTERVAL_TIME);
                    initSuccess = true;
                }
            }
        } else {
            labelView.setVisibility(View.GONE);
            Bitmap bitmap = BitmapUtils.drawableToBitMap(getContext().getResources(), appModel.icon);
            iconView.setImageBitmap(bitmap);
            nameView.setText(appModel.name);
            record.setVisibility(View.GONE);
        }
    }

    public void onPause() {
        onPause = true;
        if (mHandler.hasMessages(MSG_START_AD_ANIMAL)) {
            mHandler.removeMessages(MSG_START_AD_ANIMAL);
        }
    }

    public void onResume() {
        onPause = false;
        if (initSuccess) {
            mHandler.removeMessages(MSG_START_AD_ANIMAL);
            mHandler.sendEmptyMessageDelayed(MSG_START_AD_ANIMAL, DEFAULT_INTERVAL_TIME);
        }
    }

    public boolean isSameDayClickAd(String adType) {
        SharedPreferences sp = context.getSharedPreferences("home_activity", Context.MODE_PRIVATE);
        return TextUtils.equals(currentTimeString(), sp.getString(adType, ""));
    }

    public void saveCurrentClickAdTime(String adType) {
        SharedPreferences sp = context.getSharedPreferences("home_activity", Context.MODE_PRIVATE);
        sp.edit().putString(adType, currentTimeString()).apply();
    }

    public String currentTimeString() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String str = formatter.format(curDate);
        return str;
    }
}

