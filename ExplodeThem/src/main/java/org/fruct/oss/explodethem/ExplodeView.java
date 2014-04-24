package org.fruct.oss.explodethem;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class ExplodeView extends SurfaceView implements SurfaceHolder.Callback {
	private final ExplodeThread thread;

	public ExplodeView(Context context, AttributeSet attrs) {
		super(context, attrs);

		SurfaceHolder holder = getHolder();
		assert holder != null;
		holder.addCallback(this);

		thread = new ExplodeThread(getContext(), holder);

		setFocusable(true);
	}

	@Override
	public void surfaceCreated(SurfaceHolder surfaceHolder) {
		thread.setRunning(true);
		thread.start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
		thread.setSurfaceSize(width, height);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
		thread.setRunning(false);

		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
}
