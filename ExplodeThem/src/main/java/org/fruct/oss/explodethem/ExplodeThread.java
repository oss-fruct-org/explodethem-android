package org.fruct.oss.explodethem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ExplodeThread extends Thread {
	private static final String TAG = "ExplodeThread";

	public static int TILES_X = 6;
	public static int TILES_Y = 8;

	public static int TILES_PADDING = 4;

	public static final long TICK_MS = 32;
	public static final long TICK_PER_STEP = 400 / TICK_MS;
	public static final float RELATIVE_OFFSET = 1f / TICK_PER_STEP;

	private final Context context;
	private final SurfaceHolder holder;

	private boolean isRunning = false;
	private final Object isRunningLock = new Object();

	// Timing variables
	private long gameTime;
	private long startTime;

	private float stepRemaintTicks;
	private float stepOffset;

	private long ticksElapsed;
	private long framesElapsed;

	// Paints
	private final Paint testPaint;
	private final Paint testPaint2;
	private final Paint testPaint3;
	private final Paint eraserPaint;

	private Point point = new Point();
	private Dimensions dimensions;

	private float x, y;
	private int touchX, touchY;
	private Field field;

	// Resources
	private Bitmap backgroundOriginal;
	private Bitmap backgroundScaled;

	public ExplodeThread(Context context, SurfaceHolder holder) {
		setName("ExplodeThread");

		this.context = context;
		this.holder = holder;

		testPaint = new Paint();
		testPaint.setColor(0xffff77ff);
		testPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		testPaint.setTextSize(24);
		testPaint.setAntiAlias(true);

		testPaint2 = new Paint();
		testPaint2.setColor(0xffba4433);
		testPaint2.setStyle(Paint.Style.FILL_AND_STROKE);
		testPaint2.setTextSize(24);
		testPaint.setAntiAlias(true);

		testPaint3 = new Paint();
		testPaint3.setColor(0xff1199fa);
		testPaint3.setStyle(Paint.Style.FILL_AND_STROKE);
		testPaint3.setTextSize(24);
		testPaint.setAntiAlias(true);

		eraserPaint = new Paint();
		eraserPaint.setColor(0xff000000);
		eraserPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		field = new Field(TILES_X, TILES_Y);

		backgroundOriginal = createBitmapFromAsset("resources.jpg");
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
			return context.getAssets().open("background.jpg");
		} catch (IOException e) {
			Log.e(TAG, "Can't open asset: " +  file);
			return null;
		}
	}

	public void release() {
		if (backgroundScaled != null)
			backgroundScaled.recycle();

		backgroundOriginal.recycle();
	}

	@Override
	public void run() {
		Canvas canvas = null;

		startTime = gameTime = System.currentTimeMillis();
		ticksElapsed = 0;
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
			long delta = currentTime - gameTime;
			if (delta > 0) {
				//Log.d(TAG, "Delta " + delta);
				gameTime += TICK_MS;

				updatePhysics();
			}

			framesElapsed++;
			draw(canvas);
		}
	}

	private void updatePhysics() {
		ticksElapsed++;

		if (field.isActive()) {
			if (stepRemaintTicks == 0) {
				initializeStep();
				field.step();
				field.commit();
			}

			stepRemaintTicks--;
			stepOffset += RELATIVE_OFFSET;
		}
	}

	private void initializeStep() {
		stepRemaintTicks = TICK_PER_STEP;
		stepOffset = 0f;
	}

	private void draw(Canvas canvas) {
		assert dimensions != null;

		final float halfTileSize = dimensions.tileSize / 2;
		final float tileSize = dimensions.tileSize;

		//canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), eraserPaint);
		canvas.drawBitmap(backgroundScaled, 0, 0, null);

		//canvas.drawText("TPS: " + ticksElapsed / ((gameTime - startTime) / 1000.), 10, 30, testPaint);
		//canvas.drawText("FPS: " + framesElapsed / ((gameTime - startTime) / 1000.), 10, 50, testPaint);


		// Draw field
		for (int x = 0; x < TILES_X; x++) {
			for (int y = 0; y < TILES_Y; y++) {
				float xPos = dimensions.getOffset(x);
				float yPos = dimensions.getOffset(y) + dimensions.fieldStartY;

				canvas.drawRect(xPos, yPos,
						xPos + dimensions.tileSize, yPos + dimensions.tileSize,
						testPaint);


				Field.Entity ent = field.get(x, y);
				if (ent != Field.Entity.EMPTY) {
					float size = halfTileSize * ent.getFactor();
					canvas.drawCircle(xPos + halfTileSize, yPos + halfTileSize,
							size, ent == Field.Entity.WATER_BOMB ? testPaint3 : testPaint2);
				}
			}
		}

		List<Field.Shell> shells = field.getShells();
		for (Field.Shell shell : shells) {
			float xPos = dimensions.getOffset(shell.x)
					+ shell.dx * stepOffset * (dimensions.tileSize + dimensions.tilePadding);
			float yPos = dimensions.getOffset(shell.y)
					+ shell.dy * stepOffset  * (dimensions.tileSize + dimensions.tilePadding)
					+ dimensions.fieldStartY;

			canvas.drawCircle(xPos + halfTileSize, yPos + halfTileSize,
					4f, testPaint2);
		}

		//canvas.drawRect(x, y, x + 5, y + 5, testPaint3);
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
			dimensions = new Dimensions();
			dimensions.width = width;
			dimensions.height = height;

			dimensions.tilePadding = Utils.getDP(context, TILES_PADDING);

			dimensions.tileSize = (width - dimensions.tilePadding) / TILES_X
					- dimensions.tilePadding;

			dimensions.fieldStartY = 60;

			// Create new background
			if (backgroundScaled != null) {
				backgroundScaled.recycle();
			}

			backgroundScaled = Bitmap.createScaledBitmap(backgroundOriginal, width, height, true);
		}
	}

	public void testHit(float x, float y, Point outPoint) {
		outPoint.x = dimensions.getIndexX(x);
		outPoint.y = dimensions.getIndexY(y - dimensions.fieldStartY);
		Log.d(TAG, "Hit " + outPoint.x + " " + (outPoint.y));
	}

	public void touchDown(float x, float y) {
		synchronized (holder) {
			testHit(x, y, point);
			this.x = x;
			this.y = y;

			touchX = point.x;
			touchY = point.y;
		}
	}

	public void touchUp(float x, float y) {
		synchronized (holder) {
			testHit(x, y, point);

			if (touchX != -1 && touchY != -1 && touchY == point.y && touchX == point.x) {
				Log.d(TAG, "Fire " + touchX + " " + touchY);

				if (!field.isActive()) {
					field.fire(touchX, touchY);
					field.commit();

					if (field.isActive()) {
						initializeStep();
					}
				}
			}
		}
	}

	private static class FloatPoint {
		float x, y;
		FloatPoint() {
		}

		FloatPoint(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

	private class Dimensions {
		float tileSize;
		float tilePadding;
		int width;
		int height;

		float fieldStartY;

		float getOffset(int index) {
			return tilePadding + index * (tileSize + tilePadding);
		}
		int getIndex(float offset) {
			offset -= tilePadding / 2;
			final float size = tileSize + tilePadding;

			double dIndex = offset / size;

			if (dIndex < 0) {
				return -1;
			} else {
				return (int) dIndex;
			}
		}

		int getIndexX(float offset) {
			int index = getIndex(offset);
			return index < TILES_X ? index : -1;
		}

		int getIndexY(float offset) {
			int index = getIndex(offset);
			return index < TILES_Y ? index : -1;
		}
	}
}
