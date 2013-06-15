package com.github.bawey.melotonine.receivers.system;

import com.github.bawey.melotonine.Melotonine;
import com.github.bawey.melotonine.singletons.Preferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

	private Melotonine app;

	public ConnectivityChangeReceiver(Melotonine app) {
		super();
		this.app = app;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Boolean cutOff = intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY);
		if (cutOff != null && cutOff == true) {
			app.broadcastLostConnection();
		}
	}
}
