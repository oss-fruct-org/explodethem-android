package org.fruct.oss.explodethem;

import android.content.Context;
import android.util.TypedValue;

public class Utils {
	public static float getDP(Context context, int px) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px,
				context.getResources().getDisplayMetrics());
	}

	public static float getSP(Context context, int px) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, px,
				context.getResources().getDisplayMetrics());
	}

}
