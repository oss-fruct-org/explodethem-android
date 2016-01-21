package org.fruct.oss.explodethem;

import android.app.Application;

public class App extends Application {
	@Override
	public void onCreate() {
		super.onCreate();

		AdwowFlavor.initializeAdwow(this);
	}
}
