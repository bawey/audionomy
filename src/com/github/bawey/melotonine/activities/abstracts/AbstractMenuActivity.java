package com.github.bawey.melotonine.activities.abstracts;

import com.github.bawey.melotonine.Melotonine;
import com.github.bawey.melotonine.activities.LibraryActivity;
import com.github.bawey.melotonine.activities.MaintainanceActivity;
import com.github.bawey.melotonine.activities.PlayerActivity;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class AbstractMenuActivity extends Activity {
	protected final static int MENU_COLLECTION = 1;
	protected final static int MENU_PLAYING = 2;
	protected final static int MENU_MODE = 3;
	protected final static int MENU_MAINTAINANCE = 4;

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(0, MENU_COLLECTION, 0, "Media");
		menu.add(0, MENU_PLAYING, 0, "Player");
		menu.add(0, MENU_MODE, 0, ((Melotonine) getApplication()).isRemote() ? "Go local" : "Go remote");
		menu.add(0, MENU_MAINTAINANCE, 0, "Maintainance");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		switch (item.getItemId()) {
		case MENU_COLLECTION:
			intent = new Intent(this, LibraryActivity.class);
			break;
		case MENU_PLAYING:
			intent = new Intent(this, PlayerActivity.class);
			break;
		case MENU_MODE:
			((Melotonine) getApplication()).switchMode();
			break;
		case MENU_MAINTAINANCE:
			intent = new Intent(this, MaintainanceActivity.class);
			break;
		default:
			break;
		}
		if (intent != null) {
			startActivity(intent);
			return true;
		}
		return false;
	}
}
