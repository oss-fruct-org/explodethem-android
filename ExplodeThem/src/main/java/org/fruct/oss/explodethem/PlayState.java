package org.fruct.oss.explodethem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.Log;

import java.util.List;

import static org.fruct.oss.explodethem.ExplodeThread.BitmapHolder;


public class PlayState implements GameState {
	private static final String TAG = "PlayStaet";

	public static int TILES_X = 6;
	public static int TILES_Y = 8;
	public static int TILES_PADDING = 4;

	public static final long TICK_PER_STEP = 250 / ExplodeThread.TICK_MS;
	public static final float RELATIVE_OFFSET = 1f / TICK_PER_STEP;

	private final Context context;
	private final ExplodeThread explodeThread;
	private final NextLevelState nextLevelState;

	private float stepRemaintTicks;
	private float stepOffset;
	private int oldSparksValue;

	// Paints
	private final float textSize;
	private final Paint tilePaint;

	private final Paint sparksTextPaint;

	private final Paint textPaint;
	private final Paint textPaintOutline;

	private final Paint textPaintRight;
	private final Paint textPaintRightOutline;

	private Point point = new Point();
	private Rect rect = new Rect();
	private RectF rectF = new RectF();

	private Dimensions dimensions;

	private int touchX, touchY;
	private Field field;

	private BitmapHolder background;
	private BitmapHolder largeBomb;
	private BitmapHolder mediumBomb;
	private BitmapHolder smallBomb;
	private BitmapHolder waterBomb;

	private BitmapHolder[] explosion;
	private BitmapHolder fire;
	private BitmapHolder water;
	private float tileRadius;


	public PlayState(Context context, ExplodeThread explodeThread) {
		this.context = context;
		this.explodeThread = explodeThread;

		tilePaint = new Paint();
		tilePaint.setColor(0x84c5c0f3);
		tilePaint.setStyle(Paint.Style.FILL_AND_STROKE);
		tilePaint.setAntiAlias(true);

		field = new Field(TILES_X, TILES_Y);
		Log.d(TAG, "Total bombs: " + field.getBombsRemain());

		background = new BitmapHolder(context, "background.jpg");
		largeBomb = new BitmapHolder(context, "large-bomb.png");
		mediumBomb = new BitmapHolder(context, "medium-bomb.png");
		smallBomb = new BitmapHolder(context, "small-bomb.png");
		waterBomb = new BitmapHolder(context, "water-bomb.png");

		fire = new BitmapHolder(context, "fire.png");
		water = new BitmapHolder(context, "drop.png");

		tileRadius = Utils.getDP(context, 4);
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

		sparksTextPaint = new Paint(textPaint);
		sparksTextPaint.setTextAlign(Paint.Align.CENTER);
		sparksTextPaint.setColor(0xeeffffff);

		explosion = new BitmapHolder[6];
		for (int i = 0; i < explosion.length; i++) {
			explosion[i] = new BitmapHolder(context, "explosion-" + i + ".png");
		}

		nextLevelState = new NextLevelState(context, explodeThread, this);
	}

	@Override
	public void updatePhysics() {
		if (field.isActive()) {
			if (stepRemaintTicks == 0) {
				initializeStep();
				field.step();
				field.commit();
			}

			stepRemaintTicks--;
			stepOffset += RELATIVE_OFFSET;
		} else {
			if (field.getBombsRemain() == 0) {
				Log.d(TAG, "Win");
				explodeThread.pushState(nextLevelState);
			}
		}
	}

	private void initializeStep() {
        stepRemaintTicks = TICK_PER_STEP;
        stepOffset = 0f;
    }

	@Override
	public void draw(Canvas canvas) {
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
						tileRadius, tileRadius, tilePaint);

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
		drawText(canvas, "Score: " + field.getScore(), dimensions.scoreTextPoint, false);
		drawText(canvas, "Level: " + field.getLevel(), dimensions.levelTextPoint, true);
		drawText(canvas, "Shakes: ", dimensions.shakesTextPoint, true);
		drawSparks(canvas);
	}

	private void drawSparks(Canvas canvas) {
		String text = "" + field.getSparks();
		canvas.drawRoundRect(dimensions.sparksRect, tileRadius, tileRadius, tilePaint);

		int oldSpark = field.getSparkChange();
		if (oldSpark != -1) {
			canvas.save();
			canvas.clipRect(dimensions.sparksRect);
			String text1;
			String text2;
			float posY1;
			float posY2;
			if (oldSpark > field.getSparks()) {
				text1 = "" + oldSpark;
				text2 = "" + field.getSparks();
				posY1 = dimensions.sparksTextPoint.y - dimensions.sparksRect.height() * stepOffset;
				posY2 = posY1 + dimensions.sparksRect.height();
			} else {
				text2 = "" + oldSpark;
				text1 = "" + field.getSparks();
				posY2 = dimensions.sparksTextPoint.y + dimensions.sparksRect.height() * stepOffset;
				posY1 = posY2 - dimensions.sparksRect.height();
			}

			canvas.drawText(text1, dimensions.sparksRect.centerX(), posY1, sparksTextPaint);
			canvas.drawText(text2, dimensions.sparksRect.centerX(), posY2, sparksTextPaint);
			canvas.restore();
		} else {
			canvas.drawText(text, dimensions.sparksRect.centerX(), dimensions.sparksTextPoint.y, sparksTextPaint);
		}
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


	@Override
	public void setSize(int width, int height) {
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

		PointF sparksSize = new PointF(Utils.getDP(context, 64), Utils.getDP(context, 32));
		dimensions.sparksRect.set(width / 2 - sparksSize.x / 2,
				dimensions.shakesTextPoint.y - sparksSize.y / 2,
				width / 2 + sparksSize.x / 2,
				dimensions.shakesTextPoint.y + sparksSize.y / 2);

		textPaint.getTextBounds("99", 0, "99".length(), rect);
		dimensions.sparksTextPoint.set(dimensions.sparksRect.centerX(),
				dimensions.sparksRect.centerY() + rect.height() / 2);
	}

	public void testHit(float x, float y, Point outPoint) {
		outPoint.x = dimensions.getIndexX(x);
		outPoint.y = dimensions.getIndexY(y - dimensions.fieldStartY);
		Log.d(TAG, "Hit " + outPoint.x + " " + (outPoint.y));
	}

	@Override
	public void touchDown(float x, float y) {
		testHit(x, y, point);

		touchX = point.x;
		touchY = point.y;
	}

	@Override
	public void touchUp(float x, float y) {
		testHit(x, y, point);

		if (touchX != -1 && touchY != -1 && touchY == point.y && touchX == point.x) {
			Log.d(TAG, "Fire " + touchX + " " + touchY);

			if (!field.isActive()) {
				oldSparksValue = field.getSparks();
				field.fire(touchX, touchY);
				field.commit();

				if (field.isActive()) {
					initializeStep();
				}
			}
		}
	}

	@Override
	public void destroy() {
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

		nextLevelState.destroy();
	}

	public void nextLevel() {
		field.nextLevel();
		stepOffset = 0;
		stepRemaintTicks = 0;
	}

	private class Dimensions {
		Point scoreTextPoint = new Point();
		Point levelTextPoint = new Point();
		Point shakesTextPoint = new Point();
		PointF sparksTextPoint = new PointF();

		float tileSize;
		float tilePadding;
		int width;
		int height;

		float fieldStartY;

		float fieldWidth;
		float fieldHeight;

		RectF fieldRect = new RectF();
		RectF sparksRect = new RectF();

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
