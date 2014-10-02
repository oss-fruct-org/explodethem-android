package org.fruct.oss.explodethem;

import java.lang.Override;

public class Flavor {
	public interface Banner {
		void pause();
		void resume();
		void destroy();
		void refresh();
		void setVisibility(boolean isVisible);
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

			@Override
			public void refresh() {
			}

			@Override
			public void setVisibility(boolean isVisible) {

			}
		};
	}
}
