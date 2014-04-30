package org.fruct.oss.explodethem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.widget.EditText;


public class AboutState implements GameState {
	private final Context context;
	private final ExplodeThread explodeThread;

	private final TextPaint textPaint;
	private final Paint backgroundPaint;
	private final Paint titleTextPaint;
	private final Paint buttonPaint;
	private final Paint buttonTextPaint;
	private final Paint buttonPaintHighlight;

	private final float buttonRadius;

	private int width;
	private int height;
	private final ExplodeThread.BitmapHolder icon;
	private float titleTextPosY;
	private StaticLayout descriptionLayout;

	private float padding;

	private static final long TICKS_FADE = 800 / ExplodeThread.TICK_MS;
	private long ticksRemainFadeIn = TICKS_FADE;

	private RectF buttonRect;
	private float buttonTextPosY;

	private boolean isButtonHover = false;
	public AboutState(Context context, ExplodeThread explodeThread) {
		this.context = context;
		this.explodeThread = explodeThread;

		float textSize = Utils.getSP(context, 16);
		padding = Utils.getDP(context, 16);
		buttonRadius = Utils.getDP(context, 4);

		backgroundPaint = new Paint();

		textPaint = new TextPaint();
		textPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "coolvetica.ttf"));
		textPaint.setAntiAlias(true);
		textPaint.setColor(0xfffafef1);
		textPaint.setTextSize(textSize);
		textPaint.setTextAlign(Paint.Align.CENTER);

		titleTextPaint = new Paint(textPaint);
		titleTextPaint.setTextSize(Utils.getSP(context, 32));

		buttonPaint = new Paint();
		buttonPaint.setColor(0x84c5c0f3);
		buttonPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		buttonPaint.setAntiAlias(true);

		buttonTextPaint = new Paint();
		buttonTextPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "coolvetica.ttf"));
		buttonTextPaint.setAntiAlias(true);
		buttonTextPaint.setColor(0xfffafef1);
		buttonTextPaint.setTextSize(Utils.getSP(context, 24));
		buttonTextPaint.setTextAlign(Paint.Align.CENTER);

		buttonPaintHighlight = new Paint(buttonPaint);
		buttonPaintHighlight.setColor(0x99c5a0f3);

		icon = new ExplodeThread.BitmapHolder(context, "about-icon.png");
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
		if (ticksRemainFadeIn > 0) {
			int alpha = 200 - (int) (200 * ticksRemainFadeIn / TICKS_FADE);
			backgroundPaint.setColor(alpha << 24);
			canvas.drawRect(0, 0, width, height, backgroundPaint);
		} else {
			canvas.drawRect(0, 0, width, height, backgroundPaint);
			canvas.drawBitmap(icon.getOriginal(), width / 2 - icon.getScaled().getWidth() / 2,
					height / 4 - icon.getScaled().getHeight() / 2, null);

			canvas.drawText("Explode Them", width / 2, titleTextPosY, titleTextPaint);

			canvas.drawRoundRect(buttonRect, buttonRadius, buttonRadius,
					isButtonHover ? buttonPaintHighlight : buttonPaint);
			canvas.drawText("OK", width / 2, buttonTextPosY, buttonTextPaint);

			canvas.save();
			canvas.translate(width / 2 - descriptionLayout.getWidth() / 2, titleTextPosY + padding);
			descriptionLayout.draw(canvas);

			canvas.restore();
		}
	}

	@Override
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;

		this.icon.scale(width / 2, width / 2);

		String text = "Explode Them 1.0";

		Rect rect = new Rect();
		titleTextPaint.getTextBounds(text, 0, text.length(), rect);
		titleTextPosY = height / 4 + icon.getScaled().getHeight() / 2 + rect.height() + padding;

		textPaint.getTextBounds(text, 0, text.length(), rect);

		descriptionLayout = new StaticLayout("Copyright \u00a9 2012,2014 FRUCT Lab in IT-park of" +
				" Petrozavodsk State University. Main developer is Artemov Nikita." +
				" Android port by Kirill Ivashov.",
				textPaint, 2 * width / 3, Layout.Alignment.ALIGN_CENTER, 1, 0, false);

		float buttonHeight = Utils.getDP(context, 48);
		float buttonWidth = width / 2;
		buttonRect = new RectF(width / 2 - buttonWidth / 2,
				titleTextPosY + 2 * padding + descriptionLayout.getHeight(),
				width / 2 + buttonWidth / 2,
				titleTextPosY + 2 * padding + descriptionLayout.getHeight() + buttonHeight);

		buttonTextPaint.getTextBounds(text, 0, text.length(), rect);
		buttonTextPosY = buttonRect.bottom - buttonHeight / 2 + rect.height() / 2;
	}

	@Override
	public void touchDown(float x, float y) {
		if (buttonRect.contains(x, y)) {
			isButtonHover = true;
		}
	}

	@Override
	public void touchUp(float x, float y, MotionEvent event) {
		if (isButtonHover) {
			isButtonHover = false;

			explodeThread.replaceStateStack("menu");
		}
	}

	@Override
	public void destroy() {
		icon.recycle();
	}

	@Override
	public void moveEvent(MotionEvent event) {

	}

}
