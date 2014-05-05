package org.fruct.oss.explodethem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import uk.co.jarofgreen.lib.ShakeDetectActivity;
import uk.co.jarofgreen.lib.ShakeDetectActivityListener;

public class MainActivity extends Activity implements ShakeDetectActivityListener {
	private static final String TAG = "MainActivity";
	private ExplodeView explodeView;

	private ShakeDetectActivity shakeDetector;
	private Flavor.Banner banner;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		banner = Flavor.setupBanner(this);

		explodeView = (ExplodeView) findViewById(R.id.explode_view);
		if (savedInstanceState != null) {
			explodeView.setInitialState(savedInstanceState);
		}

		if (Flavor.isFull()) {
			shakeDetector = new ShakeDetectActivity(this);
			shakeDetector.addListener(this);
		}
	}

	@Override
	protected void onPause() {
		if (explodeView.getThread() != null) {
			explodeView.getThread().stopRendering();
		}
		if (Flavor.isFull()) {
			shakeDetector.onPause();
		}
		banner.pause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		banner.resume();
		if (explodeView.getThread() != null) {
			explodeView.getThread().continueRendering();
		}

		if (Flavor.isFull()) {
			shakeDetector.onResume();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		banner.destroy();
	}

	public void setBannerVisibility(final boolean isVisible) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				banner.setVisibility(isVisible);
			}
		});
	}

	@Override
	public void onBackPressed() {
		if (explodeView.getThread() != null) {
			if (!explodeView.getThread().popState()) {
				explodeView.getThread().pushState("menu");
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (explodeView.getThread() != null) {
			explodeView.storeState(outState);
		}

		Log.d(TAG, "onSaveInstanceState");
	}


	@Override
	public void shakeDetected() {
		Log.d(TAG, "Shake detected");

		if (!getPackageName().equals("org.fruct.oss.explodethem.full")) {
			throw new RuntimeException("Wrong permissions");
		}

		if (explodeView.getThread() != null) {
			explodeView.getThread().shakeDetected();
		}
	}
}
