package com.github.bawey.melotonine.singletons;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Settings {

	private static Settings instance;

	private Settings() {
	}

	public static Settings getInstance() {
		if (instance == null) {
			throw new RuntimeException("Requested non-initialized Settings object");
		}
		return instance;
	}

	public static synchronized void init(Context context) {
		if (instance == null) {
			instance = new Settings();
			instance.restore(context);
			instance.context = context;
		}
	}

	private Context context;
	public final static String TAG = "baweyTest";
	public final static int DIALOG_PICK_PATH = 666;

	private String musicDir;
	private Long userId;
	private String accessToken;
	private String dumpPath = "/mnt/sdcard/deveLog";
	private FileOutputStream fileOutputStream;
	private int api_requeries_limit = 5;
	private int search_results_limit = 50;

	/** getters & setters **/

	public String getMusicDir() {
		return musicDir;
	}

	public void setMusicDir(String musicFolder) {
		this.musicDir = musicFolder;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public int getApiRequeriesLimit() {
		return api_requeries_limit;
	}

	public void setApiRequeriesLimit(int api_requeries_limit) {
		this.api_requeries_limit = api_requeries_limit;
	}

	public int getSearchResultsLimit() {
		return search_results_limit;
	}

	public void setSearchResultsLimit(int search_results_limit) {
		this.search_results_limit = search_results_limit;
	}

	/** save & restore **/

	public void save(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.putString("music_folder", musicDir);
		editor.putLong("user_id", userId);
		editor.putString("access_token", accessToken);
		editor.putInt("api_requeries_limit", api_requeries_limit);
		editor.putInt("search_results_limit", search_results_limit);
		editor.commit();
	}

	public void restore(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		accessToken = prefs.getString("access_token", null);
		userId = prefs.getLong("user_id", 0);
		musicDir = prefs.getString("music_folder", "VK_DIR");
		api_requeries_limit = prefs.getInt("api_requeries_limit", 5);
		search_results_limit = prefs.getInt("search_results_limit", 50);
	}

	public synchronized void dump(String text) {
		// try {
		// if (fileOutputStream == null) {
		// Date date = new Date();
		// File dumpFile = new File(dumpPath + date.toString().replaceAll(":",
		// "."));
		// if (!dumpFile.exists()) {
		// dumpFile.createNewFile();
		// }
		// fileOutputStream = new FileOutputStream(dumpFile);
		// }
		// fileOutputStream.write(text.getBytes());
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}
}
