package com.github.bawey.melotonine.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.github.bawey.melotonine.R;
import com.github.bawey.melotonine.activities.abstracts.AbstractLibraryActivity;
import com.github.bawey.melotonine.adapters.OnlineLibraryRowAdapter;
import com.github.bawey.melotonine.singletons.PlaybackQueue;

public class LibraryActivity extends AbstractLibraryActivity {

	private ListView libraryList;
	private TextView rowsFilter;
	private String currentQuery = "";
	private int rowMode = OnlineLibraryRowAdapter.ROW_MODE_ARTIST;

	private OnEditorActionListener editorActionListener = new OnEditorActionListener() {

		/** TODO: gets launched twice upon pressing enter **/
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			Log.d(this.getClass().getName() + " onEditorAction", "actionId: " + actionId + ", event: " + event);
			if ((actionId == EditorInfo.IME_ACTION_SEARCH || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
				Log.d("yes", "will search now");
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				currentQuery = v.getText().toString();
				Log.d(this.getClass().getName() + ":onEditorAction", "query=" + currentQuery);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				revalidateList();
				return true;
			}
			return false;
		}
	};

	@Override
	public String getArtistMbid() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getReleaseGroupMbid() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		libraryList = (ListView) findViewById(R.id.libraryMatches);

		/** let's wait with that **/

		rowsFilter = (TextView) findViewById(R.id.filterKey);
		rowsFilter.setOnEditorActionListener(editorActionListener);
	}

	public void onCategorySwitched(View view) {
		this.rowMode = view.getId();
		revalidateList();
	}

	@Override
	protected String getCurrentQuery() {
		return this.currentQuery;
	}

	@Override
	protected int getCurrentRowMode() {
		return this.rowMode;
	}

	@Override
	protected ListView getLibraryListView() {
		return this.libraryList;
	}

	@Override
	public int getLayoutId() {
		return R.layout.library_layout;
	}

}
