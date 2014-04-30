package org.fruct.oss.explodethem;


import android.graphics.Canvas;
import android.os.Bundle;
import android.view.MotionEvent;

public interface GameState {
	void prepare(Bundle args);
	void updatePhysics();
	void draw(Canvas canvas);
	void setSize(int width, int height);
	void touchDown(float x, float y);
	void touchUp(float x, float y, MotionEvent event);
	void destroy();
	void moveEvent(MotionEvent event);

	void storeState(Bundle outState);
	void restoreState(Bundle inState);
}
