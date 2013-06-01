package com.github.bawey.melotonine.activities.abstracts;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.github.bawey.melotonine.Melotonine;
import com.github.bawey.melotonine.R;
import com.github.bawey.melotonine.activities.LoginActivity;
import com.github.bawey.melotonine.activities.ReleaseActivity;
import com.github.bawey.melotonine.receivers.ModeChangedReceiver;
import com.github.bawey.melotonine.singletons.Constants;
import com.github.bawey.melotonine.singletons.Settings;
import com.github.bawey.melotonine.singletons.VkApi;

public class AbstractFullscreenActivity extends AbstractMenuActivity {

	protected final static int REQUEST_LOGIN = 1;
	private ModeChangedReceiver modeChangedReceiver;
	private Thread backgroundRunner;
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
		// vk.com authentication required for the application to work
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		try {
			if (((Melotonine) getApplication()).isRemote()) {
				if (VkApi.getInstance() == null || VkApi.getInstance().getStatus(Settings.getInstance().getUserId()) == null) {
					if (Settings.getInstance().getAccessToken() == null) {
						startLoginActivity();
					} else {
						VkApi.init(Settings.getInstance().getAccessToken(), Constants.VK_API_KEY);
					}
				}
			}
		} catch (Exception e) {
			startLoginActivity();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(modeChangedReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		modeChangedReceiver = new ModeChangedReceiver(this);
		this.registerReceiver(modeChangedReceiver, new IntentFilter(Melotonine.NETWORK_MODE_CHANGED));
		this.registerReceiver(modeChangedReceiver, new IntentFilter(Melotonine.NETWORK_UNAVAILABLE));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_LOGIN) {
			if (resultCode == RESULT_OK) {
				Settings.getInstance().setAccessToken(data.getStringExtra("token"));
				Settings.getInstance().setUserId(data.getLongExtra("user_id", 0));
				Settings.getInstance().save(AbstractFullscreenActivity.this);

				VkApi.init(Settings.getInstance().getAccessToken(), Constants.VK_API_KEY);
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

}
