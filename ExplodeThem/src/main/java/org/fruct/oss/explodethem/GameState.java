package org.fruct.oss.explodethem;


import android.graphics.Canvas;
import android.os.Bundle;

public interface GameState {
	void prepare(Bundle args);
	void updatePhysics();
	void draw(Canvas canvas);
	void setSize(int width, int height);
	void touchDown(float x, float y);
	void touchUp(float x, float y);
	void destroy();
}
