package io.virtualapp.home.models;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.lody.virtual.helper.proto.AppSetting;
import com.lody.virtual.os.VUserHandle;

import java.util.List;

import io.virtualapp.entity.DefaultJson;

/**
 * @author Lody
 */
public class AppModel implements Parcelable {

	public static final Creator<AppModel> CREATOR = new Creator<AppModel>() {
		@Override
		public AppModel createFromParcel(Parcel source) {
			return new AppModel(source);
		}

		@Override
		public AppModel[] newArray(int size) {
			return new AppModel[size];
		}
	};
	public Context context;
	public String packageName;
	public String path;
	public String name;
	public Drawable icon;
	public boolean fastOpen;

	public boolean isAd;
	public String AdType;
	public String imgUrl;
	public List<DefaultJson.CrossAd> crossAdData;
	public int interval_time;

	public AppModel() {
		// For Database
	}

	public AppModel(Context context, PackageInfo packageInfo) {
		this.context = context;
		this.packageName = packageInfo.packageName;
		this.path = packageInfo.applicationInfo.publicSourceDir;
		loadData(packageInfo.applicationInfo);
	}

	public AppModel(Context context, AppSetting appSetting) {
		this.context = context;
		this.packageName = appSetting.packageName;
		this.path = appSetting.apkPath;
		loadData(appSetting.getApplicationInfo(VUserHandle.USER_OWNER));
	}

	protected AppModel(Parcel in) {
		this.packageName = in.readString();
		this.path = in.readString();
		this.name = in.readString();
		this.fastOpen = in.readByte() != 0;
	}

	public void loadData(ApplicationInfo appInfo) {
		if (appInfo == null) {
			return;
		}
		PackageManager pm = context.getPackageManager();
		try {
			CharSequence sequence = appInfo.loadLabel(pm);
			if (sequence != null) {
				name = sequence.toString();
			}
			icon = appInfo.loadIcon(pm);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.packageName);
		dest.writeString(this.path);
		dest.writeString(this.name);
		dest.writeByte(this.fastOpen ? (byte) 1 : (byte) 0);
	}
}
