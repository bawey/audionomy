package com.github.bawey.melotonine.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class ConnectivityChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			for (String key : extras.keySet()) {
				Log.d("NET", "extra! " + extras.getString(key) + "(" + key + ")");
			}
		} else {
			Log.d("NET", "no extras");
		}
	}
}
