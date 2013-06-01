package com.github.bawey.melotonine.singletons;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.util.Log;

import com.github.bawey.melotonine.activities.PlayerActivity;
import com.github.bawey.melotonine.activities.PlayerActivity.PlaybackMode;
import com.github.bawey.melotonine.db.DatabaseHelper;
import com.github.bawey.melotonine.db.DbDownload;
import com.github.bawey.melotonine.db.DbRecording;
import com.github.bawey.melotonine.db.DbRelease;
import com.github.bawey.melotonine.internals.Song;

public class PlaybackQueue {

	private static PlaybackQueue instance = null;

	protected PlaybackQueue() {
	}

	public static PlaybackQueue getInstance() {
		if (instance == null) {
			synchronized (PlaybackQueue.class) {
				if (instance == null) {
					instance = new PlaybackQueue();
				}
			}
		}
		return instance;
	}

	private LinkedList<Song> playbackQueue = new LinkedList<Song>();
	private int currentTrack = 0;

	private PlayerActivity observer;

	public LinkedList<Song> getPlaybackQueueList() {
		return playbackQueue;
	}

	public int getCurrentTrackNo() {
		return currentTrack;
	}

	public void registerObserver(PlayerActivity playerActivity) {
		observer = playerActivity;
	}

	public void removeObserver() {
		observer = null;
	}

	public void enqueue(Song song) {
		playbackQueue.add(song);
		notifyObservers();
	}

	public void enqueue(String filePath, String title, String artist, int duration, String album, String artworkPath) {
		Song song = new Song(filePath);
		song.setTitle(title);
		song.setArtist(artist);
		song.setDuration((short) duration);
		song.setAlbum(album);
		song.setImagePath(artworkPath);
		enqueue(song);
	}

	public void enqueueAll(Collection<Song> songs) {
		playbackQueue.addAll(songs);
		for (Song song : songs) {
			Log.d(this.getClass().getName(), "ADD: " + song.getArtist() + " - " + song.getTitle());
		}
		Log.d("baweyTest", "Added " + songs.size() + " songs to queue");
		notifyObservers();
	}

	public void enqueueAllRecordings(final Collection<DbRecording> dbRecordings) {
		final List<Song> songs = new LinkedList<Song>();
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (DbRecording dbRec : dbRecordings) {
					DbDownload dbDownload = DatabaseHelper.getInstance().getDownloadByMbid(dbRec.getMbid());
					Song song = new Song(dbDownload.getFilePath());
					DbRelease release = dbRec.getRelease();
					if (release != null) {
						song.setAlbum(dbRec.getRelease().getTitle());
						if (release.getImagePath() != null && release.getImagePath().length() > 0) {
							song.setImagePath(release.getImagePath());
						}
					}
					song.setArtist(dbRec.getArtist().getName());
					song.setTitle(dbRec.getTitle());
					song.setDuration((short) dbRec.getDuration());
					songs.add(song);
				}
				enqueueAll(songs);
			}
		}).start();
	}

	public void shuffle() {
		Collections.shuffle(playbackQueue);
		notifyObservers();
	}

	public int nextTrack() {
		if (playbackQueue.size() > 0) {
			currentTrack = (currentTrack + 1) % playbackQueue.size();
		}
		notifyObservers();
		return currentTrack;
	}

	public int previousTrack() {
		if (playbackQueue.size() > 0) {
			currentTrack = (playbackQueue.size() + currentTrack - 1) % playbackQueue.size();

		}
		Log.d("d", "prevTrack result=" + currentTrack);
		notifyObservers();
		return currentTrack;
	}

	public Song getCurrentTrack() {
		if (playbackQueue.size() > currentTrack) {
			return playbackQueue.get(currentTrack);
		} else {
			return null;
		}
	}

	// something...
	public Song getNextTrack(PlaybackMode mode) {
		if (mode.equals(PlaybackMode.REPEAT_ONE)) {
			return getCurrentTrack();
		}
		int nextTrack = nextTrack();
		if (nextTrack > 0 || mode.equals(PlaybackMode.REPEAT_ALL)) {
			return getCurrentTrack();
		} else {
			return null;
		}
	}

	// jailed at first track if no restart allowed
	public Song getPreviousTrack(PlaybackMode mode) {
		if (mode.equals(PlaybackMode.REPEAT_ONE)) {
			return getCurrentTrack();
		}
		previousTrack();
		if (currentTrack != playbackQueue.size() - 1 || mode.equals(PlaybackMode.REPEAT_ALL)) {
			return getCurrentTrack();
		} else {
			return null;
		}
	}

	/**
	 * CRUDE method to return some info about the track
	 * 
	 * @return
	 */
	public String getCurrentTrackInfo() {
		Song track = getCurrentTrack();
		if (track != null) {
			return track.getTitle();
		} else {
			return "";
		}
	}

	public void setCurrentTrackNo(int number) {
		this.currentTrack = number;
		if (observer != null) {
			observer.resyncPlayerWithQueue();
		}
		notifyObservers();
	}

	public void clearQueue() {
		playbackQueue.clear();
		currentTrack = 0;
		notifyObservers();
	}

	private void notifyObservers() {
		if (observer != null) {
			observer.updatePlaylistView(this.playbackQueue, this.currentTrack);
			observer.playAudioIfPending(null);
		}
	}
}
