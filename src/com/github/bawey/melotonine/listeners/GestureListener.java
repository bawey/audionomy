package com.github.bawey.melotonine.listeners;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.github.bawey.melotonine.activities.abstracts.AbstractFullscreenActivity;

public class GestureListener extends SimpleOnGestureListener {

	private static float minDistance = 120;
	private static float thresholdVelocity = 240;
	private AbstractFullscreenActivity parentActivity;

	public static float getMinDistance() {
		return minDistance;
	}

	public static float getThresholdVelocity() {
		return thresholdVelocity;
	}

	public GestureListener(AbstractFullscreenActivity parentActivity) {
		super();
		this.parentActivity = parentActivity;
		WindowManager wm = (WindowManager) parentActivity.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		int size = Math.min(display.getWidth(), display.getHeight());
		minDistance = size * 0.3f;
		thresholdVelocity = size * 0.5f;

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		Intent i = null;
		Log.d("Swiper", "Vx: " + velocityX + ", Vy:" + velocityY + ", Xdist" + (e1.getX() - e2.getX()));
		if (e1.getX() - e2.getX() > minDistance && Math.abs(velocityX) > thresholdVelocity) {
			i = new Intent(parentActivity, FullscreenActivities.getByClass(parentActivity.getClass()).getNext().getLaunchableClass());
			parentActivity.startActivity(i);
			return false; // Right to left
		} else if (e2.getX() - e1.getX() > minDistance && Math.abs(velocityX) > thresholdVelocity) {
			i = new Intent(parentActivity, FullscreenActivities.getByClass(parentActivity.getClass()).getPrev().getLaunchableClass());
			parentActivity.startActivity(i);
			return false; // Left to right
		}

		if (e1.getY() - e2.getY() > minDistance && Math.abs(velocityY) > thresholdVelocity) {
			parentActivity.reportProblem("TOP");
			return false; // Bottom to top
		} else if (e2.getY() - e1.getY() > minDistance && Math.abs(velocityY) > thresholdVelocity) {
			return false; // Top to bottom
		}
		return false;
	}
}
