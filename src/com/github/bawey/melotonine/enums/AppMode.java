package com.github.bawey.melotonine.enums;

/**
 * 
 * @author bawey
 *
 */
public enum AppMode {
	LOCAL, REMOTE;
	
	public static String actionString(){
		return "MELOTONINE_APP_MODE";
	}
}
