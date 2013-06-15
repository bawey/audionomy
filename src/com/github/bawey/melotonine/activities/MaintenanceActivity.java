package com.github.bawey.melotonine.activities;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.bawey.melotonine.R;
import com.github.bawey.melotonine.activities.abstracts.AbstractFullscreenActivity;
import com.github.bawey.melotonine.singletons.LocalContentManager;
import com.github.bawey.melotonine.singletons.Preferences;

public class MaintenanceActivity extends AbstractFullscreenActivity {

	private Preferences settings;
	private Resources r;

	@Override
	public int getLayoutId() {
		return R.layout.maintainance_layout;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		settings = Preferences.getInstance();
		r = getResources();
	}

	@Override
	protected void onResume() {
		super.onResume();
		((TextView) findViewById(R.id.text_audio_folder)).setText(settings.getMusicDir());
		((EditText) findViewById(R.id.api_requery_limit)).setText(new StringBuilder().append(settings.getApiRequeriesLimit()));
		((EditText) findViewById(R.id.search_results_limit)).setText(new StringBuilder().append(settings.getSearchResultsLimit()));
	}

	public void vkRelogin(View v) {
		startLoginActivity();
	}

	public void fixIntegrity(View v) {
		LocalContentManager.getInstance().databaseCleanup();
		Toast.makeText(this, "Database and filesystem cleanup launched", Toast.LENGTH_SHORT).show();
	}

	public void fillTags(View v) {
		Toast.makeText(this, "tags filling not yet implemented", Toast.LENGTH_SHORT).show();
	}

	public void apply(View v) {
		// TODO: remove this...
		settings.setMusicDir(LocalContentManager.VK_DIR);
		settings.setSearchResultsLimit(Integer.parseInt(((EditText) findViewById(R.id.search_results_limit)).getText().toString()));
		settings.setApiRequeriesLimit(Integer.parseInt(((EditText) findViewById(R.id.api_requery_limit)).getText().toString()));
		settings.save(this);
		Toast.makeText(this, r.getString(R.string.settings_saved), Toast.LENGTH_SHORT).show();
	}
}