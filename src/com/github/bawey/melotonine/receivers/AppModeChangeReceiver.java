package com.github.bawey.melotonine.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.bawey.melotonine.Melotonine;
import com.github.bawey.melotonine.R;
import com.github.bawey.melotonine.activities.abstracts.AbstractFullscreenActivity;

public class AppModeChangeReceiver extends BroadcastReceiver {

	public static final String FORCED = "MELOTONINE_FORCED";
	public static final String APP_MODE = "MELOTONINE_APP_MODE";

	private AbstractFullscreenActivity activity;
	
	public AppModeChangeReceiver(AbstractFullscreenActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Melotonine.APP_MODE_CHANGED)) {
			Log.d(this.getClass().getSimpleName(), "Application mode changed");
			if(intent.hasExtra(FORCED)){
				activity.reportProblem("Switching to local mode due to lost connection.");
			}
			activity.handleModeSwitch();
		} else if (intent.getAction().equals(Melotonine.NETWORK_GONE)) {
			Log.d(this.getClass().getSimpleName(), "Network is gone!");
			activity.reportProblem(R.string.connection_error);
		}
	}
}
