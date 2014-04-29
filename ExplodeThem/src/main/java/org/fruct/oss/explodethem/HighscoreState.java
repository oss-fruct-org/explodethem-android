package org.fruct.oss.explodethem;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class HighscoreState implements GameState {
	public static final String TAG = HighscoreState.class.getName();

	public static final int MAX_HIGHSCORE = 10;

	public static final String PREF_HIGHSCORE_COUNT = "count-highscore";
	public static final String PREF_HIGHSCORE_WORST = "worst-highscore";
	public static final String PREF_HIGHSCORE_PREFIX = "highscore-";
	public static final String PREF_HIGHSCORE_NAME_PREFIX = "highscore-name-";

	private final Paint titleTextPaint;
	private Context context;
	private ExplodeThread explodeThread;
	private PlayState playState;

	private float titlePosY;
	private int width;
	private int height;

	public HighscoreState(Context context, ExplodeThread explodeThread) {
		this.context = context;
		this.explodeThread = explodeThread;
		this.playState = playState;

		titleTextPaint = new Paint();
		titleTextPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "Colleged.ttf"));
		titleTextPaint.setAntiAlias(true);
		titleTextPaint.setColor(0xfffafef1);
		titleTextPaint.setTextSize(Utils.getSP(context, 32));
		titleTextPaint.setTextAlign(Paint.Align.CENTER);
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
		canvas.drawText("Highscore", width / 2, titlePosY, titleTextPaint);
	}

	@Override
	public void setSize(int width, int height) {
		Rect rect = new Rect();
		titleTextPaint.getTextBounds("Highscore", 0, "Highscore".length(), rect);

		titlePosY = Utils.getDP(context, 16) + rect.height();
		this.width = width;
		this.height = height;
	}

	@Override
	public void touchDown(float x, float y) {

	}

	@Override
	public void touchUp(float x, float y) {
		explodeThread.replaceStateStack("menu");
	}

	@Override
	public void destroy() {

	}

	public static class Highscore implements Comparable<Highscore> {
		public Highscore(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public String name;
		public int value;


		@Override
		public int compareTo(Highscore highscore) {
			return value - highscore.value;
		}
	}

	public synchronized static int getHighscoreCount(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return pref.getInt(PREF_HIGHSCORE_COUNT, 0);
	}

	public synchronized static List<Highscore> getHighscores(Context context) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

		int count = pref.getInt(PREF_HIGHSCORE_COUNT, 0);
		Log.d(TAG, "Pref count = " + count);

		ArrayList<Highscore> ret = new ArrayList<Highscore>();
		for (int i = 0; i < count; i++) {
			String keyValue = PREF_HIGHSCORE_PREFIX + i;
			String keyName = PREF_HIGHSCORE_NAME_PREFIX + i;

			Highscore hs = new Highscore(pref.getString(keyName, ""), pref.getInt(keyValue, 0));
			Log.d(TAG, "Pref name = " + hs.name + ", value = " + hs.value);

			ret.add(hs);
		}
		Collections.sort(ret);
		return ret;
	}

	public synchronized static boolean isHighscore(Context context, int value) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		return getHighscoreCount(context) < MAX_HIGHSCORE || pref.getInt(PREF_HIGHSCORE_WORST, 0) < value;
	}

	public synchronized static void insertHighscore(Context context, String name, int value) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

		List<Highscore> highscores = getHighscores(context);
		highscores.add(new Highscore(name, value));
		Collections.sort(highscores);
		while (highscores.size() > MAX_HIGHSCORE) {
			highscores.remove(highscores.size() - 1);
		}

		SharedPreferences.Editor edit = pref.edit();
		edit.putInt(PREF_HIGHSCORE_WORST, highscores.get(highscores.size() - 1).value)
				.putInt(PREF_HIGHSCORE_COUNT, highscores.size());

		for (int i = 0; i < highscores.size(); i++) {
			Highscore hs = highscores.get(i);
			edit.putInt(PREF_HIGHSCORE_PREFIX + i, hs.value);
			edit.putString(PREF_HIGHSCORE_NAME_PREFIX + i, hs.name);
		}
		edit.commit();
	}
}
