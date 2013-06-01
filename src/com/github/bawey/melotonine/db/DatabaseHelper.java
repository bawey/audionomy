package com.github.bawey.melotonine.db;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.bawey.melotonine.R;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	/** **/
	public static final String DATABASE_NAME = "baweyPlayer.db";
	public static final int DATABASE_VERSION = 19;

	/** **/
	private Dao<DbRecording, Integer> recordingDao = null;
	private RuntimeExceptionDao<DbRecording, Integer> recordingRed = null;

	private Dao<DbRelease, Integer> releaseDao = null;
	private RuntimeExceptionDao<DbRelease, Integer> releaseRed = null;

	private Dao<DbArtist, Integer> artistDao = null;
	private RuntimeExceptionDao<DbArtist, Integer> artistRed = null;

	private Dao<DbDownload, Integer> downloadDao = null;
	private RuntimeExceptionDao<DbDownload, Integer> downloadRed = null;

	private static DatabaseHelper instance;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
	}

	public static synchronized void init(Context context) {
		if (instance == null) {
			instance = new DatabaseHelper(context);
		}
	}

	public static DatabaseHelper getInstance() {
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
		Log.i(this.getClass().getName(), "database create start");
		try {
			TableUtils.createTable(arg1, DbRecording.class);
			TableUtils.createTable(arg1, DbRelease.class);
			TableUtils.createTable(arg1, DbArtist.class);
			TableUtils.createTable(arg1, DbDownload.class);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		Log.i(this.getClass().getName(), "database create complete");
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, ConnectionSource arg1, int arg2, int arg3) {
		try {
			TableUtils.dropTable(arg1, DbRecording.class, true);
			TableUtils.dropTable(arg1, DbRelease.class, true);
			TableUtils.dropTable(arg1, DbArtist.class, true);
			TableUtils.dropTable(arg1, DbDownload.class, true);
			onCreate(arg0, arg1);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/** returns RecordingDao **/
	public synchronized Dao<DbRecording, Integer> getRecordingDao() throws SQLException {
		if (recordingDao == null) {
			recordingDao = getDao(DbRecording.class);
		}
		return recordingDao;
	}

	/** **/
	public synchronized RuntimeExceptionDao<DbRecording, Integer> getRecordingRed() {
		if (recordingRed == null) {
			recordingRed = getRuntimeExceptionDao(DbRecording.class);
		}
		return recordingRed;
	}

	public synchronized Dao<DbRelease, Integer> getReleaseDao() throws SQLException {
		if (releaseDao == null) {
			releaseDao = getDao(DbRelease.class);
		}
		return releaseDao;
	}

	public synchronized RuntimeExceptionDao<DbRelease, Integer> getReleaseRed() {
		if (releaseRed == null) {
			releaseRed = getRuntimeExceptionDao(DbRelease.class);
		}
		return releaseRed;
	}

	public synchronized Dao<DbArtist, Integer> getArtistDao() throws SQLException {
		if (artistDao == null) {
			artistDao = getDao(DbArtist.class);
		}
		return artistDao;
	}

	public synchronized RuntimeExceptionDao<DbArtist, Integer> getArtistRed() {
		if (artistRed == null) {
			artistRed = getRuntimeExceptionDao(DbArtist.class);
		}
		return artistRed;
	}

	public synchronized Dao<DbDownload, Integer> getDownloadDao() throws SQLException {
		if (downloadDao == null) {
			downloadDao = getDao(DbDownload.class);
		}
		return downloadDao;
	}

	public synchronized RuntimeExceptionDao<DbDownload, Integer> getDownloadRed() {
		if (downloadRed == null) {
			downloadRed = getRuntimeExceptionDao(DbDownload.class);
		}
		return downloadRed;
	}

	public DbRecording getRecordingByMbid(String mbid) {
		List<DbRecording> recordings = getRecordingRed().queryForEq("mbid", mbid);
		return recordings.isEmpty() ? null : recordings.get(0);
	}

	public DbRelease getReleaseByGroupMbid(String groupMbid) {
		List<DbRelease> releases = getReleaseRed().queryForEq("groupMbid", groupMbid);
		return releases.isEmpty() ? null : releases.get(0);
	}

	public DbArtist getArtistByMbid(String mbid) {
		List<DbArtist> artists = getArtistRed().queryForEq("mbid", mbid);
		return artists.isEmpty() ? null : artists.get(0);
	}

	public DbDownload getDownloadByMbid(String mbid) {
		List<DbDownload> downloads = getDownloadRed().queryForEq("recording_mbid", mbid);
		return downloads.isEmpty() ? null : downloads.get(0);
	}

	public List<DbRelease> getReleasesByArtistMbid(String mbid) {
		try {
			Log.d("Database", "asking for recordings with query: " + mbid);
			QueryBuilder<DbRelease, Integer> qb = getReleaseRed().queryBuilder();
			qb.where().eq(DbRelease.COL_NAME_ARTIST, mbid);
			qb.orderBy(DbRelease.COL_NAME_RELEASE_YEAR, true);
			Log.d("Database", qb.prepareStatementString());
			return qb.query();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<DbArtist> getArtistsByAnyFiedl(String query) {
		//String[] tokens = query.split(" ");
		return null;
	}

	private Where<DbRecording, Integer> getRecordingsByRgidOrAridWhere(String id) {
		try {
			return getRecordingRed().queryBuilder().where().eq(DbRecording.COL_NAME_RELEASE, id).or().eq(DbRecording.COL_NAME_ARTIST, id);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private QueryBuilder<DbDownload, Integer> getFinishedDownloadsQB() {
		try {
			QueryBuilder<DbDownload, Integer> qb = getDownloadRed().queryBuilder();
			qb.where().eq(DbDownload.COL_NAME_IS_FINISHED, true);
			Log.d("Database", qb.prepareStatementString());
			return qb;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<DbRecording> getRecordingsByRgidOrArid(String id) {
		try {
			return getRecordingsByRgidOrAridWhere(id).query();
		} catch (SQLException sqle) {
			throw new RuntimeException(sqle);
		}
	}

	public List<DbRecording> getDownloadedRecordings() {
		try {
			return getRecordingRed().queryBuilder().where()
					.in(DbRecording.COL_NAME_MBID, getFinishedDownloadsQB().selectColumns(DbDownload.COL_NAME_RECORDING)).query();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<DbRecording> getDownloadedRecordingsByRgidOrArid(String id) {
		try {
			Where<DbRecording, Integer> wb = getRecordingRed().queryBuilder().where().eq(DbRecording.COL_NAME_RELEASE, id).or()
					.eq(DbRecording.COL_NAME_ARTIST, id);
			return wb.and().in(DbRecording.COL_NAME_MBID, getFinishedDownloadsQB().selectColumns(DbDownload.COL_NAME_RECORDING)).query();

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public List<DbDownload> getDownloadsByMbids(List<String> mbids) {
		try {
			return getDownloadRed().queryBuilder().where().in(DbDownload.COL_NAME_RECORDING, mbids).query();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public DbDownload getDownloadByAbsolutePath(String path) {
		try {
			SelectArg arg = new SelectArg();
			arg.setValue(path);
			return getDownloadRed().queryBuilder().where().eq(DbDownload.COL_NAME_FILE_PATH, arg).queryForFirst();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		recordingDao = null;
		recordingRed = null;
		artistDao = null;
		artistRed = null;
		releaseDao = null;
		releaseRed = null;
		downloadDao = null;
		downloadRed = null;
	}
}
