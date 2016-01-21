package org.fruct.oss.explodethem;

import android.app.Activity;
import android.app.Application;
import android.app.FragmentManager;

import ru.adwow.sdk.AdWow;
import ru.adwow.sdk.AdWowFragment;
import ru.adwow.sdk.Callback;
import ru.adwow.sdk.Unit;

public class AdwowFlavor {
	private static final String ADWOW_FRAGMENT_TAG = "org.fruct.oss.explodethem.ADWOW_FRAGMENT_TAG";

	public static boolean isEnabled() {
		return true;
	}

	public static void initializeAdwow(Application app) {
		String key = app.getString(R.string.adwow_key);
		String secret = app.getString(R.string.adwow_secret);
		AdWow.init(app, key, secret);
	}

	public static void setupFragment(Activity activity) {
		FragmentManager fragmentManager = activity.getFragmentManager();

		AdWowFragment adwowFragment = (AdWowFragment) fragmentManager.findFragmentByTag(ADWOW_FRAGMENT_TAG);
		if (adwowFragment == null) {
			adwowFragment = new AdWowFragment();
			fragmentManager.beginTransaction().add(adwowFragment, ADWOW_FRAGMENT_TAG).commit();
		}
	}

	public static void startAdwow(Activity activity) {
		AdWow.getInstance().openSession(new Callback() {
			@Override
			public void onFailed(AdWow adwow, Exception exception) {
			}
			@Override
			public void onFinished(AdWow adwow, Unit unit) {
			}
		});
	}

	public static void stopAdwow(Activity activity) {
		AdWow.getInstance().closeSession(new Callback() {
			@Override
			public void onFailed(AdWow kiip, Exception exception) {
			}

			@Override
			public void onFinished(AdWow adwow, Unit unit) {
			}
		});
	}

	public static void saveMoment(String name) {
		AdWow.getInstance().saveMoment(name, new Callback() {
			@Override
			public void onFailed(AdWow adWow, Exception e) {
			}

			@Override
			public void onFinished(AdWow adWow, Unit unit) {
			}
		});
	}

	public static void saveMoment(String name, int number) {
		AdWow.getInstance().saveMoment(name, number, new Callback() {
			@Override
			public void onFailed(AdWow adWow, Exception e) {
			}

			@Override
			public void onFinished(AdWow adWow, Unit unit) {
			}
		});
	}
}
