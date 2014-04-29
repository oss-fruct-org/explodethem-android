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

		shakeDetector = new ShakeDetectActivity(this);
		shakeDetector.addListener(this);
	}

	@Override
	protected void onPause() {
		if (explodeView.getThread() != null) {
			explodeView.getThread().pause();
		}
		super.onPause();

		shakeDetector.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (explodeView.getThread() != null) {
			explodeView.getThread().unpause();
		}

		shakeDetector.onResume();
	}

	@Override
	public void onBackPressed() {
		if (explodeView.getThread() != null) {
			if (!explodeView.getThread().popState()) {
				finish();
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == 0) {
			String name = data.getStringExtra(GameOverActivity.EXTRA_NAME);
			int score = data.getIntExtra(GameOverActivity.EXTRA_SCORE, 0);
			Log.d(TAG, "onActivityResult " + name);
			explodeView.updateHighscore(name, score);
		}
	}

	@Override
	public void shakeDetected() {
		Log.d(TAG, "Shake detected");

		if (explodeView.getThread() != null) {
			explodeView.getThread().shakeDetected();
		}
	}
}
