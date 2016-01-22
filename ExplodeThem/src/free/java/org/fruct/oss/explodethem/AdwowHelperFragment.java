package org.fruct.oss.explodethem;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import ru.adwow.sdk.AdWow;
import ru.adwow.sdk.AdWowFragment;
import ru.adwow.sdk.Callback;
import ru.adwow.sdk.Unit;

// TODO: желательно запрашивать COARSE и FINE как отдельные разрешения
public class AdwowHelperFragment extends Fragment {
	private static final String TAG = "AdwowHelperFragment";

	private static final String ADWOW_FRAGMENT_TAG = "org.fruct.oss.explodethem.ADWOW_FRAGMENT_TAG";
	private static final int PERMISSION_REQUEST_CODE = 123;

	private boolean isAdwowAllowed;
	private boolean isFragmentStarted;
	private boolean isAdwowStarted;

	private boolean isSnackbackShown;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.d(TAG, "onCreate");

		isAdwowAllowed = checkAdwowReadyToSetup(getActivity());

		if (isAdwowAllowed) {
			Log.d(TAG, "Starting adwow fragment immediately");
			setupAdwowFragment();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "onStart");

		isFragmentStarted = true;

		ensureAdwowState();

		showSnackbar();
	}

	// Все методы из Android M будут вызваны только если isAdwowAllowed == false,
	// а это может быть только на Android M.
	@TargetApi(Build.VERSION_CODES.M)
	private void showSnackbar() {
		if (!isSnackbackShown
				&& !isAdwowAllowed
				/*&& shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)*/) {
			View anchor = getActivity().findViewById(R.id.anchor);
			Snackbar.make(anchor, R.string.i_need_your_location, Snackbar.LENGTH_LONG)
					.setAction(android.R.string.ok, new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							onFirstPermissionStagePassed();
						}
					}).show();
			isSnackbackShown = true;
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	private void onFirstPermissionStagePassed() {
		requestPermissions(new String[]{
						Manifest.permission.ACCESS_COARSE_LOCATION,
						Manifest.permission.ACCESS_FINE_LOCATION
				},
				PERMISSION_REQUEST_CODE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == PERMISSION_REQUEST_CODE
				&& grantResults.length == 2
				&& grantResults[0] == PackageManager.PERMISSION_GRANTED
				&& grantResults[1] == PackageManager.PERMISSION_GRANTED) {
			Log.d(TAG, "Permission granted");
			isAdwowAllowed = true;
			ensureAdwowState();
		}
	}

	private void ensureAdwowState() {
		Log.d(TAG, "ensureAdwowState");

		if (!isAdwowStarted && isFragmentStarted && isAdwowAllowed) {
			Log.d(TAG, "Start adwow session");

			isAdwowStarted = true;

			setupAdwowFragment();
			AdWow.getInstance().openSession(new Callback() {
				@Override
				public void onFailed(AdWow adwow, Exception exception) {
					Log.e(TAG, "Start adwow failed", exception);
				}

				@Override
				public void onFinished(AdWow adwow, Unit unit) {
					Log.d(TAG, "Start adwow success");
				}
			});
		}

		if (!isFragmentStarted && isAdwowStarted) {
			Log.d(TAG, "Stop adwow session");

			isAdwowStarted = false;

			AdWow.getInstance().closeSession(new Callback() {
				@Override
				public void onFailed(AdWow kiip, Exception exception) {
					Log.e(TAG, "Stop adwow failed", exception);
				}

				@Override
				public void onFinished(AdWow adwow, Unit unit) {
				}
			});
		}
	}

	@Override
	public void onStop() {
		Log.d(TAG, "onStop");
		isFragmentStarted = false;
		ensureAdwowState();
		super.onStop();
	}

	private void setupAdwowFragment() {
		Log.d(TAG, "setupAdwowFragment");
		FragmentManager fragmentManager = getActivity().getFragmentManager();

		AdWowFragment adwowFragment = (AdWowFragment) fragmentManager.findFragmentByTag(ADWOW_FRAGMENT_TAG);
		if (adwowFragment == null) {
			adwowFragment = new AdWowFragment();
			fragmentManager.beginTransaction().add(adwowFragment, ADWOW_FRAGMENT_TAG).commit();
		}
	}

	private boolean checkAdwowReadyToSetup(Context context) {
		return Build.VERSION.SDK_INT < 23
				|| (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
				&& ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
	}
}
