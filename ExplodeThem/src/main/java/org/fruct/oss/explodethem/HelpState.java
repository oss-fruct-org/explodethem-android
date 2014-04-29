package org.fruct.oss.explodethem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.MotionEvent;

public class HelpState implements GameState {
	public static final String TAG = HelpState.class.getName();
	private static final long HISTORY_TIME = 100;

	public static final float FRICTION_RATE = 0.003f;
	public static final float GRAVITY_RATE = 0.004f;
	public static final int SPEED_THRESHOLD = 1;
	public static final int OFFSET_THRESHOLD = 5;
	public static final float SPEED_LIMIT = 2f;

	private final Paint backgroundPaint;

	private final Paint titleTextPaint;
	private final TextPaint textPaint;
	private final float panelDistance;

	private Context context;
	private ExplodeThread explodeThread;

	private float titlePosY;
	private int width;
	private int height;

	private float panelWidth, panelHeight;

	private String[] helpStrings;
	private Bitmap[] helpPanels;

	// Animation fields
	private float viewPosition;
	private float viewSpeed = 0f;

	private float startViewPosition;
	private float startDragX;

	private float lastMovePosX;
	private long lastMoveTime;

	private boolean isDragging = false;
	private boolean isMoving = false;

	public HelpState(Context context, ExplodeThread explodeThread) {
		this.context = context;
		this.explodeThread = explodeThread;

		backgroundPaint = new Paint();
		backgroundPaint.setColor(0xcafefefe);

		titleTextPaint = new Paint();
		titleTextPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "Colleged.ttf"));
		titleTextPaint.setAntiAlias(true);
		titleTextPaint.setColor(0xfffafef1);
		titleTextPaint.setTextSize(Utils.getSP(context, 40));
		titleTextPaint.setTextAlign(Paint.Align.CENTER);

		textPaint = new TextPaint();
		textPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "OneDirection.ttf"));
		textPaint.setAntiAlias(true);
		textPaint.setColor(0xff110011);
		textPaint.setTextSize(Utils.getSP(context, 24));

		panelDistance = Utils.getDP(context, 32);

		helpStrings = context.getResources().getStringArray(R.array.help_strings);
	}

	@Override
	public void prepare(Bundle args) {
	}

	@Override
	public void updatePhysics() {
		if (!isDragging && isMoving) {
			viewPosition -= viewSpeed * ExplodeThread.TICK_MS;

			final float sign = Math.signum(viewSpeed);
			final float speedDown = sign * FRICTION_RATE * ExplodeThread.TICK_MS;

			final float relativeViewPosition = viewPosition / width;
			final float offset = (relativeViewPosition - Utils.round(relativeViewPosition)) * width;
			final float gravity = Math.signum(offset) * GRAVITY_RATE * ExplodeThread.TICK_MS;

			final float speedChange = gravity - speedDown;

			if (Math.abs(viewSpeed) < SPEED_THRESHOLD && Math.abs(offset) < OFFSET_THRESHOLD) {
				isMoving = false;
				viewPosition = Utils.round(relativeViewPosition) * width;
			}

			viewSpeed += speedChange;
			if (Math.abs(viewSpeed) > SPEED_LIMIT) {
				viewSpeed = SPEED_LIMIT * Math.signum(viewSpeed);
			}
		}
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawBitmap(explodeThread.getCommonResources().background.getScaled(), 0, 0, null);
		canvas.drawText("Help", width / 2, titlePosY, titleTextPaint);

		float relativeViewPosition = viewPosition / width;

		int panelIdxLeft = (int) Utils.round(relativeViewPosition + 1f) % helpPanels.length;
		int panelIdxCenter = (int) Utils.round(relativeViewPosition) % helpPanels.length;
		int panelIdxRight = (int) Utils.round(relativeViewPosition - 1f) % helpPanels.length;

		float posDiff = (relativeViewPosition - Utils.round(relativeViewPosition)) * width;

		drawPanel(canvas, getPanelMod(panelIdxLeft), posDiff - width);
		drawPanel(canvas, getPanelMod(panelIdxCenter), posDiff);
		drawPanel(canvas, getPanelMod(panelIdxRight), posDiff + width);
	}

	private void drawPanel(Canvas canvas, Bitmap panel, float offset) {
		canvas.drawBitmap(panel, width / 2 - panelWidth / 2 - offset,
				height / 2 - panelHeight / 2, null);
	}

	private Bitmap getPanelMod(int idx) {
		return helpPanels[Utils.modPos(idx, helpPanels.length)];
	}

	@Override
	public void setSize(int width, int height) {
		Rect rect = new Rect();
		titleTextPaint.getTextBounds("Help", 0, "Help".length(), rect);

		titlePosY = Utils.getDP(context, 16) + rect.height();
		this.width = width;
		this.height = height;

		panelWidth = 4 * width / 5;
		panelHeight = 4 * width / 5;

		helpPanels = new Bitmap[helpStrings.length];
		for (int i = 0; i < helpStrings.length; i++) {
			String str = helpStrings[i];
			helpPanels[i] = createHelpPanel(str);
		}
	}

	private Bitmap createHelpPanel(String str) {
		Bitmap ret = Bitmap.createBitmap((int) panelWidth, (int) panelHeight, Bitmap.Config.ARGB_4444);

		RectF roundRect = new RectF(0, 0, ret.getWidth(), ret.getHeight());

		Canvas canvas = new Canvas(ret);
		canvas.drawRoundRect(roundRect, Utils.getDP(context, 8), Utils.getDP(context, 8), backgroundPaint);

		float marginLeft = Utils.getDP(context, 8);
		float marginTop = Utils.getDP(context, 16);
		StaticLayout staticLayout = new StaticLayout(str, textPaint, (int) (ret.getWidth() - marginLeft * 2),
				Layout.Alignment.ALIGN_NORMAL, 1, 0, false);

		canvas.translate(marginLeft, marginTop);
		staticLayout.draw(canvas);

		return ret;
	}

	@Override
	public void touchDown(float x, float y) {
		isDragging = true;

		startViewPosition = viewPosition;
		lastMovePosX = startDragX = x;
		lastMoveTime = System.currentTimeMillis();
	}

	@Override
	public void touchUp(float x, float y, MotionEvent event) {
		isDragging = false;
	}

	@Override
	public void moveEvent(MotionEvent event) {
		float dx = startDragX - event.getX();

		viewPosition = startViewPosition + dx;

		float speed = (event.getX() - lastMovePosX) / (System.currentTimeMillis() - lastMoveTime);

		lastMoveTime = System.currentTimeMillis();
		lastMovePosX = event.getX();

		viewSpeed = speed;
		isMoving = true;

		//Log.d(getClass().getSimpleName(), "" + speed);
	}

	@Override
	public void destroy() {
		for (Bitmap panel : helpPanels) {
			panel.recycle();
		}
	}
}
