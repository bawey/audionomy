package com.github.bawey.melotonine.customGui;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ListView;

import com.github.bawey.melotonine.activities.abstracts.AbstractFullscreenActivity;
import com.github.bawey.melotonine.listeners.FullscreenActivities;
import com.github.bawey.melotonine.listeners.GestureListener;

public class SwipeableListView extends ListView {

	private float mDiffX = 0;
	private float mDiffY = 0;
	private float mLastX = 0;
	private float mLastY = 0;

	private AbstractFullscreenActivity ctx = null;

	public SwipeableListView(Context context) {
		super(context);
		ctx = (AbstractFullscreenActivity) context;
	}

	public SwipeableListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ctx = (AbstractFullscreenActivity) context;
	}

	public SwipeableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx = (AbstractFullscreenActivity) context;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		Log.d("Swiper", "ok, listenning, action: " + ev.getAction());

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// reset difference values
			mDiffX = 0;
			mDiffY = 0;

			mLastX = ev.getX();
			mLastY = ev.getY();
			break;

		case MotionEvent.ACTION_MOVE:
			final float curX = ev.getX();
			final float curY = ev.getY();
			mDiffX += (curX - mLastX);
			mDiffY += (curY - mLastY);
			mLastX = curX;
			mLastY = curY;
			break;

		case MotionEvent.ACTION_UP:
			Log.d("Swiper", "Releasing: X=" + mDiffX + ", Y=" + mDiffY);
			if (Math.abs(mDiffX) > Math.abs(mDiffY)) {
				if (ctx != null) {
					// TODO ugly again!
					FullscreenActivities fa = FullscreenActivities.getByClass(ctx.getClass());
					if (Math.abs(mDiffX) > GestureListener.getMinDistance()) {
						fa = mDiffX < 0 ? fa.getNext() : fa.getPrev();
						Intent i = new Intent(ctx, fa.getLaunchableClass());
						ctx.startActivity(i);
					}
				}
				return false; // do not react to horizontal touch events, these
								// events will be passed to your list item view
			}
		}

		return super.onTouchEvent(ev);
	}

}
