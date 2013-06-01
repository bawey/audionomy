package com.github.bawey.melotonine.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.bawey.melotonine.Melotonine;
import com.github.bawey.melotonine.R;
import com.github.bawey.melotonine.activities.abstracts.AbstractFullscreenActivity;

public class ModeChangedReceiver extends BroadcastReceiver {

	private AbstractFullscreenActivity activity;

	public ModeChangedReceiver(AbstractFullscreenActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Melotonine.NETWORK_MODE_CHANGED)) {
			Log.d("Networking", "Network mode changed");
			activity.handleModeSwitch();
		} else if (intent.getAction().equals(Melotonine.NETWORK_UNAVAILABLE)) {
			Log.d("Networking", "Network unavailable");
			activity.reportProblem(R.string.connection_error);
		}
	}
}
