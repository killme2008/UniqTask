package com.github.killme2008.utask;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Helper for getting application info
 * 
 * @@author dennis<killme2008@gmail.com>
 * 
 */
public class ApplicationHelper {

	private final PackageManager pm;

	public PackageManager getPm() {
		return pm;
	}

	public ApplicationHelper(PackageManager pm) {
		super();
		this.pm = pm;
	}

	/**
	 * Get application info by package name
	 * 
	 * @param packageName
	 * @return
	 */
	public ApplicationInfo getApplicationInfo(String packageName) {
		try {
			return this.pm.getApplicationInfo(packageName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
		} catch (Throwable e) {
			return null;
		}
	}

}
