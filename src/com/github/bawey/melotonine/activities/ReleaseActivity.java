package com.github.bawey.melotonine.activities;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.musicbrainz.android.api.data.ReleaseGroupInfo;
import org.musicbrainz.android.api.data.ReleaseInfo;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.bawey.melotonine.R;
import com.github.bawey.melotonine.Melotonine;
import com.github.bawey.melotonine.activities.abstracts.AbstractLibraryActivity;
import com.github.bawey.melotonine.adapters.OnlineLibraryRowAdapter;
import com.github.bawey.melotonine.db.DatabaseHelper;
import com.github.bawey.melotonine.db.DbArtist;
import com.github.bawey.melotonine.db.DbRelease;
import com.github.bawey.melotonine.singletons.LocalContentManager;
import com.github.bawey.melotonine.singletons.MusicMetaProvider;

public class ReleaseActivity extends AbstractLibraryActivity {

	public static final String RELEASE_GROUP_MBID = "release_group_musicbrainz_id";
	private MusicMetaProvider mmp = MusicMetaProvider.getInstance();
	private List<ReleaseInfo> releases = null;
	private String query;
	private String releaseGroupMbid;
	private DbRelease dbRelease;

	@Override
	protected String getCurrentQuery() {
		return query;
	}

	@Override
	public String getArtistMbid() {
		return null;
	}

	@Override
	public String getReleaseGroupMbid() {
		return releaseGroupMbid;
	}

	@Override
	protected int getCurrentRowMode() {
		return OnlineLibraryRowAdapter.ROW_MODE_SONG;
	}

	@Override
	protected ListView getLibraryListView() {
		return ((ListView) findViewById(R.id.songs_list));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		releaseGroupMbid = getIntent().getExtras().getString(RELEASE_GROUP_MBID);

		this.setContentView(R.layout.release_layout);
		try {
			setAllUp(releaseGroupMbid);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setSpinnerUp(String mbid) throws IOException {
		if (((Melotonine) getApplication()).isRemote()) {
			if (releases == null) {
				releases = mmp.searchRelease(mbid);
				List<String> labels = new LinkedList<String>();

				// a way to get the release date after all
				if (dbRelease != null && dbRelease.getReleaseYear() == 0) {
					dbRelease.setReleaseYearBasedOnReleases(releases, true);
				}

				for (ReleaseInfo rInfo : releases) {
					labels.add(rInfo.getTracksNum() + "♪" + (rInfo.getDate() != null ? " - " + rInfo.getDate() : ""));
				}
				((Spinner) findViewById(R.id.releases_spinner)).setAdapter(new ArrayAdapter<String>(this, R.layout.spinner_layout, labels));
				((Spinner) findViewById(R.id.releases_spinner)).setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
						query = (releases.get(arg2).getReleaseMbid());
						revalidateList();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
			}
			// sanity check
			if (query == null || query.length() == 0) {
				query = releases.get(0).getReleaseMbid();
			}
			findViewById(R.id.releases_spinner).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.releases_spinner).setVisibility(View.GONE);
		}
	}

	private void setAllUp(String mbid) throws IOException, SQLException {
		dbRelease = DatabaseHelper.getInstance().getReleaseByGroupMbid(mbid);
		ReleaseGroupInfo rgInfo = null;
		if ((dbRelease == null || dbRelease.getReleaseYear() == 0 || dbRelease.getArtist() == null)
				&& ((Melotonine) getApplication()).isRemote()) {
			List<ReleaseGroupInfo> rgInfos = mmp.searchReleaseGroup(mbid);
			if (rgInfos.size() != 1) {
				throw new RuntimeException("non-one-element rg list");
			}
			rgInfo = rgInfos.get(0);
			if (dbRelease == null) {
				dbRelease = new DbRelease(rgInfo);
				DatabaseHelper.getInstance().getReleaseDao().create(dbRelease);
			}
			if (dbRelease.getArtist() == null) {
				DbArtist dbArtist = DatabaseHelper.getInstance().getArtistByMbid(rgInfo.getArtists().get(0).getMbid());
				if (dbArtist == null) {
					dbArtist = new DbArtist(rgInfo.getArtists().get(0));
					DatabaseHelper.getInstance().getArtistRed().create(dbArtist);
				}
				dbRelease.setArtist(dbArtist);
				DatabaseHelper.getInstance().getReleaseDao().update(dbRelease);
			}
		}
		((TextView) findViewById(R.id.release_artist)).setText(dbRelease.getArtist().getName());
		((TextView) findViewById(R.id.release_title)).setText(dbRelease.getTitle());

		if (dbRelease.getImagePath() != null && new File(dbRelease.getImagePath()).exists()) {
			((ImageView) findViewById(R.id.release_image)).setImageDrawable(Drawable.createFromPath(dbRelease.getImagePath()));
		} else if (((Melotonine) getApplication()).isRemote()) {
			LocalContentManager.getInstance().requestCoverDownload(mbid);
		}
		setSpinnerUp(getReleaseGroupMbid());
		revalidateList();
	}
}
