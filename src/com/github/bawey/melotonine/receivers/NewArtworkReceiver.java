package com.github.bawey.melotonine.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.bawey.melotonine.Melotonine;
import com.github.bawey.melotonine.activities.abstracts.AbstractLibraryActivity;

public class NewArtworkReceiver extends BroadcastReceiver {

	private AbstractLibraryActivity activity;

	public NewArtworkReceiver(AbstractLibraryActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Melotonine.NEW_ARTWORK_AVAILABLE)) {
			activity.refreshArtwork(intent.getExtras().getString("mbid"));
			Log.d("Download", "Receiver caught broadcast after new artwork has been downloaded");
		}
	}

}
