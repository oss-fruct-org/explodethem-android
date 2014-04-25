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
	private final float textSize;
	private final Paint tilePaint;

	private final Paint textPaint;
	private final Paint textPaintOutline;

	private final Paint textPaintRight;
	private final Paint textPaintRightOutline;


	private Point point = new Point();
	private Rect rect = new Rect();
	private RectF rectF = new RectF();

	private Dimensions dimensions;

	private float x, y;
	private int touchX, touchY;
	private Field field;

	// Resources
	// Background
	private BitmapHolder background;
	private BitmapHolder largeBomb;
	private BitmapHolder mediumBomb;
	private BitmapHolder smallBomb;
	private BitmapHolder waterBomb;

	private BitmapHolder[] explosion;
	private BitmapHolder fire;
	private BitmapHolder water;

	public ExplodeThread(Context context, SurfaceHolder holder) {
		setName("ExplodeThread");

		this.context = context;
		this.holder = holder;

		tilePaint = new Paint();
		tilePaint.setColor(0x84c5c0f3);
		tilePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		tilePaint.setAntiAlias(true);

		field = new Field(TILES_X, TILES_Y);

		background = new BitmapHolder("background.jpg");
		largeBomb = new BitmapHolder("large-bomb.png");
		mediumBomb = new BitmapHolder("medium-bomb.png");
		smallBomb = new BitmapHolder("small-bomb.png");
		waterBomb = new BitmapHolder("water-bomb.png");

		fire = new BitmapHolder("fire.png");
		water = new BitmapHolder("drop.png");



		textSize = Utils.getSP(context, 32);
		textPaint = new Paint();
		textPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "coolvetica.ttf"));
		textPaint.setAntiAlias(true);
		textPaint.setColor(0xfffafef1);
		textPaint.setTextSize(textSize);

		textPaintOutline = new Paint(textPaint);
		textPaintOutline.setStyle(Paint.Style.STROKE);
		textPaintOutline.setColor(0xff110011);
		textPaintOutline.setStrokeWidth(2f);

		textPaintRight = new Paint(textPaint);
		textPaintRight.setTextAlign(Paint.Align.RIGHT);

		textPaintRightOutline = new Paint(textPaintOutline);
		textPaintRightOutline.setTextAlign(Paint.Align.RIGHT);

		explosion = new BitmapHolder[6];
		for (int i = 0; i < explosion.length; i++) {
			explosion[i] = new BitmapHolder("explosion-" + i + ".png");
		}
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

	public void release() {
		background.recycle();
		largeBomb.recycle();
		mediumBomb.recycle();
		smallBomb.recycle();
		waterBomb.recycle();
		water.recycle();
		fire.recycle();

		for (BitmapHolder bh : explosion) {
			bh.recycle();
		}
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

		canvas.drawBitmap(background.getScaled(), 0, 0, null);

		//canvas.drawText("TPS: " + ticksElapsed / ((gameTime - startTime) / 1000.), 10, 30, testPaint);
		//canvas.drawText("FPS: " + framesElapsed / ((gameTime - startTime) / 1000.), 10, 50, testPaint);


		// Draw field
		for (int x = 0; x < TILES_X; x++) {
			for (int y = 0; y < TILES_Y; y++) {
				float xPos = dimensions.getOffset(x);
				float yPos = dimensions.getOffset(y) + dimensions.fieldStartY;

				canvas.drawRoundRect(new RectF(xPos, yPos, xPos + dimensions.tileSize, yPos + dimensions.tileSize),
						Utils.getDP(context, 4), Utils.getDP(context, 4), tilePaint);

				Field.Entity ent = field.get(x, y);
				Bitmap bitmapToDraw = null;
				switch (ent) {
				case LARGE_BOMB:
					bitmapToDraw = largeBomb.getScaled();
					break;
				case MEDIUM_BOMB:
					bitmapToDraw = mediumBomb.getScaled();
					break;
				case SMALL_BOMB:
					bitmapToDraw = smallBomb.getScaled();
					break;
				case WATER_BOMB:
					bitmapToDraw = waterBomb.getScaled();
					break;
				}

				if (!field.isExplodedTile(x, y) && bitmapToDraw != null) {
					canvas.drawBitmap(bitmapToDraw,
							xPos + halfTileSize - bitmapToDraw.getWidth() / 2,
							yPos + halfTileSize - bitmapToDraw.getHeight() / 2,
							null);
				}
			}
		}

		canvas.save();
		canvas.clipRect(dimensions.fieldRect);

		List<Field.Shell> shells = field.getShells();
		for (Field.Shell shell : shells) {
			BitmapHolder shellBitmap = shell.isWater ? water : fire;

			float xPos = dimensions.getOffset(shell.x)
					+ shell.dx * stepOffset * (dimensions.tileSize + dimensions.tilePadding);
			float yPos = dimensions.getOffset(shell.y)
					+ shell.dy * stepOffset  * (dimensions.tileSize + dimensions.tilePadding)
					+ dimensions.fieldStartY;

			canvas.save();
			final float halfBitmapWidth = shellBitmap.getOriginal().getWidth() / 2;
			final float halfBitmapHeight = shellBitmap.getOriginal().getHeight() / 2;

			canvas.translate(xPos + halfTileSize - halfBitmapWidth,
					yPos + halfTileSize - halfBitmapHeight);


			if (shell.dx == 0) {
				if (shell.dy > 0) {
					canvas.rotate(0, halfBitmapWidth, halfBitmapHeight);
				} else {
					canvas.rotate(180, halfBitmapWidth, halfBitmapHeight);
				}
			} else {
				if (shell.dx > 0) {
					canvas.rotate(270, halfBitmapWidth, halfBitmapHeight);
				} else {
					canvas.rotate(90, halfBitmapWidth, halfBitmapHeight);
				}
			}

			canvas.drawBitmap(shellBitmap.getOriginal(), 0, 0, null);
			canvas.restore();
		}

		canvas.restore();

		drawAnimated(canvas);
		drawText(canvas, "Score: ", dimensions.scoreTextPoint, false);
		drawText(canvas, "Level: ", dimensions.levelTextPoint, true);
		drawText(canvas, "Shakes: ", dimensions.shakesTextPoint, true);

	}

	private void drawAnimated(Canvas canvas) {
		for (Field.Explode explode : field.getExplodes()) {
			if (explode.to == Field.Entity.LARGE_BOMB
					|| explode.to == Field.Entity.MEDIUM_BOMB
					|| explode.to == Field.Entity.SMALL_BOMB) {
				drawInflate(canvas, explode);
			} else if (explode.to == Field.Entity.EMPTY) {
				drawExplosion(canvas, explode);
			}
		}
	}

	private void drawInflate(Canvas canvas, Field.Explode explode) {
		final float halfTileSize = dimensions.tileSize / 2;

		Field.Entity from = explode.from;
		Field.Entity to = explode.to;
		BitmapHolder fromHolder = null;
		if (explode.to == Field.Entity.MEDIUM_BOMB) {
			fromHolder = mediumBomb;
		} else if (explode.to == Field.Entity.LARGE_BOMB) {
			fromHolder = largeBomb;
		} else if (explode.to == Field.Entity.SMALL_BOMB) {
			fromHolder = smallBomb;
		}

		if (fromHolder != null) {
			float xPos = dimensions.getOffset(explode.x);
			float yPos = dimensions.getOffset(explode.y) + dimensions.fieldStartY;
			float newSizeFactor = from.getFactor()
					+ (to.getFactor() - from.getFactor())
					* stepOffset;

			rect.set(0, 0, fromHolder.getOriginal().getWidth(), fromHolder.getOriginal().getHeight());
			rectF.set(xPos + halfTileSize - halfTileSize * newSizeFactor,
					yPos + halfTileSize - halfTileSize * newSizeFactor,
					xPos + halfTileSize + halfTileSize * newSizeFactor,
					yPos + halfTileSize + halfTileSize * newSizeFactor);

			canvas.drawBitmap(fromHolder.getOriginal(), rect, rectF, null);
		}
	}

	private void drawExplosion(Canvas canvas, Field.Explode explode) {
		int frame = (int) (stepOffset * explosion.length);

		if (frame == explosion.length) {
			frame = explosion.length - 1;
		}

		float xPos = dimensions.getOffset(explode.x);
		float yPos = dimensions.getOffset(explode.y) + dimensions.fieldStartY;

		canvas.drawBitmap(explosion[frame].getScaled(), xPos, yPos, null);
	}

	private void drawText(Canvas canvas, String str, Point p, boolean right) {
		canvas.drawText(str, p.x, p.y, right ? textPaintRight : textPaint);
		canvas.drawText(str, p.x, p.y, right ? textPaintRightOutline : textPaintOutline);
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

			dimensions.fieldWidth = dimensions.tilePadding + (dimensions.tilePadding + dimensions.tileSize) * TILES_X;
			dimensions.fieldHeight = dimensions.tilePadding + (dimensions.tilePadding + dimensions.tileSize) * TILES_Y;

			dimensions.fieldStartY = height - dimensions.fieldHeight;

			dimensions.fieldRect = new RectF(dimensions.tilePadding,
					dimensions.tilePadding + dimensions.fieldStartY,
					dimensions.fieldWidth - dimensions.tilePadding,
					dimensions.fieldStartY
							+ (dimensions.tilePadding + dimensions.tileSize) * TILES_Y);

			// Scale bitmap holders
			background.scale(width, height);

			largeBomb.scale(dimensions.tileSize * Field.Entity.LARGE_BOMB.getFactor(),
					dimensions.tileSize * Field.Entity.LARGE_BOMB.getFactor());

			mediumBomb.scale(dimensions.tileSize * Field.Entity.MEDIUM_BOMB.getFactor(),
					dimensions.tileSize * Field.Entity.MEDIUM_BOMB.getFactor());

			smallBomb.scale(dimensions.tileSize * Field.Entity.SMALL_BOMB.getFactor(),
					dimensions.tileSize * Field.Entity.SMALL_BOMB.getFactor());

			waterBomb.scale(dimensions.tileSize * Field.Entity.WATER_BOMB.getFactor(),
					dimensions.tileSize * Field.Entity.WATER_BOMB.getFactor());

			for (BitmapHolder anExplosion : explosion) {
				anExplosion.scale(dimensions.tileSize, dimensions.tileSize);
			}


			// Text bounds
			Rect rect = new Rect();
			textPaint.getTextBounds("Score:", 0, "Score:".length(), rect);

			dimensions.scoreTextPoint.set(0, rect.height());
			dimensions.levelTextPoint.set(width, rect.height());
			dimensions.shakesTextPoint.set(width, rect.height() * 2);
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

	private class Dimensions {
		Point scoreTextPoint = new Point();
		Point levelTextPoint = new Point();
		Point shakesTextPoint = new Point();

		float tileSize;
		float tilePadding;
		int width;
		int height;

		float fieldStartY;

		float fieldWidth;
		float fieldHeight;

		RectF fieldRect = new RectF();

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

	private class BitmapHolder {
		private Bitmap original;
		private Bitmap scaled;

		BitmapHolder(String assetFile) {
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
	}
}
