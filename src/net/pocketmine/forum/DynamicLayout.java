package net.pocketmine.forum;

import java.util.ArrayList;

import net.pocketmine.forum.GridAdapter.DownloadImageTask;
import net.pocketmine.server.R;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class DynamicLayout extends LinearLayout {

	public DynamicLayout(Context context) {
		super(context);
	}

	public DynamicLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	ArrayList<View> views = new ArrayList<View>();

	public void setViews(ArrayList<View> views) {
		this.views = views;
		oldCount = 0;
		onSizeChanged(cW, cH, oW, oH);
	}

	public ArrayList<View> getViews() {
		return views;
	}

	int oldCount = 0;
	int cW = 0;
	int cH = 0;
	int oW = 0;
	int oH = 0;

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		cW = w;
		cH = h;
		oW = oldw;
		oH = oldh;

		super.onSizeChanged(w, h, oldw, oldh);

		if (w != oldw) {
			int nCount = Double.valueOf(Math.floor(w / 210)).intValue();
			if (nCount > views.size()) {
				nCount = views.size();
			}

			if (nCount != oldCount) {
				try {
					if (nCount < oldCount) {
						for (int i = oldCount - 1; i >= nCount; i--) {
							this.removeViewAt(i);

							if (listener != null) {
								listener.hide(i);
							}
						}
					} else {
						for (int i = oldCount; i < nCount; i++) {
							// Download the image!
							View view = views.get(i);
							this.addView(view);

							if (listener != null) {
								listener.show(i, view);
							}
						}
					}

					oldCount = nCount;
				} catch (NullPointerException err) {
					if (listener != null) {
						listener.clearAll();
					}
					this.removeAllViews();
					for (int i = 0; i < nCount; i++) {
						this.addView(views.get(i));

						if (listener != null) {
							listener.show(i, views.get(i));
						}
					}
				}

				post(new Runnable() {

					@Override
					public void run() {
						requestLayout();
					}
				});
			}

		}
	}

	public static interface ShowViewListener {
		public void show(int pos, View view);

		public void hide(int pos);

		public void clearAll();
	}

	private ShowViewListener listener = null;

	public void setOnShowViewListener(ShowViewListener listener) {
		this.listener = listener;
	}

}
