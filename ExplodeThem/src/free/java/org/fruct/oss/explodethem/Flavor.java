package org.fruct.oss.explodethem;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class Flavor {
	public interface Banner {
		void pause();
		void resume();
		void destroy();
	}

	public static boolean isFull() {
		return false;
	}
	public static Banner setupBanner(MainActivity activity) {
		// Получение экземпляра adView.
		final AdView adView = (AdView) activity.findViewById(R.id.banner);

		// Инициирование общего запроса.
		AdRequest adRequest = new AdRequest.Builder()
				.addTestDevice("66F5F4A83F7A89035992FD48AD60A182")
				.build();

		// Загрузка adView с объявлением.
		adView.loadAd(adRequest);

		return new Banner() {
			@Override
			public void pause() {
				adView.pause();
			}

			@Override
			public void resume() {
				adView.resume();
			}

			@Override
			public void destroy() {
				adView.destroy();
			}
		};
	}
}
