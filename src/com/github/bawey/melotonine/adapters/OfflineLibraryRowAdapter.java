package com.github.bawey.melotonine.adapters;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.bawey.melotonine.R;
import com.github.bawey.melotonine.activities.ArtistActivity;
import com.github.bawey.melotonine.activities.ReleaseActivity;
import com.github.bawey.melotonine.activities.abstracts.AbstractLibraryActivity;
import com.github.bawey.melotonine.adapters.abstracts.AbstractLibraryRowAdapter;
import com.github.bawey.melotonine.db.DatabaseHelper;
import com.github.bawey.melotonine.db.DbArtist;
import com.github.bawey.melotonine.db.DbDownload;
import com.github.bawey.melotonine.db.DbRecording;
import com.github.bawey.melotonine.db.DbRelease;
import com.github.bawey.melotonine.singletons.LocalContentManager;
import com.github.bawey.melotonine.singletons.PlaybackQueue;

public class OfflineLibraryRowAdapter extends AbstractLibraryRowAdapter {

	private DatabaseHelper dbHelper = DatabaseHelper.getInstance();

	private List<DbRecording> recordings;
	private List<DbRelease> releases;
	private List<DbArtist> artists;

	public List<DbRecording> getRecordings() {
		return recordings;
	}

	public List<DbRelease> getReleases() {
		return releases;
	}

	public List<DbArtist> getArtists() {
		return artists;
	}

	private AbstractLibraryActivity libraryActivity;
	private int rowMode;

	public OfflineLibraryRowAdapter(AbstractLibraryActivity libraryActivity, int rowMode, String query) {
		this.libraryActivity = libraryActivity;
		this.rowMode = rowMode;
		switch (this.rowMode) {
		case ROW_MODE_ALBUM:
			if (libraryActivity.getArtistMbid() == null) {
				releases = dbHelper.getReleaseRed().queryForAll();
			} else {
				releases = dbHelper.getReleasesByArtistMbid(libraryActivity.getArtistMbid());
			}
			break;
		case ROW_MODE_ARTIST:
			artists = dbHelper.getArtistRed().queryForAll();
			break;
		case ROW_MODE_SONG:
		default:
			if (libraryActivity.getReleaseGroupMbid() == null && libraryActivity.getArtistMbid() == null) {
				recordings = dbHelper.getDownloadedRecordings();
			} else {
				recordings = dbHelper.getDownloadedRecordingsByRgidOrArid(libraryActivity.getReleaseGroupMbid() != null ? libraryActivity
						.getReleaseGroupMbid() : libraryActivity.getArtistMbid());
			}
			Log.d("Database", "Retrieved " + recordings.size() + "records");
		}
	}

	@Override
	public int getCount() {
		switch (this.rowMode) {
		case ROW_MODE_ALBUM:
			return releases.size();
		case ROW_MODE_ARTIST:
			return artists.size();
		case ROW_MODE_SONG:
			return recordings.size();
		default:
			return -1;
		}
	}

	@Override
	public Object getItem(int position) {
		switch (this.rowMode) {
		case ROW_MODE_ALBUM:
			return releases.get(position);
		case ROW_MODE_ARTIST:
			return artists.get(position);
		case ROW_MODE_SONG:
		default:
			return recordings.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d("Broadcast", "refreshing list");
		LayoutInflater inflater = (LayoutInflater) libraryActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.library_row_layout, parent, false);

		ImageView imageView = (ImageView) rowView.findViewById(R.id.image);
		TextView textView = (TextView) rowView.findViewById(R.id.text);
		CheckBox checkbox = (CheckBox) rowView.findViewById(R.id.checkbox);
		ImageView fetchIcon = (ImageView) rowView.findViewById(R.id.fetching_status);

		checkbox.setVisibility(View.INVISIBLE);
		fetchIcon.setVisibility(View.GONE);

		DbArtist dbArtist = null;
		DbRelease dbRelease = null;
		switch (this.rowMode) {
		case ROW_MODE_ALBUM:
			dbRelease = releases.get(position);
			textView.setText(dbRelease.getTitle());
			if (dbRelease.getImagePath() != null && dbRelease.getImagePath().length() > 0) {
				imageView.setImageDrawable(Drawable.createFromPath(dbRelease.getImagePath()));
			} else if ((dbArtist = dbRelease.getArtist()) != null) {
				if (dbArtist.getImagePath() != null && dbArtist.getImagePath().length() > 0) {
					imageView.setImageDrawable(Drawable.createFromPath(dbArtist.getImagePath()));
				} else {
					imageView.setImageResource(R.drawable.ic_album);
				}
			}
			break;
		case ROW_MODE_ARTIST:
			dbArtist = artists.get(position);
			if (dbArtist.getImagePath() != null && dbArtist.getImagePath().length() > 0) {
				imageView.setImageDrawable(Drawable.createFromPath(dbArtist.getImagePath()));
			} else {
				imageView.setImageResource(R.drawable.ic_mic);
			}

			textView.setText(artists.get(position).getName());
			break;
		case ROW_MODE_SONG:
		default:
			textView.setText(recordings.get(position).getTitle());
			checkbox.setVisibility(View.VISIBLE);
			checkbox.setChecked(checked.contains(position));
			checkbox.setOnCheckedChangeListener(this.checkingListener);
			checkbox.setTag(position);
			if ((dbRelease = recordings.get(position).getRelease()) != null && dbRelease.getImagePath() != null
					&& dbRelease.getImagePath().length() > 0) {
				imageView.setImageDrawable(Drawable.createFromPath(dbRelease.getImagePath()));
			} else if ((dbArtist = recordings.get(position).getArtist()) != null) {
				if (dbArtist.getImagePath() != null && dbArtist.getImagePath().length() > 0) {
					imageView.setImageDrawable(Drawable.createFromPath(dbArtist.getImagePath()));
				} else {
					imageView.setImageResource(R.drawable.ic_speaker);
				}
			}
		}
		return rowView;
	}

	@Override
	public void cleanUp() {
		// TODO Auto-generated method stub
	}

	@Override
	public OnItemClickListener getListItemClickListener() {
		return listItemClickListener;
	}

	@Override
	public OnItemLongClickListener getListItemLongClickListener() {
		return listItemLongClickListener;
	}

	private OnItemLongClickListener listItemLongClickListener = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
			final Resources r = libraryActivity.getResources();
			ContextThemeWrapper cw = new ContextThemeWrapper(libraryActivity, R.style.AlertDialogTheme);
			AlertDialog.Builder ab = new AlertDialog.Builder(cw);
			ab.setTitle(r.getString(R.string.action_choose));
			final String[] options = { r.getString(R.string.delete) };
			ab.setItems(options, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface d, int choice) {
					if (options[choice].equals(r.getString(R.string.delete))) {
						LocalContentManager.getInstance().removeDownloaded(recordings.get(arg2).getMbid());
					}
				}
			});
			ab.show();

			return false;
		}
	};

	private OnItemClickListener listItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			Intent intent = null;
			switch (OfflineLibraryRowAdapter.this.rowMode) {
			case ROW_MODE_ALBUM:
				intent = new Intent(libraryActivity, ReleaseActivity.class);
				intent.putExtra(ReleaseActivity.RELEASE_GROUP_MBID, releases.get(arg2).getGroupMbid());
				break;
			case ROW_MODE_ARTIST:
				intent = new Intent(libraryActivity, ArtistActivity.class);
				intent.putExtra(ArtistActivity.ARTIST_MBID, artists.get(arg2).getMbid());
				break;
			case ROW_MODE_SONG:
				DbRecording dbRec = recordings.get(arg2);
				DbDownload dbDown = dbHelper.getDownloadByMbid(dbRec.getMbid());
				PlaybackQueue.getInstance().enqueue(dbDown.getFilePath(), dbRec.getTitle(), dbRec.getArtist().getName(),
						dbRec.getDuration(), dbRec.getReleaseTitle(), dbRec.getArtworkPath());
				Toast.makeText(libraryActivity, libraryActivity.getResources().getString(R.string.enqueued), Toast.LENGTH_SHORT).show();
				break;
			}

			if (intent != null) {
				libraryActivity.startActivity(intent);
			}
		}

	};

}
