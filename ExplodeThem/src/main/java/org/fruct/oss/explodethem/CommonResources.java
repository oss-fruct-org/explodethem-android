package org.fruct.oss.explodethem;


import android.content.Context;

public class CommonResources {
	private final Context context;

	public ExplodeThread.BitmapHolder background;

	public CommonResources(Context context) {
		this.context = context;

		background = new ExplodeThread.BitmapHolder(context, "background.jpg");
	}

	public void resize(int width, int height) {
		background.scale(width, height);
	}

	public void destroy() {
		background.recycle();
	}
}
