package com.github.bawey.melotonine;

import java.io.File;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import com.github.bawey.melotonine.db.DatabaseHelper;
import com.github.bawey.melotonine.singletons.LocalContentManager;
import com.github.bawey.melotonine.singletons.Settings;

import de.umass.lastfm.Caller;
import de.umass.lastfm.cache.FileSystemCache;

/**
 * 
 * @author bawey TODO: downloads stuck as unfinished - clean up TODO: ask for
 *         folder rescan upon removing files
 */

public class Melotonine extends Application {

	public static final String SONG_FETCHED_BROADCAST = "com.github.bawey.melotonine.SONG_FETCHED";
	public static final String NETWORK_MODE_CHANGED = "com.github.bawey.melotonine.NETWORK_SWITCHED";
	public static final String NEW_ARTWORK_AVAILABLE = "com.github.bawey.melotonine.ARTWORK_AVAILABLE";
	public static final String SONG_REMOVED_BROADCAST = "com.github.bawey.melotonine.SONG_REMOVED";
	public static final String SONG_ENQUEUED_BROADCAST = "com.github.bawey.melotonine.SONG_REMOVED";
	public static final String NETWORK_UNAVAILABLE = "com.github.bawey.melotonine.NETWORK_UNAVAILABLE";

	private boolean remoteMode = true;

	@Override
	public void onCreate() {
		super.onCreate();
		doInitialChecks();
		Settings.init(this);
		LocalContentManager.init(this);
		DatabaseHelper.init(this);

		Caller.getInstance().setUserAgent("Melotonine");
		Caller.getInstance().setCache(new FileSystemCache(getCacheDir()));
		Caller.getInstance().setDebugMode(true);

		this.remoteMode = remoteMode && isOnline();
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	public boolean isRemote() {
		return remoteMode;
	}

	public void setRemote(boolean newOnline) {
		remoteMode = newOnline;
	}

	/**
	 * Switches the mode between remote and local. Checks for network connection too.
	 *  
	 * @return true if switched to remote
	 */
	public boolean switchMode() {
		Intent i = new Intent();
		if (isOnline()) {
			remoteMode = !remoteMode;
			i.setAction(NETWORK_MODE_CHANGED);
		} else {
			remoteMode=false;
			i.setAction(NETWORK_UNAVAILABLE);
		}
		sendBroadcast(i);
		return remoteMode;
	}

	/**
	 * Test whether app can work on that phone
	 */
	private void doInitialChecks() {
		Context context = getApplicationContext();
		File[] necessaryFolders = { context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
				context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) };
		for (File folder : necessaryFolders) {
			if (folder == null) {
				// TODO: plug it to some epilogue
				throw new RuntimeException("No folders, no fun");
			}
		}
	}

}
