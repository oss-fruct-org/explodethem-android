package org.fruct.oss.explodethem;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class Flavor {
	private static final String TAG = "Flavor";

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
	public static Banner setupBanner(final MainActivity activity) {
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
				AdRequest.Builder builder = new AdRequest.Builder();

				String testDevice = activity.getString(R.string.test_devices);

				String[] devices = testDevice.split(":");

				for (String device : devices) {
					Log.d(TAG, "Adding test device " + device);
					builder.addTestDevice(device);
				}

				adView.loadAd(builder.build());
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
