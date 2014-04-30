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

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		explodeView = (ExplodeView) findViewById(R.id.explode_view);

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
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (explodeView.getThread() != null) {
			explodeView.getThread().continueRendering();
		}

		if (Flavor.isFull()) {
			shakeDetector.onResume();
		}
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
