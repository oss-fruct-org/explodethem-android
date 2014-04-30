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
import android.view.MotionEvent;
import android.widget.EditText;


public class GameOverState implements GameState, Handler.Callback {
	private final Context context;
	private final ExplodeThread explodeThread;

	private final Paint textPaint;
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
	private float textOffsetY;

	private float padding;

	private static final long TICKS_FADE = 800 / ExplodeThread.TICK_MS;
	private long ticksRemainFadeIn = TICKS_FADE;

	private int score = -1;

	private RectF editNameRect;

	private RectF buttonRect;
	private float buttonTextPosY;

	private boolean isButtonHover = false;
	private boolean isHighscore = false;

	private String enteredName = "...";

	public GameOverState(Context context, ExplodeThread explodeThread) {
		this.context = context;
		this.explodeThread = explodeThread;

		float textSize = Utils.getSP(context, 32);
		padding = Utils.getDP(context, 16);
		buttonRadius = Utils.getDP(context, 4);

		backgroundPaint = new Paint();

		textPaint = new Paint();
		textPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "coolvetica.ttf"));
		textPaint.setAntiAlias(true);
		textPaint.setColor(0xfffafef1);
		textPaint.setTextSize(textSize);
		textPaint.setTextAlign(Paint.Align.CENTER);

		titleTextPaint = new Paint(textPaint);
		titleTextPaint.setTextSize(Utils.getSP(context, 48));

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

		icon = new ExplodeThread.BitmapHolder(context, "gameover-bomb.png");

	}

	@Override
	public void prepare(Bundle args) {
		score = args.getInt("score");
		isHighscore = HighscoreState.isHighscore(context, score);
		ticksRemainFadeIn = TICKS_FADE;
	}

	@Override
	public void updatePhysics() {
		if (ticksRemainFadeIn > 0) {
			ticksRemainFadeIn--;
		} else {
			explodeThread.stopRendering();
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
			canvas.drawBitmap(icon.getScaled(), width / 2 - icon.getScaled().getWidth() / 2,
					height / 4 - icon.getScaled().getHeight() / 2, null);

			canvas.drawText("Game Over", width / 2, titleTextPosY, titleTextPaint);
			canvas.drawText("Your score: " + score, width / 2, titleTextPosY + textOffsetY, textPaint);

			if (isHighscore) {
				canvas.drawText("Your name: " + enteredName, width / 2, titleTextPosY + textOffsetY * 2, textPaint);
			}

			canvas.drawRoundRect(buttonRect, buttonRadius, buttonRadius,
					isButtonHover ? buttonPaintHighlight : buttonPaint);
			canvas.drawText("OK", width / 2, buttonTextPosY, buttonTextPaint);

			//canvas.drawRoundRect(editNameRect, buttonRadius, buttonRadius, buttonPaint);
		}
	}

	@Override
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;

		this.icon.scale(width / 2, width / 2);

		String text = "Game over";

		Rect rect = new Rect();
		titleTextPaint.getTextBounds(text, 0, text.length(), rect);
		titleTextPosY = height / 4 + icon.getScaled().getHeight() / 2 + rect.height() + padding;

		textPaint.getTextBounds(text, 0, text.length(), rect);
		textOffsetY = rect.height() + padding;

		float buttonHeight = Utils.getDP(context, 48);
		float buttonWidth = width / 2;
		buttonRect = new RectF(width / 2 - buttonWidth / 2, textOffsetY * 3 + titleTextPosY,
				width / 2 + buttonWidth / 2, textOffsetY * 3 + titleTextPosY + buttonHeight);

		editNameRect = new RectF(width / 2, titleTextPosY + textOffsetY,
				width, titleTextPosY + textOffsetY * 2);

		buttonTextPaint.getTextBounds(text, 0, text.length(), rect);
		buttonTextPosY = buttonRect.bottom - buttonHeight / 2 + rect.height() / 2;
	}

	@Override
	public void touchDown(float x, float y) {
		if (buttonRect.contains(x, y)) {
			isButtonHover = true;
		} else if (editNameRect.contains(x, y)) {
			showInputDialog(new Handler(this));
		}
	}

	private void showInputDialog(final Handler handler) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		final EditText view = new EditText(context);
		view.setInputType(InputType.TYPE_CLASS_TEXT);

		builder.setView(view);
		builder.setTitle("Enter name");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				if (view.getText() == null) {
					return;
				}

				Bundle data = new Bundle();
				data.putString("name", view.getText().toString());

				Message message = new Message();
				message.setData(data);

				handler.sendMessage(message);
			}
		});

		builder.show();
	}

	@Override
	public void touchUp(float x, float y, MotionEvent event) {
		if (isButtonHover) {
			isButtonHover = false;

			if (isHighscore && enteredName != null && enteredName.length() != 0 && !enteredName.equals("...")) {
				HighscoreState.insertHighscore(context, enteredName, score);
				explodeThread.replaceStateStack("highscore");
			} else {
				explodeThread.replaceStateStack("menu");
			}
		}
	}

	@Override
	public void destroy() {
		icon.recycle();
	}

	@Override
	public void moveEvent(MotionEvent event) {

	}

	@Override
	public void storeState(Bundle outState) {
		outState.putString("name", enteredName);
	}

	@Override
	public void restoreState(Bundle inState) {
		enteredName = inState.getString("name");
	}

	@Override
	public boolean handleMessage(Message message) {
		assert message.getData() != null;
		enteredName = message.getData().getString("name");
		return true;
	}
}
