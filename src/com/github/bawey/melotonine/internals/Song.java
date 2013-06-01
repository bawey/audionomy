package com.github.bawey.melotonine.internals;


public class Song {

	private String absolutePath;

	private String imagePath;
	private String title;
	private String artist;
	private String album;
	private short year;
	private short duration;

	/**
	 * TODO:
	 * 
	 */
	public Song(String absolutePath) {
		this.absolutePath = absolutePath;
	}
	
	public String getImagePath() {
		return imagePath;
	}



	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}



	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public short getYear() {
		return year;
	}

	public void setYear(short year) {
		this.year = year;
	}

	public short getDuration() {
		return duration;
	}

	public void setDuration(short duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		return "Song [title=" + title + ", artist=" + artist + ", album="
				+ album + ", year=" + year + ", duration=" + duration + "]";
	}
	
}
