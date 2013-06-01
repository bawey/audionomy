package com.github.bawey.melotonine.listeners;

import com.github.bawey.melotonine.activities.PlayerActivity;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class OnPlaybackEndListener implements OnCompletionListener {

	private PlayerActivity player;

	public OnPlaybackEndListener(PlayerActivity player) {
		this.player = player;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		player.followingTrack();
	}

}
