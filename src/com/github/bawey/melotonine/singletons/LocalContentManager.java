package com.github.bawey.melotonine.singletons;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.cmc.music.common.ID3WriteException;
import org.cmc.music.metadata.ImageData;
import org.cmc.music.metadata.MusicMetadata;
import org.cmc.music.metadata.MusicMetadataSet;
import org.cmc.music.myid3.MyID3;
import org.musicbrainz.android.api.data.RecordingInfo;
import org.musicbrainz.android.api.data.Release;
import org.musicbrainz.android.api.data.ReleaseArtist;
import org.musicbrainz.android.api.data.ReleaseGroupInfo;
import org.musicbrainz.android.api.data.Track;

import android.R;
import android.app.DownloadManager;
import android.app.DownloadManager.Query;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.github.bawey.melotonine.Melotonine;
import com.github.bawey.melotonine.db.DatabaseHelper;
import com.github.bawey.melotonine.db.DbArtist;
import com.github.bawey.melotonine.db.DbDownload;
import com.github.bawey.melotonine.db.DbRecording;
import com.github.bawey.melotonine.db.DbRelease;

import de.umass.lastfm.Album;
import de.umass.lastfm.Artist;
import de.umass.lastfm.ImageSize;

public class LocalContentManager {
	private static LocalContentManager instance;

	private Context context;
	private DownloadManager downloadManager;
	private final static String COVER_DESC = "cover";
	private final static String ARTIST_DESC = "artist";

	private LinkedList<DbDownload> songsQueue = new LinkedList<DbDownload>();

	private void enqueueDownload(DbDownload download) {
		synchronized (songsQueue) {
			songsQueue.add(download);
			songsQueue.notify();
		}
	}

	private DbDownload dequeueDownload() {
		DbDownload result = null;
		synchronized (songsQueue) {
			if (songsQueue.size() > 0) {
				result = songsQueue.get(0);
				songsQueue.remove(0);
			}
		}
		return result;
	}

	private LocalContentManager(Context appContext) {
		// TODO: perform a database check for old downloads (unless
		// DownloadManager will take care of that!!!)
		context = appContext;
		context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
	}

	public static void init(Context context) {
		if (instance == null) {
			instance = new LocalContentManager(context);
		}
	}

	public static LocalContentManager getInstance() {
		return instance;
	}

	public static String VK_DIR = "VK_DIR";

	public void removeDownloadedByMbids(List<String> mbids) {
		removeDownloaded(DatabaseHelper.getInstance().getDownloadsByMbids(mbids));
	}

	public void removeDownloaded(final Collection<DbDownload> dbDownloads) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (DbDownload dbDownload : dbDownloads) {
					removeDownloaded(dbDownload);
				}
			}
		}).start();
	}

	public void removeDownloaded(DbDownload dbDownload) {
		if (dbDownload != null) {
			if (dbDownload.getFilePath() != null) {
				File toDelete = new File(dbDownload.getFilePath());
				if (toDelete.exists()) {
					toDelete.delete();
				}
			}
			DatabaseHelper.getInstance().getDownloadRed().delete(dbDownload);
		}
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(Melotonine.SONG_FETCHED_BROADCAST);
		context.sendBroadcast(broadcastIntent);
	}

	public void removeDownloaded(String mbid) {
		DbDownload dbDownload = DatabaseHelper.getInstance().getDownloadByMbid(mbid);
		removeDownloaded(dbDownload);
	}

	// TODO: assumes track artist == release artist!
	// TODO: doesn't persist release year!!
	// TODO: handle copying stray songs to albums or songs between albums. never
	// allow downloading a stray song, while album version is present
	private void startDownload(String url, RecordingInfo recInfo, Track track, Release release) {
		ReleaseArtist artist = release != null ? release.getArtists().get(0) : recInfo.getArtist();
		String title = null;
		String mbid = null;
		String releaseTitle = null;
		String releaseGroupMbid = null;
		String releaseMbid = null;
		int duration = 0;
		String releaseYear = null;
		if (recInfo != null) {
			title = recInfo.getTitle();
			mbid = recInfo.getMbid();
			releaseTitle = recInfo.getReleaseGroupTitle();
			releaseGroupMbid = recInfo.getReleaseGroupMbid();
			duration = recInfo.getLength() / 1000;
		} else {
			title = track.getTitle();
			mbid = track.getRecordingMbid();
			releaseTitle = release.getTitle();
			releaseMbid = release.getMbid();
			releaseGroupMbid = release.getReleaseGroupMbid();
			duration = track.getDuration() / 1000;
			releaseYear = release.getDate();
		}

		DatabaseHelper dHelper = DatabaseHelper.getInstance();
		DbRecording dbRecording = dHelper.getRecordingByMbid(mbid);
		DbRelease dbRelease = null;
		DbArtist dbArtist = null;
		if (dbRecording == null) {
			// seems like there are no recordings with given id
			dbRelease = dHelper.getReleaseByGroupMbid(releaseGroupMbid);
			if (dbRelease == null) {
				// DB doesn't contain the release
				// TODO: powinien byc artysta dla plyty wyciagniety. zamiast
				// tego jest dla utworu!!!
				dbArtist = dHelper.getArtistByMbid(artist.getMbid());
				if (dbArtist == null) {
					dbArtist = new DbArtist();
					dbArtist.setName(artist.getName());
					dbArtist.setMbid(artist.getMbid());
					dHelper.getArtistRed().create(dbArtist);
				}
				dbRelease = new DbRelease();
				dbRelease.setArtist(dbArtist);
				dbRelease.setGroupMbid(releaseGroupMbid);
				dbRelease.setIndividualMbid(releaseMbid);
				dbRelease.setTitle(releaseTitle);
				dbRelease.setReleaseYear(releaseYear);
				dHelper.getReleaseRed().create(dbRelease);
			}
			dbRecording = new DbRecording();
			dbRecording.setArtist(dbRelease.getArtist());
			dbRecording.setDuration(duration);
			dbRecording.setMbid(mbid);
			dbRecording.setTitle(title);
			dbRecording.setRelease(dbRelease);
			if (track != null) {
				dbRecording.setTrackNumber(track.getPosition());
			}
			dHelper.getRecordingRed().create(dbRecording);
		}

		if (dbRecording.getArtist().getImagePath() == null) {
			Log.d("Download", "requesting artist picture");
			requestArtistImageDownload(artist.getMbid());
		} else {
			Log.d("Download", "artist picture already in place");
		}

		if (dbRecording.getRelease().getImagePath() == null) {
			Log.d("Download", "requesting album cover");
			requestCoverDownload(releaseGroupMbid);
		} else {
			Log.d("Download", "album cover alredy in place");
		}

		DbDownload dbDownload = dHelper.getDownloadByMbid(mbid);
		if (dbDownload != null) {
			File toDolete = new File(dbDownload.getFilePath());
			if (toDolete.exists()) {
				toDolete.delete();
			}
			dHelper.getDownloadRed().delete(dbDownload);
		}

		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		request.setDescription(artist.getName());
		request.setTitle(title);
		request.setShowRunningNotification(true);
		request.setVisibleInDownloadsUi(true);
		File targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
		targetDir = new File(targetDir.getAbsolutePath(), VK_DIR + "/" + artist.getName().replaceAll(":", "-") + "/"
				+ releaseTitle.replaceAll(":", "-"));
		targetDir.mkdirs();
		String filename = artist.getName().replaceAll(":", "-") + " - " + title.replaceAll(":", "-") + ".mp3";
		request.setDestinationUri(Uri.parse("file://" + targetDir + "/" + filename));

		dbDownload = new DbDownload();
		dbDownload.setRecording(dbRecording);
		dbDownload.setDownloadId(-1);
		dbDownload.setFilePath(targetDir.getAbsolutePath() + "/" + filename);
		dbDownload.setFinihed(false);
		dHelper.getDownloadRed().create(dbDownload);

		long downloadId = downloadManager.enqueue(request);
		dbDownload.setDownloadId(downloadId);
		dHelper.getDownloadRed().update(dbDownload);

		Intent i = new Intent();
		i.setAction(Melotonine.SONG_FETCHED_BROADCAST);
		context.sendBroadcast(i);

		Log.d("Download", "Enqueued a download");

	}

	public void startDownload(String url, Track track, Release release) {
		startDownload(url, null, track, release);
	}

	// TODO: much more parameters to come, but be crude for now!
	public void startDownload(String url, RecordingInfo rInfo) {
		startDownload(url, rInfo, null, null);
	}

	private Map<String, Long> pendingCoverDownloads = new HashMap<String, Long>();
	private Map<String, Long> pendingArtistImageDownloads = new HashMap<String, Long>();

	// painful but huh :/
	public void requestCoverDownload(String mbid) {
		if (pendingCoverDownloads.containsKey(mbid)) {
			return;
		}
		pendingCoverDownloads.put(mbid, null);
		List<ReleaseGroupInfo> rGroupInfos;
		try {
			rGroupInfos = MusicMetaProvider.getInstance().searchReleaseGroup(mbid);
		} catch (IOException e) {
			// TODO: handle this too
			Toast.makeText(context, context.getResources().getString(com.github.bawey.melotonine.R.string.connection_error), Toast.LENGTH_SHORT)
					.show();
			pendingCoverDownloads.remove(mbid);
			return;
		}
		if (rGroupInfos.size() != 1) {
			throw new RuntimeException("searching release group yielded <> 1 result");
		}
		ReleaseGroupInfo rGroupInfo = rGroupInfos.get(0);
		Collection<Album> albums = Album.search(rGroupInfo.getTitle(), rGroupInfo.getArtists().get(0).getName(), Constants.LASTFM_API_KEY);
		String imageUrl = null;
		for (Album album : albums) {
			if (rGroupInfo.getReleaseMbids().contains(album.getMbid())) {
				for (ImageSize imageSize : ImageSize.values()) {
					String tempUrl = album.getImageURL(imageSize);
					if (tempUrl != null && tempUrl.length() > 0) {
						imageUrl = tempUrl;
					}
				}
				break;
			}
		}
		if (imageUrl != null) {
			pendingCoverDownloads.put(mbid, downloadImage(imageUrl, COVER_DESC, mbid));
			Log.d("Download", "Started a download of some cover");
		} else {
			// pendingCoverDownloads.remove(mbid);
		}
	}

	private long downloadImage(String imageUrl, String desc, String mbid) {
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(imageUrl));
		request.setDescription(desc);
		request.setTitle(mbid);
		request.setShowRunningNotification(true);
		request.setVisibleInDownloadsUi(true);
		request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_PICTURES, desc + "_" + mbid);

		File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), desc + "_" + mbid);
		if (file.exists()) {
			file.delete();
		}
		return downloadManager.enqueue(request);
	}

	public void requestArtistImageDownload(String mbid) {
		if (pendingArtistImageDownloads.containsKey(mbid)) {
			return;
		}
		pendingArtistImageDownloads.put(mbid, null);

		Artist fmArtist = Artist.getInfo(mbid, Constants.LASTFM_API_KEY);
		String imageUrl = null;
		for (ImageSize imgSize : ImageSize.values()) {
			String tempUrl = fmArtist.getImageURL(imgSize);
			if (tempUrl != null && tempUrl.length() > 0) {
				imageUrl = tempUrl;
			}
		}
		if (imageUrl != null) {
			pendingArtistImageDownloads.put(mbid, downloadImage(imageUrl, ARTIST_DESC, mbid));
		}

	}

	public void setID3(DbDownload dbDownload) throws IOException, ID3WriteException {
		File src = new File(dbDownload.getFilePath());
		if (src.exists()) {
			MusicMetadataSet srcMMS = new MyID3().read(src);
			if (srcMMS != null) {
				MusicMetadata metadata = (MusicMetadata) srcMMS.getSimplified();
				DbRecording rec = dbDownload.getRecording();
				metadata.setSongTitle(rec.getTitle());
				DbArtist artist = rec.getArtist();
				if (artist != null) {
					metadata.setArtist(artist.getName());
				}
				DbRelease rel = rec.getRelease();
				if (rel != null) {
					metadata.setAlbum(rel.getTitle());
					metadata.setYear(new StringBuilder().append(rel.getReleaseYear()).toString());
				}
				metadata.setTrackNumber(rec.getTrackNumber());

				if (rel.getImagePath() != null && rel.getImagePath().length() > 0) {
					File img = (new File(rel.getImagePath()));
					Vector<ImageData> fileList = new Vector<ImageData>();
					ImageData data = new ImageData(readFile(img), "", "", 3);
					fileList.add(data);
					metadata.setPictureList(fileList);
				}
				new MyID3().update(src, srcMMS, metadata);
			}
		}
	}

	public static byte[] readFile(File file) throws IOException {
		RandomAccessFile f = new RandomAccessFile(file, "r");
		try {
			long longlength = f.length();
			int length = (int) longlength;
			if (length != longlength)
				throw new IOException("File size >= 2 GB");

			byte[] data = new byte[length];
			f.readFully(data);
			return data;
		} finally {
			f.close();
		}
	}

	BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
				long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
				Log.d("Download", "Received notification about download #" + downloadId + " completion");
				Query query = new Query();
				query.setFilterById(downloadId);
				Cursor c = downloadManager.query(query);
				if (c.moveToFirst()) {
					int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
					if (DownloadManager.STATUS_SUCCESSFUL == c.getInt(columnIndex)) {
						String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
						Log.d("Download", downloadId + "# success! local uri: " + uriString);
						DatabaseHelper dHelper = DatabaseHelper.getInstance();

						DbDownload example = new DbDownload();
						example.setDownloadId(downloadId);

						String pathString = uriString.substring(7);
						String title = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
						String desc = c.getString(c.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION));

						if (desc.equals(COVER_DESC)) {
							DbRelease release = DatabaseHelper.getInstance().getReleaseByGroupMbid(title);
							release.setImagePath(pathString);
							DatabaseHelper.getInstance().getReleaseRed().update(release);
							Log.d("Download", downloadId + "Release updated with a cover path");
							pendingCoverDownloads.remove(title);
						} else if (desc.equals(ARTIST_DESC)) {
							DbArtist artist = DatabaseHelper.getInstance().getArtistByMbid(title);
							artist.setImagePath(pathString);
							DatabaseHelper.getInstance().getArtistRed().update(artist);
							Log.d("Download", downloadId + "Artist updated with a photo path");
							pendingArtistImageDownloads.remove(title);
						} else {

							for (DbDownload dbDownload : dHelper.getDownloadRed().queryForMatching(example)) {
								if (dbDownload.getFilePath().equals(pathString)) {
									Log.d("Download", downloadId + "marked the file as downloaded!!!");
									dbDownload.setFinihed(true);
									dHelper.getDownloadRed().update(dbDownload);
									try {
										setID3(dbDownload);
									} catch (Exception e) {
										Log.w("ID3", e.getMessage());
										e.printStackTrace();
									}
									Intent broadcastIntent = new Intent();
									broadcastIntent.setAction(Melotonine.SONG_FETCHED_BROADCAST);
									context.sendBroadcast(broadcastIntent);
									context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(uriString)));
									return;
								}
							}
						}

						if (desc.equals(COVER_DESC) || desc.equals(ARTIST_DESC)) {
							Intent i = new Intent();
							i.setAction(Melotonine.NEW_ARTWORK_AVAILABLE);
							i.putExtra("mbid", title);
							context.sendBroadcast(i);
						}

					} else {
						downloadManager.remove(downloadId);
					}
				}
			}
		}
	};

	public void databaseCleanup() {
		new Thread(cleanUp).start();
	}

	private Runnable cleanUp = new Runnable() {
		@Override
		public void run() {
			DatabaseHelper db = DatabaseHelper.getInstance();

			// delete some stalled audio DbRecordings
			for (DbDownload download : db.getDownloadRed().queryForAll()) {
				if (download.isFinihed()) {
					if (!new File(download.getFilePath()).exists()) {
						Log.d("Maintainance", "file not found, removing " + download);
						db.getDownloadRed().delete(download);
					}
				} else {
					Query query = new Query();
					query.setFilterById(download.getDownloadId());
					Cursor c = downloadManager.query(query);
					if (c.moveToFirst()) {
						int columnIndex = c.getColumnIndex(DownloadManager.COLUMN_STATUS);
						if (DownloadManager.STATUS_FAILED == c.getInt(columnIndex)) {
							downloadManager.remove(download.getDownloadId());
							db.getDownloadRed().delete(download);
							Log.d("Maintainance", "failed, removing " + download);
						}
					}
				}
			}

			// delete cover and photos entries where covers are missing
			for (DbRelease release : db.getReleaseRed().queryForAll()) {
				if (release.getImagePath() != null && !new File(release.getImagePath()).exists()) {
					release.setImagePath(null);
					Log.d("Maintainance", "No image found, setting null " + release);
					db.getReleaseRed().update(release);
				}
			}
			for (DbArtist artist : db.getArtistRed().queryForAll()) {
				if (artist.getImagePath() != null && !new File(artist.getImagePath()).exists()) {
					artist.setImagePath(null);
					db.getArtistRed().update(artist);
					Log.d("Maintainance", "No image found, setting null " + artist);
				}
			}

			// find all *mp3 files and delete them if not present in database
			List<File> dirsToCheck = new LinkedList<File>();
			dirsToCheck.add(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), Preferences.getInstance()
					.getMusicDir()));
			while (!dirsToCheck.isEmpty()) {
				File currentDir = dirsToCheck.get(0);
				dirsToCheck.remove(0);
				if (!currentDir.exists()) {
					continue;
				}
				File[] files = currentDir.listFiles();
				if (files.length == 0) {
					while (files.length == 0) {
						File temp = currentDir;
						currentDir = temp.getParentFile();
						temp.delete();
						files = currentDir.listFiles();
					}
					continue;
				}
				for (int i = 0; i < files.length; ++i) {
					if (isAudio(files[i]) && db.getDownloadByAbsolutePath(files[i].getAbsolutePath()) == null) {
						Log.d("Maintainance", "Stray audio, deleting " + files[i].getAbsolutePath());
						files[i].delete();
					} else if (files[i].isDirectory()) {
						dirsToCheck.add(files[i]);
					}
				}
			}

			// Send song fetched as it fits (now)
			Intent i = new Intent();
			i.setAction(Melotonine.SONG_FETCHED_BROADCAST);
			context.sendBroadcast(i);

		}
	};

	private boolean isAudio(File file) {
		String[] tokens = file.getName().split("\\.");
		return tokens.length > 0 && tokens[tokens.length - 1].equalsIgnoreCase("mp3");
	}
}
