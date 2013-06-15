package com.github.bawey.melotonine;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import com.github.bawey.melotonine.activities.abstracts.AbstractFullscreenActivity;
import com.github.bawey.melotonine.db.DatabaseHelper;
import com.github.bawey.melotonine.enums.AppMode;
import com.github.bawey.melotonine.enums.NetMode;
import com.github.bawey.melotonine.receivers.AppModeChangeReceiver;
import com.github.bawey.melotonine.receivers.system.ConnectivityChangeReceiver;
import com.github.bawey.melotonine.singletons.LocalContentManager;
import com.github.bawey.melotonine.singletons.Preferences;
import com.github.bawey.melotonine.singletons.VkApi;
import com.perm.kate.api.KException;

import de.umass.lastfm.Caller;
import de.umass.lastfm.cache.FileSystemCache;

/**
 * 
 * @author bawey TODO: downloads stuck as unfinished - clean up TODO: ask for
 *         folder rescan upon removing files
 */

public class Melotonine extends Application {

	public static final String APP_MODE_CHANGED = "com.github.bawey.melotonine.APP_MODE_CHANGED";
	public static final String SONG_FETCHED_BROADCAST = "com.github.bawey.melotonine.SONG_FETCHED";
	public static final String NEW_ARTWORK_AVAILABLE = "com.github.bawey.melotonine.ARTWORK_AVAILABLE";
	public static final String SONG_REMOVED_BROADCAST = "com.github.bawey.melotonine.SONG_REMOVED";
	public static final String SONG_ENQUEUED_BROADCAST = "com.github.bawey.melotonine.SONG_REMOVED";
	public static final String NETWORK_GONE = "MELOTONINE_NETWORK_GONE";

	private AppMode appMode = AppMode.LOCAL;
	private NetMode netMode = NetMode.OFFLINE;

	@Override
	public void onCreate() {
		super.onCreate();
		checkRequirements();
		Preferences.init(this);
		LocalContentManager.init(this);
		DatabaseHelper.init(this);

		Caller.getInstance().setUserAgent("Melotonine");
		Caller.getInstance().setCache(new FileSystemCache(getCacheDir()));
		Caller.getInstance().setDebugMode(true);

		ConnectivityChangeReceiver ccr = new ConnectivityChangeReceiver(this);
		registerReceiver(ccr, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
		registerReceiver(ccr, new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED"));

		determineModes();
	}

	public AppMode getAppMode() {
		return appMode;
	}

	public void setAppModeInternallyOnly(AppMode appMode) {
		this.appMode = appMode;
	}

	public NetMode getNetMode() {
		return netMode;
	}

	public void setNetModeInternallyOnly(NetMode netMode) {
		this.netMode = netMode;
	}

	public void determineModes() {
		if (isDeviceOnline()) {
			if (isVkUserAuthenticated()) {
				netMode = NetMode.AUTHENTICATED;
				appMode = AppMode.REMOTE;
			} else {
				netMode = NetMode.ONLINE;
			}
		} else {
			netMode = NetMode.OFFLINE;
			appMode = AppMode.LOCAL;
		}
	}

	private void broadcastAppModeChanged(boolean forced) {
		Intent i = new Intent();
		i.setAction(APP_MODE_CHANGED);
		i.putExtra(AppMode.actionString(), appMode.name());
		if (forced) {
			i.putExtra(AppModeChangeReceiver.FORCED, true);
		}
		this.sendBroadcast(i);
	}

	public boolean isDeviceOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			// this.netMode = NetMode.ONLINE;
			return true;
		}
		// this.netMode = NetMode.OFFLINE;
		return false;
	}

	public boolean isVkUserAuthenticated() {
		VkApi api = null;
		if ((api = VkApi.getInstance()) == null) {
			Log.d(this.getClass().getSimpleName(), "VkApi is null. ");
			return false;
		}
		try {
			api.getServerTime();
		} catch (Exception e) {
			if (e instanceof KException || e instanceof IOException) {
				Log.d(this.getClass().getSimpleName(), "Failed to get server time from VK " + e.getMessage());
				return false;
			} else {
				Log.e(this.getClass().getSimpleName(), "This should not have happened");
				throw new RuntimeException(e);
			}
		}
		// this.netMode = NetMode.ONLINE;
		// this.appMode = AppMode.REMOTE;
		return true;
	}

	/**
	 * methods changing the app mode
	 * 
	 * @return success
	 **/
	public boolean goRemote(AbstractFullscreenActivity referer) {
		if (this.appMode == AppMode.REMOTE) {
			return true;
		} else {
			if (isDeviceOnline()) {
				if (isVkUserAuthenticated()) {
					this.appMode = AppMode.REMOTE;
					broadcastAppModeChanged(false);
					return true;
				} else {
					Log.d(Melotonine.class.getSimpleName(), "Shall trigger login action");
					if (referer != null) {
						referer.startLoginActivity();
					}
					return false;
				}
			} else {
				Log.d(Melotonine.class.getSimpleName(), "Shall alert about the lack of internet connection");
				broadcastLostConnection();
				return false;
			}
		}
	}

	public boolean goLocal() {
		if (this.appMode != AppMode.LOCAL) {
			this.appMode = AppMode.LOCAL;
			broadcastAppModeChanged(false);
		}
		return true;
	}

	/**
	 * Test whether app can work on that phone
	 */
	private void checkRequirements() {
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

	/**
	 * Called upon creating an activity to determine whether the state is
	 * consistent
	 */
	public void testStateConsistency() {
		if (appMode == AppMode.REMOTE) {
			if (!isDeviceOnline()) {
				appMode = AppMode.LOCAL;
				broadcastAppModeChanged(true);
			} else {
				if (isVkUserAuthenticated()) {

				}
			}
			determineModes();
			if (appMode == AppMode.LOCAL) {
			}
		}
	}

	public void broadcastLostConnection() {
		Intent i = new Intent();
		i.setAction(NETWORK_GONE);
		sendBroadcast(i);
	}

}
