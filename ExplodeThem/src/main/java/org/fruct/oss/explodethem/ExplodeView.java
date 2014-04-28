package org.fruct.oss.explodethem;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class ExplodeView extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "ExplodeView";
	private ExplodeThread thread;

	private String newHighscoreName;
	private int newHighscore;

	public ExplodeView(Context context, AttributeSet attrs) {
		super(context, attrs);

		SurfaceHolder holder = getHolder();
		assert holder != null;
		holder.addCallback(this);

		setFocusable(true);
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		Log.d(TAG, "surfaceCreated");

		thread = new ExplodeThread(getContext(), surfaceHolder);
		thread.setRunning(true);

		thread.startNewGame();

		if (newHighscoreName != null) {
			thread.showHighscore(newHighscoreName, newHighscore);
			newHighscoreName = null;
		}

		thread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
		Log.d(TAG, "surfaceChanged");
		thread.setSurfaceSize(width, height);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		Log.d(TAG, "surfaceDestroyed");

		thread.setRunning(false);

		try {
			Log.d(TAG, "waiting thread to finish");
			thread.join();
			Log.d(TAG, "thread finished");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Log.d(TAG, "Releasing thread");
		thread.release();
		thread = null;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			thread.touchDown(event.getX(), event.getY());
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			thread.touchUp(event.getX(), event.getY());
		} else {
			return super.onTouchEvent(event);
		}

		return true;
	}

	public ExplodeThread getThread() {
		return thread;
	}

	public void updateHighscore(String name, int score) {
		newHighscore = score;
		newHighscoreName = name;

		if (thread != null) {
			thread.showHighscore(newHighscoreName, newHighscore);
			newHighscoreName = null;
		}
	}
}
