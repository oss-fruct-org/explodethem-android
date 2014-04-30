package org.fruct.oss.explodethem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class ExplodeThread extends Thread {
	private static final String TAG = "ExplodeThread";

	public static final long TICK_MS = 32;
	public static final long TICK_PER_STEP = 400 / TICK_MS;

	private final Context context;
	private final SurfaceHolder holder;
	private final CommonResources commonResources;

	private boolean isRunning = false;

	private final Object isSuspendedLock = new Object();
	private boolean isSuspended = false;

	private final Object isRunningLock = new Object();

	private HashMap<String, GameState> states = new HashMap<String, GameState>();
	private ArrayList<GameState> stateStack = new ArrayList<GameState>();
	private ArrayList<String> stateStackIds = new ArrayList<String>();

	// Timing variables
	private long gameTime;

	private long ticksElapsed;
	private long framesElapsed;

	private Paint testPaint;
	private int width;
	private int height;
	private final PlayState playState;

	public ExplodeThread(Context context, SurfaceHolder holder, Bundle inState) {
		setName("ExplodeThread");

		this.context = context;
		this.holder = holder;

		testPaint = new Paint();
		testPaint.setTextSize(Utils.getSP(context, 32));
		testPaint.setColor(0xffffffff);
		testPaint.setAntiAlias(true);

		playState = new PlayState(context, this);
		states.put("play", playState);
		states.put("menu", new MenuState(context, this, playState));
		states.put("highscore", new HighscoreState(context, this));
		states.put("nextlevel", new NextLevelState(context, this, playState));
		states.put("gameover", new GameOverState(context, this));
		states.put("help", new HelpState(context, this));
		states.put("about", new AboutState(context, this));

		commonResources = new CommonResources(context);

		if (inState != null) {
			for (String stateId : states.keySet()) {
				Bundle data = inState.getBundle("state-" + stateId);
				if (data != null) {
					states.get(stateId).restoreState(data);
				}
			}
		}
	}

	public void startNewGame(Bundle inState) {
		if (inState == null ) {
			pushState("menu");
		} else {
			for (String stateId : inState.getStringArrayList("states-stack")) {
				pushState(stateId);
			}
		}
	}

	@Override
	public void run() {
		Canvas canvas = null;

		long startTime = System.currentTimeMillis();
		gameTime = System.currentTimeMillis();

		while (isRunning) {
			synchronized (isSuspendedLock) {
				while (isSuspended) {
					try {
						isSuspendedLock.wait();
					} catch (InterruptedException e) {
						if (!isRunning)
							break;
					}
				}
			}

			try {
				canvas = holder.lockCanvas(null);

				synchronized (isRunningLock) {
					if (isRunning) {
						update(canvas);
					}
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

	private void update(Canvas canvas) {
		if (stateStack.isEmpty()) {
			return;
		}

		long nextTime = gameTime + TICK_MS;
		long currentTime = System.currentTimeMillis();

		while (nextTime > currentTime) {
			try {
				long sleepTime = nextTime - currentTime;
				Thread.sleep(sleepTime <= 0 ? 1 : sleepTime);
			} catch (InterruptedException e) {
				return;
			}

			currentTime = System.currentTimeMillis();
		}

		synchronized (holder) {
			if (stateStack.isEmpty()) {
				return;
			}

			long delta = currentTime - gameTime;
			if (delta > 0) {
				//Log.d(TAG, "Delta " + delta);
				gameTime += TICK_MS;

				//ticksElapsed++;
				stateStack.get(stateStack.size() - 1).updatePhysics();
			}

			//framesElapsed++;
			for (GameState state : stateStack) {
				state.draw(canvas);
			}

			//canvas.drawText("TPS: " + ticksElapsed / ((gameTime - startTime) / 1000.), 10, 30, testPaint);
			//canvas.drawText("FPS: " + framesElapsed / ((gameTime - startTime) / 1000.), 10, 50, testPaint);
		}
	}

	public void setRunning(boolean isRunning) {
		synchronized (isRunningLock) {
			this.isRunning = isRunning;

			if (!isRunning) {
				interrupt();
			}
		}
	}

	public void setSurfaceSize(int width, int height) {
		synchronized (holder) {
			this.width = width;
			this.height = height;

			for (GameState state : states.values()) {
				state.setSize(width, height);
			}

			commonResources.resize(width, height);
			continueRendering();
		}
	}

	public void touchDown(float x, float y) {
		synchronized (holder) {
			stateStack.get(stateStack.size() - 1).touchDown(x, y);

			continueRendering();
		}
	}

	public void touchUp(float x, float y, MotionEvent event) {
		synchronized (holder) {
			stateStack.get(stateStack.size() - 1).touchUp(x, y, event);

			continueRendering();
		}
	}

	public void moveEvent(MotionEvent event) {
		synchronized (holder) {
			stateStack.get(stateStack.size() - 1).moveEvent(event);

			continueRendering();
		}
	}

	public void replaceStateStack(String stateId, Bundle args) {
		synchronized (holder) {
			while (popState())
				;

			pushState(stateId, args);

			continueRendering();
		}
	}

	public void replaceStateStack(String stateId) {
		synchronized (holder) {
			while (popState())
				;

			pushState(stateId);

			continueRendering();
		}
	}

	public void pushState(String stateId) {
		synchronized (holder) {
			GameState state = states.get(stateId);
			if (state != null) {
				state.prepare(null);
				stateStack.add(state);
				stateStackIds.add(stateId);
			}

			continueRendering();
		}
	}
	public void pushState(String stateId, Bundle args) {
		synchronized (holder) {
			GameState state = states.get(stateId);
			if (state != null) {
				state.prepare(args);
				stateStack.add(state);
				stateStackIds.add(stateId);
			}

			continueRendering();
		}
	}

	public boolean popState() {
		synchronized (holder) {
			stateStack.remove(stateStack.size() - 1);
			stateStackIds.remove(stateStackIds.size() - 1);

			continueRendering();
			return !stateStack.isEmpty();
		}
	}

	public void release() {
		synchronized (holder) {
			for (GameState state : states.values()) {
				state.destroy();
			}

			stateStack.clear();
			stateStackIds.clear();
		}

		commonResources.destroy();
	}

	public void stopRendering() {
		synchronized (isSuspendedLock) {
			if (isSuspended) {
				return;
			}

			Log.d(TAG, "stopRendering");

			isSuspended = true;
		}
	}

	public void continueRendering() {
		synchronized (isSuspendedLock) {
			if (!isSuspended) {
				return;
			}

			Log.d(TAG, "continueRendering");

			isSuspended = false;
			gameTime = System.currentTimeMillis();
			isSuspendedLock.notifyAll();
		}
	}

	public void showHighscore(String newHighscoreName, int newHighscore) {
		synchronized (holder) {
			pushState("highscore");
		}
	}

	public CommonResources getCommonResources() {
		return commonResources;
	}

	public void shakeDetected() {
		synchronized (holder) {
			if (isRunning && stateStack.get(stateStack.size() - 1) instanceof PlayState) {
				playState.shakeDetected();
				continueRendering();
			}
		}
	}

	public void storeState(Bundle outState) {
		Log.d(TAG, "storeState");

		synchronized (holder) {
			// Store states
			for (String stateId : states.keySet()) {
				Bundle stateBundle = new Bundle();
				states.get(stateId).storeState(stateBundle);
				outState.putBundle("state-" + stateId, stateBundle);
			}

			// Store state stack
			outState.putStringArrayList("states-stack", new ArrayList<String>(stateStackIds));
		}
	}

	public static class BitmapHolder {
		private Bitmap original;
		private Bitmap scaled;
		private Context context;

		BitmapHolder(Context context, String assetFile) {
			this.context = context;
			original = createBitmapFromAsset(assetFile);
		}

		void recycle() {
			original.recycle();
			if (scaled != null) {
				scaled.recycle();
			}

			original = null;
			scaled = null;
		}

		void scale(float width, float height) {
			if (scaled != null) {
				scaled.recycle();
			}

			scaled = Bitmap.createScaledBitmap(original, (int) width, (int) height, true);
		}

		void scaleWidth(float width) {
			float newHeight = width * original.getHeight() / original.getWidth();
			scale(width, newHeight);
		}

		void scaleHeight(float height) {
			float newWidth = height * original.getWidth() / original.getHeight();
			scale(newWidth, height);
		}

		Bitmap getScaled() {
			return scaled;
		}

		Bitmap getOriginal() {
			return original;
		}

		private Bitmap createBitmapFromAsset(String file) {
			InputStream in = null;

			try {
				in = openAsset(file);
				return BitmapFactory.decodeStream(in);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException ignored) {
					}
				}
			}
		}

		private InputStream openAsset(String file) {
			try {
				return context.getAssets().open(file);
			} catch (IOException e) {
				Log.e(TAG, "Can't open asset: " +  file);
				return null;
			}
		}
	}
}
