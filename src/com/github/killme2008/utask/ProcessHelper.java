package com.github.killme2008.utask;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

/**
 * Process helper to get process infos
 * 
 * @author dennis<killme2008@gmail.com>
 * 
 */
public class ProcessHelper {

	private final ActivityManager activityManager;
	private final ApplicationHelper appHelper;

	public static final String PKG_NAME = "pkgName";
	public static final String APP_NAME = "appName";
	public static final String APP_ICON = "appIcon";

	public ActivityManager getActivityManager() {
		return activityManager;
	}

	public ProcessHelper(ActivityManager activityManager,
			ApplicationHelper appHelper) {
		super();
		this.activityManager = activityManager;
		this.appHelper = appHelper;
	}

	/**
	 * Kill a app
	 * 
	 * @param packgeName
	 */
	public void killApp(String packgeName) {
		Method method = getKillMethod();
		try {
			if (method != null) {
				method.invoke(this.activityManager, packgeName);
			} else {
				this.activityManager.restartPackage(packgeName);
			}
		} catch (Exception e) {
			Log.e(ProcessList.LOG_TAG, e.getMessage());
		}

	}

	private Method getKillMethod() {
		try {
			Method method = ActivityManager.class.getDeclaredMethod(
					"killBackgroundProcesses", String.class);
			return method;
		} catch (Exception e) {
			return null;
		}
	}

	private static final Set<String> IGNORE_PKGS = new HashSet<String>();
	static {
		IGNORE_PKGS.add("system");
		IGNORE_PKGS.add("com.android.phone");
		IGNORE_PKGS.add("com.android.email");
		IGNORE_PKGS.add("com.android.systemui");
		IGNORE_PKGS.add(ProcessHelper.class.getPackage().getName());
	}

	/**
	 * Get all running apps
	 * 
	 * @param context
	 * @return
	 */
	public List<Map<String, Object>> getProcessInfos(Context context) {
		List<Map<String, Object>> rt = new ArrayList<Map<String, Object>>();
		List<RunningAppProcessInfo> list = this.activityManager
				.getRunningAppProcesses();
		if (list != null) {
			for (RunningAppProcessInfo runningAppProcessInfo : list) {
				String packageName = runningAppProcessInfo.processName;
				// Ignore uniq task
				if (IGNORE_PKGS.contains(packageName))
					continue;
				ApplicationInfo appInfo = this.appHelper
						.getApplicationInfo(packageName);
				Map<String, Object> processInfo = new HashMap<String, Object>();
				processInfo.put(PKG_NAME, packageName);
				if (appInfo != null) {
					processInfo.put(APP_NAME,
							appInfo.loadLabel(this.appHelper.getPm())
									.toString());
					processInfo.put(APP_ICON,
							appInfo.loadIcon(this.appHelper.getPm()));

				} else {
					processInfo.put(APP_NAME, packageName);
					processInfo.put(APP_ICON, R.drawable.unknow);
				}
				rt.add(processInfo);
			}
		}
		return rt;

	}
}
