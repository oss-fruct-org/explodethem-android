package org.fruct.oss.explodethem;

public class Flavor {
	public interface Banner {
		void pause();
		void resume();
		void destroy();
	}

	public static boolean isFull() {
		return true;
	}
	public static Banner setupBanner(MainActivity activity) {
		return new Banner() {
			@Override
			public void pause() {
			}

			@Override
			public void resume() {
			}

			@Override
			public void destroy() {
			}
		};
	}
}
