package com.github.bawey.melotonine.activities;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.musicbrainz.android.api.data.Artist;
import org.musicbrainz.android.api.data.ReleaseGroupInfo;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.github.bawey.melotonine.R;
import com.github.bawey.melotonine.Melotonine;
import com.github.bawey.melotonine.activities.abstracts.AbstractLibraryActivity;
import com.github.bawey.melotonine.adapters.OnlineLibraryRowAdapter;
import com.github.bawey.melotonine.db.DatabaseHelper;
import com.github.bawey.melotonine.db.DbArtist;
import com.github.bawey.melotonine.enums.AppMode;
import com.github.bawey.melotonine.singletons.LocalContentManager;
import com.github.bawey.melotonine.singletons.MusicMetaProvider;

public class ArtistActivity extends AbstractLibraryActivity {

	public final static String ARTIST_MBID = "artist_musicbrainz_id";
	private MusicMetaProvider mmp;
	List<ReleaseGroupInfo> rgInfos;
	int rowMode = OnlineLibraryRowAdapter.ROW_MODE_ALBUM;
	String arid;

	@Override
	public String getArtistMbid() {
		return arid;
	}

	@Override
	public String getReleaseGroupMbid() {
		return null;
	}

	@Override
	protected String getCurrentQuery() {
		return arid;
	}

	@Override
	protected int getCurrentRowMode() {
		return rowMode;
	}

	@Override
	protected ListView getLibraryListView() {
		return ((ListView) findViewById(R.id.list_works));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		arid = getIntent().getExtras().getString(ARTIST_MBID);
		mmp = MusicMetaProvider.getInstance();
		try {
			setAllUp();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void setAllUp() throws IOException {
		DbArtist dbArtist = DatabaseHelper.getInstance().getArtistByMbid(arid);
		Artist mbArtist = null;
		if ((dbArtist == null || dbArtist.getCountry() == null || dbArtist.getName() == null)
				&& ((Melotonine) getApplication()).getAppMode() == AppMode.REMOTE) {
			mbArtist = mmp.lookupArtist(arid);
			if (dbArtist == null) {
				dbArtist = new DbArtist(mbArtist);
				DatabaseHelper.getInstance().getArtistRed().create(dbArtist);
			} else {
				dbArtist.setName(mbArtist.getName());
				dbArtist.setCountry(mbArtist.getCountry());
				DatabaseHelper.getInstance().getArtistRed().update(dbArtist);
			}
		}

		// rgInfos = mbArtist.getReleaseGroups();

		((TextView) findViewById(R.id.artist_country)).setText(dbArtist.getCountry());
		((TextView) findViewById(R.id.artist_name)).setText(dbArtist.getName());

		if (dbArtist.getImagePath() == null || !new File(dbArtist.getImagePath()).exists()) {
			if (((Melotonine) getApplication()).getAppMode() == AppMode.REMOTE) {
				LocalContentManager.getInstance().requestArtistImageDownload(arid);
			}
		} else {
			((ImageView) findViewById(R.id.artist_image)).setImageDrawable(Drawable.createFromPath(dbArtist.getImagePath()));
		}

		revalidateList();

		((RadioGroup) findViewById(R.id.radio_works)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radio_recordings) {
					ArtistActivity.this.rowMode = OnlineLibraryRowAdapter.ROW_MODE_SONG;
				} else {
					ArtistActivity.this.rowMode = OnlineLibraryRowAdapter.ROW_MODE_ALBUM;
				}
				revalidateList();
			}
		});
	}

	@Override
	public int getLayoutId() {
		return R.layout.artist_layout;
	}

}
