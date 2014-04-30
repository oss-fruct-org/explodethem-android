package org.fruct.oss.explodethem;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

public class MenuState implements GameState {
	private static final int FADE_TICKS = (int) (500 / ExplodeThread.TICK_MS);

	private final Context context;
	private final ExplodeThread explodeThread;
	private final PlayState playState;

	private final Paint buttonPaint;
	private final Paint buttonPaintHightlight;
	private final Paint textPaint;
	private final Paint titleTextPaint;

	private int width;
	private int height;

	private float buttonHeight;
	private float buttonWidth;
	private float buttonPadding;

	private float textSize;
	private float buttonRadius;
	private Rect textRect = new Rect();

	private List<MenuItem> menuItems = new ArrayList<MenuItem>();
	private List<RectF> menuItemsRects = new ArrayList<RectF>();

	private int hoverIndex = -1;
	private float titlePosY;
	private float titleOffsetY;


	public MenuState(Context context, ExplodeThread explodeThread, PlayState playState) {
		this.context = context;
		this.explodeThread = explodeThread;
		this.playState = playState;

		textSize = Utils.getSP(context, 24);
		buttonRadius = Utils.getDP(context, 4);

		buttonPaint = new Paint();
		buttonPaint.setColor(0x84c5c0f3);
		buttonPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		buttonPaint.setAntiAlias(true);

		buttonPaintHightlight = new Paint(buttonPaint);
		buttonPaintHightlight.setColor(0x99c5a0f3);

		textPaint = new Paint();
		textPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "coolvetica.ttf"));
		textPaint.setAntiAlias(true);
		textPaint.setColor(0xfffafef1);
		textPaint.setTextSize(textSize);
		textPaint.setTextAlign(Paint.Align.CENTER);

		titleTextPaint = new Paint();
		titleTextPaint.setTypeface(Typeface.createFromAsset(context.getAssets(), "Colleged.ttf"));
		titleTextPaint.setAntiAlias(true);
		titleTextPaint.setColor(0xfffafef1);
		titleTextPaint.setTextSize(Utils.getSP(context, 40));
		titleTextPaint.setTextAlign(Paint.Align.CENTER);
	}

	private void updateMenuLayout() {
		menuItemsRects.clear();

		float yPos = height / 4;
		for (MenuItem item : menuItems) {
			RectF buttonRect = new RectF();
			buttonRect.set(width / 2 - buttonWidth / 2, yPos,
					width / 2 + buttonWidth / 2, yPos + buttonHeight);
			menuItemsRects.add(buttonRect);

			yPos += buttonHeight + buttonPadding;
		}
	}

	@Override
	public void prepare(Bundle args) {
		menuItems.clear();
		MenuItem newGameMenu;
		menuItems.add(new MenuItem("Help", "help"));
		menuItems.add(new MenuItem("Highscore", "highscore"));
		menuItems.add(new MenuItem("About", "about"));
		menuItems.add(new MenuItem("Quit", "quit"));

		if (Flavor.isFull()) {
			menuItems.add(0, newGameMenu = new MenuItem("New game", "new-game"));
			newGameMenu.subMenu = new ArrayList<MenuItem>();
			newGameMenu.subMenu.add(new MenuItem("Easy", "new-game-easy"));
			newGameMenu.subMenu.add(new MenuItem("Medium", "new-game-medium"));
			newGameMenu.subMenu.add(new MenuItem("Hard", "new-game-hard"));
		} else {
			menuItems.add(0, new MenuItem("New game", "new-game-medium"));
		}
		updateMenuLayout();
	}

	@Override
	public void updatePhysics() {
		/*if (fadeTicksRemain > 0) {
			fadeTicksRemain--;
		} else {
			fadeIn = false;
		}*/
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawBitmap(explodeThread.getCommonResources().background.getScaled(),
				0, 0, null);

		canvas.drawText("Explode", width / 2, titlePosY, titleTextPaint);
		canvas.drawText("Them", width / 2, titlePosY + titleOffsetY, titleTextPaint);

		/*if (fadeIn) {
			int alpha = 200 - 200 * fadeTicksRemain / FADE_TICKS;
			backgroundPaint.setColor(alpha << 24);
		}
		canvas.drawRect(0, 0, width, height, backgroundPaint);*/

		for (int i = 0; i < menuItems.size(); i++) {
			String str = menuItems.get(i).text;
			RectF buttonRect = menuItemsRects.get(i);

			canvas.drawRoundRect(buttonRect, buttonRadius, buttonRadius,
					hoverIndex == i ? buttonPaintHightlight : buttonPaint);
			canvas.drawText(str, width / 2,
					buttonRect.top + buttonHeight / 2 + textRect.height() / 2, textPaint);
		}
	}

	@Override
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;

		Rect rect = new Rect();
		titleTextPaint.getTextBounds("Explode them", 0, "Explode them".length(), rect);
		titlePosY = Utils.getDP(context, 16) + rect.height();
		titleOffsetY = rect.height() + Utils.getDP(context, 8);

		buttonHeight = Utils.getDP(context, 48);
		buttonPadding = Utils.getDP(context, 8);
		buttonWidth = width / 2;

		String text = "New game";
		textPaint.getTextBounds(text, 0, text.length(), textRect);

		updateMenuLayout();
	}

	@Override
	public void touchDown(float x, float y) {
		for (int i = 0; i < menuItems.size(); i++) {
			RectF buttonRect = menuItemsRects.get(i);
			if (buttonRect.contains(x, y)) {
				hoverIndex = i;
				break;
			}
		}
	}

	@Override
	public void touchUp(float x, float y, MotionEvent event) {
		if (hoverIndex >= 0) {

			MenuItem menuItem = menuItems.get(hoverIndex);
			if (null != menuItem.subMenu) {
				menuItems = menuItem.subMenu;
				updateMenuLayout();
			} else {
				onMenuItemClick(menuItem.id);
			}

			hoverIndex = -1;
		}
	}

	private void onMenuItemClick(String id) {
		if (id.equals("about")) {
			explodeThread.replaceStateStack("about");
		} if (id.equals("new-game-easy")) {
			playState.newGame(0);
		} else if (id.equals("new-game-medium")) {
			playState.newGame(1);
		} else if (id.equals("new-game-hard")) {
			playState.newGame(2);
		} else if (id.equals("highscore")) {
			explodeThread.replaceStateStack("highscore");
			return;
		} else if (id.equals("help")) {
			explodeThread.replaceStateStack("help");
			return;
		} else if (id.equals("quit")) {
			if (context instanceof MainActivity) {
				((MainActivity) context).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						((MainActivity) context).finish();
					}
				});
			}
		} else {
			return;
		}

		explodeThread.replaceStateStack("play");
	}

	@Override
	public void destroy() {

	}

	@Override
	public void moveEvent(MotionEvent event) {

	}

	private class MenuItem {
		MenuItem(String text, String id) {
			this.text = text;
			this.id = id;
		}

		String text;
		String id;

		List<MenuItem> subMenu;
	}
}
