package org.fruct.oss.explodethem;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class Flavor {
	public interface Banner {
		void pause();
		void resume();
		void destroy();
		void refresh();
		void setVisibility(boolean isVisible);
	}

	public static boolean isFull() {
		return false;
	}
	public static Banner setupBanner(MainActivity activity) {
		final AdView adView = (AdView) activity.findViewById(R.id.banner);

		Banner banner = new Banner() {
			boolean isHidden = false;

			@Override
			public void pause() {
				adView.pause();
			}

			@Override
			public void resume() {
				if (!isHidden) {
					adView.resume();
				}
			}

			@Override
			public void destroy() {
				adView.destroy();
			}

			@Override
			public void refresh() {
				AdRequest adRequest = new AdRequest.Builder()
						.addTestDevice("66F5F4A83F7A89035992FD48AD60A182")
						.addTestDevice("E75183292BA271E4AA858B7A375EB405")
						.build();

				adView.loadAd(adRequest);
			}

			@Override
			public void setVisibility(boolean isVisible) {
				adView.setVisibility(isVisible ? View.VISIBLE : View.GONE);

				isHidden = !isVisible;
				if (isHidden)
					adView.pause();
				else
					adView.resume();
			}
		};

		banner.refresh();
		return banner;
	}
}
