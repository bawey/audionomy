package com.github.bawey.melotonine.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

public class DbDownload {
	public static final String COL_NAME_DOWNLOAD_ID = "downloadId";
	public static final String COL_NAME_RECORDING = "recording_mbid";
	public static final String COL_NAME_FILE_PATH = "filePath";
	public static final String COL_NAME_IS_FINISHED = "isFinihed";

	@DatabaseField(generatedId = true)
	int id;

	@DatabaseField(columnName = COL_NAME_DOWNLOAD_ID)
	long downloadId;

	@DatabaseField(index = true, foreign = true, foreignColumnName = "mbid", columnName = COL_NAME_RECORDING)
	DbRecording recording;

	@DatabaseField(columnName = COL_NAME_FILE_PATH)
	String filePath;

	@DatabaseField(columnName = COL_NAME_IS_FINISHED)
	boolean isFinihed;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getDownloadId() {
		return downloadId;
	}

	public void setDownloadId(long downloadId) {
		this.downloadId = downloadId;
	}

	public DbRecording getRecording() {
		return recording;
	}

	public void setRecording(DbRecording recording) {
		this.recording = recording;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean isFinihed() {
		return isFinihed;
	}

	public void setFinihed(boolean isFinihed) {
		this.isFinihed = isFinihed;
	}

	@Override
	public String toString() {
		return "DbDownload [downloadId=" + downloadId + ", recording=" + recording + ", filePath=" + filePath + ", isFinihed=" + isFinihed + "]";
	}

}
