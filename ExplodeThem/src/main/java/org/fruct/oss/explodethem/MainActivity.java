package org.fruct.oss.explodethem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	private ExplodeView explodeView;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		explodeView = (ExplodeView) findViewById(R.id.explode_view);
	}

	@Override
	protected void onPause() {
		if (explodeView.getThread() != null) {
			explodeView.getThread().pause();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (explodeView.getThread() != null) {
			explodeView.getThread().unpause();
		}
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
}
