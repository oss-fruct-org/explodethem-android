package org.fruct.oss.explodethem;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HelpState implements GameState {
	public static final String TAG = HelpState.class.getName();

	private final Paint titleTextPaint;
	private final Paint textPaint;

	private Context context;
	private ExplodeThread explodeThread;
	private PlayState playState;

	private float titlePosY;
	private int width;
	private int height;

	private float panelWidth, panelHeight;

	public HelpState(Context context, ExplodeThread explodeThread) {
		this.context = context;
		this.explodeThread = explodeThread;
		this.playState = playState;

		titleTextPaint = new Paint();
		titleTextPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "Colleged.ttf"));
		titleTextPaint.setAntiAlias(true);
		titleTextPaint.setColor(0xfffafef1);
		titleTextPaint.setTextSize(Utils.getSP(context, 40));
		titleTextPaint.setTextAlign(Paint.Align.CENTER);

		textPaint = new Paint();
		textPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "OneDirection.ttf"));
		textPaint.setAntiAlias(true);
		textPaint.setColor(0xfffafef1);
		textPaint.setTextSize(Utils.getSP(context, 32));
	}

	@Override
	public void prepare(Bundle args) {
	}

	@Override
	public void updatePhysics() {

	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawBitmap(explodeThread.getCommonResources().background.getScaled(), 0, 0, null);
		canvas.drawText("Help", width / 2, titlePosY, titleTextPaint);
	}

	@Override
	public void setSize(int width, int height) {
		Rect rect = new Rect();
		titleTextPaint.getTextBounds("Help", 0, "Help".length(), rect);

		titlePosY = Utils.getDP(context, 16) + rect.height();
		this.width = width;
		this.height = height;

		panelWidth = width / 2;
		panelHeight = height / 2;
	}

	@Override
	public void touchDown(float x, float y) {

	}

	@Override
	public void touchUp(float x, float y) {

	}

	@Override
	public void destroy() {

	}
}
