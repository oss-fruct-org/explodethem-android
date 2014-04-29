package org.fruct.oss.explodethem;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;

import static org.fruct.oss.explodethem.ExplodeThread.BitmapHolder;

public class NextLevelState implements GameState{
	private final Context context;
	private final ExplodeThread explodeThread;
	private final PlayState playState;

	private static final long TICKS_FADE = 800 / ExplodeThread.TICK_MS;
	private long ticksRemainFadeIn = TICKS_FADE;

	private final Paint backgroundPaint;
	private final Paint facePaint;
	private final PorterDuffColorFilter[] colorFilters;
	private final Paint textPaint;
	private final Paint textPaintOutline;
	private final Rect textBounds = new Rect();

	private int width;
	private int height;

	private BitmapHolder faceBitmap;

	public NextLevelState(Context context, ExplodeThread explodeThread, PlayState playState) {
		this.context = context;
		this.explodeThread = explodeThread;
		this.playState = playState;

		this.backgroundPaint = new Paint();

		this.facePaint = new Paint();

		faceBitmap = new BitmapHolder(context, "level-end.png");

		// Create face color filters
		colorFilters = new PorterDuffColorFilter[(int) TICKS_FADE + 1];
		for (int i = 0; i <= TICKS_FADE; i++) {
			int alpha = 255 - (int) (255 * i / TICKS_FADE);

			colorFilters[i] = new PorterDuffColorFilter(alpha << 24, PorterDuff.Mode.DST_IN);
		}

		float textSize = Utils.getSP(context, 32);
		textPaint = new Paint();
		textPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "coolvetica.ttf"));
		textPaint.setAntiAlias(true);
		textPaint.setColor(0xfffafef1);
		textPaint.setTextSize(textSize);
		textPaint.setTextAlign(Paint.Align.CENTER);

		textPaintOutline = new Paint(textPaint);
		textPaintOutline.setStyle(Paint.Style.STROKE);
		textPaintOutline.setColor(0xff110011);
		textPaintOutline.setStrokeWidth(2f);
		textPaint.setTextAlign(Paint.Align.CENTER);

		textPaint.getTextBounds("Next", 0, 4, textBounds);
	}

	@Override
	public void prepare(Bundle args) {

	}

	@Override
	public void updatePhysics() {
		if (ticksRemainFadeIn > 0) {
			ticksRemainFadeIn--;
		}
	}

	@Override
	public void draw(Canvas canvas) {
		if (ticksRemainFadeIn >= 0) {
			int alpha = 200 - (int) (200 * ticksRemainFadeIn / TICKS_FADE);
			int alphaText = 255 - (int) (255 * ticksRemainFadeIn / TICKS_FADE);

			backgroundPaint.setColor(alpha << 24);
			canvas.drawRect(0, 0, width, height, backgroundPaint);

			facePaint.setColorFilter(colorFilters[(int) ticksRemainFadeIn]);
			textPaint.setColor((alphaText << 24) + 0xfafef1);
			textPaintOutline.setColor((alphaText << 24) + 0x110011);

			canvas.drawBitmap(faceBitmap.getScaled(),
					width / 2 - faceBitmap.getScaled().getWidth() / 2,
					height / 2 - faceBitmap.getScaled().getHeight() / 2,
					facePaint);

			int textX = width / 2;
			int textY = (int) (height / 2 + faceBitmap.getScaled().getHeight() / 2
								+ textBounds.height() + Utils.getDP(context, 8));

			drawText(canvas, "Next level", textX, textY);
		}
	}

	private void drawText(Canvas canvas, String str, int x, int y) {
		canvas.drawText(str, x, y, textPaint);
		canvas.drawText(str, x, y, textPaintOutline);
	}

	@Override
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;

		faceBitmap.scale(width / 2, width / 2);
	}

	@Override
	public void touchDown(float x, float y) {
	}

	@Override
	public void touchUp(float x, float y, MotionEvent event) {
		if (ticksRemainFadeIn == 0) {
			playState.nextLevel();
			explodeThread.popState();
			ticksRemainFadeIn = TICKS_FADE;
		}
	}

	@Override
	public void destroy() {
		faceBitmap.recycle();
	}

	@Override
	public void moveEvent(MotionEvent event) {

	}
}
