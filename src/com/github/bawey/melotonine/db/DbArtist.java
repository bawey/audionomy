package com.github.bawey.melotonine.db;

import org.musicbrainz.android.api.data.Artist;
import org.musicbrainz.android.api.data.ArtistSearchResult;
import org.musicbrainz.android.api.data.ReleaseArtist;

import com.j256.ormlite.field.DatabaseField;

public class DbArtist {

	public static final String COL_NAME_MBID = "mbid";
	public static final String COL_NAME_NAME = "name";
	public static final String COL_NAME_COUNTRY = "country";
	public static final String COL_NAME_IMAGE_PATH = "imagePath";

	@DatabaseField(generatedId = true)
	int id;

	@DatabaseField(index = true, columnName = COL_NAME_MBID)
	String mbid;

	@DatabaseField(index = true, columnName = COL_NAME_NAME)
	String name;

	@DatabaseField(columnName = COL_NAME_COUNTRY)
	String country;

	@DatabaseField(columnName = COL_NAME_IMAGE_PATH)
	String imagePath;

	public DbArtist() {
	}

	public DbArtist(ArtistSearchResult asr) {
		mbid = asr.getMbid();
		name = asr.getName();
	}

	public DbArtist(Artist artist) {
		mbid = artist.getMbid();
		name = artist.getName();
		country = artist.getCountry();
	}

	public DbArtist(ReleaseArtist releaseArtist) {
		mbid = releaseArtist.getMbid();
		name = releaseArtist.getName();
	}

	/** data manipulation **/

	public String getMbid() {
		return mbid;
	}

	public void setMbid(String mbid) {
		this.mbid = mbid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public int getId() {
		return id;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	@Override
	public String toString() {
		return "DbArtist [mbid=" + mbid + ", name=" + name + ", country=" + country + ", imagePath=" + imagePath + "]";
	}

}
