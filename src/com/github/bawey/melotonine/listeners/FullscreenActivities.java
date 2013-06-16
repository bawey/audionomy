package com.github.bawey.melotonine.listeners;

import java.util.Arrays;

import com.github.bawey.melotonine.R;
import com.github.bawey.melotonine.activities.LibraryActivity;
import com.github.bawey.melotonine.activities.MaintenanceActivity;
import com.github.bawey.melotonine.activities.PlayerActivity;
import com.github.bawey.melotonine.activities.abstracts.AbstractLibraryActivity;

public enum FullscreenActivities {

	LIBRARY_ACTIVITY(AbstractLibraryActivity.class, R.string.activity_media, R.id.imageBasket, LibraryActivity.class),
	PLAYER_ACTIVITY(PlayerActivity.class, R.string.activity_player, R.id.imagePlay, null),
	MAINTENANCE_ACTIVITY(MaintenanceActivity.class, R.string.activity_settings, R.id.imageSettings, null);

	private FullscreenActivities(Class<?> activityClass, int titleBarTextRscId, int titleBarIconViewId, Class<?> classOverride) {
		this.activityClass = activityClass;
		this.titleBarTextId = titleBarTextRscId;
		this.titleBarIconId = titleBarIconViewId;
		this.classOverride = classOverride;
	}

	private Class<?> activityClass;
	private Class<?> classOverride;
	private int titleBarTextId;
	private int titleBarIconId;

	public Class<?> getLaunchableClass() {
		return classOverride != null ? classOverride : activityClass;

	}

	public int getTitleBarTextRscId() {
		return titleBarTextId;
	}

	public int getTitleBarIconViewId() {
		return titleBarIconId;
	}

	public FullscreenActivities getNext() {
		if (this.equals(FullscreenActivities.values()[FullscreenActivities.values().length - 1])) {
			return FullscreenActivities.values()[0];
		} else {
			return values()[Arrays.asList(values()).indexOf(this) + 1];
		}
	}

	public FullscreenActivities getPrev() {
		if (this.equals(FullscreenActivities.values()[0])) {
			return FullscreenActivities.values()[FullscreenActivities.values().length - 1];
		} else {
			return values()[Arrays.asList(values()).indexOf(this) - 1];
		}
	}

	public static FullscreenActivities getByClass(Class<?> cls) {
		for (FullscreenActivities fa : FullscreenActivities.values()) {
			if (fa.getDistinctiveClass().isAssignableFrom(cls)) {
				return fa;
			}
		}
		return null;
	}

	public Class<?> getDistinctiveClass() {
		return this.activityClass;
	}
}
