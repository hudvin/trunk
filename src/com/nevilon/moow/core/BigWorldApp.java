package com.nevilon.moow.core;

import android.app.Application;

public class BigWorldApp extends Application {

	public BigWorldApp() {
		super();
	}

	@Override
	public void onCreate() {
		Preferences.init(this);
	}

}
