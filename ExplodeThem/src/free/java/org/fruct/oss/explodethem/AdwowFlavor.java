package org.fruct.oss.explodethem;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import ru.adwow.sdk.AdWow;
import ru.adwow.sdk.AdWowFragment;
import ru.adwow.sdk.Callback;
import ru.adwow.sdk.Form;
import ru.adwow.sdk.Notification;
import ru.adwow.sdk.SessionListener;
import ru.adwow.sdk.Unit;

public class AdwowFlavor {
	private static final String TAG = "AdwowFlavor";

	private static final String ADWOW_HELPER_FRAGMENT_TAG = "org.fruct.oss.explodethem.ADWOW_HELPER_FRAGMENT_TAG";

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

		AdwowHelperFragment adwowHelperFragment
				= (AdwowHelperFragment) fragmentManager.findFragmentByTag(ADWOW_HELPER_FRAGMENT_TAG);
		if (adwowHelperFragment == null) {
			adwowHelperFragment = new AdwowHelperFragment();
			fragmentManager.beginTransaction().add(adwowHelperFragment, ADWOW_HELPER_FRAGMENT_TAG).commit();
		}
	}

	public static void saveMoment(final Context context, String name) {
		AdWow.getInstance().saveMoment(name, new Callback() {
			@Override
			public void onFailed(AdWow adWow, Exception e) {
				Log.e(TAG, "Save adwow moment failed", e);
			}
			@Override
			public void onFinished(AdWow adWow, Unit unit) {
				Log.e(TAG, "Save adwow moment success");
				unit.show(context);
			}
		});
	}

	public static void saveMoment(final Context context, String name, int number) {
		AdWow.getInstance().saveMoment(name, number, new Callback() {
			@Override
			public void onFailed(AdWow adWow, Exception e) {
				Log.e(TAG, "Save adwow moment failed", e);
			}
			@Override
			public void onFinished(AdWow adWow, Unit unit) {
				Log.e(TAG, "Save adwow moment success");
				unit.show(context);
			}
		});
	}

	private static class MySessionListener implements SessionListener {
		private static final String TAG = "MySessionListener";

		@Override
		public void onSessionOpened(AdWow adWow, Unit unit, Exception e) {
			Log.d(TAG, "onSessionOpened");
		}

		@Override
		public void onSessionStateChanged(AdWow adWow) {
			Log.d(TAG, "onSessionStateChanged");
		}

		@Override
		public void onSessionClosed(AdWow adWow, Exception e) {
			Log.d(TAG, "onSessionClosed");
		}

		@Override
		public void onUnitShow(Unit unit) {
			Log.d(TAG, "onUnitShow");

		}

		@Override
		public void onUnitDismiss(Unit unit) {
			Log.d(TAG, "onUnitDismiss");
		}

		@Override
		public void onFormShow(Form form) {
			Log.d(TAG, "onFormShow");
		}

		@Override
		public void onFormDismiss(Form form) {
			Log.d(TAG, "onFormDismiss");
		}

		@Override
		public void onNotificationShow(Notification notification) {
			Log.d(TAG, "onNotificationShow");
		}

		@Override
		public void onNotificationDismiss(Notification notification) {
			Log.d(TAG, "onNotificationDismiss");
		}

		@Override
		public void onNotificationDismissWithClick(Notification notification) {
			Log.d(TAG, "onNotificationDismissWithClick");
		}
	}
}
