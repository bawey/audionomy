package com.github.bawey.melotonine.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.bawey.melotonine.R;

public class LibraryCategoriesAdapter extends BaseAdapter {

	private List<Integer> options = new ArrayList<Integer>(3);
	private Context context;

	public LibraryCategoriesAdapter(Context context, int rowMode) {
		this.context = context;
		switch (rowMode) {
		case R.id.showSongs:
			options.add(R.string.song);
		case R.id.showAlbums:
			options.add(R.string.album);
		default:
			options.add(R.string.artist);
		}
	}

	@Override
	public int getCount() {
		return options.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return options.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView view = new TextView(context);
		view.setText(options.get(position));
		return view;
	}

}
