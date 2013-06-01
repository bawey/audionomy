package com.github.bawey.melotonine.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.github.bawey.melotonine.Melotonine;
import com.github.bawey.melotonine.activities.abstracts.AbstractLibraryActivity;
import com.github.bawey.melotonine.adapters.abstracts.AbstractLibraryRowAdapter;

public class LibrarySongFetchedReceiver extends BroadcastReceiver {

	private AbstractLibraryActivity activity;

	public LibrarySongFetchedReceiver(AbstractLibraryActivity activity) {
		this.activity = activity;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Melotonine.SONG_FETCHED_BROADCAST)) {
			if (activity.getLibraryRowAdapter() != null) {
				activity.handleSongFetched();
				Log.d("Broadcast", "song fetched broadcast received");
			}
		}
	}
}
