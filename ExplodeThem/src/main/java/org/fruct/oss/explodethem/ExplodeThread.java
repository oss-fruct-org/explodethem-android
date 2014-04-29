package org.fruct.oss.explodethem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExplodeThread extends Thread {
	private static final String TAG = "ExplodeThread";

	public static final long TICK_MS = 32;
	public static final long TICK_PER_STEP = 400 / TICK_MS;

	private final Context context;
	private final SurfaceHolder holder;
	private final CommonResources commonResources;

	private boolean isRunning = false;
	private final Object isRunningLock = new Object();

	private HashMap<String, GameState> states = new HashMap<String, GameState>();
	private ArrayList<GameState> stateStack = new ArrayList<GameState>();

	// Timing variables
	private long gameTime;

	private long startTime;
	private long ticksElapsed;
	private long framesElapsed;

	private Paint testPaint;
	private int width;
	private int height;
	private boolean isPaused = false;


	public ExplodeThread(Context context, SurfaceHolder holder) {
		setName("ExplodeThread");

		this.context = context;
		this.holder = holder;

		testPaint = new Paint();
		testPaint.setTextSize(Utils.getSP(context, 32));
		testPaint.setColor(0xffffffff);
		testPaint.setAntiAlias(true);

		PlayState playState = new PlayState(context, this);
		states.put("play", playState);
		states.put("menu", new MenuState(context, this, playState));
		states.put("highscore", new HighscoreState(context, this));
		states.put("nextlevel", new NextLevelState(context, this, playState));
		states.put("gameover", new GameOverState(context, this));

		commonResources = new CommonResources(context);
	}

	public void startNewGame() {
		pushState("menu");
	}

	@Override
	public void run() {
		Canvas canvas = null;

		startTime = System.currentTimeMillis();
		gameTime = System.currentTimeMillis();

		while (isRunning) {
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
		if (isPaused || stateStack.isEmpty()) {
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
			if (isPaused || stateStack.isEmpty()) {
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
		}
	}

	public void touchDown(float x, float y) {
		synchronized (holder) {
			stateStack.get(stateStack.size() - 1).touchDown(x, y);
		}
	}

	public void touchUp(float x, float y) {
		synchronized (holder) {
			stateStack.get(stateStack.size() - 1).touchUp(x, y);
		}
	}

	public void replaceStateStack(String stateId, Bundle args) {
		synchronized (holder) {
			while (popState())
				;

			pushState(stateId, args);
		}
	}

	public void replaceStateStack(String stateId) {
		synchronized (holder) {
			while (popState())
				;

			pushState(stateId);
		}
	}

	public void pushState(String stateId) {
		synchronized (holder) {
			GameState state = states.get(stateId);
			if (state != null) {
				state.prepare(null);
				stateStack.add(state);
			}
		}
	}
	public void pushState(String stateId, Bundle args) {
		synchronized (holder) {
			GameState state = states.get(stateId);
			if (state != null) {
				state.prepare(args);
				stateStack.add(state);
			}
		}
	}

	public boolean popState() {
		synchronized (holder) {
			stateStack.remove(stateStack.size() - 1);
			return !stateStack.isEmpty();
		}
	}

	public void release() {
		synchronized (holder) {
			for (GameState state : states.values()) {
				state.destroy();
			}

			stateStack.clear();
		}

		commonResources.destroy();
	}

	public void pause() {
		synchronized (holder) {
			Log.d(TAG, "pause");
			isPaused = true;
		}
	}

	public void unpause() {
		synchronized (holder) {
			Log.d(TAG, "unpause");
			isPaused = false;
			gameTime = System.currentTimeMillis();
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
