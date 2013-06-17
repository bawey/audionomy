package com.github.bawey.melotonine.activities.abstracts;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.musicbrainz.android.api.data.RecordingInfo;
import org.musicbrainz.android.api.data.Track;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.github.bawey.melotonine.R;
import com.github.bawey.melotonine.Melotonine;
import com.github.bawey.melotonine.activities.ReleaseActivity;
import com.github.bawey.melotonine.adapters.OfflineLibraryRowAdapter;
import com.github.bawey.melotonine.adapters.OnlineLibraryRowAdapter;
import com.github.bawey.melotonine.adapters.OnlineLibraryRowAdapter.MatchBox;
import com.github.bawey.melotonine.adapters.abstracts.AbstractLibraryRowAdapter;
import com.github.bawey.melotonine.db.DatabaseHelper;
import com.github.bawey.melotonine.db.DbArtist;
import com.github.bawey.melotonine.db.DbRecording;
import com.github.bawey.melotonine.db.DbRelease;
import com.github.bawey.melotonine.enums.AppMode;
import com.github.bawey.melotonine.internals.Song;
import com.github.bawey.melotonine.receivers.LibrarySongFetchedReceiver;
import com.github.bawey.melotonine.receivers.AppModeChangeReceiver;
import com.github.bawey.melotonine.receivers.NewArtworkReceiver;
import com.github.bawey.melotonine.singletons.LocalContentManager;
import com.github.bawey.melotonine.singletons.PlaybackQueue;

public abstract class AbstractLibraryActivity extends AbstractFullscreenActivity {

	protected static final int MENU_DOWNLOAD = 11;
	protected static final int MENU_ENQUEUE = 12;
	protected static final int MENU_DELETE = 14;
	protected static final int MENU_INVERSE_SELECTION = 13;

	protected AbstractLibraryRowAdapter lra;
	private LibrarySongFetchedReceiver libraryReceiver;

	private NewArtworkReceiver newArtworkReceiver;
	private Boolean modeOnExit;
	private Resources r;

	private Runnable createNewLibraryRowAdapter = new Runnable() {
		@Override
		public void run() {
			Log.d(this.getClass().getSimpleName(), "creating a row adapter");
			try {
				if (lra != null) {
					lra.cleanUp();
				}
				Log.d("Essential", "starting to create adapter");

				boolean online = ((Melotonine) getApplication()).getAppMode() == AppMode.REMOTE;
				if (online) {
					lra = new OnlineLibraryRowAdapter(AbstractLibraryActivity.this, AbstractLibraryActivity.this.getCurrentRowMode(),
							AbstractLibraryActivity.this.getCurrentQuery());
				} else {
					lra = new OfflineLibraryRowAdapter(AbstractLibraryActivity.this, AbstractLibraryActivity.this.getCurrentRowMode(),
							AbstractLibraryActivity.this.getCurrentQuery());
				}
				Log.d("Essential", "created adapter");
			} catch (IOException e) {
				e.printStackTrace();
				reportProblem(r.getString(R.string.connection_error));
			}
			Log.d(this.getClass().getSimpleName(), "created a row adapter");
			if (!Thread.currentThread().isInterrupted()) {
				runOnUiThread(connectAdapterToListView);
			} else {
				Log.d("Threads", "interrupted before connecting the adapter");
			}
		}
	};

	private Runnable connectAdapterToListView = new Runnable() {
		@Override
		public void run() {
			Log.d("DEBUG", "lra null? - " + (lra == null));
			AbstractLibraryActivity.this.getLibraryListView().setAdapter(lra);
			AbstractLibraryActivity.this.getLibraryListView().setOnItemClickListener(lra.getListItemClickListener());
			if (lra.getRowMode() == AbstractLibraryRowAdapter.ROW_MODE_SONG) {
				AbstractLibraryActivity.this.getLibraryListView().setOnItemLongClickListener(lra.getListItemLongClickListener());
			}
			AbstractLibraryActivity.this.getLibraryListView().invalidate();
			Log.d(this.getClass().getSimpleName(), "adapter set, list invalidated");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		r = getResources();
	}

	@Override
	protected void onPause() {
		super.onPause();
		this.unregisterReceiver(libraryReceiver);
		this.unregisterReceiver(newArtworkReceiver);
		this.modeOnExit = ((Melotonine) getApplication()).getAppMode() == AppMode.REMOTE;
	}

	@Override
	protected void onResume() {
		super.onResume();
		libraryReceiver = new LibrarySongFetchedReceiver(this);
		this.registerReceiver(libraryReceiver, new IntentFilter(Melotonine.SONG_FETCHED_BROADCAST));
		newArtworkReceiver = new NewArtworkReceiver(this);
		this.registerReceiver(newArtworkReceiver, new IntentFilter(Melotonine.NEW_ARTWORK_AVAILABLE));

		if (modeOnExit != null && !modeOnExit.equals(((Melotonine) getApplication()).getAppMode() == AppMode.REMOTE)) {
			handleModeSwitch();
		}

		hideFilterInOffline();
	}

	public AbstractLibraryRowAdapter getLibraryRowAdapter() {
		return lra;
	}

	protected void dbgMsg(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}

	public void handleSongFetched() {
		if (((Melotonine) getApplication()).getAppMode() == AppMode.REMOTE) {
			getLibraryRowAdapter().notifyDataSetChanged();
		} else {
			revalidateList();
		}
	}

	/** Used to switch between online and offline modes and set up layout **/
	@Override
	public void handleModeSwitch() {
		try {
			if (this instanceof ReleaseActivity) {
				((ReleaseActivity) this).setSpinnerUp(getReleaseGroupMbid());
			}
			revalidateList();
			hideFilterInOffline();
		} catch (IOException e) {
			Toast.makeText(this, r.getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
		}
	}

	// TODO: crap-style shortcut
	private void hideFilterInOffline() {
		View view = findViewById(R.id.filterKey);
		if (view != null) {
			view.setVisibility(((Melotonine) getApplication()).getAppMode() == AppMode.REMOTE ? View.VISIBLE : View.GONE);
		}
	}

	protected synchronized void revalidateList() {
		Log.d(this.getClass().getSimpleName(), "revalidate list: start");
		if (isBackgroundRunnerAlive()) {
			killBackgroundRunner();
		}
		launchBackgroundRunner(this.createNewLibraryRowAdapter);
		Log.d(this.getClass().getSimpleName(), "revalidate list: stop");
	}

	protected abstract String getCurrentQuery();

	protected abstract int getCurrentRowMode();

	protected abstract ListView getLibraryListView();

	public abstract String getArtistMbid();

	public abstract String getReleaseGroupMbid();

	public void refreshArtwork(String mbid) {
		if (mbid.equals(getArtistMbid())) {
			DbArtist dbArtist = DatabaseHelper.getInstance().getArtistByMbid(mbid);
			ImageView img = (ImageView) findViewById(R.id.artist_image);
			img.setImageDrawable(Drawable.createFromPath(dbArtist.getImagePath()));
			img.invalidate();
		} else if (mbid.equals(getReleaseGroupMbid())) {
			DbRelease dbRelease = DatabaseHelper.getInstance().getReleaseByGroupMbid(mbid);
			ImageView img = (ImageView) findViewById(R.id.release_image);
			img.setImageDrawable(Drawable.createFromPath(dbRelease.getImagePath()));
			img.invalidate();
		}
		Log.d("Download", "refreshed artwork with mbid " + mbid);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean result = super.onPrepareOptionsMenu(menu);
		if (lra != null && lra.getRowMode() == AbstractLibraryRowAdapter.ROW_MODE_SONG) {
			if (lra.getChecked() != null && lra.getChecked().size() > 0) {
				menu.add(0, MENU_ENQUEUE, 0, getResources().getString(R.string.menu_enqueue));
				if (((Melotonine) getApplication()).getAppMode().equals(AppMode.REMOTE)) {
					menu.add(0, MENU_DOWNLOAD, 0, getResources().getString(R.string.menu_download));
				} else {
					menu.add(0, MENU_DELETE, 0, getResources().getString(R.string.delete));
				}
			}
			// TODO sorry, another time perhaps
			// menu.add(0, MENU_INVERSE_SELECTION, 0,
			// getResources().getString(R.string.menu_invert_selection));
		}
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (super.onOptionsItemSelected(item)) {
			return true;
		}
		OnlineLibraryRowAdapter onLra = null;
		switch (item.getItemId()) {
		case MENU_DOWNLOAD:

			onLra = (OnlineLibraryRowAdapter) lra;
			StringBuilder sb = new StringBuilder("checked: ");
			if (onLra.getChecked() == null || onLra.getChecked().isEmpty()) {
				Toast.makeText(AbstractLibraryActivity.this, "Nothing selected", Toast.LENGTH_SHORT).show();
				return true;
			}
			for (Integer i : onLra.getChecked()) {
				sb.append(i).append(" ");
				MatchBox vkMatch = onLra.getVkMatches().get(i).get(0);
				if (onLra.getTracks() != null) {
					LocalContentManager.getInstance().startDownload(vkMatch.audio.url, onLra.getTracks().get(i), onLra.getRelease());
				} else if (onLra.getRecordings() != null) {
					LocalContentManager.getInstance().startDownload(vkMatch.audio.url, onLra.getRecordings().get(i));
				}
			}
			onLra.getChecked().clear();

			return true;

		case MENU_DELETE:
			final OfflineLibraryRowAdapter offLra = (OfflineLibraryRowAdapter) lra;
			if (offLra.getChecked() == null || offLra.getChecked().isEmpty()) {
				Toast.makeText(AbstractLibraryActivity.this, "Nothing selected", Toast.LENGTH_SHORT).show();
				return true;
			}
			if (offLra.getRecordings() != null) {
				AlertDialog ad = new AlertDialog.Builder(AbstractLibraryActivity.this).setTitle(R.string.delete_title)
						.setMessage(R.string.delete_confirm).create();

				ad.setButton(r.getString(R.string.yes), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						List<String> mbids = new LinkedList<String>();
						for (Integer i : offLra.getChecked()) {
							mbids.add(offLra.getRecordings().get(i).getMbid());
						}
						LocalContentManager.getInstance().removeDownloadedByMbids(mbids);
						offLra.getChecked().clear();
					}
				});
				ad.setButton2(r.getString(R.string.no), (DialogInterface.OnClickListener) null);
				ad.show();
			}

			return true;

		case MENU_ENQUEUE:
			if (lra instanceof OnlineLibraryRowAdapter) {
				onLra = (OnlineLibraryRowAdapter) lra;
				Song song = null;
				if (onLra.getChecked() == null || onLra.getChecked().isEmpty()) {
					Toast.makeText(AbstractLibraryActivity.this, "Nothing selected", Toast.LENGTH_SHORT).show();
					return true;
				}
				for (Integer i : onLra.getChecked()) {
					song = new Song(onLra.getVkMatches().get(i).get(0).audio.url);
					if (onLra.getTracks() != null) {
						Track track = onLra.getTracks().get(i);
						song.setTitle(track.getTitle());
						song.setDuration((short) (track.getDuration() / 1000));
						song.setArtist(onLra.getRelease().getArtists().get(0).getName());
					} else if (onLra.getRecordings() != null) {
						RecordingInfo rInfo = onLra.getRecordings().get(i);
						song.setTitle(rInfo.getTitle());
						song.setArtist(rInfo.getArtist().getName());
						song.setDuration((short) (rInfo.getLength() / 1000));
					}
					PlaybackQueue.getInstance().enqueue(song);
				}

			} else if (lra instanceof OfflineLibraryRowAdapter) {
				OfflineLibraryRowAdapter olra = (OfflineLibraryRowAdapter) lra;
				if (olra.getChecked() == null || olra.getChecked().isEmpty()) {
					Toast.makeText(AbstractLibraryActivity.this, "Nothing selected", Toast.LENGTH_SHORT).show();
					return true;
				}
				if (olra.getRecordings() != null) {
					List<DbRecording> recordingsToPlay = new LinkedList<DbRecording>();
					for (int i : olra.getChecked()) {
						recordingsToPlay.add(olra.getRecordings().get(i));
					}
					PlaybackQueue.getInstance().enqueueAllRecordings(recordingsToPlay);
				}

			}
			return true;
		case MENU_INVERSE_SELECTION:
			return false;
		default:
			break;
		}
		return false;
	}
}
