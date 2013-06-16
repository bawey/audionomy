package com.github.bawey.melotonine.activities.abstracts;

import com.github.bawey.melotonine.Melotonine;
import com.github.bawey.melotonine.R;
import com.github.bawey.melotonine.activities.LibraryActivity;
import com.github.bawey.melotonine.activities.MaintenanceActivity;
import com.github.bawey.melotonine.activities.PlayerActivity;
import com.github.bawey.melotonine.enums.AppMode;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class AbstractMenuActivity extends Activity {
	protected final static int MENU_MODE = 3;
	protected final static int MENU_QUIT = 5;

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		menu.add(0, MENU_MODE, 0, ((Melotonine) getApplication()).getAppMode() == AppMode.REMOTE ? "Go local" : "Go remote");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_MODE:
			Melotonine app = (Melotonine) getApplication();
			if (app.getAppMode() == AppMode.REMOTE) {
				app.goLocal();
			} else {
				app.goRemote(this instanceof AbstractFullscreenActivity ? (AbstractFullscreenActivity) this : null);
			}
			break;
		default:
			break;
		}
		return false;
	}
}
