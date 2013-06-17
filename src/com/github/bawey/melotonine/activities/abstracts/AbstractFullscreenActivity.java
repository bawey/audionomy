package com.github.bawey.melotonine.activities.abstracts;

import java.util.concurrent.Semaphore;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.bawey.melotonine.Melotonine;
import com.github.bawey.melotonine.R;
import com.github.bawey.melotonine.activities.LibraryActivity;
import com.github.bawey.melotonine.activities.LoginActivity;
import com.github.bawey.melotonine.activities.MaintenanceActivity;
import com.github.bawey.melotonine.activities.PlayerActivity;
import com.github.bawey.melotonine.enums.AppMode;
import com.github.bawey.melotonine.enums.NetMode;
import com.github.bawey.melotonine.listeners.FullscreenActivities;
import com.github.bawey.melotonine.listeners.GestureListener;
import com.github.bawey.melotonine.receivers.AppModeChangeReceiver;
import com.github.bawey.melotonine.singletons.Constants;
import com.github.bawey.melotonine.singletons.Preferences;
import com.github.bawey.melotonine.singletons.VkApi;

public abstract class AbstractFullscreenActivity extends AbstractMenuActivity {

	protected final static int REQUEST_LOGIN = 1;
	private AppModeChangeReceiver modeChangedReceiver;
	private Thread backgroundRunner;
	private GestureDetector gestureDetector = null;
	private Semaphore backgroundRunnerMutex = new Semaphore(1);

	protected synchronized void launchBackgroundRunner(Runnable r) {
		if (backgroundRunner != null && backgroundRunner.isAlive()) {
			backgroundRunner.interrupt();
		} else {
			backgroundRunner = new Thread(r);
			backgroundRunner.start();
		}
	}

	protected synchronized boolean isBackgroundRunnerAlive() {
		return backgroundRunner != null && backgroundRunner.isAlive();
	}

	protected synchronized void killBackgroundRunner() {
		backgroundRunner.interrupt();
		backgroundRunner = null;
	}

	public void startLoginActivity() {
		Intent intent = new Intent();
		intent.setClass(this, LoginActivity.class);
		startActivityForResult(intent, REQUEST_LOGIN);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (requestWindowFeature(Window.FEATURE_CUSTOM_TITLE)) {
			Log.d(this.getClass().getSimpleName(), "Setting custom titlebar");
			this.setContentView(this.getLayoutId());
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar_layout);
		}

		TextView activityTitle = (TextView) findViewById(R.id.activityTitle);
		for (FullscreenActivities fa : FullscreenActivities.values()) {
			if (fa.getDistinctiveClass().isAssignableFrom(this.getClass())) {
				activityTitle.setText(fa.getTitleBarTextRscId());
			} else {
				((ImageView) findViewById(fa.getTitleBarIconViewId())).setAlpha(120);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d("Swiper", "AFA onTouchEvent");
		if (gestureDetector == null) {
			gestureDetector = new GestureDetector(new GestureListener(AbstractFullscreenActivity.this));
		}
		gestureDetector.onTouchEvent(event);
		return false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(modeChangedReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		modeChangedReceiver = new AppModeChangeReceiver(this);
		this.registerReceiver(modeChangedReceiver, new IntentFilter(Melotonine.APP_MODE_CHANGED));
		this.registerReceiver(modeChangedReceiver, new IntentFilter(Melotonine.NETWORK_GONE));

		Melotonine app = (Melotonine) getApplication();
		if (app.getNetMode() == NetMode.ONLINE) {
			if (!app.isDeviceOnline()) {
				app.goLocal();
			} else if (app.getAppMode() == AppMode.REMOTE && !app.isVkUserAuthenticated()) {
				startLoginActivity();
			}
		}
		Log.d("Bizarre", this.getClass().getSimpleName() + " starting");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_LOGIN) {
			if (resultCode == RESULT_OK) {
				Preferences.getInstance().setAccessToken(data.getStringExtra("token"));
				Preferences.getInstance().setUserId(data.getLongExtra("user_id", 0));
				Preferences.getInstance().save(AbstractFullscreenActivity.this);

				VkApi.init(Preferences.getInstance().getAccessToken(), Constants.VK_API_KEY);
				((Melotonine) getApplication()).setNetModeInternallyOnly(NetMode.AUTHENTICATED);
			} else {
				((Melotonine) getApplication()).setNetModeInternallyOnly(NetMode.ONLINE);
			}
		}
	}

	public void bridgeToUiThread(Runnable r, Thread caller) {
		Log.v("Threads", "UI runnable sent by thread " + caller.getName());
		super.runOnUiThread(r);
	}

	public void reportProblem(int resourceId) {
		reportProblem(getResources().getString(resourceId));
	}

	public void reportProblem(final String issue) {
		bridgeToUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(AbstractFullscreenActivity.this, issue, Toast.LENGTH_SHORT).show();
			}
		}, Thread.currentThread());
	}

	public void handleNetworkGone() {
		reportProblem(R.string.connection_error);
	}

	public void handleModeSwitch() {
		if (this instanceof AbstractLibraryActivity) {
			((AbstractLibraryActivity) this).handleModeSwitch();
		}
	}

	public abstract int getLayoutId();

}
