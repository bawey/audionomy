package com.github.bawey.melotonine.singletons;

import com.perm.kate.api.Api;

public class VkApi extends Api {

	private static VkApi instance;

	private VkApi(String accessToken, String apiId) {
		super(accessToken, apiId);
	}

	public static synchronized VkApi getInstance() {
		if (instance == null) {
			synchronized (VkApi.class) {
				if (Preferences.isInit() && Preferences.getInstance().getAccessToken() != null && instance == null) {
					init(Preferences.getInstance().getAccessToken(), Constants.VK_API_KEY);
				}
			}
		}
		return instance;
	}

	public static synchronized void init(String accessToken, String apiId) {
		instance = new VkApi(accessToken, apiId);
	}

}
