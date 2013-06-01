package com.github.bawey.melotonine.runnables;

import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.github.bawey.melotonine.R;
import com.github.bawey.melotonine.activities.PlayerActivity;
import com.github.bawey.melotonine.singletons.PlaybackQueue;

public class ProgressBarUpdater implements Runnable, OnSeekBarChangeListener {

	PlayerActivity activity;
	MediaPlayer player;
	Handler handler;
	PlaybackQueue queue;
	SeekBar seekBar;

	public ProgressBarUpdater(PlayerActivity activity) {
		this.activity = activity;
		this.handler = activity.getProgressBarHandler();
		this.player = activity.getMediaPlayer();
		this.queue = activity.getPlaybackQueue();
		this.seekBar = (SeekBar) activity.findViewById(R.id.progressBar);
		// plug itself as a listener to seekBar
		seekBar.setOnSeekBarChangeListener(this);
	}

	private boolean needsUpdate = false;
	
	public boolean isNeedsUpdate() {
		return needsUpdate;
	}

	public void setNeedsUpdate(boolean needsUpdate) {
		this.needsUpdate = needsUpdate;
	}

	@Override
	public void run() {
		if (player.isPlaying() || needsUpdate) {
			int duration = player.getDuration();
			int position = player.getCurrentPosition();
			seekBar.setMax(duration);
			seekBar.setProgress(position);
			needsUpdate = false;
		}
		handler.postDelayed(this, 100);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (fromUser) {
			player.seekTo(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		handler.removeCallbacks(this);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		handler.postDelayed(this, 100);
	}

}
